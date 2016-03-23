package com.intelliviz.moviefinder;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by edm on 3/18/2016.
 */
// TODO instead of movie array, use an arraylist
public class FetchMovieTask extends AsyncTask<String, Void, Movie> {
    public static final String TAG = FetchMoviesTask.class.getSimpleName();
    private ArrayAdapter<Movie> mMovieAdapter;
    private List<Movie> mMovies;

    public FetchMovieTask(ArrayAdapter<Movie> adapter, List<Movie> movies) {
        mMovieAdapter = adapter;
        mMovies = movies;
    }

    @Override
    protected Movie doInBackground(String... urls) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        StringBuffer buffer = new StringBuffer();
        Movie movie = new Movie();

        try {
            URL url = new URL(urls[0]);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();

            if (inputStream == null) {
                // Nothing to do.
                return null;
            }

            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while((line = reader.readLine()) != null) {
                buffer.append(line+"\n");
            }
            //movies = extractMoviesFromJson(buffer.toString());
        } catch (IOException e) {
            Log.e(TAG, "Error accessing internet");
        } finally {
            if(urlConnection != null) {
                urlConnection.disconnect();
            }

            if(reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing stream");
                }
            }
        }

        return movie;
    }

    private List<Movie> extractMoviesFromJson(String s) {
        JSONObject moviesObject = null;
        int page = 0;
        try {
            JSONObject oneMovie;
            moviesObject = new JSONObject(s);
            page = moviesObject.getInt("page");
            JSONArray movieArray = moviesObject.getJSONArray("results");
            List<Movie> movieList = new ArrayList<>();
            Movie movie;
            for(int i = 0; i < movieArray.length(); i++) {
                oneMovie = movieArray.getJSONObject(i);
                movie = extractMovieFromJson(oneMovie);
                if(movie != null) {
                    movieList.add(movie);
                }
            }

            return movieList;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private Movie extractMovieFromJson(JSONObject object) {
        try {
            String posterPath = object.getString("poster_path");
            String overview = object.getString("overview");
            String releaseDate = object.getString("release_date");
            String id = object.getString("id");
            String title = object.getString("title");
            Movie movie = new Movie(title, posterPath, overview, id, releaseDate, "");
            return movie;
        } catch (JSONException e) {
            Log.e(TAG, "Error reading movie");
        }

        return null;
    }

    @Override
    protected void onPostExecute(Movie movie) {
    }
}
