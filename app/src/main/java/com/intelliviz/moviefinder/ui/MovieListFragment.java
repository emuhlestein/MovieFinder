package com.intelliviz.moviefinder.ui;


import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import com.intelliviz.moviefinder.Movie;
import com.intelliviz.moviefinder.MovieAdapter;
import com.intelliviz.moviefinder.R;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class MovieListFragment extends Fragment
        implements SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = MovieListFragment.class.getSimpleName();
    private static final String API_KEY_KEY = "api_key";
    private static final String DEFAULT_PAGE = "1";
    private static final String DEFAULT_SORT_BY_OPTION = "popular";
    private static final String MOVIEDB_END_POINT = "https://api.themoviedb.org/3/movie/";
    List<Movie> mMovies = new ArrayList<>();
    private String mApiKey;
    private String mMovieUrl;
    private ArrayAdapter<Movie> mAdapter;
    private OnSelectMovieListener mListener;

    @Bind(R.id.grid_view) GridView mGridView;

    public interface OnSelectMovieListener {
        public void onSelectMovie(Movie movie);
    }

    public MovieListFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(String apiKey) {
        Bundle args = new Bundle();

        args.putString(API_KEY_KEY, apiKey);
        Fragment fragment = new MovieListFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_list, container, false);
        ButterKnife.bind(this, view);
        mAdapter = new MovieAdapter(getActivity(), mMovies);
        mGridView.setAdapter(mAdapter);
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = mMovies.get(position);
                String rd = movie.getReleaseDate();
                Log.d(TAG, movie.getTitle());
                mListener.onSelectMovie(movie);
            }
        });

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);

        getMovies();
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // causes onCreateOptionMenu to get called
        setHasOptionsMenu(true);

        mApiKey = getArguments().getString(API_KEY_KEY);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_key = getResources().getString(R.string.pref_sort_by_key);
        String sort_by = sp.getString(sort_key, DEFAULT_SORT_BY_OPTION);
        mMovieUrl = buildMovieUrl(sort_by);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnSelectMovieListener) {
            mListener = (OnSelectMovieListener)context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        String sort_by = sharedPreferences.getString(key, DEFAULT_SORT_BY_OPTION);
        mMovieUrl = buildMovieUrl(sort_by);
        getMovies();
        mAdapter.notifyDataSetChanged();
    }

    private void getMovies() {

        if(isNetworkAvailable()) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(mMovieUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String jsonData = response.body().string();
                    if (response.isSuccessful()) {
                        mMovies = extractMoviesFromJson(jsonData);
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDisplay();
                            }
                        });

                    }
                }
            });
        } else {
            // network is unavailable
        }
    }

    private void updateDisplay() {
        mAdapter.notifyDataSetChanged();
    }

    private String buildMovieUrl(String sortBy) {
        String url = MOVIEDB_END_POINT
                + sortBy
                + "?page="+ DEFAULT_PAGE
                + "&api_key=" + mApiKey;
        return url;
    }

    private boolean isNetworkAvailable() {
        boolean isAvailable = false;
        FragmentActivity activity = this.getActivity();
        if(activity != null) {
            ConnectivityManager manager =
                    (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo networkInfo = manager.getActiveNetworkInfo();


            if (networkInfo != null && networkInfo.isConnected()) {
                isAvailable = true;
            }
        }
        return isAvailable;
    }

    private List<Movie> extractMoviesFromJson(String s) {
        JSONObject moviesObject = null;
        int page = 0;
        try {
            JSONObject oneMovie;
            moviesObject = new JSONObject(s);
            page = moviesObject.getInt("page");
            JSONArray movieArray = moviesObject.getJSONArray("results");
            mMovies.clear();
            Movie movie;
            for(int i = 0; i < movieArray.length(); i++) {
                oneMovie = movieArray.getJSONObject(i);
                movie = extractMovieFromJson(oneMovie);
                if(movie != null) {
                    mMovies.add(movie);
                }
            }

            return mMovies;
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
            String averageVote = object.getString("vote_average");
            Movie movie = new Movie(title, posterPath, overview, id, releaseDate, averageVote);
            return movie;
        } catch (JSONException e) {
            Log.e(TAG, "Error reading movie");
        }

        return null;
    }
}
