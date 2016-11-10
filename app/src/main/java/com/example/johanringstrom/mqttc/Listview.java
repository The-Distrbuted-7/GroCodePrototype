package com.example.johanringstrom.mqttc;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.*;
import org.eclipse.paho.android.service.MqttAndroidClient;

import java.util.ArrayList;

/**
 * Created by johanringstrom on 27/10/16.
 */
public class Listview extends AppCompatActivity {
    private ListView Listview ;
    private EditText item;
    private static ArrayAdapter<String> listAdapter ;
    private MqttAndroidClient client;
    Connection con;
    private String ListName;
    private String TAG;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        item = (EditText) findViewById(R.id.Item);

        //Make a connection if not aready connected
       con = new Connection(Listview.this, Listview.this);
        client = con.getClient();

        //Creates a listview
        Listview = (ListView) findViewById(R.id.listVItem);
        //Create a adapter to listview
        ArrayList<String> GroList = new ArrayList<>();
        listAdapter = new ArrayAdapter<>(this, R.layout.simplerow, GroList);
        Listview.setAdapter( listAdapter );

        //Set what to do when a list item is clicked
        Listview.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                Object listItem = Listview.getItemAtPosition(position);
                String listStr = listItem.toString();
                item.setText(listStr.substring(1, listStr.length()-1));
            }
        });

        Intent intent = getIntent();
        // get data via the key
        ListName = intent.getExtras().getString("ListName");
        if (ListName != null) {
            Log.d(TAG, "???NULLLISTNAME???");
        }


        //What to do when the remove button is pressed
        final Button btnRemove = (Button) findViewById(R.id.Remove);
        btnRemove.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                con.publish("delete", ListName, item.getText().toString());
                con.publish("getList", ListName, item.getText().toString());
            }
        });
        //What to do when the add button is pressed
        final Button btnAdd = (Button) findViewById(R.id.Add);
        btnAdd.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                con.publish("add", ListName, item.getText().toString());
                con.publish("getList", ListName, item.getText().toString());
            }
        });
    }
    public ArrayAdapter<String> getListAdapter(){
        return this.listAdapter;
    }

    public void goToMain(View view) {
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
    }
}
