package fr.oqom.ouquonmange.services;

import android.content.Context;
import android.location.Location;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.joda.time.DateTime;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import cz.msebera.android.httpclient.Header;
import fr.oqom.ouquonmange.models.AuthRepository;
import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.Event;
import fr.oqom.ouquonmange.models.InterestPoint;
import fr.oqom.ouquonmange.models.Profile;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;
import fr.oqom.ouquonmange.utils.DateTimeUtils;
import rx.Observable;
import rx.Subscriber;

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

    public void createAccountUser(String username, String email, String password, final Callback<JSONObject> success, final Callback2<Throwable, JSONObject> failure){
        RequestParams params = new RequestParams();
        params.add("username",username);
        params.add("password",password);
        params.add("email",email);
        client.post(baseUrl+ "/auth/local/signup", params, new JsonHttpResponseHandler(){
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

    public Observable<Event> createEvent(final String communityUuid,
                                         final String name,
                                         final String description,
                                         final DateTime dateStart,
                                         final DateTime dateEnd
    ) {
        return Observable.create(new Observable.OnSubscribe<Event>() {
            @Override
            public void call(final Subscriber<? super Event> subscriber) {
                RequestParams params = new RequestParams();
                params.add("name", name);
                params.add("description", description);
                params.add("dateStart", DateTimeUtils.printDateTime(dateStart, DateTimeUtils.getDateTimeZoneUTC()));
                params.add("dateEnd", DateTimeUtils.printDateTime(dateEnd, DateTimeUtils.getDateTimeZoneUTC()));
                client.addHeader("Authorization", "Bearer " + getToken());
                client.post(baseUrl+ "/api/event/" + communityUuid, params, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            Event event = Event.fromJson(response);
                            subscriber.onNext(event);
                            subscriber.onCompleted();
                        } catch (JSONException e) {
                            subscriber.onError(new ThrowableWithJson(e, null));
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        subscriber.onError(new ThrowableWithJson(throwable, errorResponse));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseText, Throwable throwable) {
                        subscriber.onError(new ThrowableWithJson(throwable, null));
                    }
                });
            }
        });
    }

    public Observable<JSONObject> joinGroup(final String communityUuid, final String eventUuid, final InterestPoint interestPoint) {
        return Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(final Subscriber<? super JSONObject> subscriber) {
                RequestParams params = new RequestParams();
                params.add("event_uuid", eventUuid);
                params.add("interest_point_id", interestPoint.apiId);
                params.add("typ", interestPoint.type);
                client.addHeader("Authorization", "Bearer " + getToken());
                client.post(baseUrl+ "/api/group/" + communityUuid, params, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        subscriber.onNext(response);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        subscriber.onError(new ThrowableWithJson(throwable, errorResponse));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseText, Throwable throwable) {
                        subscriber.onError(new ThrowableWithJson(throwable, null));
                    }
                });
            }
        });
    }

    public Observable<JSONObject> quitGroup(final String communityUuid, final String eventUuid, final InterestPoint interestPoint) {
        return Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(final Subscriber<? super JSONObject> subscriber) {
                RequestParams params = new RequestParams();
                client.addHeader("Authorization", "Bearer " + getToken());
                client.delete(baseUrl + "/api/group/" + communityUuid + "/" + eventUuid + "/" + interestPoint.apiId, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        subscriber.onNext(response);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        subscriber.onError(new ThrowableWithJson(throwable, errorResponse));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseText, Throwable throwable) {
                        subscriber.onError(new ThrowableWithJson(throwable, null));
                    }
                });
            }
        });
    }

    public Observable<List<Event>> getEvents(final String communityUuid, final Calendar calendar) {
        return Observable.create(new Observable.OnSubscribe<List<Event>>() {
            @Override
            public void call(final Subscriber<? super List<Event>> subscriber) {
                RequestParams params = new RequestParams();
                client.addHeader("Authorization", "Bearer " + getToken());
                String url = baseUrl+"/api/event/" + communityUuid + "/" + calendar.getTime().getTime();
                client.get(url, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        try {
                            List<Event> interestPoints = Event.fromJson(response);
                            subscriber.onNext(interestPoints);
                            subscriber.onCompleted();
                        } catch (JSONException e) {
                            subscriber.onError(new ThrowableWithJson(e, null));
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        subscriber.onError(new ThrowableWithJson(throwable, errorResponse));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseText, Throwable throwable) {
                        subscriber.onError(new ThrowableWithJson(throwable, null));
                    }
                });
            }
        });
    }

    public void setDefaultCommunity(Community community, final Callback<JSONObject> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams params = new RequestParams();
        client.addHeader("Authorization", "Bearer " + getToken());
        client.put(baseUrl + "/api/user/community/" + community.uuid, params, new JsonHttpResponseHandler() {
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

    public void addGcmToken(String gcmToken, final Callback<JSONObject> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams params = new RequestParams();
        params.add("gsm_token", gcmToken);
        client.addHeader("Authorization", "Bearer " + getToken());
        client.put(baseUrl + "/api/user/addgcmtoken", params, new JsonHttpResponseHandler() {
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

    public Observable<List<InterestPoint>> getInterestPointsByLocation(final Location location, final String eventUuid, final String communityUuid) {
        return Observable.create(new Observable.OnSubscribe<List<InterestPoint>>() {
            @Override
            public void call(final Subscriber<? super List<InterestPoint>> subscriber) {
                RequestParams requestParams = new RequestParams();
                client.addHeader("Authorization", "Bearer " + getToken());
                String url = baseUrl + "/api/interest/point/" + communityUuid + "/" + eventUuid + "?lat=" + location.getLatitude() + "&lng=" + location.getLongitude();
                client.get(url, requestParams,  new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        try {
                            List<InterestPoint> interestPoints = InterestPoint.fromJson(response);
                            subscriber.onNext(interestPoints);
                            subscriber.onCompleted();
                        } catch (JSONException e) {
                            subscriber.onError(e);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        subscriber.onError(throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseText, Throwable throwable) {
                        subscriber.onError(throwable);
                    }
                });
            }
        });
    }

    public Observable<List<InterestPoint>> getInterestPoints(final String eventUuid, final String communityUuid) {
        return Observable.create(new Observable.OnSubscribe<List<InterestPoint>>() {
            @Override
            public void call(final Subscriber<? super List<InterestPoint>> subscriber) {
                RequestParams requestParams = new RequestParams();
                client.addHeader("Authorization", "Bearer " + getToken());
                String url = baseUrl + "/api/interest/point/" + communityUuid + "/" + eventUuid;
                client.get(url, requestParams,  new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONArray response) {
                        try {
                            List<InterestPoint> interestPoints = InterestPoint.fromJson(response);
                            subscriber.onNext(interestPoints);
                            subscriber.onCompleted();
                        } catch (JSONException e) {
                            subscriber.onError(e);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        subscriber.onError(throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseText, Throwable throwable) {
                        subscriber.onError(throwable);
                    }
                });
            }
        });
    }

    public void getAllCommunities(final Callback<JSONArray> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams params = new RequestParams();
        client.addHeader("Authorization", "Bearer " + getToken());
        String url = baseUrl+"/api/community/search";
        client.get(url, params, new JsonHttpResponseHandler() {
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

    public void getCommunitiesByQuery(String query, final Callback<JSONArray> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams requestParams = new RequestParams();
        client.addHeader("Authorization", "Bearer " + getToken());
        client.get(baseUrl + "/api/community/search?query=" + query, requestParams,  new JsonHttpResponseHandler() {
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

    public void addMemberInCommunity(String uuid, final Callback<JSONObject> success, final Callback2<Throwable, JSONObject> failure) {
        RequestParams params = new RequestParams();
        client.addHeader("Authorization", "Bearer " + getToken());
        String url = baseUrl + "/api/member/" + uuid + "/member";
        client.post(url, params, new JsonHttpResponseHandler() {
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

    public void getInterestPointDetails(
            InterestPoint interestPoint,
            String eventUuid,
            String communityUuid,
            final Callback<JSONObject> success,
            final Callback2<Throwable, JSONObject> failure
    ) {
        RequestParams params = new RequestParams();
        client.addHeader("Authorization", "Bearer " + getToken());
        String url = baseUrl + "/api/interest/point/" + communityUuid + "/" + eventUuid + "/" + interestPoint.apiId + "/" + interestPoint.type;
        client.get(url, params, new JsonHttpResponseHandler() {
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

    public Observable<JSONObject> unvoteGroup(final String communityUuid, final String eventUuid, final InterestPoint interestPoint) {
        return Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(final Subscriber<? super JSONObject> subscriber) {
                RequestParams params = new RequestParams();
                client.addHeader("Authorization", "Bearer " + getToken());
                client.delete(baseUrl + "/api/vote/" + communityUuid + "/" + eventUuid + "/" + interestPoint.apiId, params, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        subscriber.onNext(response);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        subscriber.onError(new ThrowableWithJson(throwable, errorResponse));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseText, Throwable throwable) {
                        subscriber.onError(new ThrowableWithJson(throwable, null));
                    }
                });
            }
        });
    }

    public Observable<JSONObject> voteGroup(final String communityUuid, final String eventUuid, final InterestPoint interestPoint) {
        return Observable.create(new Observable.OnSubscribe<JSONObject>() {
            @Override
            public void call(final Subscriber<? super JSONObject> subscriber) {
                RequestParams params = new RequestParams();
                params.add("event_uuid", eventUuid);
                params.add("interest_point_id", interestPoint.apiId);
                params.add("typ", interestPoint.type);
                client.addHeader("Authorization", "Bearer " + getToken());
                client.post(baseUrl+ "/api/vote/" + communityUuid, params, new JsonHttpResponseHandler(){
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        subscriber.onNext(response);
                        subscriber.onCompleted();
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        subscriber.onError(new ThrowableWithJson(throwable, errorResponse));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseText, Throwable throwable) {
                        subscriber.onError(new ThrowableWithJson(throwable, null));
                    }
                });
            }
        });
    }

    public Observable<Profile> getProfile() {
        return Observable.create(new Observable.OnSubscribe<Profile>() {
            @Override
            public void call(final Subscriber<? super Profile> subscriber) {
                RequestParams requestParams = new RequestParams();
                client.addHeader("Authorization", "Bearer " + getToken());
                String url = baseUrl + "/api/user";
                client.get(url, requestParams, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {
                        try {
                            subscriber.onNext(Profile.fromJson(response));
                            subscriber.onCompleted();
                        } catch (JSONException e) {
                            subscriber.onError(e);
                        }
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable, JSONObject errorResponse) {
                        subscriber.onError(throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String responseText, Throwable throwable) {
                        subscriber.onError(throwable);
                    }
                });
            }
        });
    }
}
