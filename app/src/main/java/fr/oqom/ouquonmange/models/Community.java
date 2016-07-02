package fr.oqom.ouquonmange.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.oqom.ouquonmange.utils.TimeUtils;

public class Community implements Parcelable {

    @Expose(serialize = false)
    public long id = 0;

    @Expose(serialize = false)
    public String uuid = "";

    public String name;

    public String description;

    @Expose(serialize = false)
    public DateTime created;

    @Expose(serialize = false, deserialize = false)
    public boolean isDefault = false;

    public Community() {}

    public Community(String name, String description) {
        this.name = name;
        this.description = description;
        this.created = null;
    }

    public Community(long id, String uuid, String name, String description, long created, boolean isDefault) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.created = TimeUtils.getDateTime(created);
        this.isDefault = isDefault;
    }

    protected Community(Parcel in) {
        id = in.readLong();
        uuid = in.readString();
        name = in.readString();
        description = in.readString();
        created = new DateTime(in.readLong());
        isDefault = 1 == in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(uuid);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeLong(created.getMillis());
        dest.writeInt(isDefault ? 1 : 0);
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

    @Override
    public int describeContents() {
        return 0;
    }
}
