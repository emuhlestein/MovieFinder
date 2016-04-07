package com.intelliviz.moviefinder;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Adapter for creating views that display images.
 *
 * Created by edm on 3/18/2016.
 */
public class MovieAdapter extends ArrayAdapter<Movie> {
    public static final String TAG = MovieAdapter.class.getSimpleName();
    private Context mContext;
    private LayoutInflater mInflater;

    public MovieAdapter(Context context, List<Movie> movies) {
        super(context, 0, movies);
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            ImageView image = (ImageView) mInflater.inflate(R.layout.movie_item_layout, null);
            convertView = image;
        }

        Movie movie = getItem(position);
        String url = String.format(ApiKeyMgr.PosterUrl, movie.getPoster());

        if (url != null) {
            Picasso
                    .with(mContext)
                    .load(url)
                    .into((ImageView) convertView);
        }

        return convertView;
    }
}
