package com.intelliviz.moviefinder.ui;


import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.intelliviz.moviefinder.ApiKeyMgr;
import com.intelliviz.moviefinder.FavoriteMovieCursorAdapter;
import com.intelliviz.moviefinder.Movie;
import com.intelliviz.moviefinder.MovieAdapter;
import com.intelliviz.moviefinder.R;
import com.intelliviz.moviefinder.db.MovieContract;
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
public class MovieListFragment extends Fragment implements
        SharedPreferences.OnSharedPreferenceChangeListener,
        LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = MovieListFragment.class.getSimpleName();
    private static final String DEFAULT_SORT_BY_OPTION = "popular";
    private static final int MOVIE_ITEM_LOADER = 0;
    List<Movie> mMovies = new ArrayList<>();
    private String mMovieUrls;
    private ArrayAdapter<Movie> mPopularAdapter;
    FavoriteMovieCursorAdapter mFavoriteMovieCursorAdapter;
    private OnSelectMovieListener mListener;
    private String mSortBy;

    @Bind(R.id.frameView) FrameLayout mViewSwitcher;
    @Bind(R.id.firstLayoutView) LinearLayout mFavoriteView;
    @Bind(R.id.firstGridView) GridView mFavoriteGridView;
    @Bind(R.id.firstEmptyView) TextView mFavoriteEmptyView;
    @Bind(R.id.secondLayoutView) LinearLayout mPopularView;
    @Bind(R.id.secondGridView) GridView mPopularGridView;
    @Bind(R.id.secondEmptyView) TextView mPopularEmptyView;


    public interface OnSelectMovieListener {
        void onSelectMovie(Movie movie);
        void onSortOnFavorite();
    }

    public MovieListFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance() {
        Fragment fragment = new MovieListFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_list, container, false);
        ButterKnife.bind(this, view);

        mPopularAdapter = new MovieAdapter(getActivity(), mMovies);
        mPopularGridView.setAdapter(mPopularAdapter);
        mPopularGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Movie movie = mMovies.get(position);
                String rd = movie.getReleaseDate();
                Log.d(TAG, movie.getTitle());
                mListener.onSelectMovie(movie);
            }
        });

        mPopularGridView.setEmptyView(mPopularEmptyView);

        mFavoriteMovieCursorAdapter = new FavoriteMovieCursorAdapter(getActivity(), null);
        mFavoriteGridView.setAdapter(mFavoriteMovieCursorAdapter);
        mFavoriteGridView.setEmptyView(mFavoriteEmptyView);

        Log.d(TAG, "Count: " + mViewSwitcher.getChildCount());

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        sp.registerOnSharedPreferenceChangeListener(this);

        String sort_key = getResources().getString(R.string.pref_sort_by_key);
        mSortBy = sp.getString(sort_key, DEFAULT_SORT_BY_OPTION);
        if(mSortBy.equals("favorite")) {
            mFavoriteView.setVisibility(View.VISIBLE);
            mPopularView.setVisibility(View.GONE);
        } else {
            mFavoriteView.setVisibility(View.GONE);
            mPopularView.setVisibility(View.VISIBLE);
        }

        getLoaderManager().initLoader(MOVIE_ITEM_LOADER, null, this);
        getMovies();


        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // causes onCreateOptionMenu to get called
        setHasOptionsMenu(true);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sort_key = getResources().getString(R.string.pref_sort_by_key);
        mSortBy = sp.getString(sort_key, DEFAULT_SORT_BY_OPTION);
        mMovieUrls = ApiKeyMgr.getMoviesUrl(mSortBy);
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mSortBy = sharedPreferences.getString(key, DEFAULT_SORT_BY_OPTION);

        if(mSortBy.equals("favorite")) {
            mFavoriteView.setVisibility(View.VISIBLE);
            mPopularView.setVisibility(View.GONE);
        } else {
            mFavoriteView.setVisibility(View.GONE);
            mPopularView.setVisibility(View.VISIBLE);
            mMovieUrls = ApiKeyMgr.getMoviesUrl(mSortBy);
            getMovies();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int loaderId, Bundle args) {
        Loader<Cursor> loader;
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        switch (loaderId) {
            case MOVIE_ITEM_LOADER:
                loader = new CursorLoader(getActivity(),
                        uri,
                        new String[]
                                {
                                        MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
                                        MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_TITLE,
                                        MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_SYNOPSIS,
                                        MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_RELEASE_DATA,
                                        MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_POSTER,
                                        MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE,
                                        MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_MOVIE_ID,
                                },
                        null,
                        null,
                        null);
                break;
            default:
                loader = null;
        }

        return loader;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mFavoriteMovieCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFavoriteMovieCursorAdapter.swapCursor(null);
    }


    private void getMovies() {

        if(mSortBy.equals("favorite")) {

        } else {
            if(isNetworkAvailable(getActivity())) {
                OkHttpClient client = new OkHttpClient();
                Request request = new Request.Builder()
                        .url(mMovieUrls)
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
                            if(getActivity() == null) {
                                Toast.makeText(getActivity(), "Activity is null", Toast.LENGTH_SHORT).show();
                            }
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
    }

    private void updateDisplay() {
        mPopularAdapter.notifyDataSetChanged();
    }

    public static boolean isNetworkAvailable(Activity activity) {
        boolean isAvailable = false;
        if(activity != null) {
            ConnectivityManager manager =
                    (ConnectivityManager) activity.getSystemService(Context.CONNECTIVITY_SERVICE);
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
