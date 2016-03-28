package com.example.android.popularmovies;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.example.android.popularmovies.model.Movie;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

/**
 * An activity representing a list of Movies. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link MovieDetailActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class MovieListActivity extends AppCompatActivity implements MovieListFragment.Callback {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */
    private boolean mTwoPane;
    MovieListFragment mMovieListFragment;
    MovieDetailFragment mMovieDetailFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_movie_list);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        mMovieListFragment = (MovieListFragment) getSupportFragmentManager().findFragmentByTag(this.getString(R.string.tag_fragment_movie_list));

        if (savedInstanceState == null || mMovieListFragment == null) {

            // Check if connected to internet.
            CoordinatorLayout clMovieList = (CoordinatorLayout) findViewById(R.id.clMovieList);

            if (!Utility.isConnectedToInternet(this)) {
                Snackbar.make(clMovieList, R.string.no_connectivity,
                        Snackbar.LENGTH_LONG).show();
            }
        }

        if (findViewById(R.id.movie_detail_container) != null) {
            // The detail container view will be present only in the
            // large-screen layouts (res/values-w900dp).
            // If this view is present, then the
            // activity should be in two-pane mode.
            mTwoPane = true;

            if (savedInstanceState == null) {
                mMovieDetailFragment = new MovieDetailFragment();

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.movie_detail_container, mMovieDetailFragment,
                                getResources().getString(R.string.tag_fragment_movie_detail))
                        .commit();
            }

        } else {
            mTwoPane = false;
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        MenuItem action_most_popular = menu.findItem(R.id.action_most_popular);
        MenuItem action_highest_rated = menu.findItem(R.id.action_highest_rated);
        MenuItem action_favorite = menu.findItem(R.id.action_favorite);

        // Get the checked item status from Prefs.
        String chooseItem = Utility.getMyAppProfileBool(this, Utility.PREF_ITEM_MOVIES);

        if (chooseItem == null) {
            chooseItem = "";
        }

        switch (chooseItem) {
            case Utility.SORT_BY_POPULAR_MOVIES:
                action_most_popular.setChecked(true);
                break;

            case Utility.SORT_BY_HIGHEST_RATED_MOVIES:
                action_highest_rated.setChecked(true);
                break;

            case Utility.SORT_BY_FAVORITE_MOVIES:
                action_favorite.setChecked(true);
                break;

            default:
                action_most_popular.setChecked(true);
                break;
        }

//        MenuItem action_item_share = menu.findItem(R.id.action_item_share);
//
//        if (mTwoPane) {
//            action_item_share.setVisible(true);
//
//            // Fetch and store ShareActionProvider
//            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(action_item_share);
//        } else {
//            action_item_share.setVisible(false);
//        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (id) {
            case R.id.action_most_popular:
                item.setChecked(true);

                // Clear the Detail Fragment on selection on Two pane mode.
                if (mTwoPane) {
                    clearDetailFragmentOnSelection();
                }

                mMovieListFragment.fetchPopularMovies();
                return true;

            case R.id.action_highest_rated:
                item.setChecked(true);

                // Clear the Detail Fragment on selection on Two pane mode.
                if (mTwoPane) {
                    clearDetailFragmentOnSelection();
                }

                mMovieListFragment.fetchTopRatedMovies();
                return true;

            case R.id.action_favorite:
                item.setChecked(true);

                // Clear the Detail Fragment on selection on Two pane mode.
                if (mTwoPane) {
                    clearDetailFragmentOnSelection();
                }

                Utility.updateMyAppProfileStr(this, Utility.PREF_ITEM_MOVIES, Utility.SORT_BY_FAVORITE_MOVIES);

                mMovieListFragment.loadData();
                return true;

            default:
                break;
        }


        // Used for my internal testing.
//        else if (id == R.id.action_delete) {
//
//            Uri deleteUri = MovieContract.MovieEntry.CONTENT_URI;
//
//            int rowsDeleted = getContentResolver().delete(deleteUri, null, null);
//            mMovieListFragment.loadData();
//
//            Log.d("MovieListActivity", "Rows Deleted: " + rowsDeleted);
//
//            return true;
//        }

        return super.

                onOptionsItemSelected(item);

    }

    private void clearDetailFragmentOnSelection() {

        // Clear the Detail fragment on selection.
        Bundle arguments = new Bundle();
        arguments.putInt(MovieDetailFragment.ARG_ITEM_ID, 0);

        MovieDetailFragment fragment = new MovieDetailFragment();
        fragment.setArguments(arguments);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.movie_detail_container, fragment, getResources().getString(R.string.tag_fragment_movie_detail))
                .commit();

    }

    // Not used
    private void saveMovieArray(ArrayList<Movie> movies) {
        SharedPreferences mPrefs = this.getSharedPreferences(Utility.PREF_MOVIES_APP,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();

        Gson gson = new Gson();
        String moviesJson = gson.toJson(movies);

        editor.putString(Utility.PREF_ITEM_MOVIES_OBJECT_JSON, moviesJson);
        editor.apply();
    }

    // Not used
    private ArrayList<Movie> retrieveMovieArray() {

        ArrayList<Movie> movies = null;

        SharedPreferences mPrefs = this.getSharedPreferences(Utility.PREF_MOVIES_APP,
                Context.MODE_PRIVATE);
        String moviesJson = mPrefs.getString(Utility.PREF_ITEM_MOVIES_OBJECT_JSON, null);

        if (moviesJson != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<ArrayList<Movie>>() {
            }.getType();
            movies = gson.fromJson(moviesJson, type);
        }

        return movies;
    }

    /**
     * Callback from MovieListFragment when an item has been selected.
     *
     * @param movieID
     */
    @Override
    public void onItemSelected(int movieID) {

        if (mTwoPane) {
            Bundle arguments = new Bundle();
            arguments.putInt(MovieDetailFragment.ARG_ITEM_ID, movieID);

            MovieDetailFragment fragment = new MovieDetailFragment();
            fragment.setArguments(arguments);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.movie_detail_container, fragment, getResources().getString(R.string.tag_fragment_movie_detail))
                    .commit();
        } else {
            Intent intent = new Intent(MovieListActivity.this, MovieDetailActivity.class);
            intent.putExtra(MovieDetailFragment.ARG_ITEM_ID, movieID);
            MovieListActivity.this.startActivity(intent);
        }
    }
}
