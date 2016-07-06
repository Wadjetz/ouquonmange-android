package fr.oqom.ouquonmange;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.google.android.gms.common.GoogleApiAvailability;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class GPSLicenceActivity extends AppCompatActivity {

    private TextView googlePlayServiceLicenceText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gpslicence);

        googlePlayServiceLicenceText = (TextView) findViewById(R.id.google_play_service_licence_text);
        googlePlayServiceLicenceText.setText("Loading");

        getLicence().subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Action1<String>() {
            @Override
            public void call(String licence) {
                googlePlayServiceLicenceText.setText(licence);
            }
        });
    }


    private Observable<String> getLicence() {
        return Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                String licence = GoogleApiAvailability.getInstance().getOpenSourceSoftwareLicenseInfo(getApplicationContext());
                subscriber.onNext(licence);
                subscriber.onCompleted();
            }
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }
}
