package com.intelliviz.moviefinder;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by edm on 3/18/2016.
 */
public class Movie implements Parcelable {
    private String mTitle;
    private String mPoster;
    private String mSynopsis;
    private String mId;
    private String mReleaseDate;
    private String mAverageVote;

    public Movie () {
    }

    public Movie(String title, String poster, String synopsis, String id, String releaseDate, String averageVote) {
        mTitle = title;
        mPoster = poster;
        mSynopsis = synopsis;
        mId = id;
        mReleaseDate = releaseDate;
        mAverageVote = averageVote;
    }

    public Movie(String title) {
        mTitle = title;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getPoster() {
        return mPoster;
    }

    public String getSynopsis() {
        return mSynopsis;
    }

    public String getId() {
        return mId;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public String getAverageVote() {
        return mAverageVote;
    }

    public Movie(Parcel in) {
        mTitle = in.readString();
        mPoster = in.readString();
        mSynopsis = in.readString();
        mId = in.readString();
        mReleaseDate = in.readString();
        mAverageVote = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mTitle);
        dest.writeString(mPoster);
        dest.writeString(mSynopsis);
        dest.writeString(mId);
        dest.writeString(mReleaseDate);
        dest.writeString(mAverageVote);
    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {

        @Override
        public Movie createFromParcel(Parcel source) {
            return new Movie(source);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };
}
