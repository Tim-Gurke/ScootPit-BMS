package de.wortmonster.jbdtrigger;

/** One explicit unlock intent. It is never persisted or replayed after its deadline. */
final class PendingUnlock {
    static final long TIMEOUT_MS=20000;
    private long id,until;
    boolean pending(){return id!=0;}
    boolean begin(long request,long now){if(request<=0||pending())return false;id=request;until=now+TIMEOUT_MS;return true;}
    long expire(long now){if(!pending()||now<until)return 0;return cancel();}
    long take(long now){if(!pending()||now>=until)return 0;return cancel();}
    long cancel(){long value=id;id=until=0;return value;}
}
