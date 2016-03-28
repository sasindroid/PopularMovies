package com.example.android.popularmovies;

import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.CursorAdapter;
import android.widget.GridView;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.model.Movie;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;


/**
 * SasikumarLakshmanan
 */
public class MovieListFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MovieListFragment";

    private GridView mGridView;
    //    private ImageAdapter mImageAdapter;
    private MovieCursorAdapter mMovieCursorAdapter;

    private View view;

    private static final int MOVIE_LOADER = 0;

    public static final String[] PROJECTION = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_ID,
            MovieContract.MovieEntry.COLUMN_MOVIE_TITLE,
            MovieContract.MovieEntry.COLUMN_POSTER_PATH,
            MovieContract.MovieEntry.COLUMN_OVERVIEW,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_BACKDROP_PATH,
            MovieContract.MovieEntry.COLUMN_POPULAR_FLAG,
            MovieContract.MovieEntry.COLUMN_TOP_RATED_FLAG,
            MovieContract.MovieEntry.COLUMN_FAVORITE_FLAG
    };

    public static final int COLUMN_MOVIE_ID = 1;
    public static final int COLUMN_MOVIE_TITLE = 2;
    public static final int COLUMN_POSTER_PATH = 3;
    public static final int COLUMN_OVERVIEW = 4;
    public static final int COLUMN_RELEASE_DATE = 5;
    public static final int COLUMN_RATING = 6;
    public static final int COLUMN_BACKDROP_PATH = 7;
    public static final int COLUMN_POPULAR_FLAG = 8;
    public static final int COLUMN_TOP_RATED_FLAG = 9;
    public static final int COLUMN_FAVORITE_FLAG = 10;


    /**
     * A callback interface that all activities containing this fragment must
     * implement. This mechanism allows activities to be notified of item
     * selections.
     */
    public interface Callback {
        /**
         * Callback from MovieListFragment when an item has been selected.
         */
        void onItemSelected(int movieID);
    }

    public MovieListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment MovieListFragment.
     */
    public static MovieListFragment newInstance(String param1, String param2) {
        MovieListFragment fragment = new MovieListFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // Save the key to retrieve it later with out a network query.
        outState.putBoolean(Utility.STATE_MOVIE_LIST_SAVED, true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.movie_list, container, false);

        Log.d(TAG, "onCreateView");

        setupMoviesGrid();

        boolean isPreviousStateExist = savedInstanceState != null ? savedInstanceState.getBoolean(Utility.STATE_MOVIE_LIST_SAVED) : false;

        if (isPreviousStateExist) {
            // Don't do a network query.
        } else {
            fetchMovies();
        }

        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        Log.d(TAG, "onActivityCreated");

        getLoaderManager().initLoader(MOVIE_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void setupMoviesGrid() {
        mGridView = (GridView) view.findViewById(R.id.movie_list);
//        mImageAdapter = new ImageAdapter(this, null);
        mMovieCursorAdapter = new MovieCursorAdapter(getContext(), null, 0);
        mGridView.setAdapter(mMovieCursorAdapter);

        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Cursor cursor = mMovieCursorAdapter.getCursor();
                cursor.moveToPosition(position);

                // Get the Movie ID out of the cursor.

                int movieID = cursor.getInt(COLUMN_MOVIE_ID);

                ((Callback) getActivity()).onItemSelected(movieID);

//                view.setBackgroundResource(R.drawable.border_gridview);

            }
        });
    }

    private void fetchMovies() {

        // Check if connected to internet.
        if (!Utility.isConnectedToInternet(getContext())) {
            return;
        }

        // Check and display the last used menu option.
        String lastUsedMenu = Utility.getMyAppProfileBool(getContext(), Utility.PREF_ITEM_MOVIES);

        Log.d("MoreListFragment", "querying FETCH lastUsedMenu: " + lastUsedMenu);

        if (lastUsedMenu == null) {
            lastUsedMenu = "";
        }

        switch (lastUsedMenu) {
            case Utility.SORT_BY_POPULAR_MOVIES:
                new FetchMoviesTask(getContext()).execute(Utility.SORT_BY_POPULAR_MOVIES);
                break;

            case Utility.SORT_BY_HIGHEST_RATED_MOVIES:
                new FetchMoviesTask(getContext()).execute(Utility.SORT_BY_HIGHEST_RATED_MOVIES);
                break;

            case Utility.SORT_BY_FAVORITE_MOVIES:
                loadData();
                break;

            default:
                new FetchMoviesTask(getContext()).execute(Utility.SORT_BY_POPULAR_MOVIES);
                break;
        }
    }

    void loadData() {
        getLoaderManager().restartLoader(MOVIE_LOADER, null, this);
    }

    /**
     * Instantiate and return a new Loader for the given ID.
     *
     * @param id   The ID whose loader is to be created.
     * @param args Any arguments supplied by the caller.
     * @return Return a new Loader instance that is ready to start loading.
     */
    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader called");

        String sortOrder = null;
//        sortOrder = MovieContract.MovieEntry.COLUMN_MOVIE_TITLE + " ASC";

        Uri movieUri = MovieContract.MovieEntry.CONTENT_URI;


        // Check and display the last used menu option.
        String lastUsedMenu = Utility.getMyAppProfileBool(getContext(), Utility.PREF_ITEM_MOVIES);

        Log.d("MoreListFragment", "querying LOADER lastUsedMenu: " + lastUsedMenu);

        if (lastUsedMenu == null) {
            lastUsedMenu = "";
        }

        String selection = null;

        switch (lastUsedMenu) {
            case Utility.SORT_BY_POPULAR_MOVIES:

                selection = MovieContract.MovieEntry.COLUMN_POPULAR_FLAG + " = " + 1;
                return new android.support.v4.content.CursorLoader(getContext(), movieUri, PROJECTION, selection, null, sortOrder);

            case Utility.SORT_BY_HIGHEST_RATED_MOVIES:

                selection = MovieContract.MovieEntry.COLUMN_TOP_RATED_FLAG + " = " + 1;
                return new android.support.v4.content.CursorLoader(getContext(), movieUri, PROJECTION, selection, null, sortOrder);

            case Utility.SORT_BY_FAVORITE_MOVIES:

                selection = MovieContract.MovieEntry.COLUMN_FAVORITE_FLAG + " = " + 1;
                return new android.support.v4.content.CursorLoader(getContext(), movieUri, PROJECTION, selection, null, sortOrder);

            default:
                return new android.support.v4.content.CursorLoader(getContext(), movieUri, PROJECTION, selection, null, sortOrder);
        }


    }

    /**
     * Called when a previously created loader has finished its load.  Note
     * that normally an application is <em>not</em> allowed to commit fragment
     * transactions while in this call, since it can happen after an
     * activity's state is saved.  See {@link //FragmentManager#beginTransaction()
     * FragmentManager.openTransaction()} for further discussion on this.
     * <p/>
     * <p>This function is guaranteed to be called prior to the release of
     * the last data that was supplied for this Loader.  At this point
     * you should remove all use of the old data (since it will be released
     * soon), but should not do your own release of the data since its Loader
     * owns it and will take care of that.  The Loader will take care of
     * management of its data so you don't have to.  In particular:
     * <p/>
     * <ul>
     * <li> <p>The Loader will monitor for changes to the data, and report
     * them to you through new calls here.  You should not monitor the
     * data yourself.  For example, if the data is a {@link Cursor}
     * and you place it in a {@link CursorAdapter}, use
     * the {@link CursorAdapter#CursorAdapter(Context,
     * Cursor, int)} constructor <em>without</em> passing
     * in either {@link CursorAdapter#FLAG_AUTO_REQUERY}
     * or {@link CursorAdapter#FLAG_REGISTER_CONTENT_OBSERVER}
     * (that is, use 0 for the flags argument).  This prevents the CursorAdapter
     * from doing its own observing of the Cursor, which is not needed since
     * when a change happens you will get a new Cursor throw another call
     * here.
     * <li> The Loader will release the data once it knows the application
     * is no longer using it.  For example, if the data is
     * a {@link Cursor} from a {@link CursorLoader},
     * you should not call close() on it yourself.  If the Cursor is being placed in a
     * {@link CursorAdapter}, you should use the
     * {@link CursorAdapter#swapCursor(Cursor)}
     * method so that the old Cursor is not closed.
     * </ul>
     *
     * @param loader The Loader that has finished.
     * @param data   The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished called - cursor: " + data.getCount());

        mMovieCursorAdapter.swapCursor(data);
    }

    /**
     * Called when a previously created loader is being reset, and thus
     * making its data unavailable.  The application should at this point
     * remove any references it has to the Loader's data.
     *
     * @param loader The Loader that is being reset.
     */
    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        Log.d(TAG, "onLoaderReset called");

        mMovieCursorAdapter.swapCursor(null);
    }


    /**
     * Created by sasikumarlakshmanan on 17/03/16.
     */
    public class FetchMoviesTask extends AsyncTask<String, Void, ArrayList<Movie>> {


        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private final Context mContext;

        public FetchMoviesTask(Context context) {
            mContext = context;
        }

        /**
         * Override this method to perform a computation on a background thread. The
         * specified parameters are the parameters passed to {@link #execute}
         * by the caller of this task.
         * <p/>
         * This method can call {@link #publishProgress} to publish updates
         * on the UI thread.
         *
         * @param params The parameters of the task.
         * @return A result, defined by the subclass of this task.
         * @see #onPreExecute()
         * @see #onPostExecute
         * @see #publishProgress
         */
        @Override
        protected ArrayList<Movie> doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr;

            Uri builtUri;

            switch (params[0]) {
                case Utility.SORT_BY_POPULAR_MOVIES:
                    builtUri = Uri.parse(Utility.BASE_URL).buildUpon()
                            .appendPath(Utility.POPULAR_MOVIES_PATH)
                            .appendQueryParameter(Utility.API_PARAM, Utility.API_KEY).build();

                    Utility.updateMyAppProfileStr(mContext, Utility.PREF_ITEM_MOVIES, Utility.SORT_BY_POPULAR_MOVIES);
                    break;

                case Utility.SORT_BY_HIGHEST_RATED_MOVIES:
                    builtUri = Uri.parse(Utility.BASE_URL).buildUpon()
                            .appendPath(Utility.HIGHEST_RATED_MOVIES_PATH)
                            .appendQueryParameter(Utility.API_PARAM, Utility.API_KEY).build();

                    Utility.updateMyAppProfileStr(mContext, Utility.PREF_ITEM_MOVIES, Utility.SORT_BY_HIGHEST_RATED_MOVIES);
                    break;

                default:
                    builtUri = Uri.parse(Utility.BASE_URL).buildUpon()
                            .appendPath(Utility.POPULAR_MOVIES_PATH)
                            .appendQueryParameter(Utility.API_PARAM, Utility.API_KEY).build();

                    Utility.updateMyAppProfileStr(mContext, Utility.PREF_ITEM_MOVIES, Utility.SORT_BY_POPULAR_MOVIES);
                    break;
            }

//            Log.d(LOG_TAG, params[0] + ":" + builtUri.toString());

            try {
                URL url = new URL(builtUri.toString());

                // Create the request to themoviesdb, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }

                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;

                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }

                moviesJsonStr = buffer.toString();

                Log.d(LOG_TAG, "JSON Returned");

                return getMoviesDataFromJson(moviesJsonStr, params[0]);

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e(LOG_TAG, "Error closing stream", e);
                    }
                }
            }

            return null;
        }

        @Override
        protected void onPostExecute(ArrayList<Movie> posterArrayList) {

//            if (posterArrayList != null && posterArrayList.size() > 0) {
//                mImageAdapter = new ImageAdapter(getBaseContext(), posterArrayList);
//                mGridView.setAdapter(mImageAdapter);
//            }

            // Check if the Fragment is added to activity to avoid possible exception.
            if (isAdded()) {
                loadData();
            }
        }

        private ArrayList<Movie> getMoviesDataFromJson(String moviesJsonStr, String sort) throws JSONException {

            final String JSON_PAGE = "page";
            final String JSON_RESULTS = "results";

            final String JSON_MOVIEID = "id";
            final String JSON_ORIGINAL_TITLE = "original_title";
            final String JSON_MOVIE_POSTER_THUMBNAIL = "poster_path";
            final String JSON_PLOT_SYNOPSIS = "overview";
            final String JSON_USER_RATING = "vote_average";
            final String JSON_RELEASE_DATE = "release_date";
            final String JSON_BACKDROP_PATH = "backdrop_path";

            JSONObject forecastJson = new JSONObject(moviesJsonStr);
            JSONArray resultsArray = forecastJson.getJSONArray(JSON_RESULTS);

            ArrayList<Movie> posterArrayList = new ArrayList<>();

            for (int i = 0; i < resultsArray.length(); i++) {
                int movieID;
                String originalTitle, posterPath, overview, releaseDate, backdropPath;
                double voteAverage;
                Movie movie;

                JSONObject resultObject = resultsArray.getJSONObject(i);

                movieID = resultObject.getInt(JSON_MOVIEID);
                originalTitle = resultObject.getString(JSON_ORIGINAL_TITLE);
                posterPath = resultObject.getString(JSON_MOVIE_POSTER_THUMBNAIL);
                overview = resultObject.getString(JSON_PLOT_SYNOPSIS);
                releaseDate = resultObject.getString(JSON_RELEASE_DATE);
                voteAverage = resultObject.getDouble(JSON_USER_RATING);
                backdropPath = resultObject.getString(JSON_BACKDROP_PATH);

                movie = new Movie(movieID, originalTitle, posterPath, overview, releaseDate, voteAverage, backdropPath, 0);


                // Add to ContentValues and insert into DB via Content Provider.
                ContentValues cv = new ContentValues();

                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_ID, movieID);
                cv.put(MovieContract.MovieEntry.COLUMN_MOVIE_TITLE, originalTitle);
                cv.put(MovieContract.MovieEntry.COLUMN_POSTER_PATH, posterPath);
                cv.put(MovieContract.MovieEntry.COLUMN_OVERVIEW, overview);
                cv.put(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate);
                cv.put(MovieContract.MovieEntry.COLUMN_RATING, voteAverage);
                cv.put(MovieContract.MovieEntry.COLUMN_BACKDROP_PATH, backdropPath);

                switch (sort) {
                    case Utility.SORT_BY_POPULAR_MOVIES:
                        cv.put(MovieContract.MovieEntry.COLUMN_POPULAR_FLAG, 1);
                        break;

                    case Utility.SORT_BY_HIGHEST_RATED_MOVIES:
                        cv.put(MovieContract.MovieEntry.COLUMN_TOP_RATED_FLAG, 1);
                        break;

                    default:

                        break;
                }

                // Insert this record now.
                mContext.getContentResolver().insert(MovieContract.MovieEntry.CONTENT_URI, cv);

                // Add to array list.
                posterArrayList.add(movie);
            }

            return posterArrayList;
        }
    }

    public void fetchPopularMovies() {
        new FetchMoviesTask(getContext()).execute(Utility.SORT_BY_POPULAR_MOVIES);
    }

    public void fetchTopRatedMovies() {
        new FetchMoviesTask(getContext()).execute(Utility.SORT_BY_HIGHEST_RATED_MOVIES);
    }
}
