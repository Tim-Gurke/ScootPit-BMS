package de.wortmonster.jbdtrigger;

/** One local BLE owner; ScootPit has priority and waits for the reader's release. */
final class BmsLinkLease {
    private boolean internal;
    private String external="",interestedMac="";
    private long expires,internalAfter,externalAfter,interestUntil;
    synchronized void internal(boolean enabled,long now){
        if(internal==enabled)return;internal=enabled;
        if(enabled)internalAfter=now+4000;
        else externalAfter=now+2000;
    }
    synchronized boolean internal(){return internal;}
    synchronized boolean internalAllowed(long now){return internal&&now>=internalAfter&&(external.isEmpty()||now>=expires);}
    synchronized boolean acquire(String token,long now){
        if(internal||now<externalAfter||token==null||token.isEmpty())return false;
        if(!external.isEmpty()&&!external.equals(token)&&now<expires)return false;
        external=token;expires=now+10000;return true;
    }
    synchronized void release(String token,long now){if(!external.equals(token))return;external="";expires=0;internalAfter=Math.max(internalAfter,now+2000);}
    synchronized void interested(String mac,long now){interestedMac=mac;interestUntil=now+12000;}
    synchronized boolean interestedIn(String mac,long now){return mac!=null&&mac.equalsIgnoreCase(interestedMac)&&now<interestUntil;}
}
