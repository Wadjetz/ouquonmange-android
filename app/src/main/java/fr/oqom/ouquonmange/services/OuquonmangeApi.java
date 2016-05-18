package fr.oqom.ouquonmange.services;

import android.content.Context;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;
import fr.oqom.ouquonmange.models.AuthRepository;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class OuquonmangeApi {

    private String baseUrl = Constants.OQOM_BASE_URL;

    private AsyncHttpClient client = new AsyncHttpClient();

    private String token;

    private Context context;

    private String getToken() {
        if (token == null) {
            AuthRepository authRepository = new AuthRepository(context);
            token = authRepository.getToken();
        }
        return token;
    }

    public OuquonmangeApi(Context context) {
        this.context = context;
    }

    public OuquonmangeApi(Context context, String baseUrl) {
        this.baseUrl = baseUrl;
        this.context = context;
    }

    public void login(String email, String password, final Callback<JSONObject> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("password", password);
        client.post(baseUrl + "/auth/local/login", params, new JsonHttpResponseHandler() {

            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                success.apply(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                failure.apply(throwable, errorResponse);
            }

        });
    }

    public void getCommunities(final Callback<JSONArray> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams params = new RequestParams();
        client.addHeader("Authorization", "Bearer " + getToken());
        client.get(baseUrl + "/api/community", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                success.apply(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                failure.apply(throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseText, Throwable throwable) {
                failure.apply(throwable, null);
            }
        });
    }

    public void createCommunity(String name, String description, final Callback<JSONObject> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams params = new RequestParams();
        params.add("name", name);
        params.add("description", description);
        client.addHeader("Authorization", "Bearer " + getToken());
        client.post(baseUrl + "/api/community", params, new JsonHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                success.apply(response);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                failure.apply(throwable, errorResponse);
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, String responseText, Throwable throwable) {
                failure.apply(throwable, null);
            }
        });
    }

}
