package com.intelliviz.moviefinder.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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

public class MainActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    public String MovieUrl = "https://api.themoviedb.org/3/movie/popular?page=2&api_key=";
    public static final String PosterUrl = "http://image.tmdb.org/t/p/w185%s";
    public static final String MOVIE_EXTRA = "movie_info";
    private List<Movie> mMovies = new ArrayList<>();
    private String mApiKey;
    ArrayAdapter<Movie> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApiKey = getApiKey();
        // TODO if api_key is null, need to show message and exit
        MovieUrl = "https://api.themoviedb.org/3/movie/popular?page=2&api_key=" + mApiKey;

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

        mAdapter = new MovieAdapter(this, mMovies);
        gridView.setAdapter(mAdapter);

        FetchMoviesTask movieTask = new FetchMoviesTask(mAdapter, mMovies);
        movieTask.execute(MovieUrl);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent settingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
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

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        Log.d(TAG, "Shared Preference Change: " + key);

        // TODO default to popular
        String sort_by = sharedPreferences.getString(key, "");
        Log.d(TAG, "New value for key: " + sort_by);
        MovieUrl = "https://api.themoviedb.org/3/movie/" + sort_by + "?api_key=" + mApiKey;
        FetchMoviesTask movieTask = new FetchMoviesTask(mAdapter, mMovies);
        movieTask.execute(MovieUrl);
    }
}
