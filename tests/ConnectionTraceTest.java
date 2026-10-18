package de.wortmonster.jbdtrigger;

public class ConnectionTraceTest {
    public static void main(String[] args) {
        ConnectionTrace trace = new ConnectionTrace();
        if (!trace.snapshot().contains("Noch keine")) throw new AssertionError("Empty trace");
        trace.start(10_000);
        trace.note(13_250, "BMS empfangen");
        trace.note(12_000, "Verspätete Rückmeldung");
        String report = trace.snapshot();
        if (!report.contains("+3,3 s · BMS empfangen") || !report.contains("+3,3 s · Verspätete Rückmeldung"))
            throw new AssertionError("Elapsed times must be monotonic and relative to this session");
        for (int i = 0; i < 1000; i++) trace.note(20_000 + i, "Ereignis " + i);
        if (trace.snapshot().split("\n").length != 80 || !trace.snapshot().contains("Ereignis 999")
                || trace.snapshot().contains("Ereignis 919\n")) throw new AssertionError("Unbounded/incorrect ring buffer");
        trace.start(100_000);
        if (trace.snapshot().contains("Ereignis") || !trace.snapshot().startsWith("+0,0 s"))
            throw new AssertionError("A new readiness session must clear the previous report");
        System.out.println("Bounded, session-local connection timings: OK");
    }
}
