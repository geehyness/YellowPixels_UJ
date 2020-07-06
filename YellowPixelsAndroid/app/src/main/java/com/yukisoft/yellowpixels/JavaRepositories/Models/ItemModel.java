package com.yukisoft.yellowpixels.JavaRepositories.Models;

import android.net.Uri;

import java.util.ArrayList;

public class ItemModel {
    private String id;
    private String name;
    private Double price;
    private String details;
    private String userId;
    private String category;
    private ArrayList<String> images = new ArrayList<>();

    /**
     * GETTERS
     */
    public String getUserId() { return userId; }
    public String getId() { return id; }
    public String getName() { return name; }
    public Double getPrice() { return price; }
    public String getDetails() { return details; }
    public String getCategory() { return category; }
    public ArrayList<String> getImages() { return images; }

    /**
     * SETTERS
     */
    public void setDetails(String details) { this.details = details; }
    public void setPrice(Double price) { this.price = price; }
    public void setName(String name) { this.name = name; }
    public void setId(String id) { this.id = id; }
    public void setUserId(String userId) { this.userId = userId; }
    public void setCategory(String category) { this.category = category; }
    public void setImages(ArrayList<String> images) { this.images = images; }

    /**
     * EMPTY CONSTRUCTOR
     */
    public ItemModel() { }

    /**
     * MAIN CONSTRUCTOR
     * @param name
     * @param price
     * @param details
     * @param userId
     * @param category
     * @param images
     */
    public ItemModel(String name, Double price, String details, String userId, String category, ArrayList<String> images) {
        this.name = name;
        this.price = price;
        this.details = details;
        this.userId = userId;
        this.category = category;
        this.images = images;
    }
}
