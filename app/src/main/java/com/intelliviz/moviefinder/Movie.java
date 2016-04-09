package com.intelliviz.moviefinder;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Class that contains information about a movie. Also, object of class can be
 * passed in a bundle to other activities because it is parcelable.
 *
 * Created by edm on 3/18/2016.
 */
public class Movie implements Parcelable {
    private String mTitle;
    private String mPoster;
    private String mSynopsis;
    private String mMovieId;
    private String mReleaseDate;
    private String mAverageVote;
    private String mRuntime;
    private long mId;

    public Movie () {
        this("", "", "", "", "", "", "0", 0);
    }

    public Movie(String title, String poster, String synopsis, String movieId, String releaseDate, String averageVote) {
        this(title, poster, synopsis, movieId, releaseDate, averageVote, "0", 0);
    }

    public Movie(String title, String poster, String synopsis, String movieId, String releaseDate, String averageVote, String runtime, long id) {
        mTitle = title;
        mPoster = poster;
        mSynopsis = synopsis;
        mMovieId = movieId;
        mReleaseDate = releaseDate;
        mAverageVote = averageVote;
        mRuntime = runtime;
        mId = id;
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

    public String getMovieId() {
        return mMovieId;
    }

    public String getReleaseDate() {
        return mReleaseDate;
    }

    public String getAverageVote() {
        return mAverageVote;
    }

    public String getRuntime() {
        return mRuntime;
    }

    public long getId() {
        return mId;
    }

    public void setRuntime(String runtime) {
        mRuntime = runtime;
    }

    public Movie(Parcel in) {
        mTitle = in.readString();
        mPoster = in.readString();
        mSynopsis = in.readString();
        mMovieId = in.readString();
        mReleaseDate = in.readString();
        mAverageVote = in.readString();
        mRuntime = in.readString();
        mId = in.readLong();
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
        dest.writeString(mMovieId);
        dest.writeString(mReleaseDate);
        dest.writeString(mAverageVote);
        dest.writeString(mRuntime);
        dest.writeLong(mId);
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
