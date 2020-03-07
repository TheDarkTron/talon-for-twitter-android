package com.klinker.android.twitter_l.services.event_cc;

import android.os.AsyncTask;
import android.util.Log;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class TrackLocationRequest extends AsyncTask<Void, Void, JSONObject> {
    private final String TAG = "TrackLocation";
    private Contract contract;
    private JsonObjectReceiver receiver;
    private int longitude;
    private int latitude;

    public TrackLocationRequest(Contract contract, JsonObjectReceiver receiver, int longitude, int latitude) {
        this.contract = contract;
        this.receiver = receiver;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        byte[] response = new byte[0];
        try {
            response = contract.submitTransaction("trackLocation", "{" +
                    "   \"latitude\":" + latitude + "," +
                    "   \"longitude\":" + longitude + "," +
                    "}");
        } catch (ContractException | TimeoutException | InterruptedException e) {
            Log.e(TAG, "Error assessing event");
            e.printStackTrace();
        }
        try {
            return new JSONObject(new String(response, StandardCharsets.UTF_8));
        } catch (JSONException e) {
            Log.e(TAG, "Error while parsing JSON: \"" + new String(response, StandardCharsets.UTF_8) + "\"");
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject location) {
        if (null != location) {
            Log.i(TAG, "tacked location: " + location);
            receiver.display(location);
        }
    }
}
