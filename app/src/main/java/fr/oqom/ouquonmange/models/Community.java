package fr.oqom.ouquonmange.models;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class Community {
    public long id;
    public String uuid;
    public String name;
    public String description;
    public int created;

    public Community(long id, String uuid, String name, String description, int created) {
        this.id = id;
        this.uuid = uuid;
        this.name = name;
        this.description = description;
        this.created = created;
    }

    public static List<Community> fromJson(JSONArray jsonArray) throws JSONException {
        List<Community> communities = new ArrayList<>();
        int total = jsonArray.length();
        for (int i=0; i<total; i++) {
            JSONObject jsonCommunity = jsonArray.getJSONObject(i);
            long id = jsonCommunity.getLong("id");
            String uuid = jsonCommunity.getString("uuid");
            String name = jsonCommunity.getString("name");
            String description = jsonCommunity.getString("description");
            int created = jsonCommunity.getInt("created");
            communities.add(new Community(id, uuid, name, description, created));
        }
        return communities;
    }
}
