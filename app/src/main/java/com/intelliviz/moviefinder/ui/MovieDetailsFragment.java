package com.intelliviz.moviefinder.ui;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.intelliviz.moviefinder.ApiKeyMgr;
import com.intelliviz.moviefinder.Movie;
import com.intelliviz.moviefinder.R;
import com.intelliviz.moviefinder.Review;
import com.intelliviz.moviefinder.Trailer;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Details activity. Show the details of a selected movie.
 */
public class MovieDetailsFragment extends Fragment {
    private static final String TAG = MovieDetailsFragment.class.getSimpleName();
    private static final String MOVIE_KEY = "movie_key";
    private static final String FAVORITE_KEY = "favorite_key";
    private Movie mMovie;
    private boolean mIsFavorite;
    private List<Review> mReviews;
    private List<Trailer> mTrailers;
    private String mMovieUrl;
    private TextView[] mReviewViews;
    private OnSelectReviewListener mListener;


    @Bind(R.id.posterView) ImageView mPosterView;
    @Bind(R.id.titleView) TextView mTitleView;
    @Bind(R.id.summaryView) TextView mSummaryView;
    @Bind(R.id.releaseDateView) TextView mReleaseDateView;
    @Bind(R.id.runtimeView) TextView mRuntimeView;
    @Bind(R.id.averageVoteView) TextView mAverageVoteView;
    @Bind(R.id.review_layout) LinearLayout mReviewLayout;
    @Bind(R.id.addToFavoritesButton) Button mAddToFavoriteButton;

    public interface OnSelectReviewListener {
        void onSelectReview(Review review);
        void onSelectTrailer(Trailer trailer);
        void onAddMovieToFavorite(Movie movie);
        void onDeleteMovieFromFavorite(Movie movie);
    }

    public static MovieDetailsFragment newInstance(Movie movie, boolean isFavorite) {
        Bundle args = new Bundle();

        args.putParcelable(MOVIE_KEY, movie);
        args.putBoolean(FAVORITE_KEY, isFavorite);
        MovieDetailsFragment fragment = new MovieDetailsFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_movie_details, container, false);
        ButterKnife.bind(this, view);

        AppCompatActivity activity = (AppCompatActivity)getActivity();
        activity.getSupportActionBar().setDisplayHomeAsUpEnabled(true);



        mTitleView.setText(mMovie.getTitle());
        mSummaryView.setText(mMovie.getSynopsis());
        mReleaseDateView.setText(mMovie.getReleaseDate());

        if(mIsFavorite) {
            String unmarkFavorite = getActivity().getResources().getString(R.string.unmark_favorite);
            mAddToFavoriteButton.setText(unmarkFavorite);
            mAddToFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onDeleteMovieClick(v);
                }
            });
        } else {
            String markFavorite = getActivity().getResources().getString(R.string.mark_as_favorite);
            mAddToFavoriteButton.setText(markFavorite);
            mAddToFavoriteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onAddMovieClick(v);
                }
            });
        }

        String url = String.format(ApiKeyMgr.PosterUrl, mMovie.getPoster());

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

        getMovie();
        getReviews();
        getTrailers();

        //createReviewViews();

        return view;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // causes onCreateOptionMenu to get called
        setHasOptionsMenu(true);

        mMovie = getArguments().getParcelable(MOVIE_KEY);
        mIsFavorite = getArguments().getBoolean(FAVORITE_KEY);

        mMovieUrl = ApiKeyMgr.getMovieUrl(mMovie.getMovieId());

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnSelectReviewListener) {
            mListener = (OnSelectReviewListener)context;
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
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

    public void onAddMovieClick(View view) {
        if(mListener != null) {
            mListener.onAddMovieToFavorite(mMovie);
        }
    }

    public void onDeleteMovieClick(View view) {
        if(mListener != null) {
            mListener.onDeleteMovieFromFavorite(mMovie);
        }
    }

    private void getReviews() {
        String url = ApiKeyMgr.getReviewsUrl(mMovie.getMovieId());
        if(MovieListFragment.isNetworkAvailable(getActivity())) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String jsonData = response.body().string();
                    JSONObject reviewsObject;

                    extractReviewsFromJson(jsonData);

                    if (response.isSuccessful()) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                createReviewViews();
                            }
                        });

                    }
                }
            });
        } else {
            // network is unavailable
        }
    }

    private void getTrailers() {
        String url = ApiKeyMgr.getTrailersUrl(mMovie.getMovieId());

        // TODO put network somewhere else
        if(MovieListFragment.isNetworkAvailable(getActivity())) {
            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {

                }

                @Override
                public void onResponse(Response response) throws IOException {
                    String jsonData = response.body().string();
                    JSONObject trailersObject;

                    extractTrailersFromJson(jsonData);

                    if (response.isSuccessful()) {

                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                createTrailerViews();
                            }
                        });

                    }
                }
            });
        } else {
            // network is unavailable
        }
    }

    private void getMovie() {

        if(MovieListFragment.isNetworkAvailable(getActivity())) {
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
                    JSONObject moviesObject;
                    try {
                        moviesObject = new JSONObject(jsonData);
                        mMovie.setRuntime(moviesObject.getString("runtime"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        return;
                    }
                    if (response.isSuccessful()) {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mRuntimeView.setText(mMovie.getRuntime()+"min");
                            }
                        });
                    }
                }
            });
        } else {
            // network is unavailable
        }
    }

    private List<Review> extractReviewsFromJson(String s) {
        JSONObject reviewsObject = null;
        int page = 0;
        try {
            mReviews = new ArrayList<>();
            JSONObject oneReview;
            reviewsObject = new JSONObject(s);
            page = reviewsObject.getInt("page");
            JSONArray reviewArray = reviewsObject.getJSONArray("results");
            Review review;
            for(int i = 0; i < reviewArray.length(); i++) {
                oneReview = reviewArray.getJSONObject(i);
                review = extractReviewFromJson(oneReview);
                if(review != null) {
                    mReviews.add(review);
                }
            }

            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private List<Review> extractTrailersFromJson(String jsonData) {
        JSONObject trailersObject = null;
        try {
            mTrailers = new ArrayList<>();
            JSONObject oneTrailer;
            trailersObject = new JSONObject(jsonData);
            JSONArray trailerArray = trailersObject.getJSONArray("results");
            Trailer trailer;
            for(int i = 0; i < trailerArray.length(); i++) {
                oneTrailer = trailerArray.getJSONObject(i);
                trailer = extractTrailerFromJson(oneTrailer);
                if(trailer != null) {
                    mTrailers.add(trailer);
                }
            }

            return null;
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    private Trailer extractTrailerFromJson(JSONObject object) {
        try {
            String key = object.getString("key");
            String url = "https://www.youtube.com/watch?v=" + key;
            Trailer trailer = new Trailer(url);
            return trailer;
        } catch (JSONException e) {
            Log.e(TAG, "Error reading trailer");
        }

        return null;
    }

    private Review extractReviewFromJson(JSONObject object) {
        try {
            String author = object.getString("author");
            String content = object.getString("content");
            Review review = new Review(author, content);
            return review;
        } catch (JSONException e) {
            Log.e(TAG, "Error reading review");
        }

        return null;
    }

    private void createReviewViews() {
        if(mReviews == null || mReviews.size() == 0) {
            return;
        }

        for(int i = 0; i < mReviews.size(); i++) {
            View view = createReviewRow(i);
            mReviewLayout.addView(view);
        }
    }

    private View createReviewRow(int num) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // With false as 3rd parameter, view is not added to parent, but the layourparams
        // are those of the parent. View will have the textview. It has to be added
        // manually, so return it.
        Button view = (Button) inflater.inflate(R.layout.review_item_layout, mReviewLayout, false);
        view.setText("Review " + (num+1));
        view.setTag(num);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    int num = (int) v.getTag();
                    mListener.onSelectReview(mReviews.get(num));
                }
            }
        });

        return view;
    }

    private void createTrailerViews() {
        if(mTrailers == null || mTrailers.size() == 0) {
            return;
        }

        for(int i = 0; i < mTrailers.size(); i++) {
            View view = createTrailerRow(i);
            mReviewLayout.addView(view);
        }
    }

    private View createTrailerRow(int num) {
        LayoutInflater inflater = getActivity().getLayoutInflater();

        // With false as 3rd parameter, view is not added to parent, but the layourparams
        // are those of the parent. View will have the textview. It has to be added
        // manually, so return it.
        Button view = (Button) inflater.inflate(R.layout.review_item_layout, mReviewLayout, false);
        view.setText("Trailer " + (num+1));
        view.setTag(num);
        view.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    int num = (int) v.getTag();
                    mListener.onSelectTrailer(mTrailers.get(num));
                }
            }
        });

        return view;
    }
}