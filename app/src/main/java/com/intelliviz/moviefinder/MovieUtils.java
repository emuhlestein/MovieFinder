package com.intelliviz.moviefinder;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.intelliviz.moviefinder.db.MovieContract;

import java.util.List;

/**
 * Created by edm on 4/8/2016.
 */
public class MovieUtils {
    public static final String TAG = MovieUtils.class.getSimpleName();
    public static Movie extractMovieFromCursor(Cursor cursor) {
        if(cursor == null) {
            return null;
        }

        int idIndex = cursor.getColumnIndex(MovieContract.MovieEntry._ID);
        int titleIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);
        int posterIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER);
        int aveVoteIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE);
        int movieIdIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_MOVIE_ID);
        int releaseDateIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RELEASE_DATA);
        int runtimeIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_RUNTIME);
        int synopsisIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_SYNOPSIS);
        long id = cursor.getLong(idIndex);
        String title = cursor.getString(titleIndex);
        String poster = cursor.getString(posterIndex);
        String aveVote = cursor.getString(aveVoteIndex);
        String movieId = cursor.getString(movieIdIndex);
        String releaseDate = cursor.getString(releaseDateIndex);
        String runtime = cursor.getString(runtimeIndex);
        String synopsis = cursor.getString(synopsisIndex);

        Movie movie = new Movie(title, poster, synopsis, movieId, releaseDate, aveVote, runtime, id);
        return movie;
    }

    public static void addMovieToFavorite(Activity activity, Movie movie, List<Review> reviews) {
        if(doesMovieExist(activity, movie)) {
            Toast.makeText(activity, activity.getString(R.string.movie_exists) + movie.getTitle(), Toast.LENGTH_LONG).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movie.getMovieId());
        values.put(MovieContract.MovieEntry.COLUMN_POSTER, movie.getPoster());
        values.put(MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE, movie.getAverageVote());
        values.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATA, movie.getReleaseDate());
        values.put(MovieContract.MovieEntry.COLUMN_RUNTIME, movie.getRuntime());
        values.put(MovieContract.MovieEntry.COLUMN_SYNOPSIS, movie.getSynopsis());
        values.put(MovieContract.MovieEntry.COLUMN_TITLE, movie.getTitle());
        Uri uri = activity.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);

        if(uri == null) {
            Log.d(TAG, "URI is null");
        }
        String id = uri.getLastPathSegment();
        for(Review review : reviews) {
            values = new ContentValues();
            values.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, movie.getMovieId());
            values.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
            values.put(MovieContract.ReviewEntry.COLUMN_CONTENT, review.getContent());
            uri = activity.getContentResolver().insert(MovieContract.ReviewEntry.CONTENT_URI, values);
        }
    }

    private static boolean doesMovieExist(Activity activity, Movie movie) {
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        String selectionClause = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";
        String[] selectionArgs = {movie.getMovieId()};
        String[] projection = {MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_MOVIE_ID};
        Cursor cursor = activity.getContentResolver().query(uri, projection, selectionClause, selectionArgs, null);
        if(cursor.moveToNext()) {
            return true;
        }

        return false;
    }
}
