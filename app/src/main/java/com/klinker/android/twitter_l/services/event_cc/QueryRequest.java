package com.klinker.android.twitter_l.services.event_cc;

import android.os.AsyncTask;
import android.util.Log;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class QueryRequest extends AsyncTask<Void, Void, JSONArray> {
    private String TAG = "Query Request";
    private Contract contract;
    private JsonObjectReceiver receiver;
    private JSONObject query;

    /**
     * sends a couch db query to the block chain.
     * See https://docs.couchdb.org/en/latest/api/database/find.html to learn how to formulate queries
     * @param contract contract
     * @param receiver callback
     * @param query JSONObject with selector query
     */
    public QueryRequest(Contract contract, JsonObjectReceiver receiver, JSONObject query) {
        this.contract = contract;
        this.receiver = receiver;
        this.query = query;
    }

    @Override
    protected JSONArray doInBackground(Void... voids) {
        byte[] response = new byte[0];
        try {
            response = contract.submitTransaction("queryEvents", query.toString());
        } catch (ContractException | TimeoutException | InterruptedException e) {
            Log.e(TAG, "Error executing query request");
            e.printStackTrace();
        }
        try {
            return new JSONArray(new String(response, StandardCharsets.UTF_8));
        } catch (JSONException e) {
            Log.e(TAG, "Error while parsing JSON: \"" + new String(response, StandardCharsets.UTF_8) + "\"");
            e.printStackTrace();
        }
        return  null;
    }

    @Override
    protected void onPostExecute(JSONArray eventsArr) {
        if (null != eventsArr && eventsArr.length() > 0) {
            // convert JSONArray to JSONObject
            Log.i(TAG, "Read Events: " + eventsArr);
            JSONObject events = new JSONObject();
            try {
                events.put("events", eventsArr);
                receiver.display(events);
            } catch (JSONException e) {
                Log.e(TAG, "Error putting JSONArray into JSONObject");
                e.printStackTrace();
            }
        }
    }
}
