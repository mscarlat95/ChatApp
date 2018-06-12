package com.scarlat.marius.chatapp.model;


import com.scarlat.marius.chatapp.general.Constants;

public class User {

    private String fullname = Constants.UNSET;
    private String email = Constants.UNSET;
    private String status = Constants.UNSET;
    private String profileImage = Constants.UNSET;

    public User() {}
    public User(String fullname, String email, String status, String profileImage) {
        super();

        this.fullname = fullname;
        this.email = email;
        this.status = status;
        this.profileImage = profileImage;
    }

    /* Setters and getters */
    public String getFullname() { return fullname; }
    public void setFullname(String fullname) { this.fullname = fullname; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getProfileImage() { return profileImage; }
    public void setProfileImage(String profileImage) { this.profileImage = profileImage; }
}
