package com.intelliviz.moviefinder.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.intelliviz.moviefinder.Movie;
import com.intelliviz.moviefinder.R;

public class MovieDetailsActivity extends AppCompatActivity {
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
        ft.add(R.id.fragment_holder, fragment);
        ft.commit();
    }
}
