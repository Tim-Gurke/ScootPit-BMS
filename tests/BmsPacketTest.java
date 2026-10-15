package de.wortmonster.jbdtrigger;
public class BmsPacketTest {
    public static void main(String[] args) {
        byte[] b = new byte[34]; b[0]=(byte)0xdd; b[1]=3; b[3]=27;
        put(b,4,4800); put(b,6,65536-123); put(b,8,625); put(b,10,1250);
        b[23]=50; b[24]=3; b[26]=2; put(b,27,2981); put(b,29,3031); b[33]=0x77;
        int sum=0; for(int i=2;i<31;i++)sum+=b[i]&255; put(b,31,(-sum)&65535);
        BmsPacket p=BmsPacket.decode(b);
        if(p==null || p.voltage!=48 || p.current!=-1.23 || p.remainingAh!=6.25
                || p.currentResolution!=0.01 || !p.chargeEnabled || !p.dischargeEnabled || p.soc!=50 || p.temperatures[0]!=25 || p.temperatures[1]!=30)throw new AssertionError("decode");
        byte[] coarse = new byte[35]; System.arraycopy(b,0,coarse,0,31);
        coarse[3]=28; coarse[31]=(byte)0x88; coarse[34]=0x77; checksum(coarse);
        p=BmsPacket.decode(coarse);
        if(p==null||p.current!=-12.3||p.currentResolution!=0.1||p.remainingAh!=62.5||p.fullAh!=125.0
                ||!p.chargeEnabled||!p.dischargeEnabled)throw new AssertionError("newer JBD /10 sentinel scaling");
        coarse[5]++; if(BmsPacket.decode(coarse)!=null)throw new AssertionError("checksum");
        if(BmsPacket.decode(new byte[20])!=null)throw new AssertionError("truncated");
        System.out.println("BMS decoding, temperatures, signed current and invalid frames: OK");
    }
    private static void put(byte[] b,int i,int n){b[i]=(byte)(n>>8);b[i+1]=(byte)n;}
    private static void checksum(byte[] b){int length=b[3]&255,sum=0;for(int i=2;i<length+4;i++)sum+=b[i]&255;put(b,length+4,(-sum)&65535);}
}
