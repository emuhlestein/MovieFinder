package com.intelliviz.moviefinder.ui;

import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import com.intelliviz.moviefinder.Movie;
import com.intelliviz.moviefinder.R;
import com.intelliviz.moviefinder.Review;
import com.intelliviz.moviefinder.Trailer;
import com.intelliviz.moviefinder.db.MovieContract;

import java.util.List;

public class MovieDetailsActivity extends AppCompatActivity implements
        MovieDetailsFragment.OnSelectReviewListener {
    private static final String LIST_FRAG_TAG = "list frag tag";
    public static final String MOVIE_EXTRA = "movie";
    public static final String FAVORITE_EXTRA = "favorite";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Intent intent = getIntent();
        boolean favorite = intent.getBooleanExtra(FAVORITE_EXTRA, false);
        Movie movie = intent.getParcelableExtra(MOVIE_EXTRA);

        FragmentManager fm = getSupportFragmentManager();
        MovieDetailsFragment fragment = MovieDetailsFragment.newInstance(movie, favorite);
        FragmentTransaction ft = fm.beginTransaction();
        ft.add(R.id.fragment_holder, fragment, LIST_FRAG_TAG);
        ft.commit();
    }

    @Override
    public void onSelectReview(Review review) {
        Fragment fragment = MovieReviewFragment.newInstance(review);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_holder, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onSelectTrailer(Trailer trailer) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(trailer.getUrl()));
        startActivity(intent);
    }

    @Override
    public void onAddMovieToFavorite(Movie movie, List<Review> reviews) {
        if(doesMovieExist(movie)) {
            Toast.makeText(this, "Movie already marked as favorite. It will not be added: " + movie.getTitle(), Toast.LENGTH_LONG).show();
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
        Uri uri = getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, values);
    }

    @Override
    public void onDeleteMovieFromFavorite(Movie movie) {
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        uri = Uri.withAppendedPath(uri, "" + movie.getId());
        //int numRows = getContentResolver().delete(uri, null, null);
        //if(numRows == 1) {
            Intent intent = new Intent();
            intent.putExtra("movie_to_delete", movie);
            setResult(RESULT_OK, intent);
            finish();
        //}
    }

    private boolean doesMovieExist(Movie movie) {
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        uri = Uri.withAppendedPath(uri, "" + movie.getMovieId());

        String[] projection = {MovieContract.MovieEntry.COLUMN_MOVIE_ID};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        if(cursor.moveToNext()) {
            return true;
        }

        return false;
    }
}
