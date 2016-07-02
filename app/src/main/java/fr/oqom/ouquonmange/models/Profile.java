package fr.oqom.ouquonmange.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

public class Profile implements Parcelable {
    public String uuid;
    public String username;
    public String email;
    public int gsmTokens;
    public DateTime created;

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

    @Override
    public String toString() {
        return "Profile{" +
                "uuid='" + uuid + '\'' +
                ", username='" + username + '\'' +
                ", gsmTokens=" + gsmTokens +
                ", created=" + created +
                '}';
    }
}
