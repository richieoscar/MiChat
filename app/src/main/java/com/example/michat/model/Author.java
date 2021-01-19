package com.example.michat.model;

import com.stfalcon.chatkit.commons.models.IUser;

public class Author implements IUser {

    private String Id;
    private String Name;
    private String Avatar;

    public Author(String id, String name, String avatar){
        Id = id;
        Name = name;
        Avatar = avatar;
    }
    @Override
    public String getId() {
        return Id;
    }

    @Override
    public String getName() {
        return Name;
    }

    @Override
    public String getAvatar() {
        return Avatar;
    }
}
