package com.klinker.android.twitter_l.services.event_cc;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Wallet;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Date;

/**
 * Initializes the fabric gateway. Provides functions to interact with the EventCC network.
 */
public class EventCC {
    private final String TAG = "EventCC";
    private final String CHANNEL = "mychannel";
    private final String CONTRACT = "eventcc";
    private Wallet wallet;
    private InputStream connectionConf;
    private Contract contract;

    /**
     * Loads certificate, key and network config. Creates the wallet, gateway and connects to the network
     * channel CHANNEL and to the contract CONTRACT.
     *
     * @param context Context is necessary to load the certificate, key and network config from assets
     * @throws IOException If initialisation of the fabric gateway fails, an exception is thrown
     */
    public EventCC(Context context) throws IOException {
        loadWalletAndConnection(context);
        connectGateway();
    }

    private void loadWalletAndConnection(Context context) throws IOException {
        AssetManager assetManager = context.getAssets();

        // read certificate and key
        InputStream cert = assetManager.open("User1@org1.example.com-cert.pem");
        InputStream key = assetManager.open("key.pem");

        // create identity
        Wallet.Identity identity = Wallet.Identity.createIdentity("Org1MSP", new InputStreamReader(cert), new InputStreamReader(key));

        // read connection config
        connectionConf = assetManager.open("connection.json");

        // create wallet
        wallet = Wallet.createInMemoryWallet();
        wallet.put("User1@org1.example.com", identity);
        Log.i(TAG, "Wallet and connection profile loaded");
    }

    private void connectGateway() throws IOException {
        Gateway.Builder builder = Gateway.createBuilder();
        builder.identity(wallet, "User1@org1.example.com");
        builder.networkConfig(connectionConf);

        Gateway gateway = builder.connect();
        Network network = gateway.getNetwork(CHANNEL);
        contract = network.getContract(CONTRACT);
        Log.i(TAG, "Gateway connected to network");
    }

    /**
     * Only for debug! Do not use in production!
     *
     * @return A list of all Events in the BC
     */
    public void getAllEvents(JsonObjectReceiver receiver) {
        GetAllEventsRequest request = new GetAllEventsRequest(contract, receiver);
        request.execute();
    }

    public void addEvent(JsonObjectReceiver receiver, String title, String description, String image, Date timestamp, double latitude, double longitude) {
        AddEventRequest request = new AddEventRequest(contract, receiver, title, description, image, timestamp, latitude, longitude);
        request.execute();
    }

    public void queryEvents(JsonObjectReceiver receiver, String query) {
        QueryRequest request = new QueryRequest(contract, receiver, query);
        request.execute();
    }

    public void queryEventsByTwitterId(JsonObjectReceiver receiver, String twitterId) {
        //TODO: build query string
        String query = "";
        QueryRequest request = new QueryRequest(contract, receiver, query);
        request.execute();
    }

    public void assessEvent(JsonObjectReceiver receiver, String eventId, int rating, String image, String description) {
        AssessRequest request = new AssessRequest(contract, receiver, eventId, rating, image, description);
    }

    private void trackLocation(JsonObjectReceiver receiver, int longitude, int latitude) {
        TrackLocationRequest request = new TrackLocationRequest(contract, receiver, longitude, latitude);
        request.execute();
    }

    private void getFullEvent(JsonObjectReceiver receiver, String eventId) {
        GetFullEventRequest request = new GetFullEventRequest(contract, receiver, eventId);
        request.execute();
    }
}
