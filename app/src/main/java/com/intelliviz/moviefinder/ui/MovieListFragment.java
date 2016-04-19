package com.intelliviz.moviefinder.ui;

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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
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
    public static final int MOVIE_ITEM_LOADER = 0;
    private static final String COLUMN_SPAN_KEY = "column span key";
    private List<Movie> mMovies = new ArrayList<>();
    private String mMovieUrls;
    private MovieAdapter mPopularAdapter;
    private FavoriteMovieCursorAdapter mFavoriteMovieCursorAdapter;
    private OnSelectMovieListener mListener;
    private String mSortBy;
    private int mSpanCount;

    @Bind(R.id.frameView) FrameLayout mViewSwitcher;
    @Bind(R.id.firstLayoutView) LinearLayout mFavoriteView;
    @Bind(R.id.firstGridView) RecyclerView mFavoriteRecyclerView;
    @Bind(R.id.firstEmptyView) TextView mFavoriteEmptyView;
    @Bind(R.id.secondLayoutView) LinearLayout mPopularView;
    @Bind(R.id.secondGridView) RecyclerView mPopularRecyclerView;
    @Bind(R.id.secondEmptyView) TextView mPopularEmptyView;


    public interface OnSelectMovieListener {

        /**
         * Callback for when a movie is selected from the general list.
         * @param movie The selected movie.
         */
        void onSelectMovie(Movie movie);

        /**
         * Callback for when a movie is selected from the favorite list.
         * @param movie The selected movie.
         */
        void onSelectFavoriteMovie(Movie movie);
    }

    public MovieListFragment() {
        // Required empty public constructor
    }

    public static Fragment newInstance(int spanCount) {
        Bundle args = new Bundle();

        Fragment fragment = new MovieListFragment();

        args.putInt(COLUMN_SPAN_KEY, spanCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_list, container, false);
        ButterKnife.bind(this, view);

        int spanCount = mSpanCount;
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getActivity(), spanCount);

        mPopularAdapter = new MovieAdapter(getActivity(), mMovies);
        mPopularAdapter.setOnSelectMovieListener(mListener);
        mPopularRecyclerView.setLayoutManager(gridLayoutManager);
        mPopularRecyclerView.setAdapter(mPopularAdapter);
        /*
        mPopularRecyclerView.addOnScrollListener(new EndlessOnScrollListener(gridLayoutManager) {
            @Override
            public void onLoadMore(int currentPage) {
                Log.d(TAG, "Loading more...");
                mPopularAdapter.clear();
                mPopularAdapter.notifyDataSetChanged();
                mMovieUrls = ApiKeyMgr.getMoviesUrl(mSortBy, currentPage);
                getMovies();
            }
        });
        */

        mFavoriteMovieCursorAdapter = new FavoriteMovieCursorAdapter(getActivity());
        mFavoriteRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), spanCount));
        mFavoriteRecyclerView.setAdapter(mFavoriteMovieCursorAdapter);
        mFavoriteMovieCursorAdapter.setOnSelectMovieListener(mListener);

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
            if(mPopularAdapter.getItemCount() == 0) {
                mPopularEmptyView.setVisibility(View.VISIBLE);
                mPopularEmptyView.setText("LIST IS EMPTY");
                mPopularRecyclerView.setVisibility(View.GONE);
            }
        }

        ((AppCompatActivity)getActivity()).getSupportActionBar().setSubtitle(getSortedBy(mSortBy));


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

        mSpanCount = getArguments().getInt(COLUMN_SPAN_KEY);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        getLoaderManager().initLoader(MOVIE_ITEM_LOADER, null, this);
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
                                        MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry.COLUMN_RUNTIME,
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
        if(mFavoriteMovieCursorAdapter.getItemCount() == 0) {
            mFavoriteEmptyView.setText("LIST IS EMPTY");
            mFavoriteEmptyView.setVisibility(View.VISIBLE);
            mFavoriteRecyclerView.setVisibility(View.GONE);
        } else {
            mFavoriteEmptyView.setVisibility(View.GONE);
            mFavoriteRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mFavoriteMovieCursorAdapter.swapCursor(null);
    }

    public void refreshList() {
        getLoaderManager().restartLoader(MOVIE_ITEM_LOADER, null, this);
    }


    private void getMovies() {

        if(mSortBy.equals("favorite")) {

        } else {
            if(isNetworkAvailable((AppCompatActivity)getActivity())) {
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
                            //MovieListFragment.this.geta
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

    public static boolean isNetworkAvailable(AppCompatActivity activity) {
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

    private String getSortedBy(String value) {
        String[] sortByOptions = getActivity().getResources().getStringArray(R.array.sort_by_options);
        String[] sortByValues = getActivity().getResources().getStringArray(R.array.sort_by_values);
        for(int i = 0; i < sortByValues.length; i++) {
            if(sortByValues[i].equals(value)) {
                return sortByOptions[i];
            }
        }
        return value;
    }
}
