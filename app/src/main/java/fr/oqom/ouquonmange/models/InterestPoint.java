package fr.oqom.ouquonmange.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InterestPoint implements Parcelable {

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
    public String imageUrl;

    public InterestPoint(String apiId, String type, String name, String address, String lat, String lng, int members, int votes, boolean isJoin, boolean isVote, String imageUrl) {
        this.apiId = apiId;
        this.type = type;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.members = members;
        this.votes = votes;
        this.isJoin = isJoin;
        this.isVote = isVote;
        this.imageUrl = imageUrl;
    }

    public InterestPoint(Parcel in) {
        apiId = in.readString();
        type = in.readString();
        name = in.readString();
        address = in.readString();
        lat = in.readString();
        lng = in.readString();
        members = in.readInt();
        votes = in.readInt();
        isJoin = (1 == in.readInt()) ? true : false;
        isVote = (1 == in.readInt()) ? true : false;
        imageUrl = in.readString();
    }

    public static List<InterestPoint> fromJson(JSONArray jsonArray) throws JSONException {
        Log.d("InterestPoint", jsonArray.toString());
        List<InterestPoint> interestPoints = new ArrayList<>();

        int total = jsonArray.length();

        for (int i=0; i<total; i++) {
            JSONObject jsonInterestPoint = jsonArray.getJSONObject(i);
            String apiId = jsonInterestPoint.getString("api_id");
            String type = jsonInterestPoint.getString("type");
            String name = jsonInterestPoint.getString("name");
            String address = jsonInterestPoint.getString("address");
            String lat = jsonInterestPoint.getString("lat");
            String lng = jsonInterestPoint.getString("lng");
            int members = jsonInterestPoint.getInt("members");
            int votes = jsonInterestPoint.getInt("votes");
            boolean isJoin = jsonInterestPoint.getBoolean("isJoin");
            boolean isVote = jsonInterestPoint.getBoolean("isVote");
            interestPoints.add(new InterestPoint(apiId, type, name, address, lat, lng, members, votes, isJoin, isVote, ""));
        }

        return interestPoints;
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
        out.writeString(imageUrl);
    }
}
