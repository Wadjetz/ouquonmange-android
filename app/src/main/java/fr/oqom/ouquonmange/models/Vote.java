package fr.oqom.ouquonmange.models;

public class Vote {
    public long id = 0;
    public String uuid;
    public String interestPointId;

    public Vote(long id, String uuid, String interestPointId) {
        this.id = id;
        this.uuid = uuid;
        this.interestPointId = interestPointId;
    }
}