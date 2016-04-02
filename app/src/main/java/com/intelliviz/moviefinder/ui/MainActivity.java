package com.intelliviz.moviefinder.ui;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ArrayAdapter;

import com.intelliviz.moviefinder.ApiKeyMgr;
import com.intelliviz.moviefinder.Movie;
import com.intelliviz.moviefinder.R;
import com.intelliviz.moviefinder.Review;

import java.util.ArrayList;

/**
 * Main activity for movie app
 */
public class MainActivity extends AppCompatActivity
        implements MovieListFragment.OnSelectMovieListener,
        MovieDetailsFragment.OnSelectReviewListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String API_KEY_NOT_SET = "api key not set";


    private static final String MOVIE_LIST_KEY = "movie_list_key";
    public String MovieUrl;
    public static final String PosterUrl = "http://image.tmdb.org/t/p/w185%s";
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
        Fragment fragment = fm.findFragmentById(R.id.fragment_holder);

        if(fragment == null) {
            fragment = MovieListFragment.newInstance();
            FragmentTransaction ft = fm.beginTransaction();
            ft.add(R.id.fragment_holder, fragment);
            ft.addToBackStack(null);
            ft.commit();
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
        Fragment fragment = MovieDetailsFragment.newInstance(movie);
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction ft = fm.beginTransaction();
        ft.replace(R.id.fragment_holder, fragment);
        ft.addToBackStack(null);
        ft.commit();
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

    /**
     * Apparently this has to be here so that the last transactions are popped off at once.
     */
    @Override
    public void onBackPressed() {
        FragmentManager fm;
        fm = getSupportFragmentManager();

        int count = fm.getBackStackEntryCount();
        if(count > 0) {
            // popBackStack is asynchronous -- it enqueues the request to pop, but the action will
            // not be performed until the application returns to its event loop. -Android Docs.
            fm.popBackStack();
            if(count == 1) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
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
