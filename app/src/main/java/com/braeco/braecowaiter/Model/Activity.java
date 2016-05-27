package com.braeco.braecowaiter.Model;

import com.braeco.braecowaiter.Enums.ActivityType;

import java.util.ArrayList;

/**
 * Created by Weiping on 2016/5/16.
 */

public class Activity {

    private int id;
    private String title;
    private String introduction;
    private String content;
    private String picture;
    private String startDate;
    private String endDate;
    private boolean valid;
    private ActivityType type;

    private ArrayList<Object> details;

    public Activity(int id, String title, String introduction, String content, String picture, String startDate, String endDate, boolean valid, ActivityType type) {
        this.id = id;
        this.title = title;
        this.introduction = introduction;
        this.content = content;
        this.picture = picture;
        this.startDate = startDate;
        this.endDate = endDate;
        this.valid = valid;
        this.type = type;
    }

    public Activity(ActivityType type) {
        this.type = type;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getIntroduction() {
        return introduction;
    }

    public void setIntroduction(String introduction) {
        this.introduction = introduction;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public boolean isValid() {
        return valid;
    }

    public void setValid(boolean valid) {
        this.valid = valid;
    }

    public ActivityType getType() {
        return type;
    }

    public void setType(ActivityType type) {
        this.type = type;
    }

    public ArrayList<Object> getDetails() {
        return details;
    }

    public void setDetails(ArrayList<Object> details) {
        this.details = details;
    }
}
