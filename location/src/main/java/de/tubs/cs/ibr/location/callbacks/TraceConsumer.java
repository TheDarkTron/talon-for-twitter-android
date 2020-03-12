package de.tubs.cs.ibr.location.callbacks;

import java.util.concurrent.TimeUnit;

import de.tubs.cs.ibr.location.data.Trace;

public class TraceConsumer {
    private TraceCallback callback;
    private long timeInterval;
    private TimeUnit timeUnit;

    private long triggers = 0;
    private long lastTrigger = 0;

    public TraceConsumer(TraceCallback callback, long timeInterval, TimeUnit timeUnit) {
        this.callback = callback;
        this.timeInterval = timeInterval;
        this.timeUnit = timeUnit;
    }

    public TraceCallback getCallback() {
        return callback;
    }

    public long getTimeInterval() {
        return timeInterval;
    }

    public TimeUnit getTimeUnit() {
        return timeUnit;
    }

    public void trigger(Trace trace) {
        triggers++;
        lastTrigger = System.currentTimeMillis();
        callback.consumeTrace(trace);
    }
}
