package com.intelliviz.moviefinder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import com.intelliviz.moviefinder.ui.MainActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

/**
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

        if(convertView == null) {
            ImageView image = (ImageView) mInflater.inflate(R.layout.movie_item_layout, null);
            image.setLayoutParams(new GridView.LayoutParams(550, 800));
            //image.setAdjustViewBounds(true);
            convertView = image;
        }

        //ImageView image = (ImageView) convertView.findViewById(R.id.image_view);
        //holder.image.setLayoutParams(new GridView.LayoutParams(485, 485));
        Log.d(TAG, "Position: " + position);

        Movie movie = getItem(position);
        String url = String.format(MainActivity.PosterUrl, movie.getPoster());

        Picasso
                .with(mContext)
                .load(url)
                .centerCrop()
                .fit()
                .into((ImageView)convertView);
        Log.d(TAG, "Loading image: " + url);

        return convertView;
    }
}
