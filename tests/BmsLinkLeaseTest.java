package de.wortmonster.jbdtrigger;
public final class BmsLinkLeaseTest {
 private static void check(boolean v){if(!v)throw new AssertionError();}
 public static void main(String[] args){
  BmsLinkLease s=new BmsLinkLease();check(s.acquire("watt",100));check(!s.acquire("other",101));
  s.internal(true,1000);check(!s.acquire("watt",1001));check(!s.internalAllowed(5000));s.release("wrong",6000);check(!s.internalAllowed(6001));s.release("watt",6500);check(!s.internalAllowed(8499));check(s.internalAllowed(8500));
  s.internal(false,9000);check(!s.acquire("watt",10999));check(s.acquire("watt",11000));
  s.internal(true,12000);check(!s.internalAllowed(20999));check(s.internalAllowed(21000));
  s.interested("AA:BB:CC:DD:EE:FF",20000);check(s.interestedIn("aa:bb:cc:dd:ee:ff",31999));check(!s.interestedIn("02:00:00:00:00:02",21000));check(!s.interestedIn("AA:BB:CC:DD:EE:FF",32000));
  System.out.println("PASS shared BLE lease: priority, release, cool-down, crash expiry and device-specific reader interest");
 }
}
