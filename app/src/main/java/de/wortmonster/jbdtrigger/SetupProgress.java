package de.wortmonster.jbdtrigger;

/** Ordered setup, with completion based on saved samples rather than a connection indicator. */
final class SetupProgress {
    enum Step { NEARBY, BLUETOOTH, LOCATION, GPS, NOTIFICATIONS, BATTERY, BMS, READINESS, LIVE_DATA, TEST_RIDE, DONE }
    static final class Status {
        boolean nearby, bluetooth, location, gps, notifications, battery, bms, readiness, liveData, recorded;
    }
    static Step next(Status s) {
        if (!s.nearby) return Step.NEARBY;
        if (!s.bluetooth) return Step.BLUETOOTH;
        if (!s.location) return Step.LOCATION;
        if (!s.gps) return Step.GPS;
        if (!s.notifications) return Step.NOTIFICATIONS;
        if (!s.battery) return Step.BATTERY;
        if (!s.bms) return Step.BMS;
        // A verified trial remains verified after the scooter has been parked or switched off.
        if (s.recorded) return Step.DONE;
        if (!s.readiness) return Step.READINESS;
        if (!s.liveData) return Step.LIVE_DATA;
        return Step.TEST_RIDE;
    }
    static boolean fresh(long at, long now) { return at > 0 && now >= at && now - at <= 8000; }
    static boolean recorded(long trialStartedAt, long now, long bmsAt, long gpsAt, long points, double meters) {
        return trialStartedAt > 0 && now >= trialStartedAt && gpsAt >= trialStartedAt
                && fresh(bmsAt, now) && fresh(gpsAt, now) && points >= 2 && Double.isFinite(meters) && meters > 0;
    }
}
