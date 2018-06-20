package com.scarlat.marius.chatapp.model;


public class Conversation {

    private static final String TAG = "Conversation";

    private boolean seen;
    private long timestamp;

    private String friendID;

    /* Constructor */
    public Conversation() { }

    public Conversation(boolean seen, long timestamp) {
        this.seen = seen;
        this.timestamp = timestamp;
    }

    /* Getters and setters */
    public boolean isSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public String getFriendID() { return friendID; }
    public void setFriendID(String friendID) { this.friendID = friendID; }
}
