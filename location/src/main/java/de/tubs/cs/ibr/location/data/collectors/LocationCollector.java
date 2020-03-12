package de.tubs.cs.ibr.location.data.collectors;

import android.content.Context;
import android.location.Location;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

/**
 * Instances of this class collect Location data whenever possible, though aiming to do it at a
 * preferred interval. Collected Location sensor results are sent to a Sampler by notifying them
 * about the new record.
 *
 * @see de.tubs.cs.ibr.location.Sampler
 */
public class LocationCollector extends Collector<Location> {

    /**
     * Preferred interval, identical to the abstract collector's preferred interval.
     */
    public static final long PREFERRED_INTERVAL = Collector.PREFERRED_INTERVAL;

    /**
     * Actual collector, LocationCollector objects own exactly one of these.
     */
    private final LocationCollectorCallback callback;

    /**
     * Initializes LocationCollector which immediately starts to collect Location data.
     *
     * You should register a Sampler as soon as possible after creation, otherwise Location data
     * points will be recorded but lost.
     *
     * @param context Context of activity requiring Location data
     * @see de.tubs.cs.ibr.location.Sampler
     */
    public LocationCollector(Context context) {
        super();

        callback = new LocationCollectorCallback(context, this);
    }

    private static class LocationCollectorCallback extends LocationCallback {

        private final FusedLocationProviderClient fuseClient;
        private final Context context;
        private final Collector<Location> collector;

        public LocationCollectorCallback(Context context, Collector<Location> collector) {
            super();

            fuseClient = LocationServices.getFusedLocationProviderClient(context);
            this.context = context;
            this.collector = collector;

            LocationRequest locationRequest = new LocationRequest();
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
            locationRequest.setInterval(PREFERRED_INTERVAL);
            locationRequest.setFastestInterval(PREFERRED_INTERVAL / 2L);

            // TODO use Worker to collect in background
            fuseClient.requestLocationUpdates(locationRequest, this, null);
        }

        @Override
        public void onLocationResult(LocationResult locationResult) {
            if (locationResult == null) {
                return;
            }
            for (Location location : locationResult.getLocations()) {
                collector.notifySamplers(location);
            }
        }

        @Override
        public void onLocationAvailability(LocationAvailability locationAvailability) {
            // TODO decide if this should be reported too
        }
    }
}
