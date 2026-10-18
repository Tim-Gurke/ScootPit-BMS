package de.wortmonster.jbdtrigger;

import java.util.ArrayDeque;
import java.util.Locale;

/** Bounded in-memory events. Callers supply fixed descriptions and numeric status codes only. */
final class ConnectionTrace {
    private static final int LIMIT = 80;
    private final ArrayDeque<String> events = new ArrayDeque<>();
    private long started = -1;
    private long last;

    synchronized void start(long now) {
        events.clear();
        started = last = now;
        note(now, "Bereitschaft gestartet");
    }

    synchronized void note(long now, String event) {
        if (started < 0) return;
        last = Math.max(last, now);
        events.addLast(String.format(Locale.GERMANY, "+%.1f s · %s", (last - started) / 1000.0, event));
        while (events.size() > LIMIT) events.removeFirst();
    }

    synchronized String snapshot() {
        return events.isEmpty() ? "Noch keine Bereitschaft in diesem App-Prozess gestartet."
                : String.join("\n", events);
    }
}
