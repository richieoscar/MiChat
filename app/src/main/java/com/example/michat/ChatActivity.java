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

import com.example.michat.model.Message;
import com.example.michat.model.User;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.Calendar;
import java.util.Date;

public class ChatActivity extends AppCompatActivity {

    private MessagesList messagesList;
    private MessagesListAdapter<Message> messagesListAdapter;

    private MqttAndroidClient client;
    private String clientId;
    private String topic;

    private static final String TAG = "ChatActivity";
    private EditText message;
    private Button send;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        if (checkNetworkConnection()) {
            createConnection();
        } else {
            Toast.makeText(this, "No network", Toast.LENGTH_SHORT).show();
        }

        //instantiate views
        bindViews();
        setUpMessageListAdapter();


        //getTopic(Unique ID)
        Intent intent = getIntent();
        if (intent != null && intent.hasExtra("sub")) {
            topic = intent.getStringExtra("sub");
        }


        sendMessage();

    }

    private void bindViews() {
        messagesList = new MessagesList(this);
        message = findViewById(R.id.editText_message);
        send = findViewById(R.id.send_button);
        messagesList = findViewById(R.id.messageListing);
    }

    private void setUpMessageListAdapter() {

        messagesListAdapter = new MessagesListAdapter("Oscar", null);
        messagesList.setAdapter(messagesListAdapter);
    }

    private void createConnection() {
        clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), "tcp://broker.hivemq.com:1883", clientId);

        try {
            IMqttToken token = client.connect();
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
                public void onFailure(IMqttToken asyncActionToken,
                                      Throwable exception) {
                    // The subscription could not be performed, maybe the user was not
                    // authorized to subscribe on the specified topic e.g. using wildcards

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
                    displayMessages(message);
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
            Toast.makeText(ChatActivity.this, "Sent", Toast.LENGTH_SHORT).show();
            send();
        }));

    }

    private void send() {
        String messages = message.getText().toString().trim();

        if (messages.isEmpty()) {
            Toast.makeText(ChatActivity.this, "You cant send an empty message", Toast.LENGTH_SHORT).show();
        } else {

            String payload = messages;
            byte[] encodedPayload = new byte[0];
            try {
                encodedPayload = payload.getBytes("UTF-8");
                MqttMessage message = new MqttMessage(encodedPayload);
                message.setRetained(true);
                client.publish(topic, message);
                Log.d(TAG, "send: message published" +message.toString());
            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }


    }

    private void displayMessages(MqttMessage message){
            String messages = message.toString();
        User user = new User("Oscar", "df", null);

        Date date = Calendar.getInstance().getTime();

        String messageToDisplay = messages.substring(0, messages.length()-1);

        Message message1 = new Message("Doe", messageToDisplay, user, date);
        Log.d(TAG, "displayMessages: "+message1);

        messagesListAdapter.addToStart(message1, true);

    }
}