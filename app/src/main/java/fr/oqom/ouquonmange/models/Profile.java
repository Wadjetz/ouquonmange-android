package fr.oqom.ouquonmange.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import fr.oqom.ouquonmange.utils.TimeUtils;

public class Profile implements Parcelable {
    public String uuid;
    public String username;
    public String email;
    public int gsmTokens;
    public DateTime created;

    public Profile(String uuid, String username, String email, int gsmTokens, long created) {
        this.uuid = uuid;
        this.username = username;
        this.email = email;
        this.gsmTokens = gsmTokens;
        this.created = TimeUtils.getDateTime(created);
    }

    protected Profile(Parcel in) {
        uuid = in.readString();
        username = in.readString();
        email = in.readString();
        gsmTokens = in.readInt();
        created = new DateTime(in.readLong());
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(username);
        dest.writeString(email);
        dest.writeInt(gsmTokens);
        dest.writeLong(created.getMillis());
    }

    public static final Creator<Profile> CREATOR = new Creator<Profile>() {
        @Override
        public Profile createFromParcel(Parcel in) {
            return new Profile(in);
        }

        @Override
        public Profile[] newArray(int size) {
            return new Profile[size];
        }
    };

    public static Profile fromJson(JSONObject json) throws JSONException {
        String uuid = json.getString("uuid");
        String username = json.getString("username");
        String email = json.getString("email");
        int gsmTokens = json.getInt("gsmTokens");
        long created = json.getLong("created");
        return new Profile(uuid, username, email, gsmTokens, created);
    }
}
