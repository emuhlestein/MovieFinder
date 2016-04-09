package com.intelliviz.moviefinder;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.intelliviz.moviefinder.ui.MovieListFragment;
import com.squareup.picasso.Picasso;

/**
 * Created by edm on 4/6/2016.
 */
public class FavoriteMovieCursorAdapter extends RecyclerView.Adapter<FavoriteMovieCursorAdapter.MovieHolder> {
    private Cursor mCursor;
    private Context mContext;
    private MovieListFragment.OnSelectMovieListener mListener;

    public FavoriteMovieCursorAdapter(Context context) {
        mContext = context;
    }

    @Override
    public MovieHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(R.layout.movie_item_layout, parent, false);
        return new MovieHolder(view);
    }

    @Override
    public void onBindViewHolder(MovieHolder holder, int position) {
        if (mCursor == null || !mCursor.moveToPosition(position)) {
            return;
        }

        Movie movie = MovieUtils.extractMovieFromCursor(mCursor);
        holder.bindMovie(movie);

        String url = String.format(ApiKeyMgr.PosterUrl, movie.getPoster());

        if (url != null) {
            Picasso
                    .with(mContext)
                    .load(url)
                    .into((ImageView) holder.itemView);
        }
    }

    @Override
    public int getItemCount() {
        if(mCursor != null) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public void setOnSelectMovieListener (MovieListFragment.OnSelectMovieListener listener) {
        mListener = listener;
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public class MovieHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {
        private Movie mMovie;
        private ImageView mImageView;

        public MovieHolder(View itemView) {
            super(itemView);
            mImageView = (ImageView)itemView;
            mImageView.setOnClickListener(this);
        }

        public Movie getMovie() {
            return mMovie;
        }

        public void bindMovie(Movie movie) {
            mMovie = movie;
        }

        @Override
        public void onClick(View v) {
            if(mListener != null) {
                mListener.onSelectFavoriteMovie(mMovie);
            }
        }
    }
}
