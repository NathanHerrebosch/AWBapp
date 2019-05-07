package com.example.awbapp;

/**
 * Represents an item in a ToDo list
 */
public class MowerDataItem {

    /**
     * Item timestamp
     */
    @com.google.gson.annotations.SerializedName("timestamp")
    private String mTimestamp;

    /**
     * Item Id
     */
    @com.google.gson.annotations.SerializedName("id")
    private String mId;

    /**
     * Indicates if the item is completed
     */
    @com.google.gson.annotations.SerializedName("complete")
    private boolean mComplete;

    /**
     * MowerDataItem constructor
     */
    public MowerDataItem() {

    }

    @Override
    public String toString() {
        return getTimestamp();
    }

    /**
     * Initializes a new MowerDataItem
     *
     * @param timestamp
     *            The item text
     * @param id
     *            The item id
     */
    public MowerDataItem(String timestamp, String id) {
        this.setTimestamp(timestamp);
        this.setId(id);
    }

    /**
     * Returns the item text
     */
    public String getTimestamp() {
        return mTimestamp;
    }

    /**
     * Sets the item text
     *
     * @param timestamp
     *            text to set
     */
    public final void setTimestamp(String timestamp) {
        mTimestamp = timestamp;
    }

    /**
     * Returns the item id
     */
    public String getId() {
        return mId;
    }

    /**
     * Sets the item id
     *
     * @param id
     *            id to set
     */
    public final void setId(String id) {
        mId = id;
    }

    /**
     * Indicates if the item is marked as completed
     */
    public boolean isComplete() {
        return mComplete;
    }

    /**
     * Marks the item as completed or incompleted
     */
    public void setComplete(boolean complete) {
        mComplete = complete;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof MowerDataItem && ((MowerDataItem) o).mId == mId;
    }
}