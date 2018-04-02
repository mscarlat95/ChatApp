package com.scarlat.marius.chatapp.model;


import java.net.URI;
import java.util.ArrayList;

public class User {

    private String name;
    private String email;
    private URI profileImage;

    private ArrayList<User> friends;



    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
