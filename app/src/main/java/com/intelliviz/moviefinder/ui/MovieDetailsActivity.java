package com.intelliviz.moviefinder.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.intelliviz.moviefinder.Movie;
import com.intelliviz.moviefinder.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Details activity. Show the details of a selected movie.
 */
public class MovieDetailsActivity extends AppCompatActivity {

    private static final String TAG = MovieDetailsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_details);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        Movie movie = intent.getParcelableExtra(MainActivity.MOVIE_EXTRA);

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
        averageVoteView.setText(new DecimalFormat("#.#").format(Float.parseFloat(movie.getAverageVote()))+"/10");

        Picasso
                .with(this)
                .load(url)
                .into(posterView);
    }
}
