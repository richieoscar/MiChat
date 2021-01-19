package com.example.michat;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private EditText uniqueId;
    private Button go;
    private static final String TOPIC = "Subscribe";
    private MqttAndroidClient client;
    private String clientId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //checks for client or device network connection
        if(checkNetworkConnection()){
            //connects to server if network available
            createConnection();
        }
        else {
            //Displays error message
            Toast.makeText(this, R.string.error_msg, Toast.LENGTH_SHORT).show();
        }

        //instatiate views
        bindViews();
        subscribe();


    }

    private void  bindViews(){
        uniqueId = findViewById(R.id.editText_topic);
        go = findViewById(R.id.button_go);
    }



    public void createConnection(){
        clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883", clientId);

        try {
            IMqttToken token = client.connect();
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(MainActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: connected");
                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(MainActivity.this, "Unable to Connect", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }

    }

    private Boolean checkNetworkConnection(){
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
       return  cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void subscribe(){

        go.setOnClickListener((v)-> {

        String topic = uniqueId.getText().toString().trim();

        if(topic.isEmpty()){
            uniqueId.setError("Field is required");
        }
        else {
            Intent intent = new Intent(this, ChatActivity.class);
            intent.putExtra("sub", topic);
            startActivity(intent);
        }
            //String topic = "foo/bar";
            int qos = 1;
            try {
                IMqttToken subToken = client.subscribe(topic, qos);
                subToken.setActionCallback(new IMqttActionListener() {
                    @Override
                    public void onSuccess(IMqttToken asyncActionToken) {
                        // The message was published
                        Log.d(TAG, "onSuccess: subscribed");
                    }

                    @Override
                    public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                        Log.d(TAG, "onFailure: could not subscribe to broker");
                        // The subscription could not be performed, maybe the user was not
                        // authorized to subscribe on the specified topic e.g. using wildcards

                    }
                });
            } catch (MqttException e) {
                e.printStackTrace();
            }

        });





    }
}