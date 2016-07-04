package fr.oqom.ouquonmange.models;

import android.os.Parcel;
import android.os.Parcelable;

import com.cardinalsolutions.sectioned_adapter.Categorizable;
import com.google.gson.annotations.Expose;

import org.joda.time.DateTime;

import fr.oqom.ouquonmange.utils.TimeUtils;

public class Event implements Parcelable, Categorizable {
    @Expose(serialize = false)
    public long id = 0;
    @Expose(serialize = false)
    public String uuid = "";
    public String name;
    public String description;
    public DateTime dateStart;
    public DateTime dateEnd;
    @Expose(serialize = false)
    public long communityId = 0;
    @Expose(serialize = false)
    public DateTime created = null;

    public Event(String name, String description, long dateStart, long dateEnd) {
        this.name = name;
        this.description = description;
        this.dateStart = TimeUtils.getDateTimeWithDefaultTZ(new DateTime(dateStart),TimeUtils.getDefaultDateTimeZoneId());
        this.dateEnd = TimeUtils.getDateTimeWithDefaultTZ(new DateTime(dateEnd),TimeUtils.getDefaultDateTimeZoneId());
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

    @Override
    public String toString() {
        return "Event{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", dateStart=" + dateStart +
                ", dateEnd=" + dateEnd +
                ", communityId=" + communityId +
                ", created=" + created +
                '}';
    }
}
