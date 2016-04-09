package com.intelliviz.moviefinder;

import android.database.Cursor;

import com.intelliviz.moviefinder.db.MovieContract;

/**
 * Created by edm on 4/8/2016.
 */
public class MovieUtils {
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
}
