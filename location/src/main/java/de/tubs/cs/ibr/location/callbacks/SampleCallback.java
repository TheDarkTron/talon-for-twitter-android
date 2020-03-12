package de.tubs.cs.ibr.location.callbacks;

import de.tubs.cs.ibr.location.data.Sample;

public interface SampleCallback {

    void consumeSample(Sample sample);
}
