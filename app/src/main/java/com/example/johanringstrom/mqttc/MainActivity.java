package com.example.johanringstrom.mqttc;

import android.content.Intent;
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
import org.json.JSONException;
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

import static android.provider.AlarmClock.EXTRA_MESSAGE;


public class MainActivity extends AppCompatActivity  {
    private MqttAndroidClient client;
    private EditText listName;
    private String TAG;
    private int qos = 1;
    private ListView ListView ;
    private static  ArrayAdapter<String> listAdapter ;
    private String clientId = "johanringstromgmailcom";
    Connection con;
    private String topic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Creates broker connection if not already created.
        con = new Connection(MainActivity.this, MainActivity.this);
        client = con.getClient();

        //List view to display list
        ListView = (ListView) findViewById(R.id.List);

        //List to enter in text edit text
        listName = (EditText) findViewById(R.id.ListNameId);

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
                con.publish("getList", listStr.substring(1, listStr.length()-1), "Test");
                goToList(listStr.substring(1, listStr.length()-1));
            }
        });


        //What to do when the remove button is pressed
        final Button btnCreateList = (Button) findViewById(R.id.createList);
        btnCreateList.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                con.publish("createList", listName.getText().toString(), "Test");
            }
        });

        //What to do when the lists button is pressed
        final Button btnGetLists = (Button) findViewById(R.id.GetLists);
        btnGetLists.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                con.publish("getListsOfLists", listName.getText().toString(), "Test");
            }
        });

        //What to do when the subscribe button is pressed
        final Button btnSubscribe = (Button) findViewById(R.id.subscribe);
        btnSubscribe.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                con.subscribeToTopic();
            }
        });
    }

    //Go to listview and pass the list name to listview activity
    public void goToList(String str){
        Intent intent = new Intent(this, Listview.class);
        intent.putExtra("ListName", str);
        startActivity(intent);
    }

    //Gets listadapter
    public ArrayAdapter<String> getListAdapter(){
        return this.listAdapter;
    }


}



