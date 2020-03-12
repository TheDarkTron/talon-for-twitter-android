package de.tubs.cs.ibr.location.callbacks;

import de.tubs.cs.ibr.location.data.Trace;

public interface TraceCallback {
    void consumeTrace(Trace trace);
}
