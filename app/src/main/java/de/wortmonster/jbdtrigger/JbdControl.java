package de.wortmonster.jbdtrigger;

/** JBD General Protocol V4, E1. Never infer a charging software lock from FET-off. */
final class JbdControl {
    static final double MAX_SWITCH_CURRENT_AMPS = 0.15;
    static final long QUIET_MS = 5000;
    static byte[] discharge(boolean enabled) {
        int value=enabled?0:2, checksum=-(0xe1+2+value)&65535;
        return new byte[]{(byte)0xdd,0x5a,(byte)0xe1,2,0,(byte)value,
            (byte)(checksum>>8),(byte)checksum,0x77};
    }
    static boolean validReply(byte[] b) {
        if(b==null || b.length<7 || (b[0]&255)!=0xdd || b.length!=(b[3]&255)+7 || b[b.length-1]!=0x77)return false;
        int sum=0;for(int i=2;i<b.length-3;i++)sum+=b[i]&255;
        return ((-sum)&65535)==((b[b.length-3]&255)<<8 | (b[b.length-2]&255));
    }
    /** A fixed low-load window; independent of learned values, previous rides and GPS reception. */
    static boolean lowLoad(double current, double resolution) {
        return Double.isFinite(current) && Double.isFinite(resolution) && resolution > 0
                && Math.abs(current) + resolution / 2.0 < MAX_SWITCH_CURRENT_AMPS;
    }
    static String rejection(boolean fresh, boolean trip, boolean manuallyPaused, boolean chargeOn,
                            boolean enabled, double current, double currentResolution,
                            long quietMs, double speed) {
        if(!fresh)return "Frische BMS-Verbindung erforderlich";
        if(!chargeOn)return "Lade-MOS ist aus: Schalten gesperrt, damit der Ladezustand unverändert bleibt";
        if(trip&&!manuallyPaused)return "Fahrt zuerst manuell pausieren";
        if(speed>=2)return "Nur bei stehendem Roller schalten";
        if(!enabled && (!Double.isFinite(current)||!Double.isFinite(currentResolution)||currentResolution<=0))return "Keine gültige Strommessung verfügbar";
        if(!enabled && currentResolution/2.0>=MAX_SWITCH_CURRENT_AMPS)return "Strommessung ist für die Abschaltprüfung zu grob";
        if(!enabled && !lowLoad(current,currentResolution))return "Stromaufnahme noch zu hoch. Roller ausschalten und mindestens 5 Sekunden unter 0,15 A warten.";
        if(!enabled && quietMs<QUIET_MS)return "Bitte mindestens 5 Sekunden mit geringer Stromaufnahme (unter 0,15 A) warten.";
        return null;
    }
}
