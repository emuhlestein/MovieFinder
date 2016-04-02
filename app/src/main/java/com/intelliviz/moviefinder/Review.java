package com.intelliviz.moviefinder;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by edm on 3/31/2016.
 */
public class Review implements Parcelable {
    private String mAuthor;
    private String mContent;

    public Review() {
    }

    public Review(String author, String content) {
        mAuthor = author;
        mContent = content;
    }

    public String getAuthor() {
        return mAuthor;
    }

    public String getContent() {
        return mContent;
    }

    public Review(Parcel in) {
        mAuthor = in.readString();
        mContent = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mAuthor);
        dest.writeString(mContent);
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {

        @Override
        public Review createFromParcel(Parcel source) {
            return new Review(source);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
        }
    };
}
