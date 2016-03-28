package com.example.android.popularmovies.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteConstraintException;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;

/**
 * Created by sasikumarlakshmanan on 15/03/16.
 */

public class MovieContentProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDBHelper mOpenHelper;

    static final int MOVIE = 100;

    public MovieContentProvider() {
    }

    @Override
    public boolean onCreate() {
        // Implement this to initialize your content provider on startup.
        mOpenHelper = new MovieDBHelper(getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        // Implement this to handle requests for the MIME type of the data
        // at the given URI.

        // Use the Uri Matcher to determine what kind of URI this is.
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        // Implement this to handle requests to insert a new row.

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE:
//                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);

                // In case of a conflict replace it.
                long _id = 0;
                try {
                    _id = db.insertWithOnConflict(MovieContract.MovieEntry.TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_ABORT);
                } catch (SQLiteConstraintException e) {
                    db.update(MovieContract.MovieEntry.TABLE_NAME, values,
                            MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = " + values.get(MovieContract.MovieEntry.COLUMN_MOVIE_ID), null);
                }

                if (_id > -1) {
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                } else {
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                }

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Implement this to handle requests to delete one or more rows.

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;

        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";

        switch (match) {
            case MOVIE:
                rowsDeleted = db.delete(
                        MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection,
                        String[] selectionArgs, String sortOrder) {
        // Implement this to handle query requests from clients.
        final SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        final int match = sUriMatcher.match(uri);
        Cursor retCursor;

        switch (match) {
            case MOVIE:
                retCursor = db.query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        return retCursor;

    }

    @Override
    public int update(Uri uri, ContentValues values, String selection,
                      String[] selectionArgs) {
        // Implement this to handle requests to update one or more rows.

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                rowsUpdated = db.update(
                        MovieContract.MovieEntry.TABLE_NAME, values, selection, selectionArgs);

                break;

            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }

        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {

        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE:
                db.beginTransaction();
                int returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }

                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;

            default:
                return super.bulkInsert(uri, values);
        }

    }

    static UriMatcher buildUriMatcher() {

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // For each type of URI create a corresponding code.
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);

        return matcher;
    }
}
