package com.example.johanringstrom.mqttc;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.*;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import java.util.UUID;

import static com.example.johanringstrom.mqttc.R.id.listView;

public class MainActivity extends AppCompatActivity implements MqttCallback {
    private MqttAndroidClient client;
    private EditText item;
    String messagesArrived;
    private String TAG;
    private int qos = 1;
    private ListView ListView ;
    private ArrayAdapter<String> listAdapter ;
    private String clientId = "johan.ringstrom@gmail.com";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //List view to display list
        ListView = (ListView) findViewById(R.id.List);

        //Item to enter in text edit text
        item = (EditText) findViewById(R.id.item);

        //Create a adapter to listview
        ArrayList<String> GroList = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, R.layout.simplerow, GroList);
        ListView.setAdapter( listAdapter );

        //Set what to do when a list item is clicked
        ListView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Object listItem = ListView.getItemAtPosition(position);
                String listStr = listItem.toString();
                item.setText(listStr.substring(1, listStr.length()-1));
            }
        });

        //What to do when the add button is pressed
        final Button btnAdd = (Button) findViewById(R.id.add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                publish("add");
            }
        });

        //What to do when the remove button is pressed
        final Button btnRemove = (Button) findViewById(R.id.remove);
        btnRemove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                publish("remove");
            }
        });

        //Set clientId and create new create a MqttAndroid client
        String clientId = MqttClient.generateClientId();
        client =
                new MqttAndroidClient(this.getApplicationContext(), "tcp://test.mosquitto.org:1883",
                        clientId);
        //Tryes to connect this client to a the  broker.
        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                public String TAG;

                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Log.d(TAG, "onSuccess");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

        client.setCallback(this);

    }


    //Publish add or delete message to broker in json format send in binary.
    public void publish(String addOrDelete) {

        String topic = "RootGro/JohanPhone";
        item.findViewById(R.id.item);
        String message = item.getText().toString();
        String payload = "{"+ "\"" + addOrDelete + "\"" +":"+ "\"" + message +"\"" +"}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage itemMsg = new MqttMessage(encodedPayload);
            client.publish(topic, itemMsg);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    //Subscribe to a predefined topic
    public  void subscribeToTopic(View view) {
        String topic = "JohanPhone/List";
        try {
            IMqttToken subToken = client.subscribe(topic, qos);
            subToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The message was published

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //Gets a list by sending getList message in jason format in binary.
    public void getList(View view){
        item.findViewById(R.id.item);
        String message = item.getText().toString();
        String topic = "RootGro/JohanPhone/List";
        String payload = "{"+ "\""+ "getList"+ "\"" +":"+ "\""+ message+ "\"" +"}";
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage itemMsg = new MqttMessage(encodedPayload);
            client.publish(topic, itemMsg);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    //Unsubscribe to predefined list.
    public void unSubscribe(View view){
        String topic = "JohanPhone/List";
        try {
            IMqttToken unsubToken = client.unsubscribe(topic);
            unsubToken.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // The subscription could successfully be removed from the client
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // some error occurred, this is very unlikely as even if the client
                    // did not had a subscription to the topic the unsubscribe action
                    // will be successfully
                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void connectionLost(Throwable cause) {
        // Called when the connection to the server has been lost.
        // An application may choose to implement reconnection
        // logic at this point. This sample simply exits.
        Log.d(TAG, "Connection to " + "broker.hivemq.com" + " lost!");
        System.exit(1);
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        // Called when a message has been delivered to the hhahaha test
        // server. The token passed in here is the same one
        // that was passed to or returned from the original call to publish.
        // This allows applications to perform asynchronous
        // delivery without blocking until delivery completes.
        //
        // This sample demonstrates asynchronous deliver and
        // uses the token.waitForCompletion() call in the main thread which
        // blocks until the delivery has completed.
        // Additionally the deliveryComplete method will be called if
        // the callback is set on the client
        //
        // If the connection to the server breaks before delivery has completed
        // delivery of a message will complete after the client has re-connected.
        // The getPendingTokens method will provide tokens for any messages
        // that are still to be delivered.
    }

    //Get messages on the subscribed message. Clears listview and add the received message split up to a array.
    public void messageArrived(String topic, MqttMessage message) throws Exception {
        String StrMsg = message.toString();
        StrMsg = StrMsg.substring(1,StrMsg.length()-1);
        String[] ArrMsg = StrMsg.split(",");

        listAdapter.clear();
        listAdapter.addAll(ArrMsg);

    }

}

