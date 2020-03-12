package de.tubs.cs.ibr.location.data;

/*
Background location gathering is throttled and location is computed, and delivered only a few times an hour. [1]

[1]: https://developer.android.com/guide/topics/location/battery
 */


import android.location.Location;
import android.util.Log;

import com.google.android.gms.location.FusedLocationProviderClient;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import de.tubs.cs.ibr.location.data.collectors.AccessPointScan;


public class Sample {

    private List<Field> fields;

    public Sample() {
        fields = new LinkedList<>();
    }

    public Sample addField(Field field) {
        this.fields.add(field);
        return this;
    }

    public List<Field> getFields() {
        return fields;
    }

    public <T> Memory<T> getMemory(Class<T> resultType) {
        Memory<T> mem = new Memory<>();
        for (Field f: fields) {
            if (resultType.isInstance(f.getData())) {
                mem.add((T) f.getData());
            }
        }
        return mem;
    }
}
