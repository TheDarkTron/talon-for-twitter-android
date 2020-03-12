package de.tubs.cs.ibr.location.data.collectors;

import java.util.ArrayList;
import java.util.List;

import de.tubs.cs.ibr.location.Sampler;

/**
 * The abstract collector class is used to force compatibility of all different collector types.
 *
 * It only manages a list of samplers, which are all notified when a value is gathered. It's the job
 * of the Sampler to group incoming values into samples.
 *
 * @param <V> the type of collection result.
 * @see Sampler
 */
public abstract class Collector<V> {

    /**
     * Preferred interval for gathering data.
     *
     * Specific collectors might use this value as a guide and reference but use a different
     * internal interval due to sensor specifics or limitations.
     */
    public static final long PREFERRED_INTERVAL = 1000L * 60;
    private List<Sampler> samplers;

    Collector() {
        samplers = new ArrayList<>();
    }

    /**
     * Getter for sampler list.
     *
     * This list can be modified from outside, and new values will be sent to the updated list's
     * entries. This is however not inherently threadsafe.
     *
     * @return the registered samplers
     */
    protected List<Sampler> getSamplers() {
        return samplers;
    }

    /**
     * Adds a sampler to the internal list of samplers.
     *
     * The added sampler will be notified of the incoming data.
     * @param sampler Sampler object to be added to the distribution list.
     * @see Sampler#notify(Object)
     */
    public void registerSampler(Sampler sampler) {
        samplers.add(sampler);
    }

    /**
     * All registered Sampler objects will be notified with the value as argument.
     *
     * The Sampler is expected to check the type of the value with <code>instanceof</code> and
     * handle all possible type of collectible data.
     * @param value value to notify the Sampler objects with
     * @see Sampler#notify(Object)
     */
    protected void notifySamplers(V value) {
        for (Sampler sampler : samplers) {
            sampler.notify(value);
        }
    }
}
