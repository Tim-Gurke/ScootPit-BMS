package de.wortmonster.jbdtrigger;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
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
    private final Runnable chooseBms, startRide;
    private AlertDialog dialog, permissionHelp;
    private LinearLayout content;

    AppSetup(Activity activity, SharedPreferences settings, Runnable chooseBms, Runnable startRide) {
        this.activity = activity;
        this.settings = settings;
        this.chooseBms = chooseBms;
        this.startRide = startRide;
        state = activity.getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
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
        if (dialog != null && dialog.isShowing()) { refresh(); return; }
        content = new LinearLayout(activity);
        content.setOrientation(LinearLayout.VERTICAL);
        int pad = Math.round(16 * activity.getResources().getDisplayMetrics().density);
        content.setPadding(pad, pad, pad, pad);
        ScrollView scroll = new ScrollView(activity);
        scroll.addView(content);
        dialog = new AlertDialog.Builder(activity).setTitle("ScootPit einrichten")
                .setView(scroll).setPositiveButton("Zum Cockpit", null).create();
        dialog.show();
        dialog.getWindow().setLayout(-1, (int)(activity.getResources().getDisplayMetrics().heightPixels * .86f));
        refresh();
    }

    void refresh() {
        if (dialog == null || !dialog.isShowing()) return;
        content.removeAllViews();
        text("Damit ScootPit deinen Roller findet, richte die folgenden Punkte ein. "
                + "Android fragt jede Freigabe erst nach deinem Antippen ab. Du kannst jederzeit zum Cockpit zurückkehren.");
        boolean bluetooth = connectionAllowed();
        row("1. Geräte in der Nähe", bluetooth,
                Build.VERSION.SDK_INT >= 31
                    ? "Erlaubt die Bluetooth-Suche und die Verbindung zum BMS. Eingeschaltetes Bluetooth allein reicht nicht."
                    : "Auf dieser Android-Version benötigt die Bluetooth-Suche die präzise Standortfreigabe.",
                "Bluetooth erlauben", () -> request(BLUETOOTH, bluetoothPermissions()));
        row("2. Bluetooth", bluetoothEnabled(), "Bluetooth am Handy muss eingeschaltet sein.",
                "Bluetooth einschalten", () -> {
                    if (!connectionAllowed()) request(BLUETOOTH, bluetoothPermissions());
                    else open(new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE), Settings.ACTION_BLUETOOTH_SETTINGS);
                });
        row("3. Präziser Standort", granted(Manifest.permission.ACCESS_FINE_LOCATION),
                "Für Geschwindigkeit, Fahrterkennung und Fahrtroute. Bitte während der App-Nutzung erlauben und Präzise wählen.",
                "Standort erlauben", () -> request(LOCATION, locationPermissions()));
        row("4. Standort am Handy", locationEnabled(), "Für die GPS-Fahrtaufzeichnung muss der Standortdienst eingeschaltet sein.",
                "Standort einschalten", () -> open(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS), Settings.ACTION_SETTINGS));
        if (Build.VERSION.SDK_INT >= 33) row("5. Benachrichtigungen · empfohlen", granted(Manifest.permission.POST_NOTIFICATIONS),
                "Zeigt Bereitschaft, Fahrtstatus und Routine-Nachrichten. Eine Ablehnung verhindert die Bluetooth-Verbindung nicht.",
                "Benachrichtigungen erlauben", () -> request(NOTIFICATIONS, new String[]{Manifest.permission.POST_NOTIFICATIONS}));
        PowerManager power = activity.getSystemService(PowerManager.class);
        row("Akku-Hintergrundbetrieb · empfohlen", power != null && power.isIgnoringBatteryOptimizations(activity.getPackageName()),
                "Für zuverlässige Erkennung bei gesperrtem Handy. Bei Samsung zusätzlich unter App-Info → Akku → Nicht eingeschränkt prüfen.",
                "Akku-Ausnahme anfragen", () -> open(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        Uri.parse("package:" + activity.getPackageName())), Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
        text("BMS auswählen · " + (hasBms() ? "Gespeichert" : "Noch offen")
                + "\nGehe zum Roller. Trenne andere BMS-Apps, die gerade mit diesem BMS verbunden sind.");
        Button select = action("BMS auswählen", () -> { close(); chooseBms.run(); });
        select.setEnabled(connectionAllowed() && bluetoothEnabled() && (Build.VERSION.SDK_INT >= 31 || locationEnabled()));
        if (!select.isEnabled()) text("Vor der Suche bitte die Bluetooth-Freigabe und Bluetooth aktivieren; bis Android 11 auch den Standort.");
        text("Bereitschaft starten\nNach der BMS-Auswahl startet die Bereitschaft die automatische Verbindung und Fahrterkennung.");
        Button start = action("Bereitschaft starten", () -> { close(); startRide.run(); });
        start.setEnabled(rideReady() && hasBms() && !BmsMonitorService.running);
        if (!start.isEnabled()) text(BmsMonitorService.running ? "Bereitschaft ist bereits aktiv."
                : "Vor dem Start bitte die erforderlichen Freigaben, Bluetooth, Standort und die BMS-Auswahl abschließen. Benachrichtigungen und Akku-Ausnahme bleiben optional.");
    }

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

    private void row(String title, boolean allowed, String explanation, String button, Runnable clicked) {
        text(title + " · " + (allowed ? "Erledigt" : "Noch offen") + "\n" + explanation);
        if (!allowed) action(button, clicked);
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
