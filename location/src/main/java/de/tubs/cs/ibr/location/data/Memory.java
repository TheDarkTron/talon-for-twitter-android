package de.tubs.cs.ibr.location.data;

import java.util.LinkedList;
import java.util.List;

/**
 * This generic class is designed to keep track of time sensitive data which is continuously
 * generated.
 *
 * It allows easy access to the newest data as well as a specified amount of historic data.
 * @param <D> Type of data which needs to be stored.
 */
public class Memory<D> {
    private D newest;
    private long newestTime;
    private List<D> aggregate;

    private final long limit;

    /**
     * Creates a memory object which is allowed to grow indefinitely. Never use this if you are
     * intending to keep this object alive for long, with many samples to save.
     */
    @Deprecated
    public Memory() {
        this(0);
    }

    /**
     * Creates a memory object which stores a limited amount of historical data.
     * @param limit Amount of historic data to be kept.
     */
    public Memory(long limit) {
        aggregate = new LinkedList<>();
        this.limit = 0;
    }

    /**
     * Appends new data to history and replaces the newest data point with the provided data.
     *
     * If a limit is set and the list is at its maximum length the oldest data will also be
     * deleted.
     * @param item new data point.
     */
    public void add(D item) {
        newest = item;
        newestTime = System.currentTimeMillis();
        aggregate.add(item);
        if (limit != 0 && aggregate.size() > limit) {
            aggregate.remove(0);
        }
    }

    /**
     * Remove all historic data.
     *
     * This still keeps the newest data point in memory, it will however not be included in the
     * aggregate accessible through <code>getAggregate</code>
     *
     * @see #getAggregate()
     * @see #getNewest()
     */
    public void clear() {
        aggregate.clear();
    }

    /**
     * Returns the newest data point recorded. This will always return a value as long as a single
     * value has ever been recorded in this memory object.
     * @return newest data point.
     */
    public D getNewest() {
        return newest;
    }

    /**
     * Returns true if the memory has ever received any data. This status persists even if the
     * aggregate has been reset.
     *
     * @see #clear()
     * @return true iff Memory holds data.
     */
    public boolean hasData() {
        return newest != null;
    }

    /**
     * Returns true if the memory has received any data since <code>time</code>.
     * @param time Timestamp (from <code>System.currentTimeMillis()</code>)
     * @return true iff Memory holds data newer than <code>time</code>.
     */
    public boolean hasDataSince(long time) {
        return hasData() && (newestTime > time);
    }

    /**
     * Get the historic data.
     *
     * If the memory has not been cleared since the newest data point has been recorded, then the
     * returned list will include the newest data point. If the memory was cleared and since then
     * no new data points were recorded, then the newest data point will not be included in the
     * history list, but will still be accessible through <code>getNewest</code>.
     *
     * @see #getNewest()
     * @see #clear()
     * @return historic data list
     */
    public List<D> getAggregate() {
        return aggregate;
    }
}
