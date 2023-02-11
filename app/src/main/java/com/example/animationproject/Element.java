package com.example.animationproject;

import android.graphics.Bitmap;

public class Element {
    private int position;
    private String nameOfAnimation;
    private String userEmail;
    private String uriImg; //ссылка из облака
    private Bitmap bmp;
    private int bgColor;
    private int activityType;
    // все используемые конструкторы
    public Element() {
    }
    public Element(String nameOfAnimation, String userEmail, String uriImg) {
        this.nameOfAnimation = nameOfAnimation;
        this.userEmail = userEmail;
        this.uriImg = uriImg;
    }
    public Element(Bitmap bmp) {
        this.bmp = bmp;
    }
    // геттеры-сеттеры
    public int getBgColor() {
        return bgColor;
    }
    public void setBgColor(int bgColor) {
        this.bgColor = bgColor;
    }

    public int getActivityType() {
        return activityType;
    }
    public void setActivityType(int activityType) {
        this.activityType = activityType;
    }

    public int getPosition() {
        return position;
    }
    public void setPosition(int position) {
        this.position = position;
    }

    public String getNameOfAnimation() {
        return nameOfAnimation;
    }
    public void setNameOfAnimation(String nameOfAnimation) {
        this.nameOfAnimation = nameOfAnimation;
    }

    public String getUserEmail() {
        return userEmail;
    }
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    public Bitmap getBmp() {
        return bmp;
    }
    public void setBmp(Bitmap bmp) {
        this.bmp = bmp;
    }

    public String getUriImg() {
        return uriImg;
    }
    public void setUriImg(String uriImg) {
        this.uriImg = uriImg;
    }
}

