package fr.oqom.ouquonmange.models;

import java.util.List;

public class InterestPointDetails {
    public InterestPoint interestPoint;
    public List<CommunityMember> members;
    public List<CommunityMember> votants;

    @Override
    public String toString() {
        return "InterestPointDetails{" +
                "interestPoint=" + interestPoint +
                ", members=" + members +
                ", votants=" + votants +
                '}';
    }
}
