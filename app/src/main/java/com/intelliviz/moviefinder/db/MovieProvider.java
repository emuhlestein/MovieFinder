package com.intelliviz.moviefinder.db;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * Created by edm on 4/4/2016.
 */
public class MovieProvider extends ContentProvider {
    private SqliteHelper mSqliteHelper;
    private static final String DBASE_NAME = "movies";
    private static final int DBASE_VERSION = 5;
    private static final int MOVIE_LIST = 101;
    private static final int MOVIE_ID = 102;
    private static final int REVIEW_LIST = 201;
    private static final int REVIEW_ID = 202;


    private static UriMatcher sUriMatcher;

    static {
        sUriMatcher = new UriMatcher((UriMatcher.NO_MATCH));

        // all movies
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE, MOVIE_LIST);

        // a particular movie
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_MOVIE + "/#", MOVIE_ID);

        // all reviews
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEW, REVIEW_LIST);

        // a particular review
        sUriMatcher.addURI(MovieContract.CONTENT_AUTHORITY, MovieContract.PATH_REVIEW + "/#", REVIEW_ID);
    }

    @Override
    public boolean onCreate() {
        Context context = getContext();
        mSqliteHelper = new SqliteHelper(context);
        return true;
    }

    @Nullable
    @Override
    public String getType(Uri uri) {
        switch(sUriMatcher.match(uri)) {
            case MOVIE_LIST:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case REVIEW_LIST:
                return MovieContract.ReviewEntry.CONTENT_TYPE;
            case REVIEW_ID:
                return MovieContract.ReviewEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("Unknown uri");
        }
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteQueryBuilder sqLiteQueryBuilder = new SQLiteQueryBuilder();
        switch(sUriMatcher.match(uri)) {
            case MOVIE_LIST:
                // get all movies: "movie/"
                sqLiteQueryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME);
                break;
            case MOVIE_ID:
                // get a particular movie: "movie/#"
                sqLiteQueryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MovieContract.MovieEntry.COLUMN_MOVIE_ID +
                        "=" + uri.getLastPathSegment());
                break;
            case REVIEW_LIST:
                // get all reviews: "review/"
                sqLiteQueryBuilder.setTables(MovieContract.ReviewEntry.TABLE_NAME);
                break;
            case REVIEW_ID:
                // get a particular review: "review/#"
                sqLiteQueryBuilder.setTables(MovieContract.ReviewEntry.TABLE_NAME);
                sqLiteQueryBuilder.appendWhere(MovieContract.MovieEntry.COLUMN_MOVIE_ID +
                        "=" + uri.getLastPathSegment());
                break;
        }

        SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
        Cursor cursor = sqLiteQueryBuilder.query(db, projection, selection, selectionArgs, null, null, sortOrder);

        int count = cursor.getCount();

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        long rowId;
        SQLiteDatabase db;
        Uri returnUri;

        db = mSqliteHelper.getWritableDatabase();

        switch(sUriMatcher.match(uri)) {
            case MOVIE_LIST:
                // The second parameter will allow an empty row to be inserted. If it was null, then no row
                // can be inserted if values is empty.
                rowId = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (rowId > -1) {
                    returnUri = ContentUris.withAppendedId(uri, rowId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            case REVIEW_LIST:
                // The second parameter will allow an empty row to be inserted. If it was null, then no row
                // can be inserted if values is empty.
                rowId = db.insert(MovieContract.ReviewEntry.TABLE_NAME, null, values);
                if (rowId > -1) {
                    returnUri = ContentUris.withAppendedId(uri, rowId);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown uri");
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
        int rowsDeleted = 0;
        String id;

        switch(sUriMatcher.match(uri)) {
            case MOVIE_LIST:
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case MOVIE_ID:
                id = uri.getLastPathSegment();
                rowsDeleted = db.delete(MovieContract.MovieEntry.TABLE_NAME,
                        MovieContract.MovieEntry._ID + "=" + id, null);
                break;
            case REVIEW_LIST:
                rowsDeleted = db.delete(MovieContract.ReviewEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case REVIEW_ID:
                id = uri.getLastPathSegment();
                rowsDeleted = db.delete(MovieContract.ReviewEntry.TABLE_NAME,
                        MovieContract.ReviewEntry._ID + "=" + id, null);
                break;
            default:
                throw new IllegalArgumentException("Unknown uri");
        }
        return rowsDeleted;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mSqliteHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated = 0;
        String id;

        switch(sUriMatcher.match(uri)) {
            case MOVIE_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME,
                            values,
                            MovieContract.MovieEntry._ID + "=?",
                            new String[]{id});
                } else {
                    rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME,
                            values,
                            MovieContract.MovieEntry._ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            case REVIEW_ID:
                id = uri.getLastPathSegment();
                if (TextUtils.isEmpty(selection)) {
                    rowsUpdated = db.update(MovieContract.ReviewEntry.TABLE_NAME,
                            values,
                            MovieContract.ReviewEntry._ID + "=?",
                            new String[]{id});
                } else {
                    rowsUpdated = db.update(MovieContract.ReviewEntry.TABLE_NAME,
                            values,
                            MovieContract.ReviewEntry._ID + "=" + id
                                    + " and "
                                    + selection,
                            selectionArgs);
                }
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    private static class SqliteHelper extends SQLiteOpenHelper {

        public SqliteHelper(Context context) {
            super(context, DBASE_NAME, null, DBASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            // create the category table
            String sql = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME +
                    " ( " + MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MovieContract.MovieEntry.COLUMN_MOVIE_ID + " TEXT NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_AVERAGE_VOTE + " TEXT NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_POSTER + " TEXT NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_RELEASE_DATA + " TEXT NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_RUNTIME + " TEXT NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_SYNOPSIS + " TEXT NOT NULL, " +
                    MovieContract.MovieEntry.COLUMN_TITLE + " TEXT NOT NULL);";

            db.execSQL(sql);

            // create the category version table
            sql = "CREATE TABLE " + MovieContract.ReviewEntry.TABLE_NAME +
                    " ( " + MovieContract.ReviewEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    MovieContract.ReviewEntry.COLUMN_CONTENT + " TEXT NOT NULL, " +
                    MovieContract.ReviewEntry.COLUMN_AUTHOR + " TEXT NOT NULL);";

            db.execSQL(sql);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
            db.execSQL("DROP TABLE IF EXISTS " + MovieContract.ReviewEntry.TABLE_NAME);
            onCreate(db);
        }
    }
}
