package com.example.michat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.michat.model.Message;

import java.util.ArrayList;

public class MessageAdapter extends ArrayAdapter<Message> {


    public MessageAdapter(@NonNull Context context, @NonNull ArrayList<Message> messages) {
        super(context, 0, messages);


    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Message message = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view, parent, false);

        }
            TextView msg = convertView.findViewById(R.id.textView_msg);
            msg.setText(message.getMessage());
            return  convertView;
    }

}
