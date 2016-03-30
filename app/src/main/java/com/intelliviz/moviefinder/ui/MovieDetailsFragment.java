package com.intelliviz.moviefinder.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.intelliviz.moviefinder.Movie;
import com.intelliviz.moviefinder.R;
import com.squareup.picasso.Picasso;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Details activity. Show the details of a selected movie.
 */
public class MovieDetailsFragment extends Fragment {
    private static final String TAG = MovieDetailsFragment.class.getSimpleName();
    private static final String MOVIE_KEY = "movie_key";
    private Movie mMovie;

    @Bind(R.id.posterView) ImageView mPosterView;
    @Bind(R.id.titleView) TextView mTitleView;
    @Bind(R.id.summaryView) TextView mSummaryView;
    @Bind(R.id.releaseDateView) TextView mReleaseDateView;
    @Bind(R.id.averageVoteView) TextView mAverageVoteView;

    public static MovieDetailsFragment newInstance(Movie movie) {
        Bundle args = new Bundle();

        args.putParcelable(MOVIE_KEY, movie);
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.activity_movie_details, container, false);
        ButterKnife.bind(this, view);

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mTitleView.setText(mMovie.getTitle());
        mSummaryView.setText(mMovie.getSynopsis());
        mReleaseDateView.setText(mMovie.getReleaseDate());

        String url = String.format(MainActivity.PosterUrl, mMovie.getPoster());

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd");
        String str = mMovie.getReleaseDate();
        try {
            Date date = formatter.parse(mMovie.getReleaseDate());

            formatter = new SimpleDateFormat("yyyy");
            str = formatter.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        mReleaseDateView.setText(str);
        mAverageVoteView.setText(new DecimalFormat("#.#").format(Float.parseFloat(mMovie.getAverageVote())) + "/10");

        Picasso
                .with(getActivity())
                .load(url)
                .into(mPosterView);

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        //inflater.inflate(R.menu.dir_list_fragment_menu, menu);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // causes onCreateOptionMenu to get called
        setHasOptionsMenu(true);

        mMovie = getArguments().getParcelable(MOVIE_KEY);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                FragmentManager fm = getFragmentManager();
                fm.popBackStack();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
