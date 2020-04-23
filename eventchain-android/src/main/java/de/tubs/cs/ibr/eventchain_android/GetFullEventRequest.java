package de.tubs.cs.ibr.eventchain_android;

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

    GetFullEventRequest(Contract contract, JsonObjectReceiver receiver, String eventId) {
        this.contract = contract;
        this.receiver = receiver;
        this.eventId = eventId;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        byte[] response = new byte[0];
        try {
            response = contract.evaluateTransaction("getFullEvent", eventId);
        } catch (ContractException e) {
            Log.e(TAG, "Error getting full event: \"" + eventId + "\"");
        }

        try {
            return new JSONObject(new String(response, StandardCharsets.UTF_8));
        } catch (JSONException e) {
            Log.e(TAG, "Error while parsing JSON: \"" + new String(response, StandardCharsets.UTF_8) + "\"");
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject event) {
        if (null != event) {
            Log.i(TAG, "Got full event: " + event);
            receiver.display(event);
        }
    }
}
