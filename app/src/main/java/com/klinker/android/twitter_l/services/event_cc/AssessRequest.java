package com.klinker.android.twitter_l.services.event_cc;

import android.os.AsyncTask;
import android.util.Log;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeoutException;

public class AssessRequest extends AsyncTask<Void, Void, JSONObject> {
    private final String TAG = "AssessRequest";
    private Contract contract;
    private JsonObjectReceiver receiver;
    private String eventId, image, description;
    private int rating;

    public AssessRequest(Contract contract, JsonObjectReceiver receiver, String eventId, int rating, String image, String description) {
        this.contract = contract;
        this.receiver = receiver;
        this.eventId = eventId;
        this.image = image;
        this.description = description;
        this.rating = rating;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        byte[] response = new byte[0];
        try {
            response = contract.submitTransaction("assessEvent", "{" +
                    "   \"event\":\"" + eventId + "\"," +
                    "   \"rating\":" + rating + ".0," +
                    "   \"image\":\"" + image + "\"," +
                    "   \"description\":\"" + description + "\"" +
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
    protected void onPostExecute(JSONObject assessment) {
        if (null != assessment) {
            Log.i(TAG, "assessed: " + assessment);
            receiver.display(assessment);
        }
    }
}
