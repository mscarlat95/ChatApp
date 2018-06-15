package com.scarlat.marius.chatapp.model;


import com.scarlat.marius.chatapp.general.Constants;

public class Friend {

    private static final String TAG = "Friend";

    private long friendshipDate;
    private String friendId = Constants.UNSET;

    /* Constructor */
    public Friend() {}

    public String getFriendId() { return  friendId; }
    public void setFriendId(String userId) { this.friendId = userId; }

    /* Gettters and Setters */
    public long getFriendshipDate() { return friendshipDate; }
    public void setFriendshipDate(long friendshipDate) { this.friendshipDate = friendshipDate; }
}
