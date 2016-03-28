package com.example.android.popularmovies;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ContentValues;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.android.popularmovies.data.MovieContract;
import com.example.android.popularmovies.model.Review;
import com.example.android.popularmovies.model.Trailer;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * A fragment representing a single Movie detail screen.
 * This fragment is either contained in a {@link MovieListActivity}
 * in two-pane mode (on tablets) or a {@link MovieDetailActivity}
 * on handsets.
 */
public class MovieDetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the item ID that this fragment
     * represents.
     */

    private static final String TAG = "MovieDetailFragment";
    public static final String ARG_ITEM_ID = "item_id";

    private int mMovieID = 0;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    private ImageView ivPoster, ivBackdrop;
    private TextView tvRating, tvYear, tvOverview, tvTitle;
    ToggleButton tbFavorite;
    LinearLayout llTrailers, llReviews, llMovieDetailParent;

    ArrayList<Trailer> mTrailerArrayList;
    ArrayList<Review> mReviewArrayList;

    private static final int MOVIE_DETAIL_LOADER = 1;

    boolean mIsMultiPane = false;

    ShareActionProvider mShareActionProvider;

    String mOriginalTitle;
    String mTrailerAllLinks;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public MovieDetailFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Log.d(TAG, "onCreate");
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        if (mMovieID > 0) {
            inflater.inflate(R.menu.detail_menu, menu);

            MenuItem action_item_share = menu.findItem(R.id.action_item_share);

            // Fetch and store ShareActionProvider
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(action_item_share);
        }

//        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.action_item_share:

                setShareIntent();
                return true;

            default:
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    // Call to update the share intent
    private void setShareIntent() {

        if (mShareActionProvider != null) {

            String shareText = mOriginalTitle + mTrailerAllLinks;

//            Log.d(TAG, "mShareActionProvider shareText: " + shareText);

            if(mTrailerAllLinks == null || mTrailerAllLinks.trim().length() < 1) {
                Toast.makeText(getContext(), R.string.no_trailer_found, Toast.LENGTH_SHORT).show();
                return;
            }

            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareText);
            shareIntent.setType("text/plain");
//                startActivity(Intent.createChooser(sendIntent, getContext().getResources().getString(R.string.share_text)));

            PackageManager pm = getContext().getPackageManager();
            List<ResolveInfo> resolveList = pm.queryIntentActivities(shareIntent, 0);

            if (resolveList.size() == 0) {
                Toast.makeText(getContext(), R.string.cannot_share, Toast.LENGTH_SHORT).show();
            } else {

                mShareActionProvider.setShareIntent(shareIntent);
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getArguments() != null && getArguments().containsKey(ARG_ITEM_ID)) {
            mMovieID = getArguments().getInt(ARG_ITEM_ID);
        }

        View rootView = inflater.inflate(R.layout.movie_detail, container, false);

        ivBackdrop = (ImageView) rootView.findViewById(R.id.ivBackdrop);
        llMovieDetailParent = (LinearLayout) rootView.findViewById(R.id.llMovieDetailParent);

        if (mMovieID > 0) {
            llMovieDetailParent.setVisibility(View.VISIBLE);
        } else {
            llMovieDetailParent.setVisibility(View.GONE);
        }

        if (ivBackdrop == null) {
            mIsMultiPane = false;
            Activity activity = this.getActivity();
            collapsingToolbarLayout = (CollapsingToolbarLayout) activity.findViewById(R.id.toolbar_layout);
        } else {
            mIsMultiPane = true;
        }

        tvTitle = (TextView) rootView.findViewById(R.id.tvTitle);
        ivPoster = (ImageView) rootView.findViewById(R.id.ivPoster);
        tvRating = (TextView) rootView.findViewById(R.id.tvRating);
        tvYear = (TextView) rootView.findViewById(R.id.tvYear);
        tvOverview = (TextView) rootView.findViewById(R.id.tvOverview);
        tbFavorite = (ToggleButton) rootView.findViewById(R.id.tbFavorite);
        llTrailers = (LinearLayout) rootView.findViewById(R.id.llTrailers);
        llReviews = (LinearLayout) rootView.findViewById(R.id.llReviews);

        boolean isPreviousStateExist = savedInstanceState != null ? savedInstanceState.getBoolean(Utility.STATE_MOVIE_DETAIL_SAVED) : false;

        if (isPreviousStateExist) {
            // Don't do a network query.

            mTrailerArrayList = savedInstanceState.getParcelableArrayList(Utility.TRAILERS_ARRAY_LIST);
            mReviewArrayList = savedInstanceState.getParcelableArrayList(Utility.REVIEWS_ARRAY_LIST);

            if (mTrailerArrayList != null) {
                addTrailers(getContext(), mTrailerArrayList);
            }

            if (mReviewArrayList != null) {
                addReviews(getContext(), mReviewArrayList);
            }

        } else {
            fetchMovieDetails();
        }

        return rootView;
    }

    private void fetchMovieDetails() {
        if (Utility.isConnectedToInternet(getContext()) && mMovieID > 0) {
            new FetchMovieTrailersTask(getContext()).execute();
            new FetchMovieReviewsTask(getContext()).execute();
        }
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        getLoaderManager().initLoader(MOVIE_DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);

        Log.d(TAG, "onActivityCreated");
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "onSaveInstanceState");

        // Save the key to retrieve it later with out a network query.
        outState.putBoolean(Utility.STATE_MOVIE_DETAIL_SAVED, true);

        if (mTrailerArrayList != null) {
            outState.putParcelableArrayList(Utility.TRAILERS_ARRAY_LIST, mTrailerArrayList);
        }

        if (mReviewArrayList != null) {
            outState.putParcelableArrayList(Utility.REVIEWS_ARRAY_LIST, mReviewArrayList);
        }
    }

    private void updateFavoriteFlag(boolean fav, int movieID) {
        Uri updateUri = MovieContract.MovieEntry.CONTENT_URI;

        ContentValues cv = new ContentValues();

        cv.put(MovieContract.MovieEntry.COLUMN_FAVORITE_FLAG, fav ? 1 : 0);

        String selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = " + movieID;

        getContext().getContentResolver().update(updateUri, cv, selection, null);
    }

    private String getYearFromDate(String releaseDate) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        try {
            Date date = dateFormat.parse(releaseDate);
            Calendar c = Calendar.getInstance();
            c.setTime(date);
            return String.valueOf(c.get(Calendar.YEAR));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "";
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

        if (mMovieID > 0) {
            String selection = MovieContract.MovieEntry.COLUMN_MOVIE_ID + " = " + mMovieID;
            Uri movieDetailUri = MovieContract.MovieEntry.CONTENT_URI;

            return new android.support.v4.content.CursorLoader(getContext(), movieDetailUri, MovieListFragment.PROJECTION, selection, null, null);
        } else {
            return null;
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
     * @param cursor The data generated by the Loader.
     */
    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor != null && cursor.moveToFirst()) {

            mOriginalTitle = cursor.getString(MovieListFragment.COLUMN_MOVIE_TITLE);
            String posterPath = cursor.getString(MovieListFragment.COLUMN_POSTER_PATH);
            String overview = cursor.getString(MovieListFragment.COLUMN_OVERVIEW);
            String releaseDate = cursor.getString(MovieListFragment.COLUMN_RELEASE_DATE);
            double voteAverage = cursor.getDouble(MovieListFragment.COLUMN_RATING);
            String backdropPath = cursor.getString(MovieListFragment.COLUMN_BACKDROP_PATH);
            int favorite = cursor.getInt(MovieListFragment.COLUMN_FAVORITE_FLAG);

            // Set backdrop image
            if (mIsMultiPane) {
                if (ivBackdrop != null) {

                    Log.d(TAG, "backdropPath: " + backdropPath);
                    Picasso.with(getContext()).load("https://image.tmdb.org/t/p/w342" + backdropPath).into(ivBackdrop);
                }
            } else {
                if (collapsingToolbarLayout != null) {
                    new LoadBackDropImageTask().execute(backdropPath);
                }
            }

            // Set movie title
            tvTitle.setText(mOriginalTitle);

            // Set release date
            tvYear.setText(getYearFromDate(releaseDate));


            // Set Favorite flag & listener
            boolean favoriteFlag = favorite == 1;
            tbFavorite.setChecked(favoriteFlag);

            tbFavorite.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                    // true - Update the db - Mark the movie as favorite
                    // false - Update the db - Un-mark the movie as favorite

                    updateFavoriteFlag(isChecked, mMovieID);
                }
            });


            // Set poster image
            Picasso.with(getContext()).load("https://image.tmdb.org/t/p/w185" + posterPath).into(ivPoster);

            // Set rating
            String rating = String.format(getResources().getString(R.string.rating), String.valueOf(voteAverage));
            tvRating.setText(rating);
            tvOverview.setText(overview);
        }
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

    }

    private class LoadBackDropImageTask extends AsyncTask<String, Void, Drawable> {

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
        protected Drawable doInBackground(String... params) {

            Bitmap bitmap;
            try {
                bitmap = Picasso.with(getContext()).load("https://image.tmdb.org/t/p/w342" + params[0]).get();
                return new BitmapDrawable(getContext().getResources(), bitmap);

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(Drawable drawable) {
            collapsingToolbarLayout.setBackground(drawable);

        }
    }


    /**
     * Created by sasikumarlakshmanan on 18/03/16.
     */
    public class FetchMovieTrailersTask extends AsyncTask<String, Void, ArrayList<Trailer>> {


        private final String LOG_TAG = FetchMovieTrailersTask.class.getSimpleName();

        private final Context mContext;

        public FetchMovieTrailersTask(Context context) {
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
        protected ArrayList<Trailer> doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieTrailersJsonStr;

            Uri builtUri = Uri.parse(Utility.BASE_URL).buildUpon()
                    .appendPath(String.valueOf(mMovieID))
                    .appendPath(Utility.TRAILERS_MOVIE_PATH)
                    .appendQueryParameter(Utility.API_PARAM, Utility.API_KEY).build();


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

                movieTrailersJsonStr = buffer.toString();

                Log.d(LOG_TAG, "JSON Returned: " + movieTrailersJsonStr);

                return getMovieTrailerDataFromJson(movieTrailersJsonStr);

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
        protected void onPostExecute(ArrayList<Trailer> trailerArrayList) {

//            for (Trailer trailer : trailerArrayList) {
//                Log.d("Trailer", trailer.toString());
//            }


            // Save the array list to the member variable.
            mTrailerArrayList = trailerArrayList;

            // Update the Details screen with Trailers
            addTrailers(getContext(), trailerArrayList);
        }
    }

    private ArrayList<Trailer> getMovieTrailerDataFromJson(String movieTrailersJsonStr) throws JSONException {

        final String JSON_RESULTS = "results";

        final String JSON_TRAILER_ID = "id";
        final String JSON_TRAILER_KEY = "key";
        final String JSON_TRAILER_NAME = "name";
        final String JSON_TRAILER_SITE = "site";

        JSONObject forecastJson = new JSONObject(movieTrailersJsonStr);
        JSONArray resultsArray = forecastJson.getJSONArray(JSON_RESULTS);

        ArrayList<Trailer> trailerArrayList = new ArrayList<>();

        for (int i = 0; i < resultsArray.length(); i++) {

            String trailerID, trailerKey, trailerName, trailerSite;

            JSONObject resultObject = resultsArray.getJSONObject(i);

            trailerID = resultObject.getString(JSON_TRAILER_ID);
            trailerKey = resultObject.getString(JSON_TRAILER_KEY);
            trailerName = resultObject.getString(JSON_TRAILER_NAME);
            trailerSite = resultObject.getString(JSON_TRAILER_SITE);

            Trailer trailer = new Trailer(trailerID, trailerKey, trailerName, trailerSite);
            trailerArrayList.add(trailer);
        }

        return trailerArrayList;
    }

    private void addTrailers(final Context context, final ArrayList<Trailer> trailerArrayList) {

        if (trailerArrayList == null || trailerArrayList.size() < 1) {
            return;
        }

        mTrailerAllLinks = "";

        int i = 0;

        for (Trailer trailer : trailerArrayList) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.trailer_layout, null, false);

            view.setPadding(5, 5, 5, 5);

            TextView tvInvisibleValueholder = (TextView) view.findViewById(R.id.tvInvisibleValueholder);
            TextView tvTrailerName = (TextView) view.findViewById(R.id.tvTrailerName);
            ImageView ivTrailerImage = (ImageView) view.findViewById(R.id.ivTrailerImage);

            tvInvisibleValueholder.setText(String.valueOf(i++));
            tvTrailerName.setText(trailer.trailerName);
            String youtubeURL = "http://img.youtube.com/vi/" + trailer.trailerKey + "/0.jpg";

            Log.d("TrailersAdapter", trailerArrayList.size() + " YoutubeURL: " + youtubeURL);

            // 480 x 360
            // http://img.youtube.com/vi/<key from json>/0.jpg
            Picasso.with(context).load(youtubeURL).into(ivTrailerImage);

            mTrailerAllLinks = mTrailerAllLinks
                    + "\n\n" + trailer.trailerName + "\n"
                    + "http://www.youtube.com/watch?v=" + trailer.trailerKey;


            view.setOnClickListener(new View.OnClickListener() {
                /**
                 * Called when a view has been clicked.
                 *
                 * @param v The view that was clicked.
                 */
                @Override
                public void onClick(View v) {


//                    Toast.makeText(context, "clicked: "
//                            + ((TextView) v.findViewById(R.id.tvInvisibleValueholder)).getText().toString(), Toast.LENGTH_SHORT).show();

                    TextView tvInvisibleValueholder = (TextView) v.findViewById(R.id.tvInvisibleValueholder);
                    int index = Integer.valueOf(tvInvisibleValueholder.getText().toString());

                    watchTrailerVideo(trailerArrayList.get(index).trailerKey);

                }
            });

            llTrailers.addView(view);
        }
    }

    private void watchTrailerVideo(String id) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + id));
            startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://www.youtube.com/watch?v=" + id));
            startActivity(intent);
        }
    }


    /**
     * Created by sasikumarlakshmanan on 19/03/16.
     */
    public class FetchMovieReviewsTask extends AsyncTask<String, Void, ArrayList<Review>> {


        private final String LOG_TAG = FetchMovieReviewsTask.class.getSimpleName();

        private final Context mContext;

        public FetchMovieReviewsTask(Context context) {
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
        protected ArrayList<Review> doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String movieReviewsJsonStr;

            Uri builtUri = Uri.parse(Utility.BASE_URL).buildUpon()
                    .appendPath(String.valueOf(mMovieID))
                    .appendPath(Utility.REVIEWS_MOVIE_PATH)
                    .appendQueryParameter(Utility.API_PARAM, Utility.API_KEY).build();

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

                movieReviewsJsonStr = buffer.toString();

                Log.d(LOG_TAG, "JSON Returned: " + movieReviewsJsonStr);

                return getMovieReviewDataFromJson(movieReviewsJsonStr);

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
        protected void onPostExecute(ArrayList<Review> reviewArrayList) {

            // Save the array list to the member variable.
            mReviewArrayList = reviewArrayList;

            // Update the Details screen with Trailers
            addReviews(getContext(), reviewArrayList);
        }
    }

    private ArrayList<Review> getMovieReviewDataFromJson(String movieTrailersJsonStr) throws JSONException {

        final String JSON_RESULTS = "results";

        final String JSON_REVIEW_ID = "id";
        final String JSON_REVIEW_AUTHOR = "author";
        final String JSON_REVIEW_CONTENT = "content";

        JSONObject forecastJson = new JSONObject(movieTrailersJsonStr);
        JSONArray resultsArray = forecastJson.getJSONArray(JSON_RESULTS);

        ArrayList<Review> reviewArrayList = new ArrayList<>();

        for (int i = 0; i < resultsArray.length(); i++) {

            String reviewID;
            String reviewAuthor;
            String reviewContent;

            JSONObject resultObject = resultsArray.getJSONObject(i);

            reviewID = resultObject.getString(JSON_REVIEW_ID);
            reviewAuthor = resultObject.getString(JSON_REVIEW_AUTHOR);
            reviewContent = resultObject.getString(JSON_REVIEW_CONTENT);

            Review review = new Review(reviewID, reviewAuthor, reviewContent);
            reviewArrayList.add(review);
        }

        return reviewArrayList;
    }

    private void addReviews(final Context context, final ArrayList<Review> reviewArrayList) {

        if (reviewArrayList == null) {
            return;
        }

        for (Review review : reviewArrayList) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = inflater.inflate(R.layout.review_layout, null, false);

//            view.setPadding(10, 10, 10, 10);

            TextView tvReviewAuthor = (TextView) view.findViewById(R.id.tvReviewAuthor);
            TextView tvReviewContent = (TextView) view.findViewById(R.id.tvReviewContent);

            tvReviewAuthor.setText(review.reviewAuthor);
            tvReviewContent.setText(review.reviewContent);

            llReviews.addView(view);
        }
    }

    public String getOriginalTitle() {
        return mOriginalTitle;
    }

    public String getTrailerAllLinks() {
        return mTrailerAllLinks;
    }
}
