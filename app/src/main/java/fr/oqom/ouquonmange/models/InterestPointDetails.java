package fr.oqom.ouquonmange.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InterestPointDetails implements Parcelable {
    @SerializedName("interest_point")
    public InterestPoint interestPoint;
    public List<CommunityMember> members;
    public List<CommunityMember> votants;

    protected InterestPointDetails(Parcel in) {
        interestPoint = in.readParcelable(InterestPoint.class.getClassLoader());
        members = in.createTypedArrayList(CommunityMember.CREATOR);
        votants = in.createTypedArrayList(CommunityMember.CREATOR);
    }

    public static final Creator<InterestPointDetails> CREATOR = new Creator<InterestPointDetails>() {
        @Override
        public InterestPointDetails createFromParcel(Parcel in) {
            return new InterestPointDetails(in);
        }

        @Override
        public InterestPointDetails[] newArray(int size) {
            return new InterestPointDetails[size];
        }
    };

    @Override
    public String toString() {
        return "InterestPointDetails{" +
                "interestPoint=" + interestPoint +
                ", members=" + members +
                ", votants=" + votants +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(interestPoint, flags);
        dest.writeTypedList(members);
        dest.writeTypedList(votants);
    }
}
