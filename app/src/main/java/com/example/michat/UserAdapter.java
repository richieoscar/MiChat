package com.example.michat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;


import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.michat.model.User;

import java.util.ArrayList;
import java.util.List;

public class UserAdapter extends ArrayAdapter<User> {


    public UserAdapter(@NonNull Context context,  @NonNull ArrayList<User> users) {
        super(context, 0, users);


    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        User user = getItem(position);
        if(convertView == null){
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_view, parent, false);

        }
            TextView msg = convertView.findViewById(R.id.textView_msg);
            msg.setText(user.getMessage());
            return  convertView;
    }

}
