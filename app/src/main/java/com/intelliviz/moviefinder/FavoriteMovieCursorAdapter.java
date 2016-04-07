package com.intelliviz.moviefinder;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.intelliviz.moviefinder.db.MovieContract;
import com.squareup.picasso.Picasso;

/**
 * Created by edm on 4/6/2016.
 */
public class FavoriteMovieCursorAdapter extends CursorAdapter {
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
