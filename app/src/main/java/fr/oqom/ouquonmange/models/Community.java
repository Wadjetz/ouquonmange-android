package fr.oqom.ouquonmange.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Community implements Parcelable {
    public long id;
    public String uuid;
    public String name;
    public String description;
    public int created;
    public boolean isDefault = false;

    public Community() {}

    public Community(long id, String uuid, String name, String description, int created, boolean isDefault) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.created = created;
        this.isDefault = isDefault;
    }

    protected Community(Parcel in) {
        id = in.readLong();
        uuid = in.readString();
        name = in.readString();
        description = in.readString();
        created = in.readInt();
        isDefault = in.readInt() > 0;
    }

    public static final Creator<Community> CREATOR = new Creator<Community>() {
        @Override
        public Community createFromParcel(Parcel in) {
            return new Community(in);
        }

        @Override
        public Community[] newArray(int size) {
            return new Community[size];
        }
    };

    public static List<Community> fromJson(JSONArray jsonArray) throws JSONException {
        List<Community> communities = new ArrayList<>();
        int total = jsonArray.length();
        for (int i=0; i<total; i++) {
            JSONObject jsonCommunity = jsonArray.getJSONObject(i);
            long id = jsonCommunity.getLong("id");
            String uuid = jsonCommunity.getString("uuid");
            String name = jsonCommunity.getString("name");
            String description = jsonCommunity.getString("description");
            int created = jsonCommunity.getInt("created");
            communities.add(new Community(id, uuid, name, description, created, false));
        }
        return communities;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(uuid);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeInt(created);
        dest.writeInt(isDefault ? 1 : 0);
    }
}
