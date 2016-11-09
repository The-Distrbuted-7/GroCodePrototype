package com.example.johanringstrom.mqttc;
import android.app.Activity;
import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.widget.EditText;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.*;
import com.example.johanringstrom.mqttc.MainActivity;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by johanringstrom on 05/11/16.
 */
public class Connection extends AppCompatActivity implements MqttCallback {
    private MqttAndroidClient client;
    private MqttMessage Mqttmessage;
    private String TAG;
    //private int qos = 1;
    public String[] ArrMsg;
    private String clientId = "johanringstromgmailcom";
    private int qos = 1;

    public Connection(final Context context,  final Activity activity){

        if(client == null) {
            //Set clientId and create new create a MqttAndroid client
            //String clientId = MqttClient.generateClientId();
            client =
                    new MqttAndroidClient(context, "tcp://test.mosquitto.org:1883",
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

    }
    //Publish add or delete message to broker in json format send in binary.
    public void publish(String addOrDeleteOrCreate, EditText listName, EditText item) {

        String listNameMsg = listName.getText().toString();
        String message = item.getText().toString();
        String topic = "RootGro/"+ clientId;
        // + clientId + "/" + listName + "\"";
        JSONObject obj = new JSONObject();
        JSONObject obj2 = new JSONObject();
        try {
            obj.put("clientId", clientId);
            obj.put("request", addOrDeleteOrCreate);
            obj.put("data", obj2.put(listNameMsg, message));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JSONObject payload = obj;

        /*String payload = "{"+
                "\"" + "ClientId" + "\"" +":"+ "\"" + clientId +"\"" +","
                + "\"" + "Request" + "\"" +":"+ "\"" + addOrDeleteOrCreate +"\"" +","
                + "\"" + listNameMsg + "\"" +":"+ "\"" + message +"\""
                + "}";*/
        //"{"+ "\"" + addOrDelete + "\"" +":"+ "\"" + message +"\"" +"}";*/
        byte[] encodedPayload = new byte[0];
        try {
            encodedPayload = payload.toString().getBytes("UTF-8");
            MqttMessage itemMsg = new MqttMessage(encodedPayload);
            client.publish(topic, itemMsg);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }

    //Subscribe to a predefined topic
    public  void subscribeToTopic() {
        String topic = "RootClient/"+ clientId+ "/#";
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


    //Unsubscribe to predefined list.
    public void unSubscribe(){
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
        ArrMsg = StrMsg.split(",");


        MainActivity Main = new MainActivity();


        Main.getListAdapter().clear();
        Main.getListAdapter().addAll(ArrMsg);
        // MainActivity.listAdapter.clear();
        //MainActivity.listAdapter.addAll(ArrMsg);


    }
    public MqttAndroidClient getClient(){
        return client;
    }

}
