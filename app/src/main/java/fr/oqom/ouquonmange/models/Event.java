package fr.oqom.ouquonmange.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.cardinalsolutions.sectioned_adapter.Categorizable;
import com.google.gson.annotations.Expose;

import org.joda.time.DateTime;

public class Event implements Parcelable, Categorizable {
    @Expose(serialize = false)
    public long id;
    @Expose(serialize = false)
    public String uuid;
    public String name;
    public String description;
    public DateTime dateStart;
    public DateTime dateEnd;
    @Expose(serialize = false)
    public long communityId;
    @Expose(serialize = false)
    public DateTime created;

    public Event(String name, String description, DateTime dateStart, DateTime dateEnd) {
        this.id = 0;
        this.uuid = "";
        this.name = name;
        this.description = description;
        this.dateStart = dateStart;
        this.dateEnd = dateEnd;
        this.communityId = 0;
        this.created = null;
    }

    protected Event(Parcel in) {
        id = in.readLong();
        uuid = in.readString();
        name = in.readString();
        description = in.readString();
        dateStart = new DateTime(in.readLong());
        dateEnd = new DateTime(in.readLong());
        communityId = in.readLong();
        created = new DateTime(in.readLong());
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(uuid);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeLong(dateStart.getMillis());
        dest.writeLong(dateEnd.getMillis());
        dest.writeLong(communityId);
        dest.writeLong(created.getMillis());
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public String getCategory() {
        return dateStart.getHourOfDay() + "h";
    }
}
