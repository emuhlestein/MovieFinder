package com.intelliviz.moviefinder.ui;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.intelliviz.moviefinder.ApiKeyMgr;
import com.intelliviz.moviefinder.R;
import com.intelliviz.moviefinder.db.MovieContract;
import com.squareup.picasso.Picasso;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * A simple {@link Fragment} subclass.
 */
public class FavoriteMovieFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>{
    private static final int MOVIE_ITEM_LOADER = 0;
    private FavoriteMovieCursorAdapter mAdapter;

    @Bind(R.id.grid_view) GridView mGridView;

    public static Fragment newInstance() {
        Fragment fragment = new FavoriteMovieFragment();
        return fragment;
    }

    public FavoriteMovieFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_favorite_movie, container, false);
        ButterKnife.bind(this, view);

        mAdapter = new FavoriteMovieCursorAdapter(getActivity(), null);
        mGridView.setAdapter(mAdapter);

        getLoaderManager().initLoader(MOVIE_ITEM_LOADER, null, this);

        return view;
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
        mAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }

    private class FavoriteMovieCursorAdapter extends CursorAdapter {
        private LayoutInflater mInflater;

        public FavoriteMovieCursorAdapter(Context context, Cursor c) {
            super(context, c);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            View view = mInflater.inflate(R.layout.movie_item_layout, parent, false);
            return view;
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            ImageView imageView = (ImageView) view.findViewById(R.id.image_view);
            int index = cursor.getColumnIndex(MovieContract.MovieEntry.COLUMN_POSTER);

            String poster = cursor.getString(index);


            String url = String.format(ApiKeyMgr.PosterUrl, poster);

            if (url != null) {
                Picasso
                        .with(context)
                        .load(url)
                        .into(imageView);
            }
        }
    }
}
