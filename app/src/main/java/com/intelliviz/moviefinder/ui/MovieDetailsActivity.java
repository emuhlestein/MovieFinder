package com.intelliviz.moviefinder.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.intelliviz.moviefinder.Movie;
import com.intelliviz.moviefinder.OnUpdateUI;
import com.intelliviz.moviefinder.R;
import com.squareup.picasso.Picasso;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MovieDetailsActivity extends AppCompatActivity implements OnUpdateUI {

    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Movie movie = (Movie)intent.getParcelableExtra(MainActivity.MOVIE_EXTRA);
        Log.d(TAG, "Starting new activity for movie: " + movie.getTitle());

        ImageView posterView = (ImageView) findViewById(R.id.posterView);
        TextView titleView = (TextView) findViewById(R.id.titleView);
        TextView summaryView = (TextView) findViewById(R.id.summaryView);
        TextView releaseDateView = (TextView) findViewById(R.id.releaseDateView);
        TextView averageVoteView = (TextView) findViewById(R.id.averageVoteView);


        titleView.setText(movie.getTitle());
        summaryView.setText(movie.getSynopsis());
        releaseDateView.setText(movie.getReleaseDate());

        String url = String.format(MainActivity.PosterUrl, movie.getPoster());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
        String str = movie.getReleaseDate();
        try {
            Date date = formatter.parse(movie.getReleaseDate());

            formatter = new SimpleDateFormat("yyyy");
            str = formatter.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        releaseDateView.setText(str);
        averageVoteView.setText(movie.getAverageVote()+"/10");

        Date curDate = new Date();

        Log.d(TAG, "Movie Release Date: " + movie.getReleaseDate());


        Picasso
                .with(this)
                .load(url)
                .resize(600, 1000)
                .centerCrop()
                .into(posterView);

    }

    @Override
    public void updateUI(Movie movie) {

    }
}
