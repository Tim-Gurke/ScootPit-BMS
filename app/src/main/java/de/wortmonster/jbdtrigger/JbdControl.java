package de.wortmonster.jbdtrigger;

/** JBD General Protocol V4, E1. Never infer a charging software lock from FET-off. */
final class JbdControl {
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
    static String rejection(boolean fresh, boolean trip, boolean manuallyPaused, boolean chargeOn,
                            boolean enabled, double current, double currentResolution,
                            long quietMs, double speed, double displayCurrent,
                            double displayResolution) {
        if(!fresh)return "Frische BMS-Verbindung erforderlich";
        if(!chargeOn)return "Lade-MOS ist aus: Schalten gesperrt, damit der Ladezustand unverändert bleibt";
        if(trip&&!manuallyPaused)return "Fahrt zuerst manuell pausieren";
        if(speed>=2)return "Nur bei stehendem Roller schalten";
        if(!enabled && displayCurrent<=0)return "Standstrom-Referenz fehlt. Nach einer Fahrt den eingeschalteten Roller mit GPS-Empfang stehen lassen, bis die Referenz gelernt ist. Danach den Roller ausschalten und erneut sperren.";
        if(!enabled && !DisplayCurrentLearner.safelyBelow(current,currentResolution,displayCurrent,displayResolution))return "Standstrom liegt noch an – zum Sperren muss der Strom sicher unter die Standstrom-Referenz fallen";
        if(!enabled && quietMs<5000)return "Strom muss mindestens 5 Sekunden sicher unter der Standstrom-Referenz bleiben";
        return null;
    }
}
