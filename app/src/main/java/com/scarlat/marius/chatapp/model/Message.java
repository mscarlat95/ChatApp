package com.scarlat.marius.chatapp.model;


public class Message {

    private static final String TAG = "Message";

    private String id;
    private String message;
    private String type;
    private long timestamp;
    private boolean seen;
    private String from;

    /* Constructor */
    public Message() {}

    public Message(String message, String type, long timestamp, boolean seen) {
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
        this.seen = seen;
    }

    /* Getters and setters */
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean getSeen() { return seen; }
    public void setSeen(boolean seen) { this.seen = seen; }

    public String getFrom() { return from; }
    public void setFrom(String from) { this.from = from; }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Message) {
            if (((Message) obj).getId().equals(this.getId())) {
                return true;
            }
        }

        return false;
    }
}
