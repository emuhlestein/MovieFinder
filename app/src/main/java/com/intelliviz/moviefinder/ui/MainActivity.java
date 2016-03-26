package com.intelliviz.moviefinder.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
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

/**
 * Main activity for movie app
 */
public class MainActivity extends AppCompatActivity
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String API_KEY_NOT_SET = "api key not set";
    private static final String DEFAULT_SORT_BY_OPTION = "popular";
    private static final String DEFAULT_PAGE = "1";
    private static final String MOVIEDB_END_POINT = "https://api.themoviedb.org/3/movie/";
    public String MovieUrl;
    public static final String PosterUrl = "http://image.tmdb.org/t/p/w185%s";
    public static final String MOVIE_EXTRA = "movie_info";
    private List<Movie> mMovies = new ArrayList<>();
    private String mApiKey = API_KEY_NOT_SET; // Put api key here;
    ArrayAdapter<Movie> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mApiKey = getApiKey();
        if(mApiKey == null) {
            if(mApiKey.equals(API_KEY_NOT_SET)) {
                fatalError();
            }
        }

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        sp.registerOnSharedPreferenceChangeListener(this);
        String sort_key = getResources().getString(R.string.pref_sort_by_key);
        String sort_by = sp.getString(sort_key, DEFAULT_SORT_BY_OPTION);
        if(sort_by == null) {
            MovieUrl = buildMovieUrl(DEFAULT_SORT_BY_OPTION);
        } else {
            MovieUrl = buildMovieUrl(sort_by);
        }

        GridView gridView = (GridView) findViewById(R.id.grid_view);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = mMovies.get(position);

                Intent intent = new Intent(MainActivity.this, MovieDetailsActivity.class);
                intent.putExtra(MOVIE_EXTRA, movie);
                startActivity(intent);
            }
        });

        mAdapter = new MovieAdapter(this, mMovies);
        gridView.setAdapter(mAdapter);

        FetchMoviesTask movieTask = new FetchMoviesTask(mAdapter, mMovies);
        movieTask.execute(MovieUrl);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            Intent settingsActivity = new Intent(this, SettingsActivity.class);
            startActivity(settingsActivity);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String sort_by = sharedPreferences.getString(key, DEFAULT_SORT_BY_OPTION);
        MovieUrl = buildMovieUrl(sort_by);
        FetchMoviesTask movieTask = new FetchMoviesTask(mAdapter, mMovies);
        movieTask.execute(MovieUrl);
    }

    private String buildMovieUrl(String sortBy) {
        String url = MOVIEDB_END_POINT
                + sortBy
                + "?page="+ DEFAULT_PAGE
                + "&api_key=" + mApiKey;
        return url;
    }

    /**
     * Get the api key from an external file: api_key.json located in the assets directory.
     * File is not under source code control. It is listed in .gitignore.
     * @return The api key, if found. Otherwise, null.
     */
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

    /**
     * Show fatal error and exit app.
     */
    private void fatalError() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Application Error")
                .setMessage("No api key-app must exit");
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.this.finish();
            }
        });
        builder.show();
    }
}
