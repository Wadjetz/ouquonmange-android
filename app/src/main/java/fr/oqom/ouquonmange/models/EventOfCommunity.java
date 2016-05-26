package fr.oqom.ouquonmange.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hedhili on 25/05/2016.
 */
public class EventOfCommunity {
    public long id;
    public String uuid;
    public String name;
    public String description;
    public int date_start;
    public int date_end;
    public long id_community;
    public int created;

    public EventOfCommunity(long id, String uuid, String name, String description, int date_start,int date_end, long id_community, int created ) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.date_start = date_start;
        this.date_end = date_end;
        this.id_community = id_community;
        this.created = created;
    }
    public static List<EventOfCommunity> fromJson(JSONArray jsonArray) throws JSONException {
        List<EventOfCommunity> eventOfCommunityList = new ArrayList<>();
        int total = jsonArray.length();
        for (int i = 0; i < total; i++) {
            JSONObject jsonEvents = jsonArray.getJSONObject(i);
            long id = jsonEvents.getLong("id");
            String uuid = jsonEvents.getString("uuid");
            String name = jsonEvents.getString("name");
            String description = jsonEvents.getString("description");
            int date_start = jsonEvents.getInt("dateStart");
            int date_end = jsonEvents.getInt("dateEnd");
            int id_community = jsonEvents.getInt("communityId");
            int created = jsonEvents.getInt("created");
        eventOfCommunityList.add(new EventOfCommunity(id,uuid,name,description,date_start,date_end,id_community,created));
        }
    return eventOfCommunityList;
    }

}
