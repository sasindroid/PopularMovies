package com.example.android.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by sasikumarlakshmanan on 12/03/16.
 */
public class Utility {

    public static final String BASE_URL = "http://api.themoviedb.org/3/movie/";
    public static final String POPULAR_MOVIES_PATH = "popular";
    public static final String HIGHEST_RATED_MOVIES_PATH = "top_rated";
    public static final String TRAILERS_MOVIE_PATH = "videos";
    public static final String REVIEWS_MOVIE_PATH = "reviews";

    public static final String API_KEY = "keytothemoviedb";
    public static final String API_PARAM = "api_key";

    public static final String SORT_BY_POPULAR_MOVIES = "popular_movies";
    public static final String SORT_BY_HIGHEST_RATED_MOVIES = "highest_rated_movies";
    public static final String SORT_BY_FAVORITE_MOVIES = "favorite_movies";

    public static final String PREF_MOVIES_APP = "PREF_MOVIES_APP";

    public static final String PREF_ITEM_MOVIES = "PREF_ITEM_MOVIES";
    public static final String PREF_ITEM_MOVIES_OBJECT = "PREF_ITEM_MOVIES_OBJECT";
    public static final String PREF_ITEM_MOVIES_OBJECT_JSON = "PREF_ITEM_MOVIES_OBJECT_JSON";
    public static final String STATE_MOVIE_LIST_SAVED = "STATE_MOVIE_LIST_SAVED";
    public static final String STATE_MOVIE_DETAIL_SAVED = "STATE_MOVIE_DETAIL_SAVED";

    public static final String TRAILERS_ARRAY_LIST = "TRAILERS_ARRAY_LIST";
    public static final String REVIEWS_ARRAY_LIST = "REVIEWS_ARRAY_LIST";

    public static void updateMyAppProfileStr(Context context, String key,
                                             String value) {

        SharedPreferences mPrefs;

        mPrefs = context.getSharedPreferences(PREF_MOVIES_APP,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static String getMyAppProfileBool(Context context, String key) {

        SharedPreferences mPrefs;

        mPrefs = context.getSharedPreferences(PREF_MOVIES_APP,
                Context.MODE_PRIVATE);
        return mPrefs.getString(key, null);
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager cm =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
//        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        return activeNetwork != null && activeNetwork.isConnected();
    }

}
