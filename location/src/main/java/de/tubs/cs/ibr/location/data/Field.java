package de.tubs.cs.ibr.location.data;

/**
 * Class used to wrap arbitrary data points. Each data point is paired with the time at which
 * the field object was created. Most Android sensor data has its own creation time attribute.
 * This can be used to compare actual data creation and consumption by the library.
 * @param <D> type of data point wrapped.
 */
public class Field<D> {

    private final long created;
    private final D data;

    /**
     * Wraps data point in Field type.
     * @param data to be wrapped.
     */
    public Field(D data) {
        this.created = System.currentTimeMillis();
        this.data = data;
    }

    /**
     * Gets the time at which the field object was created. This might be different to the time
     * recorded by the wrapped object itself.
     * @return
     */
    public long getCreated() {
        return created;
    }

    /**
     * Getter for the internal data point.
     * @return internal data point.
     */
    public D getData() {
        return data;
    }

    /**
     * Identical to <code>new Field(data)</code> in case you prefer procedural style.
     * @param data to be wrapped.
     * @param <D> type of wrapped data point.
     * @return new Field wrapping data point.
     */
    public static <D> Field from(D data) {
        return new Field<>(data);
    }
}
