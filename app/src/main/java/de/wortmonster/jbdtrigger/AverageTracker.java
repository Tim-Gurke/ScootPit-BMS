package de.wortmonster.jbdtrigger;

import java.io.*;
import java.util.*;

/** Distance-weighted ride statistics. Reset counters and distance windows survive ride boundaries. */
final class AverageTracker {
    static final int WINDOW=0,TRIP=1,RESET=2,MAX_WINDOW_METERS=100000;
    private static final int MAX_SAMPLES=4096,MAX_COUNTERS=512;
    private final ArrayList<double[]> samples=new ArrayList<>();
    private final LinkedHashMap<String,double[]> counters=new LinkedHashMap<>();
    private double[] total=new double[4],trip=new double[4];

    AverageTracker(){samples.add(total.clone());}
    void beginTrip(){trip=total.clone();}
    void record(double meters,double movingMilliseconds,double wattHours,boolean energyReliable){
        if(!finite(meters)||!finite(movingMilliseconds)||!finite(wattHours))return;
        total[0]+=meters;total[1]+=movingMilliseconds;total[2]+=wattHours;
        // A telemetry gap also loses energy while stationary. Keep a monotone coverage marker.
        if(!energyReliable)total[3]+=Math.max(1,meters);
        if(meters>0)samples.add(total.clone());
        while(samples.size()>2&&samples.get(1)[0]<total[0]-MAX_WINDOW_METERS)samples.remove(0);
        if(samples.size()>MAX_SAMPLES){
            // Keep recent short windows at full resolution; compact only the oldest half.
            ArrayList<double[]> compact=new ArrayList<>();int half=samples.size()/2;
            for(int i=0;i<half;i+=2)compact.add(samples.get(i));
            compact.addAll(samples.subList(half,samples.size()));samples.clear();samples.addAll(compact);
        }
    }
    boolean ensureCounter(String id){
        if(counters.containsKey(id))return false;
        reset(id);return true;
    }
    void reset(String id){
        if(id==null||!id.matches("[A-Za-z0-9_-]{1,80}"))throw new IllegalArgumentException("Invalid average counter");
        if(!counters.containsKey(id)&&counters.size()>=MAX_COUNTERS)counters.remove(counters.keySet().iterator().next());
        counters.put(id,total.clone());
    }
    Reading reading(int mode,double windowMeters,String id){
        double[] base;
        if(mode==TRIP)base=trip;
        else if(mode==RESET){base=counters.get(id);if(base==null)return Reading.empty();}
        else{
            if(!Double.isFinite(windowMeters)||windowMeters<1||windowMeters>MAX_WINDOW_METERS)return Reading.empty();
            double target=total[0]-windowMeters;
            if(target<samples.get(0)[0]-1e-8)return Reading.empty();
            base=null;double[] previous=null;
            for(double[] sample:samples){
                if(sample[0]>=target){
                    if(previous==null)base=sample.clone();
                    else{double fraction=(target-previous[0])/(sample[0]-previous[0]);base=new double[4];for(int i=0;i<4;i++)base[i]=previous[i]+fraction*(sample[i]-previous[i]);}
                    break;
                }previous=sample;
            }
            if(base==null)return Reading.empty();
        }
        return new Reading(Math.max(0,total[0]-base[0]),Math.max(0,total[1]-base[1]),Math.max(0,total[2]-base[2]),Math.max(0,total[3]-base[3]));
    }
    static final class Reading {
        final double meters,movingMs,wh,missingEnergyMeters;
        Reading(double meters,double movingMs,double wh,double missingEnergyMeters){this.meters=meters;this.movingMs=movingMs;this.wh=wh;this.missingEnergyMeters=missingEnergyMeters;}
        static Reading empty(){return new Reading(0,0,0,0);}
        double speedKmh(){return meters>0&&movingMs>0?meters/movingMs*3600:Double.NaN;}
        double whPerKm(){return meters>0&&missingEnergyMeters<1e-6?wh/meters*1000:Double.NaN;}
    }
    String encode(){
        try{ByteArrayOutputStream bytes=new ByteArrayOutputStream();DataOutputStream out=new DataOutputStream(bytes);
            out.writeInt(1);writePoint(out,total);writePoint(out,trip);out.writeInt(samples.size());
            for(double[] point:samples)writePoint(out,point);
            out.writeInt(counters.size());for(Map.Entry<String,double[]> entry:counters.entrySet()){out.writeUTF(entry.getKey());writePoint(out,entry.getValue());}
            out.close();return Base64.getEncoder().encodeToString(bytes.toByteArray());
        }catch(IOException e){throw new IllegalStateException(e);}
    }
    static AverageTracker restore(String encoded){try{return decode(encoded);}catch(Exception e){return new AverageTracker();}}
    static AverageTracker decode(String encoded)throws IOException{
        if(encoded==null||encoded.isEmpty())return new AverageTracker();
        if(encoded.length()>1000000)throw new IOException("Average history too large");
        try{DataInputStream in=new DataInputStream(new ByteArrayInputStream(Base64.getDecoder().decode(encoded)));
            if(in.readInt()!=1)throw new IOException("Unknown average history");
            AverageTracker tracker=new AverageTracker();tracker.total=readPoint(in);tracker.trip=readPoint(in);within(tracker.trip,tracker.total);
            int count=in.readInt();if(count<1||count>MAX_SAMPLES)throw new IOException("Invalid average history");tracker.samples.clear();double[] previous=null;
            for(int i=0;i<count;i++){double[] point=readPoint(in);within(point,tracker.total);if(previous!=null){within(previous,point);if(point[0]<=previous[0])throw new IOException("Invalid average distance");}tracker.samples.add(point);previous=point;}
            int resets=in.readInt();if(resets<0||resets>MAX_COUNTERS)throw new IOException("Invalid average counters");
            for(int i=0;i<resets;i++){String id=in.readUTF();double[] point=readPoint(in);within(point,tracker.total);if(!id.matches("[A-Za-z0-9_-]{1,80}")||tracker.counters.put(id,point)!=null)throw new IOException("Invalid average counter");}
            if(in.read()!=-1)throw new IOException("Unexpected average data");return tracker;
        }catch(IllegalArgumentException e){throw new IOException("Invalid average history",e);}
    }
    private static boolean finite(double value){return Double.isFinite(value)&&value>=0;}
    private static void writePoint(DataOutputStream out,double[] point)throws IOException{for(double value:point)out.writeDouble(value);}
    private static double[] readPoint(DataInputStream in)throws IOException{double[] result=new double[4];for(int i=0;i<4;i++){result[i]=in.readDouble();if(!finite(result[i]))throw new IOException("Invalid average value");}return result;}
    private static void within(double[] before,double[] after)throws IOException{for(int i=0;i<4;i++)if(before[i]>after[i]+1e-6)throw new IOException("Invalid average baseline");}
}
