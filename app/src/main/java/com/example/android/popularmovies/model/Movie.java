package com.example.android.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sasikumarlakshmanan on 12/03/16.
 */
public class Movie implements Parcelable {
    public final int movieID;
    public final String originalTitle;
    public final String posterPath;
    public final String overview;
    public final String releaseDate;
    public final double voteAverage;
    public final String backdropPath;
    public final int favorite;

    public Movie(int movieID, String originalTitle, String posterPath,
                 String overview, String releaseDate, double voteAverage,
                 String backdropPath, int favorite) {
        this.movieID = movieID;
        this.originalTitle = originalTitle;
        this.posterPath = posterPath;
        this.overview = overview;
        this.releaseDate = releaseDate;
        this.voteAverage = voteAverage;
        this.backdropPath = backdropPath;
        this.favorite = favorite;
    }

    /**
     * Describe the kinds of special objects contained in this Parcelable's
     * marshalled representation.
     *
     * @return a bitmask indicating the set of special object types marshalled
     * by the Parcelable.
     */
    @Override
    public int describeContents() {
        return 0;
    }

    /**
     * Flatten this object in to a Parcel.
     *
     * @param dest  The Parcel in which the object should be written.
     * @param flags Additional flags about how the object should be written.
     *              May be 0 or {@link #PARCELABLE_WRITE_RETURN_VALUE}.
     */
    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeInt(movieID);
        dest.writeString(originalTitle);
        dest.writeString(posterPath);
        dest.writeString(overview);
        dest.writeString(releaseDate);
        dest.writeDouble(voteAverage);
        dest.writeString(backdropPath);
        dest.writeInt(favorite);
    }

    public static final Parcelable.Creator<Movie> CREATOR = new Parcelable.Creator<Movie>() {
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    private Movie(Parcel in) {
        movieID = in.readInt();
        originalTitle = in.readString();
        posterPath = in.readString();
        overview = in.readString();
        releaseDate = in.readString();
        voteAverage = in.readDouble();
        backdropPath = in.readString();
        favorite = in.readInt();
    }
}
