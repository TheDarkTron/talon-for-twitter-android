package com.klinker.android.twitter_l.services.event_cc;

import android.os.AsyncTask;
import android.util.Log;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class QueryRequest extends AsyncTask<Void, Void, JSONObject> {
    private String TAG = "Query Request";
    private Contract contract;
    private JsonObjectReceiver receiver;
    private String query;

    public QueryRequest(Contract contract, JsonObjectReceiver receiver, String query) {
        this.contract = contract;
        this.receiver = receiver;
        this.query = query;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        byte[] response = new byte[0];
        try {
            response = contract.submitTransaction("queryEvents", query);
        } catch (ContractException | TimeoutException | InterruptedException e) {
            Log.e(TAG, "Error executing query request");
            e.printStackTrace();
        }
        try {
            return new JSONObject(new String(response, StandardCharsets.UTF_8));
        } catch (JSONException e) {
            Log.e(TAG, "Error while parsing JSON: \"" + new String(response, StandardCharsets.UTF_8) + "\"");
            e.printStackTrace();
        }
        return  null;
    }

    @Override
    protected void onPostExecute(JSONObject events) {
        if (null != events) {
            Log.i(TAG, "Read Events: " + events);
            receiver.display(events);
        }
    }
}
