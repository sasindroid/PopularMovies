package com.example.android.popularmovies.data;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by sasikumarlakshmanan on 15/03/16.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.popularmovies";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

//    public static final String PATH_POPULAR_MOVIES = "popular";
//    public static final String PATH_TOP_RATED_MOVIES = "top_rated";

    public static final String PATH_MOVIE = "movie";

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = "movies";

        public static final String COLUMN_MOVIE_ID = "movie_id";
        public static final String COLUMN_MOVIE_TITLE = "original_title";
        public static final String COLUMN_POSTER_PATH = "poster_path";
        public static final String COLUMN_OVERVIEW = "overview";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_RATING = "vote_average";
        public static final String COLUMN_BACKDROP_PATH = "backdrop_path";
        public static final String COLUMN_POPULAR_FLAG = "popular_flag";
        public static final String COLUMN_TOP_RATED_FLAG = "top_rated_flag";
        public static final String COLUMN_FAVORITE_FLAG = "favorite_flag";


        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);

        }
    }
}
