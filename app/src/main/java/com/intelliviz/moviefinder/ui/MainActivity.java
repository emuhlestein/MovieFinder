package com.intelliviz.moviefinder.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.intelliviz.moviefinder.ApiKeyMgr;
import com.intelliviz.moviefinder.Movie;
import com.intelliviz.moviefinder.MovieUtils;
import com.intelliviz.moviefinder.R;
import com.intelliviz.moviefinder.Review;
import com.intelliviz.moviefinder.Trailer;
import com.intelliviz.moviefinder.db.MovieContract;

import java.util.ArrayList;
import java.util.List;

/**
 * Main activity for movie app
 */
public class MainActivity extends AppCompatActivity implements
        MovieListFragment.OnSelectMovieListener,
        MovieDetailsFragment.OnSelectReviewListener,
        SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final int DETAILS_ACTIVITY = 0;
    private static final String DETAIL_FRAG_TAG = "detail frag tag";
    private static final String LIST_FRAG_TAG = "list frag tag";
    private static final String API_KEY_NOT_SET = "api key not set";
    private static final String MOVIE_LIST_KEY = "movie_list_key";
    private boolean mIsTablet;
    public static final String MOVIE_EXTRA = "movie_info";
    private ArrayList<Movie> mMovies = new ArrayList<>();
    private String API_KEY = null; // Put api key here;
    ArrayAdapter<Movie> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment);

        if(!ApiKeyMgr.checkApiKey(this, API_KEY)) {
            fatalError();
        }

        FragmentManager fm = getSupportFragmentManager();
        Fragment fragment;
        View detailsView = findViewById(R.id.details_fragment);
        if(detailsView == null){
            fragment = MovieListFragment.newInstance(2);
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_holder, fragment, LIST_FRAG_TAG);
            ft.commit();
            mIsTablet = false;
        } else {
            fragment = MovieListFragment.newInstance(4);
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_holder, fragment, LIST_FRAG_TAG);
            fragment = MovieDetailsFragment.newInstance(null, null, false);
            ft.add(R.id.details_fragment, fragment, DETAIL_FRAG_TAG);
            ft.commit();
            mIsTablet = true;

            SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
            sp.registerOnSharedPreferenceChangeListener(this);
        }
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
            SettingsFragment fragment = SettingsFragment.newInstance();
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_holder, fragment);
            ft.addToBackStack(null);
            ft.commit();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putParcelableArrayList(MOVIE_LIST_KEY, mMovies);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onSelectMovie(Movie movie) {

        if(mIsTablet) {
            MovieDetailsFragment detailsFragment =  ((MovieDetailsFragment)getSupportFragmentManager()
                    .findFragmentByTag(DETAIL_FRAG_TAG));
            if(detailsFragment != null) {
                detailsFragment.updateMovie(movie);
            }
        } else {
            Intent intent = new Intent(this, MovieDetailsActivity.class);
            intent.putExtra(MovieDetailsActivity.MOVIE_EXTRA, movie);
            intent.putExtra(MovieDetailsActivity.FAVORITE_EXTRA, false);
            startActivity(intent);
        }
    }

    @Override
    public void onSelectFavoriteMovie(Movie movie) {
        ArrayList<Review> reviews = getReviews(movie);
        if(mIsTablet) {
            Fragment fragment = MovieDetailsFragment.newInstance(movie, reviews, true);
            FragmentManager fm = getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();
            ft.replace(R.id.fragment_holder, fragment);
            ft.addToBackStack(null);
            ft.commit();
        } else {
            Intent intent = new Intent(this, MovieDetailsActivity.class);

            intent.putExtra(MovieDetailsActivity.MOVIE_EXTRA, movie);
            intent.putParcelableArrayListExtra(MovieDetailsActivity.REVIEWS_EXTRA, reviews);
            intent.putExtra(MovieDetailsActivity.FAVORITE_EXTRA, true);
            startActivityForResult(intent, DETAILS_ACTIVITY);
        }
    }

    @Override
    public void onSelectReview(Review review) {
        Fragment fragment = MovieReviewFragment.newInstance(review);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_holder, fragment);
        ft.addToBackStack(null);
        ft.commit();
    }

    @Override
    public void onSelectTrailer(Trailer trailer) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(trailer.getUrl()));
        startActivity(intent);
    }

    @Override
    public void onAddMovieToFavorite(Movie movie, List<Review> reviews) {
        MovieUtils.addMovieToFavorite(this, movie, reviews);
    }

    @Override
    public void onDeleteMovieFromFavorite(Movie movie) {
        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        uri = Uri.withAppendedPath(uri, "" + movie.getId());
        int numRows = getContentResolver().delete(uri, null, null);
        MovieListFragment movieListFragment =  ((MovieListFragment)getSupportFragmentManager()
                .findFragmentByTag(LIST_FRAG_TAG));
        if(movieListFragment != null) {
            movieListFragment.refreshList();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == DETAILS_ACTIVITY) {
            if(data == null) {
                return;
            }

            Movie movie = data.getParcelableExtra("movie_to_delete");
            if(movie != null) {
                onDeleteMovieFromFavorite(movie);
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if(mIsTablet) {
            MovieDetailsFragment detailsFragment =  ((MovieDetailsFragment)getSupportFragmentManager()
                    .findFragmentByTag(DETAIL_FRAG_TAG));
            if(detailsFragment != null) {
                detailsFragment.updateMovie(null);
            }
        }
    }

    /**
     * Get all the reviews for the specified movie.
     * @param movie The movie.
     * @return The list of reviews.
     */
    private ArrayList<Review> getReviews(Movie movie) {
        ArrayList<Review> reviews = new  ArrayList<>();
        Uri uri = MovieContract.ReviewEntry.CONTENT_URI;

        String[] projection = {MovieContract.ReviewEntry.COLUMN_CONTENT, MovieContract.ReviewEntry.COLUMN_AUTHOR, MovieContract.ReviewEntry.COLUMN_MOVIE_ID};
        Cursor cursor = getContentResolver().query(uri, projection, null, null, null);
        while(cursor.moveToNext()) {
            int contentIndex = cursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_CONTENT);
            int authorIndex = cursor.getColumnIndex(MovieContract.ReviewEntry.COLUMN_AUTHOR);
            String content = cursor.getString(contentIndex);
            String author = cursor.getString(authorIndex);
            Review review = new Review("", author, content);
            reviews.add(review);
        }

        return reviews;
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
