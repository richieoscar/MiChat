package com.example.michat.model;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.IUser;

import java.util.Date;

public class Message implements IMessage {

    private String Id;
    private String Text;
    private Author author;
    private Date createdAt;

    public Message(String id, String text, Author author, Date date){
        Id = id;
        Text = text;
        this.author = author;
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
        return author;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }
}
