package fr.oqom.ouquonmange.services;

import android.content.Context;
import android.location.Location;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestHandle;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

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

    public RequestHandle login(String email, String password, final Callback<JSONObject> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams params = new RequestParams();
        params.put("email", email);
        params.put("password", password);
        return client.post(baseUrl + "/auth/local/login", params, new JsonHttpResponseHandler() {

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

    public RequestHandle getCommunities(final Callback<JSONArray> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams params = new RequestParams();
        client.addHeader("Authorization", "Bearer " + getToken());
        return client.get(baseUrl + "/api/community", params, new JsonHttpResponseHandler() {
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

    public RequestHandle createCommunity(String name, String description, final Callback<JSONObject> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams params = new RequestParams();
        params.add("name", name);
        params.add("description", description);
        client.addHeader("Authorization", "Bearer " + getToken());
        return client.post(baseUrl + "/api/community", params, new JsonHttpResponseHandler() {
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

    public RequestHandle createAccountUser(String username, String email, String password, final Callback<JSONObject> success, final Callback2<Throwable, JSONObject> failure){
        RequestParams params = new RequestParams();
        params.add("username",username);
        params.add("password",password);
        params.add("email",email);
        return client.post(baseUrl+ "/auth/local/signup", params, new JsonHttpResponseHandler(){
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

    public RequestHandle createEvent(
            String communityUuid,
            String name,
            String description,
            Calendar dateStart,
            Calendar dateEnd,
            final Callback<JSONObject> success,
            final Callback2<Throwable, JSONObject> failure
    ) {
        RequestParams params = new RequestParams();
        params.add("name", name);
        params.add("description", description);
        params.add("dateStart", Constants.dateTimeFormat.format(dateStart.getTime().getTime()));
        params.add("dateEnd", Constants.dateTimeFormat.format(dateEnd.getTime().getTime()));
        client.addHeader("Authorization", "Bearer " + getToken());
        return client.post(baseUrl+ "/api/event/" + communityUuid, params, new JsonHttpResponseHandler(){
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

    public RequestHandle joinGroup(
            String communityUuid,
            String eventUuid,
            String interestPointId,
            final Callback<JSONObject> success,
            final Callback2<Throwable, JSONObject> failure
    ) {
        RequestParams params = new RequestParams();
        params.add("event_uuid", eventUuid);
        params.add("interest_point_id", interestPointId);
        client.addHeader("Authorization", "Bearer " + getToken());
        return client.post(baseUrl+ "/api/group/" + communityUuid, params, new JsonHttpResponseHandler(){
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

    public void quitGroup(
            String communityUuid,
            String eventUuid,
            String interestPointId,
            final Callback<JSONObject> success,
            final Callback2<Throwable, JSONObject> failure
    ) {
        RequestParams params = new RequestParams();
        client.addHeader("Authorization", "Bearer " + getToken());
        client.delete(baseUrl + "/api/group/" + communityUuid + "/" + eventUuid + "/" + interestPointId, params, new JsonHttpResponseHandler() {
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

    public RequestHandle getEventsByUUID(
            String communityUuid,
            Calendar calendar,
            final Callback<JSONArray> success,
            final Callback2<Throwable, JSONObject> failure
    ) {
        RequestParams params = new RequestParams();
        client.addHeader("Authorization", "Bearer " + getToken());
        String url = baseUrl+"/api/event/" + communityUuid + "/" + calendar.getTime().getTime();
        return client.get(url, params, new JsonHttpResponseHandler() {
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

    public RequestHandle addGcmToken(String gcmToken, final Callback<JSONObject> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams params = new RequestParams();
        params.add("gsm_token", gcmToken);
        client.addHeader("Authorization", "Bearer " + getToken());
        return client.put(baseUrl + "/api/user/addgcmtoken", params, new JsonHttpResponseHandler() {
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

    public RequestHandle getInterestPointsByLocation(Location location, String eventUuid, String communityUuid, final Callback<JSONObject> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams requestParams = new RequestParams();
        client.addHeader("Authorization", "Bearer " + getToken());
        return client.get(baseUrl + "/api/interest/point/" + communityUuid + "/" + eventUuid + "?lat=" + location.getLatitude() + "&lng=" + location.getLongitude(), requestParams,  new JsonHttpResponseHandler() {
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

    public RequestHandle getInterestPoints(String eventUuid, String communityUuid ,final Callback<JSONObject> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams requestParams = new RequestParams();
        client.addHeader("Authorization", "Bearer " + getToken());
        return client.get(baseUrl + "/api/interest/point/" + communityUuid + "/" + eventUuid, requestParams,  new JsonHttpResponseHandler() {
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

    public RequestHandle getAllCommunities(final Callback<JSONArray> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams params = new RequestParams();
        client.addHeader("Authorization", "Bearer " + getToken());
        String url = baseUrl+"/api/community/search";
        return client.get(url, params, new JsonHttpResponseHandler() {
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

    public RequestHandle getCommunitiesByQuery(String query, final Callback<JSONArray> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams requestParams = new RequestParams();
        client.addHeader("Authorization", "Bearer " + getToken());
        return client.get(baseUrl + "/api/community/search?query=" + query, requestParams,  new JsonHttpResponseHandler() {
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

    public RequestHandle addMemberInCommunity(String uuid, final Callback<JSONObject> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams params = new RequestParams();
        client.addHeader("Authorization", "Bearer " + getToken());
        String url = baseUrl + "/api/member/" + uuid + "/member";
        return client.post(url, params, new JsonHttpResponseHandler() {
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

    public RequestHandle getInterestPointDetails(
            String interestPointId,
            String eventUuid,
            String communityUuid,
            final Callback<JSONObject> success,
            final Callback2<Throwable, JSONObject> failure
    ) {
        RequestParams params = new RequestParams();
        client.addHeader("Authorization", "Bearer " + getToken());
        String url = baseUrl + "/api/interest/point/" + communityUuid + "/" + eventUuid + "/" + interestPointId;
        return client.get(url, params, new JsonHttpResponseHandler() {
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
