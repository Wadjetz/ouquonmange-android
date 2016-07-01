package fr.oqom.ouquonmange.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class User implements Parcelable {
    public String uuid;
    public String username;

    public User(String uuid,String username){
        this.uuid = uuid;
        this.username = username;
    }

    protected User(Parcel in) {
        this.uuid = in.readString();
        this.username = in.readString();
    }


    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(username);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel in) { return new User(in); }

        @Override
        public User[] newArray(int size) { return new User[size]; }
    };

    public static List<User> fromJson(JSONArray jsonArray) throws JSONException {
        List<User> eventList = new ArrayList<>();
        int total = jsonArray.length();
        for (int i = 0; i < total; i++) {
            JSONObject jsonEvents = jsonArray.getJSONObject(i);
            String uuid = jsonEvents.getString("uuid");
            String username = jsonEvents.getString("username");
            eventList.add(new User(uuid,username));
        }
        return eventList;
    }

}
