package com.example.michat.model;



import java.util.List;

public class Message {

    public Message(String message) {
        this.message = message;
    }

   private  String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
