package fr.oqom.ouquonmange.models;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.annotations.Expose;

import org.joda.time.DateTime;

import fr.oqom.ouquonmange.R;

public class Community implements Parcelable {

    @Expose(serialize = false)
    public long id = 0;

    @Expose(serialize = false)
    public String uuid = "";

    public String name;

    public String description;

    public String typ;

    @Expose(serialize = false)
    public DateTime created;

    @Expose(serialize = false, deserialize = false)
    public boolean isDefault = false;

    public Community() {}

    public Community(String name, String description, String typ) {
        this.name = name;
        this.description = description;
        this.typ = typ;
        this.created = null;
    }

    protected Community(Parcel in) {
        id = in.readLong();
        uuid = in.readString();
        name = in.readString();
        description = in.readString();
        typ = in.readString();
        created = new DateTime(in.readLong());
        isDefault = 1 == in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeLong(id);
        dest.writeString(uuid);
        dest.writeString(name);
        dest.writeString(description);
        dest.writeString(typ);
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

    @Override
    public String toString() {
        return "Community{" +
                "id=" + id +
                ", uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", typ='" + typ + '\'' +
                ", created=" + created +
                ", isDefault=" + isDefault +
                '}';
    }

    public String getCommunityType(Context context) {
        switch (typ) {
            case "public": return context.getResources().getStringArray(R.array.community_types)[0];
            case "private": return context.getResources().getStringArray(R.array.community_types)[1];
            case "closed": return context.getResources().getStringArray(R.array.community_types)[2];
            default: throw new ArrayIndexOutOfBoundsException("Unknown Community Type");
        }
    }
}
