package com.yukisoft.yellowpixels.JavaRepositories.Models;

import android.net.Uri;

import com.yukisoft.yellowpixels.JavaRepositories.Fixed.AccountType;

import java.util.Date;

public class UserModel {
    private String id;
    private String dpURI;
    private AccountType type;
    private String email;
    private String name;
    private Date dateRegistered;
    private String whatsappNum;
    private String location;
    private String details;
    private String category;
    private boolean verified = false;

    public boolean isVerified() {return verified; }

    public void setVerified(boolean verified) { this.verified = verified; }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    public String getDpURI() {
        return dpURI;
    }

    public void setDpURI(String dpURI) {
        this.dpURI = dpURI;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public Date getDateRegistered() {
        return dateRegistered;
    }

    public void setDateRegistered(Date dateRegistered) {
        this.dateRegistered = dateRegistered;
    }

    public String getLandLine() {
        return landLine;
    }

    public void setLandLine(String landLine) {
        this.landLine = landLine;
    }

    private String landLine;

    /**
     *
     * GETTER METHODS
     *
     */
    public String getId() {
        return id;
    }
    public AccountType getType() { return type; }
    public String getEmail() { return email; }
    public String getName() { return name; }
    public String getWhatsappNum() { return whatsappNum; }

    /**
     *
     * SETTER METHODS
     *
     */
    public void setId(String id) { this.id = id; }
    public void setType(AccountType type) { this.type = type; }
    public void setEmail(String email) { this.email = email; }
    public void setName(String name) { this.name = name; }
    public void setWhatsappNum(String whatsappNum) { this.whatsappNum = whatsappNum; }

    /**
     * EMPTY CONSTRUCTOR
     */
    public UserModel() {
    }

    /**
     * MAIN CONSTRUCTOR
     * @param id
     * @param email
     * @param name
     */
    public UserModel(String id, String name, String email, AccountType type, Date dateRegistered) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.type = type;
        this.dateRegistered = dateRegistered;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }
}
