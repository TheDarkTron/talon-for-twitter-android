package de.tubs.cs.ibr.location.data.collectors;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;

import java.util.List;

public class AccessPointCollector extends Collector<AccessPointScan> {

    final WifiManager wm;

    public AccessPointCollector(Context context) {
        wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);

        BroadcastReceiver br = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if(intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)) {
                    collect();
                    // TODO repeat
                }
            }
        };

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        context.registerReceiver(br, intentFilter);

        boolean success = wm.startScan();
        if (!success) {
            // TODO handle setup failure
        }
    }

    private void collect() {
        AccessPointScan aps = new AccessPointScan(wm.getScanResults());
        notifySamplers(aps);
    }


}
