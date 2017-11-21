package com.erugbycoach.halfbaked.Models;

/**
 * Created by William D Howell on 11/21/2017.
 */

import android.os.Parcel;
import android.os.Parcelable;

public class Instructions implements Parcelable {
    private int id;
    private String shortDescription;
    private String instruction;
    private String videoUrl;
    private String thumbnailUrl;

    public Instructions(int id, String shortDescription, String instruction, String videoUrl, String thumbnailUrl) {
        this.id = id;
        this.shortDescription = shortDescription;
        this.instruction = instruction;
        this.videoUrl = videoUrl;
        this.thumbnailUrl = thumbnailUrl;
    }

    //Getters & Setters
    public int getId() {
        return id;
    }

    public String getShortDescription() {
        return shortDescription;
    }

    public String getInstruction() {
        return instruction;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(shortDescription);
        parcel.writeString(instruction);
        parcel.writeString(videoUrl);
        parcel.writeString(thumbnailUrl);
    }
}

