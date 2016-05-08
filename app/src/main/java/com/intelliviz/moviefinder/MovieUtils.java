package com.intelliviz.moviefinder;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import com.intelliviz.moviefinder.db.MovieContract;

import java.util.ArrayList;
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
        if(doesMovieExist(activity, movie) != -1) {
            //Toast.makeText(activity, activity.getString(R.string.movie_exists) + movie.getTitle(), Toast.LENGTH_LONG).show();
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
        String id = uri.getLastPathSegment();
        movie.setId(Long.parseLong(id));

        if(reviews != null) {
            for (Review review : reviews) {
                values = new ContentValues();
                values.put(MovieContract.ReviewEntry.COLUMN_MOVIE_ID, movie.getMovieId());
                values.put(MovieContract.ReviewEntry.COLUMN_AUTHOR, review.getAuthor());
                values.put(MovieContract.ReviewEntry.COLUMN_CONTENT, review.getContent());
                uri = activity.getContentResolver().insert(MovieContract.ReviewEntry.CONTENT_URI, values);
            }
        } else {
            Log.d(TAG, "Reviews is null");
        }
    }

    public static void dumpMovies(Activity activity) {
        List<Movie> movies = getAllMovies(activity);
        for(Movie movie : movies) {
            Log.d(TAG, movie.getTitle() + "  " + movie.getId());
        }
    }

    public static List<Movie> getAllMovies(Activity activity) {
        List<Movie> movies = new ArrayList<Movie>();
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        String[] projection = {MovieContract.MovieEntry._ID,
                MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                MovieContract.MovieEntry.COLUMN_TITLE};
        Cursor cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        while(cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex(MovieContract.MovieEntry._ID);
            int movieTitleIndex = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_TITLE);

            long id = cursor.getLong(idIndex);
            String title = cursor.getString(movieTitleIndex);

            Movie movie = new Movie(title, id);
            movies.add(movie);
        }

        return movies;
    }

    public static void markFavoriteMovies(Activity activity, List<Movie> movies) {
       for(Movie movie : movies) {
            markFavoriteMovie(activity, movie);
       }
    }

    public static void removeMovieFromFavorites(Activity activity, Movie movie) {
        // delete movie from favorites list
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        uri = Uri.withAppendedPath(uri, "" + movie.getId());
        int numRows = activity.getContentResolver().delete(uri, null, null);

        // delete reviews associated with the movie
        uri = MovieContract.ReviewEntry.CONTENT_URI;
        String where = MovieContract.ReviewEntry.TABLE_NAME + "." + MovieContract.ReviewEntry.COLUMN_MOVIE_ID + " = ?";
        String[] args = {movie.getMovieId()};
        numRows = activity.getContentResolver().delete(uri, where, args);

        movie.setId(-1);

        /*
        MovieListFragment movieListFragment = ((MovieListFragment) activity.getSupportFragmentManager()
                .findFragmentByTag(LIST_FRAG_TAG));
        if (movieListFragment != null) {
            movieListFragment.refreshList();
        }
        */
    }

    public static void markFavoriteMovie(Activity activity, Movie movie) {
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        String selectionClause = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";
        String[] selectionArgs = {movie.getMovieId()};
        String[] projection = {MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_MOVIE_ID};
        Cursor cursor = activity.getContentResolver().query(uri, projection, selectionClause, selectionArgs, null);
        if(cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex(MovieContract.MovieEntry._ID);
            long id = cursor.getLong(idIndex);
            movie.setId(id);
        }
    }

    public static long doesMovieExist(Activity activity, Movie movie) {
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        String selectionClause = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = ?";
        String[] selectionArgs = {movie.getMovieId()};
        String[] projection = {MovieContract.MovieEntry._ID, MovieContract.MovieEntry.COLUMN_MOVIE_ID};
        Cursor cursor = activity.getContentResolver().query(uri, projection, selectionClause, selectionArgs, null);
        if(cursor.moveToNext()) {
            int idIndex = cursor.getColumnIndex(MovieContract.MovieEntry._ID);
            long id = cursor.getLong(idIndex);
            return id;
        }

        return -1;
    }
}
