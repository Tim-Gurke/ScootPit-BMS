package de.wortmonster.jbdtrigger;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/** Runs against actual revoked/granted Android permissions on a disposable emulator. */
final class SetupCheck {
    private final Instrumentation runner;
    SetupCheck(Instrumentation runner) { this.runner = runner; }

    void run(boolean granted) {
        Bundle result = new Bundle();
        try {
            runner.getTargetContext().getSharedPreferences("settings",0).edit().clear().commit();
            runner.getTargetContext().getSharedPreferences(AppSetup.PREFS,0).edit().clear().commit();
            Activity activity = runner.startActivitySync(new Intent(runner.getTargetContext(), MainActivity.class)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
            runner.waitForIdleSync();
            AppSetup setup = (AppSetup)field(activity,"appSetup");
            checked(() -> {
                if (!setup.isShowing()) throw new AssertionError("Fresh launch must explain setup before asking Android");
                View root = ((AlertDialog)field(setup,"dialog")).getWindow().getDecorView();
                if (find(root,"Schritt ") == null) throw new AssertionError("One guided setup step at a time");
                if (setup.connectionAllowed() != granted) throw new AssertionError("Live Android Bluetooth permission state");
                if (!granted && (button(root,"Bluetooth erlauben") == null || button(root,"BMS auswählen") != null))
                    throw new AssertionError("Explain Bluetooth before opening a search");
            });
            {
                try {
                    android.graphics.Bitmap screenshot = runner.getUiAutomation().takeScreenshot();
                    java.io.File folder = new java.io.File(activity.getExternalFilesDir(null),"screenshots");
                    folder.mkdirs();
                    try (java.io.FileOutputStream out = new java.io.FileOutputStream(new java.io.File(folder,
                            granted ? "einrichtung-freigegeben.png" : "einrichtung-fehlende-freigaben.png"))) {
                        screenshot.compress(android.graphics.Bitmap.CompressFormat.PNG,100,out);
                    }
                    screenshot.recycle();
                } catch (Exception e) { throw new RuntimeException(e); }
            }
            if (!granted) {
                checked(() -> {
                    setup.close();
                    invoke(activity,"openBmsPicker");
                    if (!setup.isShowing() || field(activity,"picker") != null)
                        throw new AssertionError("Denied search must reopen actionable setup, not an empty picker");
                    // Persistently denied requests must offer an explicit path to App Info.
                    activity.getSharedPreferences(AppSetup.PREFS,0).edit()
                            .putBoolean("asked_" + android.Manifest.permission.BLUETOOTH_SCAN,true)
                            .putBoolean("asked_" + android.Manifest.permission.BLUETOOTH_CONNECT,true).commit();
                    button(((AlertDialog)field(setup,"dialog")).getWindow().getDecorView(),"Bluetooth erlauben").performClick();
                });
                checked(() -> {
                    AlertDialog help = (AlertDialog)field(setup,"permissionHelp");
                    if (help == null || !help.isShowing() || find(help.getWindow().getDecorView(),"App-Einstellungen öffnen") == null)
                        throw new AssertionError("Permanently denied Bluetooth must offer settings");
                    setup.close();
                    invoke(activity,"startMonitoring");
                    if (BmsMonitorService.running || field(activity,"picker") != null || !setup.isShowing())
                        throw new AssertionError("Readiness without required permissions must remain in setup");
                });
            } else {
                guidedRecordingCheck(activity,setup);
            }
            checked(() -> { setup.close(); activity.finish(); });
            result.putString("stream", granted ? "Setup with granted Bluetooth and location: OK\n" : "Setup without permissions: OK\n");
            runner.finish(Activity.RESULT_OK,result);
        } catch (Throwable failure) {
            BmsMonitorService.running=false;
            result.putString("stream","FAIL: " + android.util.Log.getStackTraceString(failure));
            runner.finish(Activity.RESULT_CANCELED,result);
        }
    }

    private void guidedRecordingCheck(Activity activity, AppSetup setup) throws Exception {
        checked(() -> {
            View root = root(setup);
            if (find(root,"Bluetooth erlauben") != null || find(root,"Standort erlauben") != null)
                throw new AssertionError("Granted permissions are already complete");
            if (android.os.Build.VERSION.SDK_INT >= 33) {
                if (button(root,"Benachrichtigungen erlauben") == null) throw new AssertionError("Explain optional notifications");
                button(root,"Vorerst überspringen").performClick();
            }
            if (button(root(setup),"Akku-Ausnahme anfragen") != null) button(root(setup),"Vorerst überspringen").performClick();
            if (button(root(setup),"BMS auswählen") == null) throw new AssertionError("Continue from permissions to BMS selection");
            button(root(setup),"BMS auswählen").performClick();
            if (setup.isShowing() || field(activity,"picker") == null) throw new AssertionError("Open real BMS picker from wizard");
            ((AlertDialog)field(field(activity,"picker"),"dialog")).dismiss();
        });
        checked(() -> {
            if (!setup.isShowing() || button(root(setup),"BMS auswählen") == null)
                throw new AssertionError("Cancelled BMS selection must return to the same wizard step");
            button(root(setup),"BMS auswählen").performClick();
            try {
                Method select = MainActivity.class.getDeclaredMethod("selectBms",String.class,String.class);
                select.setAccessible(true); select.invoke(activity,"02:00:00:00:00:01","Beispiel-BMS");
            } catch (Exception failure) { throw new RuntimeException(failure); }
            ((AlertDialog)field(field(activity,"picker"),"dialog")).dismiss();
        });
        checked(() -> {
            if (!setup.isShowing() || button(root(setup),"Bereitschaft starten") == null)
                throw new AssertionError("Validated BMS selection must continue to readiness");
            BmsMonitorService.running = true;
            setup.refresh();
            if (find(root(setup),"warte auf aktuelle Daten") == null || find(root(setup),"Geschafft") != null)
                throw new AssertionError("Readiness alone must not finish setup");
        });
        android.content.SharedPreferences settings=activity.getSharedPreferences("settings",0);
        android.content.SharedPreferences proof=activity.getSharedPreferences(AppSetup.PREFS,0);
        String profile=settings.getString(ScooterProfiles.ACTIVE,"");
        long now=System.currentTimeMillis();
        Intent live = new Intent().putExtra("scooter_id",profile).putExtra("timestamp",now).putExtra("bms_at",now)
                .putExtra("bms_connected",true).putExtra("discharge_enabled",true);
        checked(() -> {
            setup.onStatus(live);
            if (find(root(setup),"Kurze Testfahrt") == null || find(root(setup),"Geschafft") != null)
                throw new AssertionError("Live BMS data alone must not finish setup");
            setup.onStatus(new Intent(live).putExtra("recording_error","Test-Schreibfehler"));
            if (find(root(setup),"Speicherfehler") == null || find(root(setup),"Geschafft") != null)
                throw new AssertionError("A recording write error must remain actionable");
            setup.onStatus(new Intent(live).putExtra("trip_active",true));
            if (find(root(setup),"warte auf genaue GPS-Daten") == null)
                throw new AssertionError("An open recorder without GPS is not a successful trial");
            setup.close(); setup.pause();
        });
        TripRecorder recorder=new TripRecorder(activity,System.currentTimeMillis());
        android.location.Location one=new android.location.Location("gps");
        one.setLatitude(49); one.setLongitude(8); one.setAccuracy(3); one.setTime(System.currentTimeMillis()); one.setSpeed(3);
        if (!recorder.add(one,80,48,-2,96,0,96,20,new double[]{25,30})) throw new AssertionError("Write first actual sample");
        now=System.currentTimeMillis();
        AppSetup.noteRecording(activity,profile,"02:00:00:00:00:01",now,now,now,recorder.recordedPoints(),0);
        if (proof.getLong("verified_at",0)>0) throw new AssertionError("A single stationary sample cannot finish setup");
        android.location.Location two=new android.location.Location(one);two.setLatitude(49.00003);two.setTime(System.currentTimeMillis());
        if (!recorder.add(two,80,48,-2,96,.026,96,20,new double[]{25,30})) throw new AssertionError("Write second actual sample");
        now=System.currentTimeMillis();
        AppSetup.noteRecording(activity,"different-profile","02:00:00:00:00:01",now,now,now,recorder.recordedPoints(),3);
        if (proof.getLong("verified_at",0)>0) throw new AssertionError("Wrong scooter cannot finish setup");
        AppSetup.noteRecording(activity,profile,"02:00:00:00:00:02",now,now,now,recorder.recordedPoints(),3);
        if (proof.getLong("verified_at",0)>0) throw new AssertionError("Wrong BMS cannot finish setup");
        AppSetup.noteRecording(activity,profile,"02:00:00:00:00:01",now,now,now,recorder.recordedPoints(),3);
        if (proof.getLong("verified_at",0)==0) throw new AssertionError("Background recording proof must persist without an open wizard");
        TripRecorder.Summary summary=recorder.finish(now,3,10.8,0,.026,96,20,25,30,1000,0);
        if (java.nio.file.Files.readAllLines(summary.csvFile.toPath()).size()!=3) throw new AssertionError("Two actual saved CSV samples");
        checked(() -> {
            BmsMonitorService.running=false;setup.onStatus(new Intent());setup.show();
            if (find(root(setup),"Geschafft") == null || find(root(setup),"aktuell läuft keine Fahrt") == null)
                throw new AssertionError("Report recorded trial after returning, without claiming a live ride");
            if (proof.getBoolean("guide_pending",true)) throw new AssertionError("Clear reminder only after stored recording proof");
        });
    }
    private static View root(AppSetup setup) { return ((AlertDialog)field(setup,"dialog")).getWindow().getDecorView(); }

    private void checked(Runnable action) {
        final java.util.concurrent.atomic.AtomicReference<Throwable> error = new java.util.concurrent.atomic.AtomicReference<>();
        runner.runOnMainSync(() -> { try { action.run(); } catch (Throwable failure) { error.set(failure); } });
        runner.waitForIdleSync();
        if (error.get() != null) throw new AssertionError(error.get());
    }
    private static Object field(Object target, String name) {
        try { Field field = target.getClass().getDeclaredField(name); field.setAccessible(true); return field.get(target); }
        catch (Exception failure) { throw new RuntimeException(failure); }
    }
    private static void invoke(Object target,String name) {
        try { Method method = target.getClass().getDeclaredMethod(name); method.setAccessible(true); method.invoke(target); }
        catch (Exception failure) { throw new RuntimeException(failure); }
    }
    private static Button button(View view,String text) {
        if (view instanceof Button && ((Button)view).getText().toString().equals(text)) return (Button)view;
        if (view instanceof ViewGroup) for (int i=0;i<((ViewGroup)view).getChildCount();i++) {
            Button child = button(((ViewGroup)view).getChildAt(i),text); if (child != null) return child;
        }
        return null;
    }
    private static View find(View view,String text) {
        if (view instanceof TextView && ((TextView)view).getText().toString().contains(text)) return view;
        if (view instanceof ViewGroup) for (int i=0;i<((ViewGroup)view).getChildCount();i++) {
            View child = find(((ViewGroup)view).getChildAt(i),text); if (child != null) return child;
        }
        return null;
    }
}
