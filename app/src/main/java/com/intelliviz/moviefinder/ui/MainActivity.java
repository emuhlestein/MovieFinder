package com.intelliviz.moviefinder.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.intelliviz.moviefinder.FetchMoviesTask;
import com.intelliviz.moviefinder.Movie;
import com.intelliviz.moviefinder.MovieAdapter;
import com.intelliviz.moviefinder.R;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public static final String MOVIE_API_KEY = "";
    public static final String MovieUrl = "https://api.themoviedb.org/3/movie/popular?api_key=" + MOVIE_API_KEY;
    public static final String PosterUrl = "http://image.tmdb.org/t/p/w185%s";
    public static final String MOVIE_EXTRA = "movie_info";
    private List<Movie> mMovies = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        GridView gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = mMovies.get(position);

                Log.d(TAG, "Selected movie: " + movie.getTitle());

                Intent intent = new Intent(MainActivity.this, MovieDetailsActivity.class);
                intent.putExtra(MOVIE_EXTRA, movie);
                startActivity(intent);
            }
        });

        ArrayAdapter<Movie> adapter = new MovieAdapter(this, mMovies);
        gridView.setAdapter(adapter);

        FetchMoviesTask movieTask = new FetchMoviesTask(adapter, mMovies);
        movieTask.execute(MovieUrl);

        File file = getFilesDir();

        File[] files = file.listFiles();
        for(File f : files) {
            Log.d(TAG, "File: " + f.getAbsolutePath());
        }

        //openFileInput()
    }
}
