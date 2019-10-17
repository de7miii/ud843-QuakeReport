package com.example.android.quakereport;

public class Earthquake {
    private String mMagnitude;
    private String mPlace;
    private String mTime;
    private String mWebUrl;

    public Earthquake(String magnitude, String place, String time) {
        mMagnitude = magnitude;
        mPlace = place;
        mTime = time;
    }

    public String getMagnitude() {
        return mMagnitude;
    }

    public void setMagnitude(String magnitude) {
        mMagnitude = magnitude;
    }

    public String getPlace() {
        return mPlace;
    }

    public void setPlace(String place) {
        mPlace = place;
    }

    public String getTime() {
        return mTime;
    }

    public void setTime(String time) {
        mTime = time;
    }

    public String getWebUrl() {
        return mWebUrl;
    }

    public void setWebUrl(String webUrl) {
        mWebUrl = webUrl;
    }
}
