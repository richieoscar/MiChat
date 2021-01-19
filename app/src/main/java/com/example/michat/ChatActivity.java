package com.example.michat;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.michat.model.Message;
import com.example.michat.model.Author;
import com.stfalcon.chatkit.messages.MessagesList;
import com.stfalcon.chatkit.messages.MessagesListAdapter;

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
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class ChatActivity extends AppCompatActivity {

    private MessagesList messagesList;
    private MessagesListAdapter<Message> messagesListAdapter;
   // private MessageInput messageInput;

    private MqttAndroidClient client;
    private String clientId;
    private String topic;

    private static final String TAG = "ChatActivity";
    private static  final int INTERVAl = 1000*60*60;
    private EditText message;
    private Button send;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);


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
                  ab.setTitle("Topic(" +topic +")");
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
        if(iD == R.id.disconnect ){
            try {
                client.disconnect();
                startActivity(new Intent(this, MainActivity.class));
                finish();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    @Override
    protected void onRestart() {
        super.onRestart();

        if(client.isConnected()){
        subscribeToBroker(topic);


        }
        else{
            createConnection();
        }
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

  //  @Override
//    protected void onDestroy() {
//        try {
//            client.disconnect();
//        } catch (MqttException e) {
//            e.printStackTrace();
//        }
//        super.onDestroy();
//    }

    private void createConnection() {
        clientId = MqttClient.generateClientId();
        MqttConnectOptions options = new MqttConnectOptions();

        options.setKeepAliveInterval(INTERVAl);
        options.setCleanSession(false);
        String serverURI = "tcp://broker.hivemq.com:1883";
        client = new MqttAndroidClient(this.getApplicationContext(), serverURI, clientId);


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
               // String msg = message.toString();
               // Toast.makeText(ChatActivity.this, msg, Toast.LENGTH_SHORT).show();
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
                Log.d(TAG, "send: mqttMessage published" +mqttMessage.toString());
                message.setText("");


            } catch (UnsupportedEncodingException | MqttException e) {
                e.printStackTrace();
            }
        }



    }

    private void loadMoreMessages(){
        messagesListAdapter.setLoadMoreListener(new MessagesListAdapter.OnLoadMoreListener() {
            @Override
            public void onLoadMore(int page, int totalItemsCount) {


            }
        });
    }

    private void displayMessages(MqttMessage message){
        String messages = message.toString();
        Author author = new Author("Osc", "Os", null);

        Date date = Calendar.getInstance().getTime();

        String displayMessage = messages;

        List<Message> addMessage = new ArrayList<>();

        Message msg = new Message("Oscar", displayMessage, author, date);
        addMessage.add(msg);
        Log.d(TAG, "displayMessages: "+msg);

       //messagesListAdapter.addToStart(msg, true);
      messagesListAdapter.notifyDataSetChanged();
        messagesListAdapter.addToEnd(addMessage, true);

    }

    private void deleteMessage(Message message){

        messagesListAdapter.deleteSelectedMessages();
    }

}