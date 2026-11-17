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
                for (String title : new String[]{"Geräte in der Nähe", "Präziser Standort", "Akku-Hintergrundbetrieb", "BMS auswählen", "Bereitschaft starten"})
                    if (find(root,title) == null) throw new AssertionError("Missing setup guidance: " + title);
                if (setup.connectionAllowed() != granted) throw new AssertionError("Live Android Bluetooth permission state");
                Button select = button(root,"BMS auswählen");
                if (!granted && select.isEnabled()) throw new AssertionError("Search cannot start without permission");
                if (button(root,"Bereitschaft starten").isEnabled()) throw new AssertionError("No readiness without BMS selection");
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
                checked(() -> {
                    View root = ((AlertDialog)field(setup,"dialog")).getWindow().getDecorView();
                    if (find(root,"Bluetooth erlauben") != null || find(root,"Standort erlauben") != null)
                        throw new AssertionError("Granted permissions must no longer be requested");
                    if (android.os.Build.VERSION.SDK_INT >= 33 && find(root,"Benachrichtigungen erlauben") == null)
                        throw new AssertionError("Optional notification denial must remain visible");
                });
            }
            checked(() -> { setup.close(); activity.finish(); });
            result.putString("stream", granted ? "Setup with granted Bluetooth and location: OK\n" : "Setup without permissions: OK\n");
            runner.finish(Activity.RESULT_OK,result);
        } catch (Throwable failure) {
            result.putString("stream","FAIL: " + android.util.Log.getStackTraceString(failure));
            runner.finish(Activity.RESULT_CANCELED,result);
        }
    }

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
