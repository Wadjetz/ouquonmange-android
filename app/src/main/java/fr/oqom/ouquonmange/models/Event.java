package fr.oqom.ouquonmange.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.cardinalsolutions.sectioned_adapter.Categorizable;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.oqom.ouquonmange.utils.TimeUtils;

public class Event implements Parcelable, Categorizable {
    public long id;
    public String uuid;
    public String name;
    public String description;
    public DateTime dateStart;
    public DateTime dateEnd;
    public long idCommunity;
    public DateTime created;

    public Event(long id, String uuid, String name, String description, long dateStart, long dateEnd, long idCommunity, long created) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.dateStart = TimeUtils.getDateTime(dateStart);
        this.dateEnd = TimeUtils.getDateTime(dateEnd);
        this.idCommunity = idCommunity;
        this.created = TimeUtils.getDateTime(created);
    }

    protected Event(Parcel in) {
        id = in.readLong();
        uuid = in.readString();
        name = in.readString();
        description = in.readString();
        dateStart = new DateTime(in.readLong());
        dateEnd = new DateTime(in.readLong());
        idCommunity = in.readLong();
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
            eventList.add(Event.fromJson(jsonArray.getJSONObject(i)));
        }
        return eventList;
    }

    public static Event fromJson(JSONObject json) throws JSONException {
        long id = json.getLong("id");
        String uuid = json.getString("uuid");
        String name = json.getString("name");
        String description = json.getString("description");
        long dateStart = json.getLong("dateStart");
        long dateEnd = json.getLong("dateEnd");
        long idCommunity = json.getLong("communityId");
        long created = json.getLong("created");
        return new Event(id, uuid, name, description, dateStart, dateEnd, idCommunity, created);
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
        dest.writeLong(dateStart.getMillis());
        dest.writeLong(dateEnd.getMillis());
        dest.writeLong(idCommunity);
        dest.writeLong(created.getMillis());
    }

    @Override
    public String getCategory() {
        return dateStart.getHourOfDay() + "h";
    }
}
