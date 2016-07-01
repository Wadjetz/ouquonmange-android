package fr.oqom.ouquonmange.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Event implements Parcelable {
    public long id;
    public String uuid;
    public String name;
    public String description;
    public DateTime date_start;
    public DateTime date_end;
    public long id_community;
    public DateTime created;

    public Event(long id, String uuid, String name, String description, long date_start, long date_end, long id_community, long created) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.date_start = new DateTime(date_start);
        this.date_end = new DateTime(date_end);
        this.id_community = id_community;
        this.created = new DateTime(created);
    }

    protected Event(Parcel in) {
        id = in.readLong();
        uuid = in.readString();
        name = in.readString();
        description = in.readString();
        date_start = new DateTime(in.readLong());
        date_end = new DateTime(in.readLong());
        id_community = in.readLong();
        created = new DateTime(in.readLong());
    }

    public static final Creator<Event> CREATOR = new Creator<Event>() {
        @Override
        public Event createFromParcel(Parcel in) {
            return new Event(in);
        }

        @Override
        public Event[] newArray(int size) {
            return new Event[size];
        }
    };

    public static List<Event> fromJson(JSONArray jsonArray) throws JSONException {
        List<Event> eventList = new ArrayList<>();
        int total = jsonArray.length();
        for (int i = 0; i < total; i++) {
            JSONObject jsonEvents = jsonArray.getJSONObject(i);
            long id = jsonEvents.getLong("id");
            String uuid = jsonEvents.getString("uuid");
            String name = jsonEvents.getString("name");
            String description = jsonEvents.getString("description");
            long date_start = jsonEvents.getLong("dateStart");
            long date_end = jsonEvents.getLong("dateEnd");
            int id_community = jsonEvents.getInt("communityId");
            long created = jsonEvents.getLong("created");
            eventList.add(new Event(id,uuid,name,description,date_start,date_end,id_community,created));
        }
        return eventList;
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
        dest.writeLong(date_start.getMillis());
        dest.writeLong(date_end.getMillis());
        dest.writeLong(id_community);
        dest.writeLong(created.getMillis());
    }
}
