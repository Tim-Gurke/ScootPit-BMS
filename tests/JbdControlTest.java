package de.wortmonster.jbdtrigger;
import java.util.Arrays;
public class JbdControlTest {
    public static void main(String[] args){
        byte[] expected={(byte)0xdd,0x5a,(byte)0xe1,2,0,2,(byte)0xff,0x1b,0x77};
        if(!Arrays.equals(expected,JbdControl.discharge(false)))throw new AssertionError("Manufacturer discharge-off example");
        byte[] on=JbdControl.discharge(true);
        if(on[5]!=0 || (on[7]&255)!=0x1d)throw new AssertionError("Release discharge software lock");
        byte[] ack={(byte)0xdd,(byte)0xe1,0,0,0,0,0x77};
        if(!JbdControl.validReply(ack))throw new AssertionError("ACK");
        ack[2]=(byte)0x80;ack[4]=(byte)0xff;ack[5]=(byte)0x80;
        if(!JbdControl.validReply(ack))throw new AssertionError("Error reply");
        ack[4]=0;if(JbdControl.validReply(ack))throw new AssertionError("Corrupt ACK");
        reject(false,false,false,true,false,0,0.01,6000,0,0.3,0.01);
        reject(true,true,false,true,false,0,0.01,6000,0,0.3,0.01);
        reject(true,false,false,false,true,0,0.01,6000,0,0.3,0.01);
        reject(true,false,false,true,false,-0.30,0.01,6000,0,0.30,0.01);
        reject(true,false,false,true,false,0,0.01,4999,0,0.30,0.01);
        reject(true,false,false,true,false,0,0.01,6000,4,0.30,0.01);
        reject(true,false,false,true,false,Double.NaN,0.01,6000,0,0.30,0.01);
        reject(true,false,false,true,false,0,0.01,6000,0,0,0);
        reject(true,false,false,true,false,0,0.10,6000,0,0.10,0.10);
        if(JbdControl.rejection(true,false,true,true,false,0,0.01,6000,0,0.30,0.01)!=null)throw new AssertionError("Manual paused cutoff below learned baseline");
        if(JbdControl.rejection(true,false,false,true,true,0,0.01,0,0,0,0)!=null)throw new AssertionError("Re-enable output without learned display value");
        System.out.println("JBD command, ACK checksum, learned display-current and manual-pause safeguards: OK");
    }
    private static void reject(boolean fresh,boolean trip,boolean manual,boolean charge,boolean enabled,double current,double resolution,long quiet,double speed,double baseline,double baselineResolution){
        if(JbdControl.rejection(fresh,trip,manual,charge,enabled,current,resolution,quiet,speed,baseline,baselineResolution)==null)throw new AssertionError("Unsafe command accepted");
    }
}
