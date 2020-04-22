package de.tubs.cs.ibr.eventchain_android;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

import androidx.annotation.Nullable;

/**
 * Entry point to the module.
 * Initializes the fabric gateway. Provides functions to interact with the EventCC network.
 * <p>
 * Documentation of the returned JSON can be found here: <a href="https://gitlab.ibr.cs.tu-bs.de/ds-media-blockchain/event-bc/-/blob/master/api.md">API.md in the event-bc repository</a>
 */
public class EventCC extends Service {
    private final String TAG = "EventCC";
    private final String CHANNEL = "mychannel";
    private final String CONTRACT = "eventcc";
    private final String CONTRACT_SGX = "ecc";
    private final String MSPID = "Org1MSP";
    private final String MSPID_SGX = "SampleOrg";
    private Wallet wallet;
    private InputStream connectionConf;
    private Contract contract;

    private final IBinder binder = new LocalBinder();

    /**
     * Do not use no argument constructor. This is only required because class is a Service
     */
    public EventCC() {

    }

    public EventCC(Context context) throws IOException {
        try {
            attachBaseContext(context);
        } catch (IllegalStateException e) {
            Log.e(TAG, "Context all ready set");
        }
        loadWalletAndConnection();
        connectGateway();
    }

    /**
     * Loads "User1@org1.example.com-cert.pem" and "key.pem" from the assets and creates an
     * wallet with an identity, which is used to identify against the block chain network.
     * Loads "connection.json" from assets which is used to bootstrap the connection to the
     * bock chain network.
     *
     * @throws IOException if loading fails
     */
    private void loadWalletAndConnection() throws IOException {
        AssetManager assetManager = this.getAssets();

        // read certificate and key
        InputStream cert = assetManager.open("eventCC/User1@org1.example.com-cert.pem");
        InputStream key = assetManager.open("eventCC/key.pem");

        // create identity
        Wallet.Identity identity = Wallet.Identity.createIdentity(MSPID_SGX, new InputStreamReader(cert), new InputStreamReader(key));

        // read connection config
        connectionConf = assetManager.open("eventCC/connection.json");

        // create wallet
        wallet = Wallet.createInMemoryWallet();
        wallet.put("User1@org1.example.com", identity);
        Log.i(TAG, "Wallet and connection profile loaded");
    }

    /**
     * Builds a gateway from the wallet and the connection profile and connects
     * to the block chain network. Loads the channel and contract.
     *
     * @throws IOException if connection fails
     */
    private void connectGateway() throws IOException {
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "User1@org1.example.com");
        builder.networkConfig(connectionConf);

        Gateway gateway = builder.connect();
        Network network = gateway.getNetwork(CHANNEL);
        contract = network.getContract(CONTRACT_SGX);
        Log.i(TAG, "Gateway connected to blockchain network");
    }

    /**
     * Only for debug! Do not use in production! Instead use {@link #queryEvents(JsonObjectReceiver, JSONObject)}
     * <p>
     * The callback will receive: A JSONObject containing a JSONArray named "events" of all events in the BC
     */
    public void getAllEvents(JsonObjectReceiver receiver) {
        if (null == contract) {
            Log.e(TAG, "not connected to EventChain");
            return;
        }

        GetAllEventsRequest request = new GetAllEventsRequest(contract, receiver);
        request.execute();
    }

    /**
     * Adds an event to the block chain
     * <p>
     * The callback will receive: A JSONObject containing all fields of an event. E.g.:
     *
     * <pre>
     * {
     *    "docType": "event",
     *    "id": "event-0e14c",
     *    "title": "Name of Event",
     *    "image": "-3e584d8556ef6da2120e6f00e5bf15effa742c347c16ea2c.jpg",
     *    "location":{
     *       "latitude": 52,
     *       "longitude": 10
     *    },
     *    "timestamp": "1559300653",
     *    "description": "First Event",
     *    "creator": "testUser",
     *    "trustworthiness": 0
     * }
     * </pre>
     *
     * @param receiver    callback object needs to implement display() method
     * @param title       title of the event (twitter text)
     * @param description description of the event (twitter id)
     * @param image       image of event (not used)
     * @param timestamp   timestamp in unix format (seconds since epoch)
     * @param latitude    latitude of event
     * @param longitude   longitude of event
     */
    public void addEvent(JsonObjectReceiver receiver, String title, String description, String image, Date timestamp, int latitude, int longitude) {
        if (null == contract) {
            Log.e(TAG, "not connected to EventChain");
            return;
        }

        AddEventRequest request = new AddEventRequest(contract, receiver, title, description, image, timestamp, latitude, longitude);
        request.execute();
    }

    /**
     * Send a couch bd query to the bloch chain
     *
     * @param receiver callback object needs to implement display() method
     * @param query    couch db query
     */
    public void queryEvents(JsonObjectReceiver receiver, JSONObject query) {
        if (null == contract) {
            Log.e(TAG, "not connected to EventChain");
            return;
        }

        QueryRequest request = new QueryRequest(contract, receiver, query);
        request.execute();
    }

    /**
     * convenience method to get the corresponding event of a twitter post
     *
     * @param receiver  callback object needs to implement display() method
     * @param description id of the twitter post
     */
    public void queryEventsByDescription(JsonObjectReceiver receiver, String description) {
        if (null == contract) {
            Log.e(TAG, "not connected to EventChain");
            return;
        }

        try {
            JSONObject query = new JSONObject(
                    "{" +
                            "   \"selector\":{" +
                            "      \"docType\":\"event\"," +
                            "      \"description\":\"" + description + "\"" +
                            "   }" +
                            "}");
            queryEvents(receiver, query);
        } catch (JSONException e) {
            Log.e(TAG, "Error building query for description: \"" + description + "\"");
            e.printStackTrace();
        }
    }

    public void assessEvent(AssessReceiver receiver, String eventId, int rating, String image, String description) {
        if (null == contract) {
            Log.e(TAG, "not connected to EventChain");
            return;
        }

        AssessRequest request = new AssessRequest(contract, receiver, eventId, rating, image, description);
        request.execute();
    }

    public void trackLocation(JsonObjectReceiver receiver, int longitude, int latitude) {
        if (null == contract) {
            Log.e(TAG, "not connected to EventChain");
            return;
        }

        TrackLocationRequest request = new TrackLocationRequest(contract, receiver, longitude, latitude);
        request.execute();
    }

    public void getFullEvent(JsonObjectReceiver receiver, String eventId) {
        if (null == contract) {
            Log.e(TAG, "not connected to EventChain");
            return;
        }

        GetFullEventRequest request = new GetFullEventRequest(contract, receiver, eventId);
        request.execute();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            loadWalletAndConnection();
            connectGateway();
        } catch (IOException e) {
            Log.e(TAG, "Could not connect gateway to blockchain network");
        }
    }

    public class LocalBinder extends Binder {
        public EventCC getService() {
            return EventCC.this;
        }
    }
}
