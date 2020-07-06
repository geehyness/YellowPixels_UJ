package com.yukisoft.yellowpixels.JavaRepositories.Models;

import java.util.ArrayList;
import java.util.Date;

public class ChatModelFull {
    private String id;
    private UserModel business;
    private Date lastMessageDate;
    private ArrayList<MessageModel> messages = new ArrayList<>();

    public void setMessages(ArrayList<MessageModel> messages) {
        this.messages = messages;
    }

    public UserModel getBusiness() {
        return business;
    }

    public void setBusiness(UserModel business) {
        this.business = business;
    }

    public Date getLastMessageDate() {
        return lastMessageDate;
    }

    public void setLastMessageDate(Date lastMessageDate) {
        this.lastMessageDate = lastMessageDate;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ArrayList<MessageModel> getMessages() {
        return messages;
    }

    public void addMessages (MessageModel message) {
        messages.add(message);
    }

    public String getLastMessage() {
        return messages.get(messages.size()-1).message;
    }

    public ChatModelFull() {
    }

    public ChatModelFull(UserModel business, Date lastMessageDate, ArrayList<MessageModel> messages) {
        this.business = business;
        this.lastMessageDate = lastMessageDate;
        this.messages = messages;
    }
}
