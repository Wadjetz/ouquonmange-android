package fr.oqom.ouquonmange.models;

public class VoteGroup {
    public String event_uuid;
    public String interest_point_id;
    public String typ;

    public VoteGroup(String event_uuid, String interest_point_id, String typ) {
        this.event_uuid = event_uuid;
        this.interest_point_id = interest_point_id;
        this.typ = typ;
    }
}
