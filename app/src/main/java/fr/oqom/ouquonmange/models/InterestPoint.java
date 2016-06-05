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

    public InterestPoint(String foursquareId, String name, String address) {
        this.foursquareId = foursquareId;
        this.name = name;
        this.address = address;
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
            interestPoints.add(new InterestPoint(foursquareId, name, address));
        }

        return interestPoints;
    }
}
