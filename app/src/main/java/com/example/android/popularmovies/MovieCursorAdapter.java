package com.example.android.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.squareup.picasso.Picasso;

/**
 * Created by sasikumarlakshmanan on 16/03/16.
 */
public class MovieCursorAdapter extends CursorAdapter {
    /**
     * Recommended constructor.
     *
     * @param context The context
     * @param c       The cursor from which to get the data.
     * @param flags   Flags used to determine the behavior of the adapter; may
     *                be any combination of {@link #FLAG_AUTO_REQUERY} and
     *                {@link #FLAG_REGISTER_CONTENT_OBSERVER}.
     */
    public MovieCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public Object getItem(int position) {
        return super.getItem(position);
    }

    /**
     * Makes a new view to hold the data pointed to by cursor.
     *
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     *                moved to the correct position.
     * @param parent  The parent to which the new view is attached to
     * @return the newly created view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.movie_poster_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    /**
     * Bind an existing view to the data pointed to by cursor
     *
     * @param view    Existing view, returned earlier by newView
     * @param context Interface to application's global information
     * @param cursor  The cursor from which to get the data. The cursor is already
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

//        Movie movie = mMovieArray.get(position);
        String posterPath = cursor.getString(MovieListFragment.COLUMN_POSTER_PATH);

//            Picasso.with(getBaseContext()).load("https://image.tmdb.org/t/p/w185" + movie.posterPath).into(moviePoster);

        // Made changes as per my reviewer comments.
        Picasso.with(context).load("https://image.tmdb.org/t/p/w185" + posterPath)
//                    .placeholder(R.drawable.no_image_found)
                .error(R.drawable.no_image_found)
                .into(viewHolder.ivMoviePoster);

    }

    public static class ViewHolder {

        public final RelativeLayout rlMovePoster;
        public final ImageView ivMoviePoster;

        public ViewHolder(View view) {
            rlMovePoster = (RelativeLayout) view.findViewById(R.id.rlMovePoster);
            ivMoviePoster = (ImageView) view.findViewById(R.id.ivMoviePoster);
        }
    }
}
