package com.example.godutch.ui.gallery;

import android.os.Parcel;
import android.os.Parcelable;

import com.example.godutch.Constants;

public class GoDutchPhoto implements Parcelable {
    private String url;
    private String title;

    public GoDutchPhoto(String url, String title) {
        this.url = url;
        this.title = title;
    }

    protected GoDutchPhoto(Parcel in) {
        this.url = in.readString();
        this.title = in.readString();
    }

    public static final Creator<GoDutchPhoto> CREATOR = new Creator<GoDutchPhoto>() {
        @Override
        public GoDutchPhoto createFromParcel(Parcel parcel) {
            return new GoDutchPhoto(parcel);
        }

        @Override
        public GoDutchPhoto[] newArray(int i) {
            return new GoDutchPhoto[i];
        }
    };

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(this.url);
        parcel.writeString(this.title);
    }

    @Override
    public String toString() {
        return "/tmp" + this.url.substring(Constants.SERVER_IP.length() + 1);
    }
}
