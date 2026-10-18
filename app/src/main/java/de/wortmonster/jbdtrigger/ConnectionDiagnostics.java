package de.wortmonster.jbdtrigger;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;

/** A shareable snapshot with no Bluetooth addresses, profile names, locations or ride data. */
final class ConnectionDiagnostics {
    static final ConnectionTrace TRACE = new ConnectionTrace();
    private ConnectionDiagnostics() {}

    static String report(Context context) {
        String version = "?";
        try { version = context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName; }
        catch (PackageManager.NameNotFoundException ignored) {}
        String bluetooth = "unbekannt";
        try {
            BluetoothManager manager = context.getSystemService(BluetoothManager.class);
            BluetoothAdapter adapter = manager == null ? null : manager.getAdapter();
            bluetooth = adapter == null ? "nicht verfügbar" : adapter.isEnabled() ? "ein" : "aus";
        } catch (SecurityException ignored) { bluetooth = "Berechtigung fehlt"; }
        PowerManager power = context.getSystemService(PowerManager.class);
        return "ScootPit BMS " + version + " – Verbindungsdiagnose\n"
                + "Gerät: " + Build.MANUFACTURER + " " + Build.MODEL + "\n"
                + "Android: " + Build.VERSION.RELEASE + " (API " + Build.VERSION.SDK_INT + ")\n"
                + "Bluetooth: " + bluetooth + "\n"
                + "Bluetooth-Suche erlaubt: " + (Build.VERSION.SDK_INT < 31 ? "über Standortberechtigung"
                        : allowed(context, Manifest.permission.BLUETOOTH_SCAN)) + "\n"
                + "Bluetooth-Verbindung erlaubt: " + (Build.VERSION.SDK_INT < 31 ? "ja"
                        : allowed(context, Manifest.permission.BLUETOOTH_CONNECT)) + "\n"
                + "Präziser Standort erlaubt: " + allowed(context, Manifest.permission.ACCESS_FINE_LOCATION) + "\n"
                + "Von Akkuoptimierung ausgenommen: " + (power != null
                        && power.isIgnoringBatteryOptimizations(context.getPackageName()) ? "ja" : "nein") + "\n\n"
                + TRACE.snapshot() + "\n\n"
                + "Nur lokale Diagnose; ohne Geräteadresse, Profilnamen und Fahrtroute. "
                + "Teilen erfolgt ausschließlich über deine gewählte App.";
    }

    private static String allowed(Context context, String permission) {
        return context.checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED ? "ja" : "nein";
    }
}
