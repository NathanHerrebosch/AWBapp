package com.example.awbapp;

/**
 * Represents an item with mowerdata
 */
public class MowerDataItem {

    //Item timestamp
    @com.google.gson.annotations.SerializedName("timestamp")
    private String mTimestamp;

    //Item Id
    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    //Indicates if the item is completed.
    @com.google.gson.annotations.SerializedName("complete")
    private boolean mComplete;

    //the latitude
    @com.google.gson.annotations.SerializedName("lat")
    private float mLat;

    //the longitude
    @com.google.gson.annotations.SerializedName("lng")
    private float mLng;

    //the value of the x-axis of the joystick
    @com.google.gson.annotations.SerializedName("x_axis")
    private float mXaxis;

    //the value of the y-axis of the joystick
    @com.google.gson.annotations.SerializedName("y_axis")
    private float mYaxis;

    //the value of the z-axis of the joystick
    @com.google.gson.annotations.SerializedName("z_axis")
    private float mZaxis;

    //the up button of the joystick (1 = pressed)
    @com.google.gson.annotations.SerializedName("up")
    private boolean mUp;

    //the down button of the joystick (1 = pressed)
    @com.google.gson.annotations.SerializedName("down")
    private boolean mDown;

    //the angle of the first sensor
    @com.google.gson.annotations.SerializedName("angle1")
    private float mAngle1;

    //the angle of the second sensor
    @com.google.gson.annotations.SerializedName("angle2")
    private float mAngle2;

    //the angle of the third sensor
    @com.google.gson.annotations.SerializedName("angle3")
    private float mAngle3;

    //the angle of the fourth sensor
    @com.google.gson.annotations.SerializedName("angle4")
    private float mAngle4;

    //the angle of the fifth sensor
    @com.google.gson.annotations.SerializedName("angle5")
    private float mAngle5;

    //the temperature of the oil
    @com.google.gson.annotations.SerializedName("temperature")
    private float mTemperature;

    //the ventilator state (0 = off, 1 = cooling, 2 = cleaning)
    @com.google.gson.annotations.SerializedName("ventilator")
    private String ventilator;

    /**
     *  Here are the setters and getters of the fields defined above
     */

    public String getTimestamp() {
        return mTimestamp;
    }

    public final void setTimestamp(String Timestamp) {
        mTimestamp = Timestamp;
    }

    public String getId() {
        return mId;
    }

    public final void setId(String id) {
        mId = id;
    }

    public boolean isComplete() {
        return mComplete;
    }

    public void setComplete(boolean complete) {
        mComplete = complete;
    }

    public float getmLat() {
        return mLat;
    }

    public void setmLat(float mLat) {
        this.mLat = mLat;
    }

    public float getmLng() {
        return mLng;
    }

    public void setmLng(float mLng) {
        this.mLng = mLng;
    }

    public float getmXaxis() {
        return mXaxis;
    }

    public void setmXaxis(float mXaxis) {
        this.mXaxis = mXaxis;
    }

    public float getmYaxis() {
        return mYaxis;
    }

    public void setmYaxis(float mYaxis) {
        this.mYaxis = mYaxis;
    }

    public float getmZaxis() {
        return mZaxis;
    }

    public void setmZaxis(float mZaxis) {
        this.mZaxis = mZaxis;
    }

    public boolean ismUp() {
        return mUp;
    }

    public void setmUp(boolean mUp) {
        this.mUp = mUp;
    }

    public boolean ismDown() {
        return mDown;
    }

    public void setmDown(boolean mDown) {
        this.mDown = mDown;
    }

    public float getmAngle1() {
        return mAngle1;
    }

    public void setmAngle1(float mAngle1) {
        this.mAngle1 = mAngle1;
    }

    public float getmAngle2() {
        return mAngle2;
    }

    public void setmAngle2(float mAngle2) {
        this.mAngle2 = mAngle2;
    }

    public float getmAngle3() {
        return mAngle3;
    }

    public void setmAngle3(float mAngle3) {
        this.mAngle3 = mAngle3;
    }

    public float getmAngle4() {
        return mAngle4;
    }

    public void setmAngle4(float mAngle4) {
        this.mAngle4 = mAngle4;
    }

    public float getmAngle5() {
        return mAngle5;
    }

    public void setmAngle5(float mAngle5) {
        this.mAngle5 = mAngle5;
    }

    public float getmTemperature() {
        return mTemperature;
    }

    public void setmTemperature(float mTemperature) {
        this.mTemperature = mTemperature;
    }

    public String getVentilator() {
        return ventilator;
    }

    public void setVentilator(String ventilator) {
        this.ventilator = ventilator;
    }

    //the constructor of the class
    public MowerDataItem() {

    }

    @Override
    public String toString() {
        return getId() + " " + getTimestamp();
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MowerDataItem && ((MowerDataItem) o).mId == mId;
    }
}