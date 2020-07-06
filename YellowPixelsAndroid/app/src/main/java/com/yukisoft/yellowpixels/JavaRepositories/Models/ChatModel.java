package com.yukisoft.yellowpixels.JavaRepositories.Models;

import java.util.ArrayList;
import java.util.Date;

public class ChatModel {
    private String id;
    private String businessId;
    private Date lastMessageDate;
    private ArrayList<MessageModel> messages = new ArrayList<>();

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
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

    public ChatModel() {
    }

    public ChatModel(String businessId, Date lastMessageDate, ArrayList<MessageModel> messages) {
        this.businessId = businessId;
        this.lastMessageDate = lastMessageDate;
        this.messages = messages;
    }
}
