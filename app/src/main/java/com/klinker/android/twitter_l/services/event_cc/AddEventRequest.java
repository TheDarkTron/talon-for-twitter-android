package com.klinker.android.twitter_l.services.event_cc;

import android.os.AsyncTask;
import android.util.Log;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeoutException;

public class AddEventRequest extends AsyncTask<Void, Void, JSONObject> {
    private final String TAG = "PostEvent";
    private Contract contract;
    private JsonObjectReceiver receiver;
    private String title;
    private String description;
    private String image;
    private Date timestamp;
    private int latitude;
    private int longitude;

    public AddEventRequest(Contract contract, JsonObjectReceiver receiver, String title, String description, String image, Date timestamp, int latitude, int longitude) {
        this.contract = contract;
        this.receiver = receiver;
        this.title = title;
        this.description = description;
        this.image = image;
        this.timestamp = timestamp;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    @Override
    protected JSONObject doInBackground(Void... voids) {
        byte[] response = new byte[0];
        try {
            response = contract.submitTransaction("addEvent", "{" +
                    "  \"title\": \"" + title + "\"," +
                    "  \"description\": \"" + description + "\"," +
                    "  \"image\": \"" + image + "\"," +
                    "  \"location\":{" +
                    "    \"latitude\": " + latitude + "," +
                    "    \"longitude\": " + longitude + "" +
                    "  }," +
                    "  \"timestamp\": \"" + (timestamp.getTime() / 1000) + "\"" +
                    "}");
        } catch (ContractException | TimeoutException | InterruptedException e) {
            Log.e(TAG, "Error posting event: " + title);
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
            Log.i(TAG, "posted event: " + event);
            receiver.display(event);
        }
    }
}
