package com.example.android.popularmovies.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by sasikumarlakshmanan on 19/03/16.
 */
public class Review implements Parcelable {

    public final String reviewID;
    public final String reviewAuthor;
    public final String reviewContent;

    public Review(String reviewID, String reviewAuthor, String reviewContent) {
        this.reviewID = reviewID;
        this.reviewAuthor = reviewAuthor;
        this.reviewContent = reviewContent;
    }

    protected Review(Parcel in) {
        reviewID = in.readString();
        reviewAuthor = in.readString();
        reviewContent = in.readString();
    }

    public static final Creator<Review> CREATOR = new Creator<Review>() {
        @Override
        public Review createFromParcel(Parcel in) {
            return new Review(in);
        }

        @Override
        public Review[] newArray(int size) {
            return new Review[size];
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
        dest.writeString(reviewID);
        dest.writeString(reviewAuthor);
        dest.writeString(reviewContent);
    }
}
