package com.example.godutch.ui.gallery;

import android.os.Parcel;
import android.os.Parcelable;

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

    public static GoDutchPhoto[] getPhotos() {
        return new GoDutchPhoto[]{
                new GoDutchPhoto("http://i.imgur.com/zuG2bGQ.jpg", "Galaxy"),
                new GoDutchPhoto("http://i.imgur.com/ovr0NAF.jpg", "Space Shuttle"),
                new GoDutchPhoto("http://i.imgur.com/n6RfJX2.jpg", "Galaxy Orion"),
                new GoDutchPhoto("http://i.imgur.com/qpr5LR2.jpg", "Earth"),
                new GoDutchPhoto("http://i.imgur.com/pSHXfu5.jpg", "Astronaut"),
                new GoDutchPhoto("http://i.imgur.com/3wQcZeY.jpg", "Satellite"),
        };
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
}
