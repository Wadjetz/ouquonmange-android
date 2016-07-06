package fr.oqom.ouquonmange.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class InterestPoint implements Parcelable, Comparable {

    @SerializedName("api_id")
    public String apiId;
    public String type;
    public String name;
    public String address;
    public String lat;
    public String lng;
    public int members;
    public int votes;
    public boolean isJoin;
    public boolean isVote;
    public List<String> image = new ArrayList<>();
    public List<String> categories = new ArrayList<>();

    public InterestPoint(Parcel in) {
        apiId = in.readString();
        type = in.readString();
        name = in.readString();
        address = in.readString();
        lat = in.readString();
        lng = in.readString();
        members = in.readInt();
        votes = in.readInt();
        isJoin = (1 == in.readInt());
        isVote = (1 == in.readInt());
        image.clear();
        in.readStringList(image);
        categories.clear();
        in.readStringList(categories);
    }

    public static final Parcelable.Creator<InterestPoint> CREATOR = new Parcelable.Creator<InterestPoint>() {
        public InterestPoint createFromParcel(Parcel in) {
            return new InterestPoint(in);
        }

        public InterestPoint[] newArray(int size) {
            return new InterestPoint[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(apiId);
        out.writeString(type);
        out.writeString(name);
        out.writeString(address);
        out.writeString(lat);
        out.writeString(lng);
        out.writeInt(members);
        out.writeInt(votes);
        out.writeInt(isJoin ? 1 : 0);
        out.writeInt(isVote ? 1 : 0);
        out.writeStringList(image);
        out.writeStringList(categories);
    }

    @Override
    public String toString() {
        return "InterestPoint{" +
                "apiId='" + apiId + '\'' +
                ", type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", lat='" + lat + '\'' +
                ", lng='" + lng + '\'' +
                ", members=" + members +
                ", votes=" + votes +
                ", isJoin=" + isJoin +
                ", isVote=" + isVote +
                ", image=" + image +
                ", categories=" + categories +
                '}';
    }

    @Override
    public int compareTo(Object another) {
        InterestPoint ip = (InterestPoint) another;
        if ( ( (ip.members * 2) + ip.votes) > ((members * 2) + votes) ) {
            return 1;
        } else if ( ((ip.members * 2) + ip.votes) == ((members * 2) + votes) ) {
            return 0;
        } else {
            return -1;
        }
    }
}
