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

public class QueryRequest extends AsyncTask<Void, Void, JSONObject> {
    private String TAG = "Query Request";
    private Contract contract;
    private JsonObjectReceiver receiver;
    private JSONObject query;

    /**
     * sends a couch db query to the block chain.
     * See https://docs.couchdb.org/en/latest/api/database/find.html to learn how to formulate queries
     *
     * @param contract contract
     * @param receiver callback
     * @param query    JSONObject with selector query
     */
    QueryRequest(Contract contract, JsonObjectReceiver receiver, JSONObject query) {
        this.contract = contract;
        this.receiver = receiver;
        this.query = query;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        byte[] response = new byte[0];
        try {
            response = contract.evaluateTransaction("queryEvents", query.toString());
        } catch (ContractException e) {
            Log.e(TAG, "Error executing query request \"" + query.toString() + "\"");
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
            Log.i(TAG, "Got Events: \"" + events + "\"");
            receiver.display(events);
        }
    }
}
