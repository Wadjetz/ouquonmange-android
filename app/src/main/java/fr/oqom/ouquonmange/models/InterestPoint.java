package fr.oqom.ouquonmange.models;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class InterestPoint {

    public String foursquareId;
    public String name;
    public String address;
    public String lat;
    public String lng;
    public List<Long> members;
    public boolean isJoin;
    public String imageUrl;

    public InterestPoint(String foursquareId, String name, String address, String lat, String lng, List<Long> members, boolean isJoin, String imageUrl) {
        this.foursquareId = foursquareId;
        this.name = name;
        this.address = address;
        this.lat = lat;
        this.lng = lng;
        this.members = members;
        this.isJoin = isJoin;
        this.imageUrl = imageUrl;
    }

    public static List<InterestPoint> fromJson(JSONArray jsonArray) throws JSONException {
        Log.d("InterestPoint", jsonArray.toString());
        List<InterestPoint> interestPoints = new ArrayList<>();

        int total = jsonArray.length();

        for (int i=0; i<total; i++) {
            JSONObject jsonInterestPoint = jsonArray.getJSONObject(i);
            String foursquareId = jsonInterestPoint.getString("foursquareId");
            String name = jsonInterestPoint.getString("name");
            String address = jsonInterestPoint.getString("address");
            String lat = jsonInterestPoint.getString("lat");
            String lng = jsonInterestPoint.getString("lng");
            //JSONObject data = jsonInterestPoint.getJSONObject("data");
            List<Long> members = new ArrayList<>();// jsonInterestPoint.getJSONArray("members");
            boolean isJoin = jsonInterestPoint.getBoolean("isJoin");
            JSONObject data = jsonInterestPoint.getJSONObject("data");
            interestPoints.add(new InterestPoint(foursquareId, name, address, lat, lng, /*data*/ members, isJoin, null));
        }

        return interestPoints;
    }
}
