package de.wortmonster.jbdtrigger;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.Uri;
import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Runs on the instrumentation worker, with bundled/platform data and no network access. */
final class AltitudeCheck {
    static void run(Context context, SharedPreferences prefs) throws Exception {
        Location germany = fix(49, 8, 100);
        Location washington = fix(38.9, -77.04, 100);
        Location equator = fix(0, 0, 100);
        AltitudeReference.prepare(context, germany);
        AltitudeReference.prepare(context, washington);
        AltitudeReference.prepare(context, equator);
        inRange(AltitudeReference.meters(germany), 40, 60, "German geoid correction");
        inRange(AltitudeReference.meters(washington), 120, 145, "Negative geoid correction");
        inRange(AltitudeReference.meters(equator), 70, 95, "Equatorial geoid correction");
        if (germany.getAltitude() != 100 || washington.getAltitude() != 100)
            throw new AssertionError("Raw ellipsoid heights were overwritten");
        double height = AltitudeReference.meters(germany);
        AltitudeReference.prepare(context, germany);
        if (Math.abs(AltitudeReference.meters(germany) - height) > .0001)
            throw new AssertionError("A prepared fix was corrected twice");
        Location absent = new Location("gps");
        AltitudeReference.prepare(context, absent);
        if (!Double.isNaN(AltitudeReference.meters(absent))) throw new AssertionError("Missing altitude became zero");

        File folder = TripImporter.folder(context, prefs);
        Set<String> existing = new HashSet<>();
        File[] before = folder.listFiles();
        if (before != null) for (File file : before) existing.add(file.getName());
        long start = System.currentTimeMillis() - 60_000;
        try {
            TripRecorder recorder = new TripRecorder(context, start);
            germany.setTime(start);
            recorder.add(germany, 80, 48, -2, 96, 0, 96, 20, new double[]{25,30});
            Location next = new Location(germany);
            next.setTime(start + 1000);
            recorder.add(next, 80, 48, -2, 96, .026, 96, 20, new double[]{25,30});
            absent.setLatitude(49); absent.setLongitude(8); absent.setTime(start + 2000);
            recorder.add(absent, 80, 48, -2, 96, .052, 96, 20, new double[]{25,30});
            TripRecorder.Summary summary = recorder.finish(start + 3000, 0, 10.8, 0, .052, 96, 20, 25, 30, 3000, 0);
            List<String> csv = Files.readAllLines(summary.csvFile.toPath(), StandardCharsets.UTF_8);
            String[] first = csv.get(1).split(";", -1);
            if (!csv.get(0).endsWith("GPS_Ellipsoidhoehe_m;Hoehenbezug")
                    || Math.abs(TripJournal.number(first[5]) - height) > .01
                    || TripJournal.number(first[15]) != 100 || !"MSL".equals(first[16])
                    || !csv.get(3).split(";", -1)[5].isEmpty())
                throw new AssertionError("CSV must retain corrected height, raw height and missing measurements");
            String gpx = new String(Files.readAllBytes(summary.gpxFile.toPath()), StandardCharsets.UTF_8);
            if (!gpx.contains("sp:altitude_reference") || !gpx.contains(">MSL<") || gpx.contains("<ele>100.00</ele>"))
                throw new AssertionError("GPX height/datum mismatch");
            String missingPoint = gpx.substring(gpx.lastIndexOf("<trkpt"));
            if (missingPoint.contains("<ele>")) throw new AssertionError("GPX invented missing altitude");
            TripImporter.importFile(context, prefs, Uri.fromFile(summary.csvFile), "altitude.csv");
            TripImporter.importFile(context, prefs, Uri.fromFile(summary.gpxFile), "altitude.gpx");
            int imported = 0;
            for (TripJournal.Ride ride : TripJournal.list(context, prefs, 7)) {
                if (ride.start != start) continue;
                TripJournal.loadPoints(ride);
                if (!"MSL".equals(ride.metadata.optString("altitude_reference"))
                        || ride.points.size() != 3 || Math.abs(ride.points.get(0).altitude - height) > .01
                        || !Double.isNaN(ride.points.get(2).altitude))
                    throw new AssertionError("Recording/import changed the height or lost its reference");
                if (ride.metadata.optBoolean("imported")) imported++;
            }
            if (imported != 2) throw new AssertionError("CSV and GPX round trips were not both checked");
        } finally {
            File[] after = folder.listFiles();
            if (after != null) for (File file : after) if (!existing.contains(file.getName())) file.delete();
        }
    }

    private static Location fix(double lat, double lon, double height) {
        Location location = new Location("gps");
        location.setLatitude(lat); location.setLongitude(lon); location.setAltitude(height);
        location.setAccuracy(3); location.setVerticalAccuracyMeters(5); location.setSpeed(3);
        return location;
    }

    private static void inRange(double value, double min, double max, String message) {
        if (!Double.isFinite(value) || value < min || value > max) throw new AssertionError(message + ": " + value);
    }
}
