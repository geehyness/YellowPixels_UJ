package com.yukisoft.yellowpixels.JavaRepositories.Models;

import java.util.Date;

public class MessageModel {
    String id, message, from;
    Date timeSent;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Date getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(Date timeSent) {
        this.timeSent = timeSent;
    }

    public MessageModel(String message, String from, Date timeSent) {
        this.message = message;
        this.from = from;
        this.timeSent = timeSent;
    }

    public MessageModel() {
    }
}
