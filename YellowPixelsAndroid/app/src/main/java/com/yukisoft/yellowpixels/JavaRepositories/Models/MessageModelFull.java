package com.yukisoft.yellowpixels.JavaRepositories.Models;

import java.util.Date;

public class MessageModelFull {
    String id, message;
    UserModel from;
    Date timeSent;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public UserModel getFrom() {
        return from;
    }

    public void setFrom(UserModel from) {
        this.from = from;
    }

    public Date getTimeSent() {
        return timeSent;
    }

    public void setTimeSent(Date timeSent) {
        this.timeSent = timeSent;
    }

    public MessageModelFull(String message, UserModel from, Date timeSent) {
        this.message = message;
        this.from = from;
        this.timeSent = timeSent;
    }

    public MessageModelFull() {
    }
}
