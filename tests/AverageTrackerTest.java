package de.wortmonster.jbdtrigger;

public final class AverageTrackerTest {
    private static void close(double expected,double actual){if(!Double.isFinite(actual)||Math.abs(expected-actual)>1e-7)throw new AssertionError(expected+" != "+actual);}
    private static void missing(double actual){if(!Double.isNaN(actual))throw new AssertionError("Expected unavailable reading: "+actual);}
    public static void main(String[] args)throws Exception{
        AverageTracker tracker=new AverageTracker();tracker.beginTrip();tracker.reset("speed");tracker.reset("energy");
        tracker.record(100,20000,2,true);tracker.record(100,40000,4,true);
        close(12,tracker.reading(1,500,"speed").speedKmh());close(30,tracker.reading(1,500,"energy").whPerKm());
        close(9,tracker.reading(0,100,"speed").speedKmh());close(40,tracker.reading(0,100,"energy").whPerKm());
        close(10.8,tracker.reading(0,150,"speed").speedKmh());close(1000d*5/150,tracker.reading(0,150,"energy").whPerKm());
        missing(tracker.reading(0,201,"speed").speedKmh());
        tracker.reset("speed");tracker.record(100,10000,0,true);
        close(36,tracker.reading(2,500,"speed").speedKmh());close(20,tracker.reading(2,500,"energy").whPerKm());
        close(0,tracker.reading(0,50,"energy").whPerKm());
        tracker=AverageTracker.decode(tracker.encode());tracker.beginTrip();tracker.record(200,20000,10,true);
        close(36,tracker.reading(1,500,"speed").speedKmh());close(50,tracker.reading(1,500,"energy").whPerKm());
        close(32,tracker.reading(2,500,"energy").whPerKm());
        tracker.record(0,0,2,true);close(60,tracker.reading(1,500,"energy").whPerKm());
        tracker.record(50,5000,0,false);missing(tracker.reading(1,500,"energy").whPerKm());close(36,tracker.reading(1,500,"speed").speedKmh());
        tracker.record(100,10000,3,true);close(30,tracker.reading(0,100,"energy").whPerKm());
        tracker.record(Double.NaN,1,1,true);tracker.record(-1,1,1,true);tracker=AverageTracker.decode(tracker.encode());
        tracker.reset("energy");missing(tracker.reading(2,500,"energy").whPerKm());
        for(int i=0;i<10000;i++)tracker.record(1,100,0.01,true);
        close(36,tracker.reading(0,100,"speed").speedKmh());close(10,tracker.reading(0,100,"energy").whPerKm());
        AverageTracker.decode(tracker.encode());
        boolean rejected=false;try{AverageTracker.decode("invalid");}catch(java.io.IOException expected){rejected=true;}if(!rejected)throw new AssertionError("Invalid persisted state accepted");
        System.out.println("AverageTracker: windows, interpolation, rides, separate resets, persistence and missing BMS coverage OK");
    }
}
