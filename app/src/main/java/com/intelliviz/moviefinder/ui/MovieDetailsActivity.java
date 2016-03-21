package com.intelliviz.moviefinder.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.intelliviz.moviefinder.Movie;
import com.intelliviz.moviefinder.R;

public class MovieDetailsActivity extends AppCompatActivity {

    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        Intent intent = getIntent();
        Movie movie = (Movie)intent.getParcelableExtra(MainActivity.MOVIE_EXTRA);
        Log.d(TAG, "Starting new activity for movie: " + movie.getTitle());

    }
}
