package de.tubs.cs.ibr.location;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.util.Log;

import java.util.List;

import de.tubs.cs.ibr.location.callbacks.SampleCallback;
import de.tubs.cs.ibr.location.callbacks.TraceConsumer;
import de.tubs.cs.ibr.location.data.Field;
import de.tubs.cs.ibr.location.data.Memory;
import de.tubs.cs.ibr.location.data.Sample;
import de.tubs.cs.ibr.location.data.collectors.AccessPointScan;
import de.tubs.cs.ibr.location.data.collectors.Collector;

/**
 * Samplers are object which aggregate data from multiple Collectors, by being registered with them,
 * and once enough new data has been collected new Samples are omitted to callbacks registered
 * with the Sampler object.
 *
 * @see TraceCollector for receiving Traces instead of Samples
 * @see Sample
 * @see de.tubs.cs.ibr.location.data.Trace
 */
public class Sampler {

    private static final String TAG = "SAMPLER";

    private final List<SampleCallback> sampleCallbacks;
    private final List<TraceConsumer> traceConsumer;
    private final Memory<Location> locationMemory;
    private final Memory<AccessPointScan> wifiMemory;
    private Sample sample;
    private long lastSample;

    /**
     * Creates a new Sampler which shares access to the lists of callbacks. These lists can be
     * altered after creating the Sampler and will only be read.
     * @param sampleCallbacks list of callbacks to notify once a Sample is ready.
     * @param traceConsumer list of callbacks to notify once a Trace is ready.
     */
    public Sampler(List<SampleCallback> sampleCallbacks, List<TraceConsumer> traceConsumer) {
        this.sampleCallbacks = sampleCallbacks;
        this.traceConsumer = traceConsumer;

        locationMemory = new Memory<>();
        wifiMemory = new Memory<>();
        sample = new Sample();
    }

    /**
     * Method to be used by Collectors to provide the Sampler with new data points.
     *
     * Incoming data is sorted into memory by reflection through <code>instanceof</code>.
     * @param value new data type.
     * @param <V> <i>native</i> type of data point. (Should never be Field)
     */
    public <V> void notify(V value) {
        if (value instanceof Location) {
            locationMemory.add((Location) value);
            sample.addField(new Field<>(locationMemory.getNewest()));
        } else if (value instanceof AccessPointScan) {
            wifiMemory.add((AccessPointScan) value);
            sample.addField(new Field<>(wifiMemory.getNewest()));
        } else {
            Log.e("SAMPLER", "Unhandled value of type " + value.getClass().getCanonicalName());
        }

        if (checkCompleteSample()) {
            completeSample();
        }
    }

    /**
     * This current returns true iff all sensor types have received an updated data point.
     *
     * @return true iff the current Sample is complete.
     */
    private boolean checkCompleteSample() {
        boolean allNew = true;
        boolean anyNew = false;
        /* determine actual value of allNew and anyNew */
        for (Memory mem : new Memory[]{locationMemory, wifiMemory}) {
            if (!mem.hasData()) {
                return false; // some sensor has not sent *any* data.
            }
            if (!mem.hasDataSince(lastSample)) {
                allNew = false; // some sensor hasn't updated since the last sample
            } else {
                anyNew = true; // some sensor has updated since the last sample
            }
        }
        /* allNew and anyNew now have the correct value */
        if (anyNew && System.currentTimeMillis() - lastSample >= Collector.PREFERRED_INTERVAL) {
            return true; // as long as something is new and we want a sample -> create sample
        }
        return allNew; // otherwise only sample if every sensor has sent something new
    }

    private void completeSample() {
        for (SampleCallback callback : sampleCallbacks) {
            callback.consumeSample(sample);
        }

        locationMemory.clear();
        wifiMemory.clear();
        sample = new Sample();
        lastSample = System.currentTimeMillis();
    }

    /**
     * Builds a new Sample by using the newest data point from all types of data collected.
     * TODO check if memory contains any data
     *
     * @return <i>newest</i> Sample which could be created right now.
     */
    protected Sample adHocSample() {
        Sample adhoc = new Sample();

        adhoc.addField(new Field<>(locationMemory.getNewest()));
        adhoc.addField(new Field<>(wifiMemory.getNewest()));

        return adhoc;
    }
}
