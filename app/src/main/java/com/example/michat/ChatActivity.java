package com.example.michat;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import com.example.michat.model.Message;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

public class ChatActivity extends AppCompatActivity {


    private ListView listView;

    private MqttAndroidClient client;
    private String clientId;
    private String topic;

    private static final String TAG = "ChatActivity";
    private static final int INTERVAl = 1000 * 60 * 60;

    private EditText message;
    private Button send;
    private MessageAdapter adapter;
    public static final String SERVER_URI = "tcp://broker.hivemq.com:1883";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        //check for device network connection
        if (checkNetworkConnection()) {
            createConnection();
        } else {
            Toast.makeText(this, "No Internet", Toast.LENGTH_SHORT).show();
        }

        //instantiate views
        bindViews();
        setUpMessageListAdapter();


        //getTopic(Unique ID)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("sub")) {
            topic = intent.getStringExtra("sub");
            ActionBar ab = getSupportActionBar();
            ab.setTitle("Topic(" + topic + ")");
            ab.setDisplayHomeAsUpEnabled(true);
        }

        //publish message
        sendMessage();


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int iD = item.getItemId();

        switch (iD) {
            case R.id.disconnect: {

                try {
                    client.disconnect();
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                } catch (MqttException e) {
                    e.printStackTrace();
                }
                return true;
            }
            case R.id.clear_chat: {
                adapter.clear();
                return true;
            }
            default:
                return false;
        }

    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if (client.isConnected()) {
            subscribeToBroker(topic);


        } else {
            createConnection();
        }
    }

    private void bindViews() {
        message = findViewById(R.id.editText_message);
        send = findViewById(R.id.send_button);
        listView = findViewById(R.id.listView);


    }

    private void setUpMessageListAdapter() {
        ArrayList<Message> arrayMessages = new ArrayList<>();

        adapter = new MessageAdapter(this, arrayMessages);
        listView.setAdapter(adapter);
    }


    private void createConnection() {
        clientId = MqttClient.generateClientId();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setKeepAliveInterval(INTERVAl);
        options.setCleanSession(false);
        client = new MqttAndroidClient(this.getApplicationContext(), SERVER_URI, clientId);


        try {

            IMqttToken token = client.connect(options);
            token.setActionCallback(new IMqttActionListener() {
                @Override
                public void onSuccess(IMqttToken asyncActionToken) {
                    // We are connected
                    Toast.makeText(ChatActivity.this, "Connected", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onSuccess: connected");

                    //after connection the sender also subscribes to the topic
                    subscribeToBroker(topic);

                }

                @Override
                public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                    // Something went wrong e.g. connection timeout or firewall problems
                    Toast.makeText(ChatActivity.this, "Unable to Connect", Toast.LENGTH_SHORT).show();
                    Log.d(TAG, "onFailure");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


    }

    private void subscribeToBroker(String topic) {
        topic = this.topic;
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
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // could not subscribe to broker
                    Log.d(TAG, "onFailure: not subscribed");

                }
            });
        } catch (MqttException e) {
            e.printStackTrace();
        }


        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {

            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                String msg = message.toString();
                Message delMessage = new Message(msg);

                addToMessageListView(msg);

                deleteMessage(delMessage);

            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {

            }
        });


    }

    private Boolean checkNetworkConnection() {
        ConnectivityManager cm = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnectedOrConnecting();
    }

    private void sendMessage() {
        send.setOnClickListener((v -> {
            send();

        }));

    }

    private void send() {
        String messages = message.getText().toString();

        if (messages.isEmpty()) {
            Toast.makeText(ChatActivity.this, R.string.empty_message, Toast.LENGTH_SHORT).show();
        } else {

            String payload = messages;
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage mqttMessage = new MqttMessage(encodedPayload);
                mqttMessage.setRetained(true);

                client.publish(topic, mqttMessage);
                Log.d(TAG, "send: mqttMessage published" + mqttMessage.toString());
                message.setText("");


            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }


    }

    private void addToMessageListView(String messages) {

        Message newMsg = new Message(messages);
        adapter.add(newMsg);
        adapter.notifyDataSetChanged();

    }

    private void deleteMessage(Message message) {
        listView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                adapter.getPosition(message);
                adapter.remove(message);
                adapter.notifyDataSetChanged();
            }
        });


    }

}