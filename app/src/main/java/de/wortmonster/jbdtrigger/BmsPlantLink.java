package de.wortmonster.jbdtrigger;

import android.content.*;
import android.content.pm.*;
import android.net.Uri;
import android.os.*;
import org.json.*;
import java.security.MessageDigest;
import java.util.*;
import java.util.concurrent.*;

/** Calls official local Watt'n'Pit, which authenticates the existing plant LAN. No routes/GPS. */
final class BmsPlantLink implements AutoCloseable {
    private static final Uri URI=Uri.parse("content://de.wattpit.app.roller-link");
    private final Context context;
    private final SharedPreferences prefs;
    private final Handler main=new Handler(Looper.getMainLooper());
    private final Runnable yield;
    private final ScheduledExecutorService worker=Executors.newSingleThreadScheduledExecutor();
    private final String token;
    private final long epoch;
    private volatile String mac="",message="BMS-Zuständigkeit wird geprüft";
    private volatile boolean ready,available,granted,wanted,released,yielding,stopped,tripEnded=true,busy;
    private volatile long generation,lastHost,homeSince;
    private long nextSequence;
    private static final class Sample {final String mac;final byte[] frame;final long elapsed,sequence;Sample(String m,byte[] f,long at,long seq){mac=m;frame=f.clone();elapsed=at;sequence=seq;}}
    private volatile Sample sample;
    BmsPlantLink(Context c,Runnable callback){context=c;yield=callback;prefs=c.getSharedPreferences("settings",Context.MODE_PRIVATE);
        token=UUID.randomUUID().toString();epoch=prefs.getLong("plant_reader_epoch",0)+1;prefs.edit().putLong("plant_reader_epoch",epoch).commit();
        wanted=prefs.getBoolean("plant_reader_wanted",false);
    }
    private String readerToken(){return token;}
    void start(){worker.scheduleWithFixedDelay(this::exchange,0,2,TimeUnit.SECONDS);}
    void state(String address,boolean ended,boolean controlBusy){
        if(!address.equals(mac)){mac=address;sample=null;nextSequence=0;generation++;ready=false;available=granted=released=yielding=false;homeSince=0;}
        tripEnded=ended;busy=controlBusy;
    }
    void sample(String address,byte[] bytes){sample=new Sample(address,bytes,SystemClock.elapsedRealtime(),++nextSequence);if(!available&&!wanted&&!released){wanted=true;prefs.edit().putBoolean("plant_reader_wanted",true).apply();}}
    void disconnected(){sample=null;}
    void request(){wanted=true;released=yielding=false;granted=false;generation++;prefs.edit().putBoolean("plant_reader_wanted",true).apply();worker.execute(this::exchange);}
    void cancelTake(){wanted=false;granted=false;generation++;prefs.edit().putBoolean("plant_reader_wanted",false).apply();worker.execute(()->callCancel());}
    boolean allowed(){if(!ready)return false;if(released||yielding)return !available&&SystemClock.elapsedRealtime()-lastHost>15000;return !available||wanted&&granted;}
    boolean awaiting(){return ready&&available&&!allowed();}
    boolean shouldYield(){return yielding&&!busy&&tripEnded;}
    void closedForReturn(){released=true;yielding=false;disconnected();worker.execute(this::exchange);}
    String message(){return message;}
    @SuppressWarnings("deprecation") private boolean trusted(){
        PackageManager pm=context.getPackageManager();ProviderInfo provider=pm.resolveContentProvider(URI.getAuthority(),0);if(provider==null)return false;
        if(!provider.packageName.equals("de.wattpit.app"))throw new SecurityException("Unerwartete Anlagen-App");
        try{PackageInfo info=pm.getPackageInfo(provider.packageName,Build.VERSION.SDK_INT>=28?PackageManager.GET_SIGNING_CERTIFICATES:PackageManager.GET_SIGNATURES);
            Signature[] signatures=Build.VERSION.SDK_INT>=28?info.signingInfo.getApkContentsSigners():info.signatures;
            if(signatures!=null)for(Signature signature:signatures){StringBuilder hex=new StringBuilder();for(byte b:MessageDigest.getInstance("SHA-256").digest(signature.toByteArray()))hex.append(String.format(Locale.ROOT,"%02x",b&255));if(hex.toString().equals("0e6e04bc2b50990a8f28bebae31dea596c2cd2f8563f1505aaaafc07277faeb2"))return true;}
        }catch(PackageManager.NameNotFoundException e){return false;}catch(java.security.NoSuchAlgorithmException e){throw new IllegalStateException(e);}
        throw new SecurityException("Anlagen-App hat eine andere Signatur");
    }
    private Bundle call(String action,String address,JSONObject data){Bundle extras=new Bundle();extras.putString("payload",data.toString());return context.getContentResolver().call(URI,action,address,extras);}
    private void callCancel(){try{if(trusted()&&!mac.isEmpty())call("cancel",mac,new JSONObject().put("token",readerToken()).put("epoch",epoch));}catch(Exception ignored){}}
    private synchronized void exchange(){
        if(stopped||mac.isEmpty())return;long version=generation;String address=mac;
        try{
            if(!trusted()){ready=true;available=false;granted=true;message="Direkte BMS-Verbindung · keine gekoppelte Zentrale";return;}
            long now=SystemClock.elapsedRealtime();Sample captured=sample;long age=captured==null?Long.MAX_VALUE:now-captured.elapsed;
            byte[] packet=captured==null||!captured.mac.equalsIgnoreCase(address)?null:captured.frame;BmsPacket decoded=packet==null?null:BmsPacket.decode(packet);
            boolean offer=wanted&&!released&&decoded!=null&&age>=0&&age<=3000&&!decoded.dischargeEnabled&&tripEnded&&!busy&&homeSince>0&&now-homeSince>=8000;
            String action=released?"released":offer?"offer":wanted?"take":"inspect";
            JSONObject input=new JSONObject().put("token",readerToken()).put("epoch",epoch).put("sequence",captured==null?-1:captured.sequence).put("age",age).put("tripEnded",tripEnded);
            if(packet!=null&&age>=0&&age<=8000)input.put("frame",Base64.getEncoder().encodeToString(packet));
            Bundle response=call(action,address,input);if(version!=generation||stopped)return;
            ready=true;
            if(response==null||response.getInt("protocol")!=2||!response.getBoolean("available")){available=false;granted=true;homeSince=0;message="Zentrale nicht erreichbar · S20 verbindet sich direkt";return;}
            JSONObject result=new JSONObject(response.getString("response","{}"));
            if(result.has("available")&&!result.optBoolean("available")){available=false;granted=true;homeSince=0;message="Dieses BMS wird direkt vom Fahrhandy gelesen";return;}
            available=true;if(now-lastHost>7000||homeSince==0)homeSince=now;lastHost=now;
            granted=result.optBoolean("granted");message=result.optString("message",response.getString("error","Warte auf BMS-Freigabe der Zentrale"));
            if(result.optBoolean("returnRequested")&&!yielding&&!released){yielding=true;main.post(()->{if(!stopped&&version==generation&&shouldYield())yield.run();else yielding=false;});}
            if(released&&result.optBoolean("hostFresh")){wanted=false;released=false;granted=false;prefs.edit().putBoolean("plant_reader_wanted",false).apply();message="Roller wieder mit Zentrale verbunden";}
        }catch(SecurityException e){ready=true;available=true;granted=false;message="BMS-Übergabe verweigert · App-Signaturen prüfen";}
        catch(Exception e){if(version==generation){ready=true;available=false;granted=true;homeSince=0;message="Zentrale nicht erreichbar · direkte BMS-Verbindung";}}
    }
    @Override public void close(){stopped=true;generation++;worker.shutdownNow();}
}
