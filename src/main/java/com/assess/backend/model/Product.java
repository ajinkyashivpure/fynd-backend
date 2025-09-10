package com.assess.backend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "products")
public class Product {
    @Id
    private String id;

    private String title;
    private String description;
    private BigDecimal price;
    private String url;
    private String imageUrl;


    private List<String> categories = new ArrayList<>();
    // Vector embedding - must match the path in Atlas Vector Search index

    private List<Double> vectorData;
    private List<Double> imageVectorData;
    private Double score;
    private String brand;
    private String gender;
    private String type;

    //getters and setters



    //    private String brand;
//    private String color;
//    private String size;
//    private boolean inStock;
//    private int stockQuantity;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Double> getImageVectorData() {
        return imageVectorData;
    }

    public void setImageVectorData(List<Double> imageVectorData) {
        this.imageVectorData = imageVectorData;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public List<String> getCategories() {
        return categories;
    }

    public void setCategories(List<String> categories) {
        this.categories = categories;
    }

//    public String getBrand() {
//        return brand;
//    }
//
//    public void setBrand(String brand) {
//        this.brand = brand;
//    }
//
//    public String getColor() {
//        return color;
//    }
//
//    public void setColor(String color) {
//        this.color = color;
//    }
//
//    public String getSize() {
//        return size;
//    }
//
//    public void setSize(String size) {
//        this.size = size;
//    }
//
//    public boolean isInStock() {
//        return inStock;
//    }
//
//    public void setInStock(boolean inStock) {
//        this.inStock = inStock;
//    }
//
//    public int getStockQuantity() {
//        return stockQuantity;
//    }
//
//    public void setStockQuantity(int stockQuantity) {
//        this.stockQuantity = stockQuantity;
//    }

    public List<Double> getVectorData() {
        return vectorData;
    }

    public void setVectorData(List<Double> vectorData) {
        this.vectorData = vectorData;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}