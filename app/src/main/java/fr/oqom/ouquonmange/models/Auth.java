package fr.oqom.ouquonmange.models;

import io.realm.RealmObject;
import io.realm.annotations.Required;

public class Auth extends RealmObject {
    @Required
    private String token;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
