package de.wortmonster.jbdtrigger;

import android.content.*;
import android.content.pm.*;
import android.database.Cursor;
import android.net.Uri;
import android.os.*;
import java.security.MessageDigest;

/** Only the official local Watt'n'Pit app can read BMS telemetry or request a BLE lease. */
public final class BmsShareProvider extends ContentProvider {
    static final BmsLinkLease LEASE=new BmsLinkLease();
    private static final String PEER="de.wattpit.app",CERT="0e6e04bc2b50990a8f28bebae31dea596c2cd2f8563f1505aaaafc07277faeb2";
    private static String packetMac="";
    private static byte[] packet;
    private static long packetAt,packetElapsed;
    @Override public boolean onCreate(){return true;}
    static void internal(boolean running){LEASE.internal(running,SystemClock.elapsedRealtime());if(!running)clear();}
    static boolean mayConnect(){return LEASE.internalAllowed(SystemClock.elapsedRealtime());}
    static boolean interested(String mac){return LEASE.interestedIn(mac,SystemClock.elapsedRealtime());}
    static synchronized void publish(String mac,byte[] frame){packetMac=mac;packet=frame.clone();packetAt=System.currentTimeMillis();packetElapsed=SystemClock.elapsedRealtime();}
    static synchronized void clear(){packet=null;packetAt=packetElapsed=0;packetMac="";}
    @SuppressWarnings("deprecation")
    private void authorize(){
        if(Binder.getCallingUid()==android.os.Process.myUid())return;
        PackageManager pm=getContext().getPackageManager();String[] names=pm.getPackagesForUid(Binder.getCallingUid());
        if(names!=null)for(String name:names)if(PEER.equals(name))try{
            PackageInfo info=pm.getPackageInfo(PEER,Build.VERSION.SDK_INT>=28?PackageManager.GET_SIGNING_CERTIFICATES:PackageManager.GET_SIGNATURES);
            Signature[] signatures=Build.VERSION.SDK_INT>=28?info.signingInfo.getApkContentsSigners():info.signatures;
            if(signatures!=null)for(Signature signature:signatures){byte[] digest=MessageDigest.getInstance("SHA-256").digest(signature.toByteArray());StringBuilder hex=new StringBuilder();for(byte b:digest)hex.append(String.format(java.util.Locale.ROOT,"%02x",b&255));if(CERT.equals(hex.toString()))return;}
        }catch(Exception ignored){}
        throw new SecurityException("BMS-Daten sind nur für die lokale Watt’n’Pit-App freigegeben.");
    }
    @Override public Bundle call(String method,String mac,Bundle extras){
        authorize();if(mac==null||!mac.matches("(?i)([0-9a-f]{2}:){5}[0-9a-f]{2}"))throw new IllegalArgumentException("Ungültiges BMS");
        long now=SystemClock.elapsedRealtime();Bundle out=new Bundle();out.putInt("protocol",1);
        switch(method){
            case "snapshot":LEASE.interested(mac,now);out.putBoolean("internal",LEASE.internal());synchronized(BmsShareProvider.class){if(packet!=null&&mac.equalsIgnoreCase(packetMac)&&now>=packetElapsed&&now-packetElapsed<=8000){out.putByteArray("frame",packet.clone());out.putLong("at",packetAt);out.putLong("elapsed",packetElapsed);out.putString("mac",packetMac);}}break;
            case "acquire":out.putBoolean("granted",LEASE.acquire(extras==null?null:extras.getString("token"),now));break;
            case "release":LEASE.release(extras==null?null:extras.getString("token"),now);break;
            default:throw new IllegalArgumentException("Unbekannter BMS-Lesezugriff");
        }return out;
    }
    @Override public Cursor query(Uri u,String[] p,String s,String[] a,String o){authorize();throw new UnsupportedOperationException();}
    @Override public String getType(Uri u){authorize();return "application/vnd.scootpit.bms";}
    @Override public Uri insert(Uri u,ContentValues v){throw new UnsupportedOperationException();}
    @Override public int update(Uri u,ContentValues v,String s,String[] a){throw new UnsupportedOperationException();}
    @Override public int delete(Uri u,String s,String[] a){throw new UnsupportedOperationException();}
}
