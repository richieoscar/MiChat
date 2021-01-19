package com.example.michat.model;

import com.stfalcon.chatkit.commons.models.IDialog;
import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

public class Message implements IMessage {

    private String Id;
    private String Text;
    private User user;
    private Date createdAt;

    public Message(String id, String text, User user, Date date){
        Id = id;
        Text = text;
        this.user = user;
    }
    @Override
    public String getId() {
        return Id;
    }

    @Override
    public String getText() {
        return Text;
    }

    @Override
    public IUser getUser() {
        return user;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }
}
