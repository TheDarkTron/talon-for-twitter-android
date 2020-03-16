package de.tubs.cs.ibr.eventchain_android;

import android.os.AsyncTask;
import android.util.Log;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class GetAllEventsRequest extends AsyncTask<Void, Void, JSONObject> {
    private final String TAG = "Get All Events Request";
    private Contract contract;
    private JsonObjectReceiver receiver;

    GetAllEventsRequest(Contract contract, JsonObjectReceiver receiver) {
        this.contract = contract;
        this.receiver = receiver;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        byte[] response = new byte[0];
        try {
            response = contract.submitTransaction("getEvents");
        } catch (ContractException | TimeoutException | InterruptedException e) {
            Log.e(TAG, "Error getting all events");
        }

        try {
            JSONObject object = new JSONObject();
            return object.put("events", new JSONArray(new String(response, StandardCharsets.UTF_8)));
        } catch (JSONException e) {
            Log.e(TAG, "Error while parsing JSON: \"" + new String(response, StandardCharsets.UTF_8) + "\"");
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject events) {
        if (null != events) {
            Log.i(TAG, "Read Events: \"" + events + "\"");
            receiver.display(events);
        }
    }
}
