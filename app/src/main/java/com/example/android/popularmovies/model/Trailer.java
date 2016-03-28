package com.example.android.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sasikumarlakshmanan on 19/03/16.
 */
public class Trailer implements Parcelable {

    public final String trailerID;
    public final String trailerKey;
    public final String trailerName;
    public final String trailerSite;

    public Trailer(String trailerID, String trailerKey, String trailerName, String trailerSite) {
        this.trailerID = trailerID;
        this.trailerKey = trailerKey;
        this.trailerName = trailerName;
        this.trailerSite = trailerSite;
    }

    protected Trailer(Parcel in) {
        trailerID = in.readString();
        trailerKey = in.readString();
        trailerName = in.readString();
        trailerSite = in.readString();
    }

    public static final Creator<Trailer> CREATOR = new Creator<Trailer>() {
        @Override
        public Trailer createFromParcel(Parcel in) {
            return new Trailer(in);
        }

        @Override
        public Trailer[] newArray(int size) {
            return new Trailer[size];
        }
    };

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
        dest.writeString(trailerID);
        dest.writeString(trailerKey);
        dest.writeString(trailerName);
        dest.writeString(trailerSite);
    }
}
