package de.tubs.cs.ibr.location;

import android.content.Context;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.tubs.cs.ibr.location.callbacks.SampleCallback;
import de.tubs.cs.ibr.location.callbacks.TraceCallback;
import de.tubs.cs.ibr.location.callbacks.TraceConsumer;
import de.tubs.cs.ibr.location.data.collectors.AccessPointCollector;
import de.tubs.cs.ibr.location.data.collectors.LocationCollector;

/**
 * Allowing access to recent Samples without recording them by using the Sampler directly.
 *
 * If instead you want to handle each collected Sample yourself, you need to register an object as
 * a sample callback (<code>registerSampleCallback</code>). Please take note of the behaviour of the
 * Sampler object, collecting multiple data points of different types and origins and emitting a
 * Sample once a new data sample for <b>all</b> sensor types have been received.
 *
 * If you need a Sample at a specific time, the latest data points for each sensor can be used to
 * create such Sample. TODO There is no public interface for that yet
 *
 * @see de.tubs.cs.ibr.location.data.Sample
 * @see de.tubs.cs.ibr.location.data.Trace
 * @see Sampler
 * @see #registerSampleCallback(SampleCallback)
 * @see #registerTraceCallback(TraceCallback, long, TimeUnit)
 */
public class TraceCollector {

    private final Context context;
    private final List<SampleCallback> sampleCallbacks;
    private final List<TraceConsumer> traceCallbacks; // TODO trigger
    private final LocationCollector locationCollector;
    private final AccessPointCollector accessPointCollector;
    private final Sampler sampler;

    /**
     * Main interface for interacting with this library. Able to send Traces or individual Samples
     * to a registered callback object.
     *
     * @see SampleCallback interface required to be implemented to receive Sample objects.
     * @see TraceCallback interface required to be implemented to receive Trace objects.
     * @see #registerSampleCallback(SampleCallback)
     * @see #registerTraceCallback(TraceCallback, long, TimeUnit)
     * @param context
     */
    public TraceCollector(Context context) {
        this.context = context;
        sampleCallbacks = new LinkedList<>();
        traceCallbacks = new LinkedList<>();
        locationCollector = new LocationCollector(context);
        accessPointCollector = new AccessPointCollector(context);
        sampler = new Sampler(sampleCallbacks, traceCallbacks);

        startCollection();
    }

    /**
     * This starts all internal Collector objects, and registers them to the Sampler and
     * Tracer objects TODO tracer objects
     */
    private void startCollection() {
        locationCollector.registerSampler(sampler);
        accessPointCollector.registerSampler(sampler);
    }

    /**
     * This method still needs to be implemented.
     *
     * This will need to be called onPause of the Apps activity, as AP collection is
     * only possible when the App is open it seems.
     *
     * TODO implement pause
     */
    public void pauseCollection() {

    }

    /**
     * Registers an object to receive Samples on creation.
     * @param callback to receive new Samples on creation.
     */
    public void registerSampleCallback(SampleCallback callback) {
        sampleCallbacks.add(callback);
    }

    /**
     * Registers an object to receive Traces on update.
     * @param callback to receive Traces once <i>longer</i> or newer Traces are available
     * @param timeInterval multiplier of timeUnit parameter, preferred update time.
     * @param timeUnit unit of time for timeInterval parameter.
     */
    public void registerTraceCallback(TraceCallback callback, long timeInterval, TimeUnit timeUnit) {
        traceCallbacks.add(new TraceConsumer(callback, timeInterval, timeUnit));
    }
}
