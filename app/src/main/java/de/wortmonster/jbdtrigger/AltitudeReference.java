package de.wortmonster.jbdtrigger;

import android.content.Context;
import android.location.Location;
import androidx.core.location.LocationCompat;
import androidx.core.location.altitude.AltitudeConverterCompat;
import java.io.IOException;

/** Local WGS84 -> mean sea level conversion; never replaces the original GPS altitude. */
final class AltitudeReference {
    static final String MSL = "MSL";
    static final String WGS84 = "WGS84";
    static final String UNKNOWN = "unknown";
    static final String XML_NAMESPACE = "https://github.com/Tim-Gurke/ScootPit-BMS";

    private AltitudeReference() {}

    /** Call on a worker: the first conversion can load the bundled geoid database. */
    static void prepare(Context context, Location location) {
        if (location == null || !location.hasAltitude() || !Double.isFinite(location.getAltitude())) return;
        if (LocationCompat.hasMslAltitude(location)
                && Double.isFinite(LocationCompat.getMslAltitudeMeters(location))) return;
        try {
            AltitudeConverterCompat.addMslAltitudeToLocation(context, location);
        } catch (IOException | RuntimeException exception) {
            // An unavailable correction is a missing measurement, never an ellipsoid value labelled MSL.
            LocationCompat.removeMslAltitude(location);
        }
    }

    static double meters(Location location) {
        if (location == null || !LocationCompat.hasMslAltitude(location)) return Double.NaN;
        double value = LocationCompat.getMslAltitudeMeters(location);
        return Double.isFinite(value) ? value : Double.NaN;
    }

    static String normalize(String reference) {
        if (MSL.equalsIgnoreCase(reference)) return MSL;
        if (WGS84.equalsIgnoreCase(reference)) return WGS84;
        return UNKNOWN;
    }

    static String label(String reference) {
        if (MSL.equals(reference)) return "Höhe über Meeresspiegel (MSL)";
        if (WGS84.equals(reference)) return "GPS-Höhe (WGS84-Ellipsoid, ältere Fahrt)";
        return "GPS-Höhe (Höhenbezug unbekannt)";
    }

    static String gpxMetadata(String reference) {
        return "<metadata><extensions><sp:altitude_reference xmlns:sp=\"" + XML_NAMESPACE
                + "\">" + normalize(reference) + "</sp:altitude_reference></extensions></metadata>\n";
    }
}
