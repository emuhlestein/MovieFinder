package com.intelliviz.moviefinder;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
            convertView = mInflater.inflate(R.layout.movie_item_layout, null);
        }

        ImageView image = (ImageView) convertView.findViewById(R.id.image_view);
        //holder.image.setLayoutParams(new GridView.LayoutParams(485, 485));

        Movie movie = getItem(position);
        String url = String.format(MainActivity.PosterUrl, movie.getPoster());

        Picasso
                .with(mContext)
                .load(url)
                .resize(600, 1000)
                .centerCrop()
                .into(image);
        Log.d(TAG, "Loading image: " + url);

        return convertView;
    }
}
