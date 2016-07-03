package fr.oqom.ouquonmange.models;

import java.util.List;

public class CommunityDetails {
    public Community community;
    public List<CommunityMember> members;

    @Override
    public String toString() {
        return "CommunityDetails{" +
                "community=" + community +
                ", members=" + members +
                '}';
    }
}
