package fr.oqom.ouquonmange.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;

public class Profile extends RealmObject implements Parcelable {
    public String uuid;
    public String username;
    public String email;
    public int gsmTokens;
    @Ignore
    public DateTime created;

    public Profile() {}

    public Profile(String uuid, String username, String email, int gsmTokens, DateTime created) {
        this.uuid = uuid;
        this.username = username;
        this.email = email;
        this.gsmTokens = gsmTokens;
        this.created = created;
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

    @Override
    public String toString() {
        return "Profile{" +
                "uuid='" + uuid + '\'' +
                ", username='" + username + '\'' +
                ", gsmTokens=" + gsmTokens +
                ", created=" + created +
                '}';
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getGsmTokens() {
        return gsmTokens;
    }

    public void setGsmTokens(int gsmTokens) {
        this.gsmTokens = gsmTokens;
    }

    public DateTime getCreated() {
        return created;
    }

    public void setCreated(DateTime created) {
        this.created = created;
    }
}
