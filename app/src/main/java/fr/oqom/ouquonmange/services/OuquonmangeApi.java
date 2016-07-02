package fr.oqom.ouquonmange.services;

import android.content.Context;
import android.location.Location;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

import cz.msebera.android.httpclient.Header;
import fr.oqom.ouquonmange.models.AuthRepository;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.InterestPoint;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;
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
}
