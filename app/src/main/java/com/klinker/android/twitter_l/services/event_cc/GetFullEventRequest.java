package com.klinker.android.twitter_l.services.event_cc;

import android.os.AsyncTask;
import android.util.Log;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class GetFullEventRequest extends AsyncTask<Void, Void, JSONObject> {
    private final String TAG = "GetFullEvent";
    private Contract contract;
    private JsonObjectReceiver receiver;
    private String eventId;

    public GetFullEventRequest(Contract contract, JsonObjectReceiver receiver, String eventId) {
        this.contract = contract;
        this.receiver = receiver;
        this.eventId = eventId;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        byte[] response = new byte[0];
        try {
            response = contract.submitTransaction("getFullEvent", eventId);
        } catch (ContractException | TimeoutException | InterruptedException e) {
            Log.e(TAG, "Error getting full event: " + eventId);
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
    protected void onPostExecute(JSONObject event) {
        if (null != event) {
            Log.i(TAG, "full event: " + event);
            receiver.display(event);
        }
    }
}
