package de.wortmonster.jbdtrigger;
public final class PendingUnlockTest {
 private static void check(boolean value){if(!value)throw new AssertionError();}
 public static void main(String[] args){
  PendingUnlock request=new PendingUnlock();check(!request.pending());check(!request.begin(0,100));
  check(request.begin(7,100));check(!request.begin(8,101));check(request.expire(20099)==0);check(request.take(20099)==7);check(request.take(20100)==0);
  check(request.begin(8,100));check(request.take(20100)==0);check(request.expire(20100)==8);check(!request.pending());
  check(request.begin(9,500));check(request.cancel()==9);check(request.take(501)==0);check(request.expire(100000)==0);
  PendingUnlock restarted=new PendingUnlock();check(!restarted.pending());check(restarted.take(501)==0);
  System.out.println("PASS explicit unlock expires at its monotonic deadline, is consumed once, cancels without replay, and never survives a service restart");
 }
}
