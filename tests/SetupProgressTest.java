package de.wortmonster.jbdtrigger;

public final class SetupProgressTest {
    public static void main(String[] args) {
        SetupProgress.Status s = new SetupProgress.Status();
        check(SetupProgress.next(s) == SetupProgress.Step.NEARBY, "Explain permission before search");
        s.nearby = true; check(SetupProgress.next(s) == SetupProgress.Step.BLUETOOTH, "Permission does not enable Bluetooth");
        s.bluetooth = true; check(SetupProgress.next(s) == SetupProgress.Step.LOCATION, "GPS permission is separate");
        s.location = true; check(SetupProgress.next(s) == SetupProgress.Step.GPS, "GPS service is separate");
        s.gps = true; check(SetupProgress.next(s) == SetupProgress.Step.NOTIFICATIONS, "Explain optional notifications");
        s.notifications = true; check(SetupProgress.next(s) == SetupProgress.Step.BATTERY, "Explain optional battery exemption");
        s.battery = true; check(SetupProgress.next(s) == SetupProgress.Step.BMS, "No readiness without BMS");
        s.bms = true; check(SetupProgress.next(s) == SetupProgress.Step.READINESS, "A saved address is not a running service");
        s.readiness = true; check(SetupProgress.next(s) == SetupProgress.Step.LIVE_DATA, "Running service does not prove data reception");
        s.liveData = true; check(SetupProgress.next(s) == SetupProgress.Step.TEST_RIDE, "Fresh data does not prove recording");
        s.recorded = true; check(SetupProgress.next(s) == SetupProgress.Step.DONE, "Finish after real saved recording");
        s.readiness = s.liveData = false;
        check(SetupProgress.next(s) == SetupProgress.Step.DONE, "Remember trial after parking");
        s.nearby = false;
        check(SetupProgress.next(s) == SetupProgress.Step.NEARBY, "Revoked permission needs repair even after a trial");
        long now=100000;
        check(SetupProgress.recorded(90000,now,now,now,2,4),"Fresh stored moving samples");
        check(!SetupProgress.recorded(0,now,now,now,2,4),"No trial before user setup");
        check(!SetupProgress.recorded(90000,now,0,now,2,4),"No BMS");
        check(!SetupProgress.recorded(90000,now,now-8001,now,2,4),"Stale BMS");
        check(!SetupProgress.recorded(90000,now,now,now-8001,2,4),"Stale GPS");
        check(!SetupProgress.recorded(99000,now,now,98000,2,4),"No samples preceding this trial");
        check(!SetupProgress.recorded(90000,now,now,now,1,4),"One point does not establish a route");
        check(!SetupProgress.recorded(90000,now,now,now,2,0),"Stationary data does not establish a ride");
        check(!SetupProgress.recorded(90000,now,now,now,2,Double.NaN),"Unknown distance");
        check(!SetupProgress.recorded(90000,now,now+1,now,2,4),"Future data");
        System.out.println("Ordered setup and saved recording proof: OK");
    }
    private static void check(boolean result,String message) { if (!result) throw new AssertionError(message); }
}
