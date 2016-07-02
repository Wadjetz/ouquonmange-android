package fr.oqom.ouquonmange.models;

public class Group {
    public long id = 0;
    public String uuid;
    public String interestPointId;

    public Group(long id, String uuid, String interestPointId) {
        this.id = id;
        this.uuid = uuid;
        this.interestPointId = interestPointId;
    }
}
