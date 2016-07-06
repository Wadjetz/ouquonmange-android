package fr.oqom.ouquonmange.models;

import android.os.Parcel;
import android.os.Parcelable;

import org.joda.time.DateTime;

public class CommunityMember implements Parcelable {
    public String uuid;
    public String username;
    public DateTime created;
    public String role;
    public DateTime joined;
    public String status;
    public boolean isAdmin;

    protected CommunityMember(Parcel in) {
        uuid = in.readString();
        username = in.readString();
        role = in.readString();
        status = in.readString();
        isAdmin = (1 == in.readInt());
    }

    public static final Creator<CommunityMember> CREATOR = new Creator<CommunityMember>() {
        @Override
        public CommunityMember createFromParcel(Parcel in) {
            return new CommunityMember(in);
        }

        @Override
        public CommunityMember[] newArray(int size) {
            return new CommunityMember[size];
        }
    };

    @Override
    public String toString() {
        return "CommunityMember{" +
                "uuid='" + uuid + '\'' +
                ", username='" + username + '\'' +
                ", created=" + created +
                ", role='" + role + '\'' +
                ", joined=" + joined +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(uuid);
        dest.writeString(username);
        dest.writeString(role);
        dest.writeString(status);
        dest.writeInt(isAdmin ? 1 : 0);
    }
}
