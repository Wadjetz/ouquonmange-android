package fr.oqom.ouquonmange.services;

import org.json.JSONObject;

public class ThrowableWithJson extends Throwable {
    private Throwable throwable;
    private JSONObject json;

    public ThrowableWithJson(Throwable throwable, JSONObject json) {
        this.throwable = throwable;
        this.json = json;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }

    public JSONObject getJson() {
        return json;
    }

    public void setJson(JSONObject json) {
        this.json = json;
    }
}
