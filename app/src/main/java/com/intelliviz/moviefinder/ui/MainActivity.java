package com.intelliviz.moviefinder.ui;

import android.content.Intent;
import android.content.res.AssetManager;
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

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = MainActivity.class.getSimpleName();
    public String MovieUrl = "https://api.themoviedb.org/3/movie/popular?api_key=";
    public static final String PosterUrl = "http://image.tmdb.org/t/p/w185%s";
    public static final String MOVIE_EXTRA = "movie_info";
    private List<Movie> mMovies = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        String api_key = getApiKey();
        // TODO if api_key is null, need to show message and exit
        MovieUrl = "https://api.themoviedb.org/3/movie/popular?api_key=" + api_key;

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
    }

    private String getApiKey() {
        AssetManager assetManager = getAssets();
        try {
            InputStream in = assetManager.open("api_key.json");
            InputStreamReader inputStream = new InputStreamReader(in);
            BufferedReader reader = new BufferedReader(inputStream);
            StringBuffer buffer = new StringBuffer();
            String line;
            while((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
            }

            JSONObject obj = new JSONObject(buffer.toString());
            String api_key = obj.getString("api_key");
            return api_key;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

}
