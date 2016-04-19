package com.intelliviz.moviefinder.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.intelliviz.moviefinder.Movie;
import com.intelliviz.moviefinder.MovieUtils;
import com.intelliviz.moviefinder.R;
import com.intelliviz.moviefinder.Review;
import com.intelliviz.moviefinder.Trailer;
import com.intelliviz.moviefinder.db.MovieContract;

import java.util.ArrayList;
import java.util.List;

public class MovieDetailsActivity extends AppCompatActivity implements
        MovieDetailsFragment.OnSelectReviewListener {
    private static final String LIST_FRAG_TAG = "list frag tag";
    public static final String MOVIE_EXTRA = "movie";
    public static final String FAVORITE_EXTRA = "favorite";
    public static final String REVIEWS_EXTRA = "reviews";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Intent intent = getIntent();
        boolean favorite = intent.getBooleanExtra(FAVORITE_EXTRA, false);
        Movie movie = intent.getParcelableExtra(MOVIE_EXTRA);
        ArrayList<Review> reviews = intent.getParcelableArrayListExtra(REVIEWS_EXTRA);

        FragmentManager fm = getSupportFragmentManager();
        MovieDetailsFragment fragment = MovieDetailsFragment.newInstance(movie, reviews, favorite);
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
        MovieUtils.addMovieToFavorite(this, movie, reviews);
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
}
