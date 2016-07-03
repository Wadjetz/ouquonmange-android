package fr.oqom.ouquonmange.services;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.joda.time.DateTime;

import java.io.IOException;

import fr.oqom.ouquonmange.models.AuthRepository;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.utils.DateTimeConverter;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import rx.schedulers.Schedulers;

public class Service {
    public static OuQuOnMangeService getInstance(Context context) {

        Gson gson = new GsonBuilder().registerTypeAdapter(DateTime.class, new DateTimeConverter()).create();

        final AuthRepository authRepository = new AuthRepository(context);
        final String token = authRepository.getToken();

        Interceptor interceptor = new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request newRequest = chain.request().newBuilder().addHeader("Authorization", "Bearer " + token).build();
                return chain.proceed(newRequest);
            }
        };

        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.interceptors().add(interceptor);
        OkHttpClient client = builder.build();

        Retrofit retrofit = new Retrofit.Builder()
                .addCallAdapterFactory(RxJavaCallAdapterFactory.createWithScheduler(Schedulers.io()))
                .addConverterFactory(GsonConverterFactory.create(gson))
                .baseUrl(Constants.OQOM_BASE_URL)
                .client(client)
                .build();

        return retrofit.create(OuQuOnMangeService.class);
    }
}
