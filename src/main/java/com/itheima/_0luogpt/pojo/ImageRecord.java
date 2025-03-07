package com.itheima._0luogpt.pojo;

import java.sql.Timestamp;

public class ImageRecord {
    private int id;
    private String userPrompt;
    private byte[] imageData;
    private Timestamp generatedAt;
    private String aiResponse; // AIController 的回答
    private String imageRequest; // 用户文生图的要求

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserPrompt() {
        return userPrompt;
    }

    public void setUserPrompt(String userPrompt) {
        this.userPrompt = userPrompt;
    }

    public byte[] getImageData() {
        return imageData;
    }

    public void setImageData(byte[] imageData) {
        this.imageData = imageData;
    }

    public Timestamp getGeneratedAt() {
        return generatedAt;
    }

    public void setGeneratedAt(Timestamp generatedAt) {
        this.generatedAt = generatedAt;
    }

    public String getAiResponse() {
        return aiResponse;
    }

    public void setAiResponse(String aiResponse) {
        this.aiResponse = aiResponse;
    }

    public String getImageRequest() {
        return imageRequest;
    }

    public void setImageRequest(String imageRequest) {
        this.imageRequest = imageRequest;
    }
}
