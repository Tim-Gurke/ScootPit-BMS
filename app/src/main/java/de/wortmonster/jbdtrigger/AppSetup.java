package de.wortmonster.jbdtrigger;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

/** Device-local setup; Android grants are always read live, never imported from a profile. */
final class AppSetup {
    static final String PREFS = "app_setup";
    private static final int BLUETOOTH = 43, LOCATION = 44, NOTIFICATIONS = 45;
    private final Activity activity;
    private final SharedPreferences state, settings;
    private final Runnable chooseBms, startRide, diagnostics;
    private AlertDialog dialog, permissionHelp;
    private LinearLayout content;
    private Intent status;
    private String lastRendering = "";
    private boolean resumed, disposed, returnAfterPicker;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable poll = new Runnable() { public void run() {
        refresh(); if (resumed && isShowing()) handler.postDelayed(this, 1000);
    }};

    AppSetup(Activity activity, SharedPreferences settings, Runnable chooseBms, Runnable startRide, Runnable diagnostics) {
        this.activity = activity;
        this.settings = settings;
        this.chooseBms = chooseBms;
        this.startRide = startRide;
        this.diagnostics = diagnostics;
        state = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
        status = BmsMonitorService.latestStatus;
    }

    void introduce() {
        if (state.getBoolean("introduced", false)) return;
        state.edit().putBoolean("introduced", true).apply();
        // Existing fully configured installations do not need another introduction.
        if (!rideReady() || !hasBms()) show();
    }

    boolean connectionAllowed() {
        return Build.VERSION.SDK_INT >= 31
                ? granted(Manifest.permission.BLUETOOTH_SCAN) && granted(Manifest.permission.BLUETOOTH_CONNECT)
                : granted(Manifest.permission.ACCESS_FINE_LOCATION);
    }

    boolean rideReady() {
        return connectionAllowed() && granted(Manifest.permission.ACCESS_FINE_LOCATION)
                && bluetoothEnabled() && locationEnabled();
    }

    private boolean hasBms() {
        return BluetoothAdapter.checkBluetoothAddress(settings.getString("device_address", ""))
                || (settings.getBoolean("auto_scooter", true) && !ScooterProfiles.knownBms(settings).isEmpty());
    }

    private boolean granted(String permission) {
        return activity.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    boolean bluetoothEnabled() {
        if (!connectionAllowed()) return false;
        try {
            BluetoothManager manager = activity.getSystemService(BluetoothManager.class);
            return manager != null && manager.getAdapter() != null && manager.getAdapter().isEnabled();
        } catch (SecurityException unavailable) { return false; }
    }

    private boolean locationEnabled() {
        LocationManager manager = activity.getSystemService(LocationManager.class);
        return manager != null && (Build.VERSION.SDK_INT >= 28 ? manager.isLocationEnabled()
                : manager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER));
    }

    void show() {
        if (disposed || activity.isFinishing()) return;
        if (dialog != null && dialog.isShowing()) { refresh(); return; }
        state.edit().putBoolean("guide_pending", !verified()).apply();
        lastRendering = "";
        content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(16 * activity.getResources().getDisplayMetrics().density);
        content.setPadding(pad, pad, pad, pad);
        ScrollView scroll = new ScrollView(activity) {
            @Override protected void onMeasure(int width, int height) {
                int maximum = Math.round(getResources().getDisplayMetrics().heightPixels * .68f);
                int offered = android.view.View.MeasureSpec.getMode(height) == android.view.View.MeasureSpec.UNSPECIFIED
                        ? maximum : Math.min(maximum, android.view.View.MeasureSpec.getSize(height));
                super.onMeasure(width, android.view.View.MeasureSpec.makeMeasureSpec(offered, android.view.View.MeasureSpec.AT_MOST));
            }
        };
        scroll.addView(content);
        dialog = new AlertDialog.Builder(activity).setTitle("ScootPit einrichten")
                .setView(scroll).setPositiveButton("Zum Cockpit", null).create();
        dialog.setOnDismissListener(d -> handler.removeCallbacks(poll));
        dialog.show();
        dialog.getWindow().setLayout(-1, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        refresh();
        resume();
    }

    private SetupProgress.Status snapshot() {
        SetupProgress.Status s = new SetupProgress.Status();
        s.nearby = connectionAllowed(); s.bluetooth = bluetoothEnabled();
        s.location = granted(Manifest.permission.ACCESS_FINE_LOCATION); s.gps = locationEnabled();
        s.notifications = Build.VERSION.SDK_INT < 33 || granted(Manifest.permission.POST_NOTIFICATIONS)
                || state.getBoolean("skip_notifications", false);
        PowerManager power = activity.getSystemService(PowerManager.class);
        s.battery = (power != null && power.isIgnoringBatteryOptimizations(activity.getPackageName()))
                || state.getBoolean("skip_battery", false);
        s.bms = hasBms(); s.readiness = BmsMonitorService.running;
        long now = System.currentTimeMillis();
        s.liveData = s.readiness && status != null
                && settings.getString(ScooterProfiles.ACTIVE, "").equals(status.getStringExtra("scooter_id"))
                && status.getBooleanExtra("bms_connected", false)
                && SetupProgress.fresh(status.getLongExtra("bms_at", 0), now)
                && SetupProgress.fresh(status.getLongExtra("timestamp", 0), now);
        s.recorded = verified();
        return s;
    }

    void refresh() {
        if (!isShowing() || disposed) return;
        SetupProgress.Status s = snapshot();
        SetupProgress.Step step = SetupProgress.next(s);
        if (step.ordinal() >= SetupProgress.Step.READINESS.ordinal() && step != SetupProgress.Step.DONE) ensureTrial();
        boolean trip = status != null && status.getBooleanExtra("trip_active", false);
        boolean paused = status != null && status.getBooleanExtra("trip_paused", false);
        String error = status == null ? "" : status.getStringExtra("recording_error");
        boolean gps = status != null && SetupProgress.fresh(status.getLongExtra("gps_at", 0), System.currentTimeMillis());
        boolean output = status != null && status.getBooleanExtra("discharge_enabled", false);
        String detail = status == null ? "" : status.getStringExtra("state");
        String rendering = step + "/" + trip + "/" + paused + "/" + error + "/" + gps + "/" + output + "/" + detail;
        if (rendering.equals(lastRendering)) return;
        lastRendering = rendering;
        content.removeAllViews();
        text(step == SetupProgress.Step.DONE ? "Einrichtung abgeschlossen · Aufzeichnung geprüft"
                : "Schritt " + (step.ordinal() + 1) + " von 10");
        switch (step) {
            case NEARBY:
                text("Geräte in der Nähe erlauben\nScootPit braucht diese Freigabe, um dein Bluetooth-BMS zu suchen und sich zu verbinden."
                        + (Build.VERSION.SDK_INT < 31 ? " Auf dieser Android-Version benötigt die Suche dafür den präzisen Standort." : ""));
                action("Bluetooth erlauben", () -> request(BLUETOOTH, bluetoothPermissions())); break;
            case BLUETOOTH:
                text("Bluetooth einschalten\nDie App-Freigabe ist erteilt. Jetzt muss Bluetooth am Handy eingeschaltet sein.");
                action("Bluetooth einschalten", () -> open(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), Settings.ACTION_BLUETOOTH_SETTINGS)); break;
            case LOCATION:
                text("Präzisen Standort erlauben\nFür Geschwindigkeit, Fahrterkennung und Fahrtroute. Wähle während der App-Nutzung und Präzise.");
                action("Standort erlauben", () -> request(LOCATION, locationPermissions())); break;
            case GPS:
                text("Standort am Handy einschalten\nFür die GPS-Fahrtaufzeichnung muss der Standortdienst eingeschaltet sein.");
                action("Standort einschalten", () -> open(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), Settings.ACTION_SETTINGS)); break;
            case NOTIFICATIONS:
                text("Benachrichtigungen · empfohlen\nZeigt Bereitschaft, Fahrtstatus und Routine-Nachrichten. Du kannst diesen Schritt überspringen.");
                action("Benachrichtigungen erlauben", () -> request(NOTIFICATIONS, new String[]{Manifest.permission.POST_NOTIFICATIONS}));
                action("Vorerst überspringen", () -> skip("skip_notifications")); break;
            case BATTERY:
                text("Akku-Hintergrundbetrieb · empfohlen\nFür zuverlässige Erkennung bei gesperrtem Handy. Bei Samsung zusätzlich App-Info → Akku → Nicht eingeschränkt prüfen. Du kannst diesen Schritt überspringen.");
                action("Akku-Ausnahme anfragen", () -> open(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:" + activity.getPackageName())), Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                action("Vorerst überspringen", () -> skip("skip_battery")); break;
            case BMS:
                text("BMS auswählen\nGehe mit dem Handy zum Roller. Trenne andere Apps, die gerade mit diesem BMS verbunden sind. Wähle dein eigenes BMS; ScootPit prüft seine Antwort.");
                action("BMS auswählen", () -> { returnAfterPicker = true; close(); chooseBms.run(); }); break;
            case READINESS:
                text("Bereitschaft starten\nScootPit verbindet sich jetzt automatisch mit deinem gespeicherten BMS. Eine aktivierte Bereitschaft allein bedeutet noch keine Fahrtaufzeichnung.");
                action("Bereitschaft starten", () -> { ensureTrial(); startRide.run(); refresh(); }); break;
            case LIVE_DATA:
                text("BMS-Verbindung prüfen · warte auf aktuelle Daten\nBleibe am Roller und lasse das BMS eingeschaltet. Trenne andere BMS-Apps und gegebenenfalls das bisher verbundene Handy beziehungsweise die Zentrale. Dieser Schritt ist erst erledigt, wenn gültige aktuelle BMS-Daten ankommen.");
                if (detail != null && !detail.isEmpty()) text("Verbindungsstatus: " + detail);
                action("Verbindungsdiagnose öffnen", () -> { close(); diagnostics.run(); }); break;
            case TEST_RIDE:
                text("Kurze Testfahrt · Aufzeichnung prüfen");
                if (error != null && !error.isEmpty()) text("Speicherfehler: " + error);
                else if (!output) {
                    text("Der BMS-Lastausgang ist ausgeschaltet. Prüfe ihn im Cockpit im Stand und schalte ihn dort bei Bedarf bewusst ein.");
                    action("Lastausgang im Cockpit prüfen", this::close);
                } else if (paused) text("Die Fahrt ist pausiert. Bei sicherer Weiterfahrt setzt die Automatik die Aufzeichnung fort.");
                else if (!trip) text("Bereitschaft und BMS-Daten sind vorhanden. Im Stand startet noch keine Aufzeichnung. Verstaue das Handy und fahre eine kurze, sichere Strecke mit dem Roller. Du musst unterwegs nichts am Handy bedienen.");
                else if (!gps) text("Fahrt erkannt · warte auf genaue GPS-Daten. Fahre im Freien eine kurze, sichere Strecke; prüfe das Ergebnis anschließend im Stand.");
                else text("Fahrt erkannt · warte auf gespeicherte Fahrtpunkte. Die Einrichtung bestätigt den Erfolg erst nach mindestens zwei geschriebenen Punkten und einer erkannten Strecke.");
                text("Nach der Testfahrt kannst du hier nachsehen. Der Nachweis wird auch bei gesperrtem Handy gespeichert. Bei ausbleibendem Start: Motorlast und die Fahrt-Erkennung im Cockpit prüfen.");
                if (detail != null && !detail.isEmpty()) text("Aktueller Status: " + detail);
                action("Zum Cockpit und später prüfen", this::close); break;
            case DONE:
                state.edit().putBoolean("guide_pending", false).apply();
                text("Geschafft: Aktuelle BMS-Daten, GPS-Strecke und mindestens zwei lokal gespeicherte Fahrtpunkte wurden bestätigt.");
                if (error != null && !error.isEmpty()) text("Aktueller Speicherfehler: " + error);
                else text(trip ? (paused ? "Aktuell ist die Fahrt pausiert." : "Aktuell läuft eine Fahrtaufzeichnung.")
                        : "Die Testaufzeichnung wurde bestätigt; aktuell läuft keine Fahrt.");
                text("Nach dem Beenden der Fahrt findest du sie im Fahrtenbuch. Externe Ordnerkopien und der Hintergrundbetrieb deines Handys lassen sich zusätzlich im Alltag prüfen."); break;
        }
    }

    private void skip(String key) { state.edit().putBoolean(key, true).apply(); refresh(); }
    private void ensureTrial() {
        String profile = settings.getString(ScooterProfiles.ACTIVE, ""), address = settings.getString("device_address", "");
        if (state.getLong("trial_started_at", 0) == 0 || !profile.equals(state.getString("trial_profile", ""))
                || !address.equalsIgnoreCase(state.getString("trial_address", "")))
            state.edit().putLong("trial_started_at", System.currentTimeMillis()).putString("trial_profile", profile)
                    .putString("trial_address", address).remove("verified_at").putBoolean("guide_pending", true).apply();
    }
    private boolean verified() {
        return state.getLong("verified_at", 0) > 0
                && settings.getString(ScooterProfiles.ACTIVE, "").equals(state.getString("trial_profile", ""))
                && settings.getString("device_address", "").equalsIgnoreCase(state.getString("trial_address", ""));
    }
    static void noteRecording(Context context, String profile, String address, long now, long bmsAt, long gpsAt, long points, double meters) {
        SharedPreferences proof = context.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
        if (proof.getLong("verified_at", 0) > 0 || !profile.equals(proof.getString("trial_profile", ""))
                || !address.equalsIgnoreCase(proof.getString("trial_address", ""))) return;
        if (SetupProgress.recorded(proof.getLong("trial_started_at", 0), now, bmsAt, gpsAt, points, meters))
            proof.edit().putLong("verified_at", now).putBoolean("guide_pending", false).apply();
    }
    void onStatus(Intent value) { status = new Intent(value); refresh(); }
    boolean needsAttention() { return !rideReady() || state.getBoolean("guide_pending", false); }
    void pickerClosed() { if (returnAfterPicker && !disposed) { returnAfterPicker = false; handler.post(this::show); } }
    void resume() { resumed = true; handler.removeCallbacks(poll); if (isShowing()) handler.post(poll); }
    void pause() { resumed = false; handler.removeCallbacks(poll); }
    boolean shouldRestore() { return isShowing() || returnAfterPicker; }
    void destroy() { disposed = true; pause(); close(); }

    private String[] bluetoothPermissions() {
        return Build.VERSION.SDK_INT >= 31
                ? new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT}
                : locationPermissions();
    }

    private String[] locationPermissions() {
        return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    }

    private void request(int code, String[] permissions) {
        boolean blocked = false;
        for (String permission : permissions) if (!granted(permission)
                && state.getBoolean("asked_" + permission, false)
                && !activity.shouldShowRequestPermissionRationale(permission)) blocked = true;
        if (blocked) {
            String where = code == NOTIFICATIONS ? "Benachrichtigungen → Zulassen"
                    : code == LOCATION || Build.VERSION.SDK_INT < 31 ? "Berechtigungen → Standort → Während der Nutzung → Präzise"
                    : "Berechtigungen → Geräte in der Nähe → Zulassen";
            permissionHelp = new AlertDialog.Builder(activity).setTitle("Freigabe in den Einstellungen")
                    .setMessage("Android zeigt diese Abfrage nicht mehr an. Wähle in der App-Info: " + where + ".")
                    .setPositiveButton("App-Einstellungen öffnen", (d,w) -> appInfo()).setNegativeButton("Später", null).show();
            return;
        }
        SharedPreferences.Editor edit = state.edit();
        for (String permission : permissions) if (!granted(permission)) edit.putBoolean("asked_" + permission, true);
        edit.apply();
        activity.requestPermissions(permissions, code);
    }

    private void text(String value) {
        TextView label = new TextView(activity);
        label.setText(value);
        label.setTextSize(16);
        int pad = Math.round(8 * activity.getResources().getDisplayMetrics().density);
        label.setPadding(0, pad, 0, pad);
        content.addView(label);
    }

    private Button action(String title, Runnable clicked) {
        Button button = new Button(activity);
        button.setText(title);
        button.setOnClickListener(v -> clicked.run());
        content.addView(button, new LinearLayout.LayoutParams(-1, -2));
        return button;
    }

    private void open(Intent intent, String fallback) {
        try { activity.startActivity(intent); }
        catch (android.content.ActivityNotFoundException | SecurityException unavailable) {
            try { activity.startActivity(new Intent(fallback)); }
            catch (android.content.ActivityNotFoundException | SecurityException alsoUnavailable) { appInfo(); }
        }
    }

    private void appInfo() {
        try { activity.startActivity(new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.parse("package:" + activity.getPackageName()))); }
        catch (android.content.ActivityNotFoundException | SecurityException unavailable) {
            android.widget.Toast.makeText(activity, "Bitte Android-Einstellungen → Apps → ScootPit BMS öffnen.", android.widget.Toast.LENGTH_LONG).show();
        }
    }

    boolean handles(int request) { return request == BLUETOOTH || request == LOCATION || request == NOTIFICATIONS; }
    boolean isShowing() { return dialog != null && dialog.isShowing(); }
    void close() { if (permissionHelp != null) permissionHelp.dismiss(); if (dialog != null) dialog.dismiss(); }
}
