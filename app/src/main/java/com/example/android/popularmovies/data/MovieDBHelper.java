package com.example.android.popularmovies.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by sasikumarlakshmanan on 15/03/16.
 */
public class MovieDBHelper extends SQLiteOpenHelper {

    // If you change the database schema, you must increment the database version.
    private static final int DATABASE_VERSION = 1;

    static final String DATABASE_NAME = "movies.db";

    public MovieDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Called when the database is created for the first time. This is where the
     * creation of tables and the initial population of the tables should happen.
     *
     * @param db The database.
     */
    @Override
    public void onCreate(SQLiteDatabase db) {

        final String SQL_CREATE_MOVIES_TABLE = "CREATE TABLE " + MovieContract.MovieEntry.TABLE_NAME
                + " (" +
                MovieContract.MovieEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                MovieContract.MovieEntry.COLUMN_MOVIE_ID + " INTEGER UNIQUE NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_MOVIE_TITLE + " TEXT NOT NULL, " +
                MovieContract.MovieEntry.COLUMN_POSTER_PATH + " TEXT, " +
                MovieContract.MovieEntry.COLUMN_OVERVIEW + " TEXT, " +
                MovieContract.MovieEntry.COLUMN_RELEASE_DATE + " TEXT, " +
                MovieContract.MovieEntry.COLUMN_RATING + " REAL, " +
                MovieContract.MovieEntry.COLUMN_BACKDROP_PATH + " TEXT, " +
                MovieContract.MovieEntry.COLUMN_POPULAR_FLAG + " INTEGER DEFAULT 0, " +
                MovieContract.MovieEntry.COLUMN_TOP_RATED_FLAG + " INTEGER DEFAULT 0, " +
                MovieContract.MovieEntry.COLUMN_FAVORITE_FLAG + " INTEGER DEFAULT 0);";

        db.execSQL(SQL_CREATE_MOVIES_TABLE);
    }

    /**
     * Called when the database needs to be upgraded. The implementation
     * should use this method to drop tables, add tables, or do anything else it
     * needs to upgrade to the new schema version.
     * <p>
     * <p>
     * The SQLite ALTER TABLE documentation can be found
     * <a href="http://sqlite.org/lang_altertable.html">here</a>. If you add new columns
     * you can use ALTER TABLE to insert them into a live table. If you rename or remove columns
     * you can use ALTER TABLE to rename the old table, then create the new table and then
     * populate the new table with the contents of the old table.
     * </p><p>
     * This method executes within a transaction.  If an exception is thrown, all changes
     * will automatically be rolled back.
     * </p>
     *
     * @param db         The database.
     * @param oldVersion The old database version.
     * @param newVersion The new database version.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + MovieContract.MovieEntry.TABLE_NAME);
        onCreate(db);
    }
}
