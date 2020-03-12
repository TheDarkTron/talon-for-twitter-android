package de.tubs.cs.ibr.location.data.collectors;

import android.net.wifi.ScanResult;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class AccessPointScan implements Iterable<AccessPointScan.SimpleAPInfo> {
    private final List<ScanResult> aps;
    private final List<SimpleAPInfo> sapis;

    public AccessPointScan(List<ScanResult> aps) {
        this.aps = aps;

        sapis = new ArrayList<>(aps.size());

        for (ScanResult sr : aps) {
            sapis.add(new SimpleAPInfo(sr));
        }
    }

    public SimpleAPInfo get(int index) {
        return sapis.get(index);
    }

    public List<ScanResult> getScanResults() {
        return new ArrayList<>(aps);
    }

    public int size() {
        return sapis.size();
    }

    @NonNull
    @Override
    public Iterator iterator() {
        return sapis.iterator();
    }

    public static class SimpleAPInfo {
        private final String BSSID;
        private final String SSID;
        private final int level; // dB
        private final long timestamp;
        public final CharSequence venueName; // passpoint network

        public SimpleAPInfo(ScanResult sr) {
            this.BSSID = sr.BSSID;
            this.SSID = sr.SSID;
            this.level = sr.level;
            this.timestamp = sr.timestamp;
            this.venueName = sr.venueName;
        }

        public String getBSSID() {
            return BSSID;
        }

        public String getSSID() {
            return SSID;
        }

        public int getLevel() {
            return level;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public CharSequence getVenueName() {
            return venueName;
        }
    }
}
