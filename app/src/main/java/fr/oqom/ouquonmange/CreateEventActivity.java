package fr.oqom.ouquonmange;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Calendar;
import org.joda.time.DateTime;
import org.json.JSONException;

import fr.oqom.ouquonmange.dialogs.DateTimePickerDialog;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.Event;
import fr.oqom.ouquonmange.services.OuquonmangeApi;
import fr.oqom.ouquonmange.services.ThrowableWithJson;
import fr.oqom.ouquonmange.utils.Callback2;
import fr.oqom.ouquonmange.utils.TimeUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class CreateEventActivity extends AppCompatActivity {

    private static final String LOG_TAG = "CreateEventActivity";

    private TextInputLayout titleLayout, layoutDateStart, layoutDateEnd, layoutDayStart, layoutDayEnd;
    private EditText titleInput, descriptionInput, dateStartInput, dateEndInput, dayStartInput, dayEndInput;
    private Button saveEventAction;
    private CoordinatorLayout coordinatorLayout;

    private DateTime day = TimeUtils.now();
    private DateTime dateStart = TimeUtils.now();
    private DateTime dateEnd = TimeUtils.now();

    private OuquonmangeApi api;

    private String communityUuid;

    private ProgressBar progressBar;
    private Snackbar snackbar;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(Constants.EVENT_DATE, day.getMillis());
        outState.putString(Constants.COMMUNITY_UUID, communityUuid);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        day = new DateTime(savedInstanceState.getLong(Constants.EVENT_DATE));
        communityUuid = savedInstanceState.getString(Constants.COMMUNITY_UUID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        initLayoutWidgets();
        Intent intent = getIntent();
        communityUuid =  intent.getStringExtra(Constants.COMMUNITY_UUID);
        day = TimeUtils.getDateTime(intent.getLongExtra(Constants.EVENT_DATE, TimeUtils.now().getMillis()));

        this.dateStart = day.toDateTime();
        this.dateEnd = day.toDateTime();

        this.dayStartInput.setText(TimeUtils.printDate(day, getApplicationContext()));
        this.dayEndInput.setText(TimeUtils.printDate(day, getApplicationContext()));

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        snackbar = Snackbar.make(coordinatorLayout, R.string.no_internet, Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close), closeSnackBarEvent);


        progressBar = (ProgressBar) findViewById(R.id.progress);
        api = new OuquonmangeApi(getApplicationContext());
        progressBar.setVisibility(View.GONE);

        dateStartInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DateTimePickerDialog dateTimePickerDialog = new DateTimePickerDialog();
                dateTimePickerDialog.setCallback(new Callback2<Integer, Integer>() {
                    @Override
                    public void apply(Integer hours, Integer minutes) {
                        dateStart = dateStart.withHourOfDay(hours).withMinuteOfHour(minutes);
                        dateStartInput.setText(TimeUtils.printTime(dateStart, getApplicationContext()));
                    }
                });
                dateTimePickerDialog.show(getFragmentManager(), "date_time_start_picker");
            }
        });

        dateEndInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DateTimePickerDialog dateTimePickerDialog = new DateTimePickerDialog();
                dateTimePickerDialog.setCallback(new Callback2<Integer, Integer>() {
                    @Override
                    public void apply(Integer hours, Integer minutes) {
                        dateEnd = dateEnd.withHourOfDay(hours).withMinuteOfHour(minutes);
                        dateEndInput.setText(TimeUtils.printTime(dateEnd, getApplicationContext()));
                    }
                });
                dateTimePickerDialog.show(getFragmentManager(), "date_time_end_picker");
            }
        });
    }

    private void initLayoutWidgets() {
        titleLayout = (TextInputLayout) findViewById(R.id.layout_event_title);
        layoutDateStart = (TextInputLayout) findViewById(R.id.layout_event_date_start);
        layoutDateEnd = (TextInputLayout) findViewById(R.id.layout_event_date_end);

        layoutDayEnd = (TextInputLayout) findViewById(R.id.layout_event_day_date_end);
        layoutDateEnd = (TextInputLayout) findViewById(R.id.layout_event_day_date_start);

        titleInput = (EditText) findViewById(R.id.input_event_title);
        descriptionInput = (EditText) findViewById(R.id.input_event_description);
        dateStartInput = (EditText) findViewById(R.id.input_event_date_start);
        dateEndInput = (EditText) findViewById(R.id.input_event_date_end);

        dayStartInput = (EditText) findViewById(R.id.input_event_day_date_end);
        dayEndInput = (EditText) findViewById(R.id.input_event_day_date_start);

        saveEventAction = (Button) findViewById(R.id.action_create_event);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        saveEventAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitEvent();
            }
        });
    }

    private View.OnClickListener closeSnackBarEvent = new View.OnClickListener() {
        @Override
        public void onClick(View view) {
            snackbar.dismiss();
        }
    };

    private void submitEvent() {
        final String name = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();

        if(validateFormCreateEvent()) {
            hiddenVirtualKeyboard();
            progressBar.setVisibility(View.VISIBLE);
            if(checkConnection(getApplicationContext())) {

                api.createEvent(communityUuid, name, description, dateStart, dateEnd)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Event>() {
                            @Override
                            public void call(Event event) {
                                Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                                intent.putExtra(Constants.COMMUNITY_UUID, communityUuid);
                                intent.putExtra(Constants.EVENT_DATE, day.getMillis());
                                startActivity(intent);
                                finish();
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                ThrowableWithJson throwableWithJson = (ThrowableWithJson) throwable;
                                showApiError(throwableWithJson);
                                progressBar.setVisibility(View.GONE);
                            }
                        });
            }else{
                refreshSnackBar();
            }

        } else {
            snackbar.setText(getText(R.string.create_event_error_validation)).setActionTextColor(Color.parseColor("#D32F2F")).show();
        }
    }

    private boolean validateFormCreateEvent() {
        boolean flag = true, dateStartIsEmpty = true , dateEndIsEmpty = true;

        String title = titleInput.getText().toString();
        String dateStart = dateStartInput.getText().toString();
        String dateEnd = dateEndInput.getText().toString();

        DateTime dateNow = TimeUtils.now();

        int minLengthName = Constants.MIN_LENGTH_NAME_COMMUNITY;
        int maxLengthName = Constants.MAX_LENGTH_NAME_COMMUNITY;
        if (title.isEmpty()) {
            titleLayout.setError(getString(R.string.error_field_required));
            if (flag) {
                requestFocus(titleInput);
            }
            flag = false;
        } else if (title.length() > maxLengthName || title.length() < minLengthName) {
            titleLayout.setError(getString(R.string.error_invalid_titleOfCommunity) + " ( between " + minLengthName + " and " + maxLengthName + " characters )");
            if (flag) {
                requestFocus(titleInput);
            }
            flag = false;
        } else {
            titleLayout.setErrorEnabled(false);
        }

        if (dateStart.isEmpty()) {
            layoutDateStart.setError(getString(R.string.error_field_required));
            if (flag) {
                requestFocus(dateStartInput);
            }
            dateStartIsEmpty = false;
            flag = false;
        } else {
            layoutDateStart.setErrorEnabled(false);
        }

        if (dateEnd.isEmpty()) {
            layoutDateEnd.setError(getString(R.string.error_field_required));
            if (flag) {
                requestFocus(dateEndInput);
            }
            dateEndIsEmpty = false;
            flag = false;
        } else {
            layoutDateEnd.setErrorEnabled(false);
        }

        if (dateStartIsEmpty) {
            if (this.dateStart.isBefore(dateNow)) {
                layoutDateStart.setError(getString(R.string.error_start_date_in_the_past));
                if (flag) {
                    requestFocus(layoutDateStart);
                }
                flag = false;
            } else {
                layoutDateStart.setErrorEnabled(false);
            }
        }

        if (dateEndIsEmpty) {
            if (this.dateEnd.isBefore(dateNow)) {
                layoutDateEnd.setError(getString(R.string.error_end_date_in_the_past));
                if (flag) {
                    requestFocus(layoutDateEnd);
                }
                flag = false;
            } else {
                layoutDateEnd.setErrorEnabled(false);
            }
        }

        if (dateEndIsEmpty && dateStartIsEmpty) {
            if (this.dateStart.isAfter(this.dateEnd)) {
                layoutDateEnd.setError(getString(R.string.error_end_date_prior_start));
                if (flag) {
                    requestFocus(layoutDateEnd);
                }
                flag = false;
            } else {
                layoutDateEnd.setErrorEnabled(false);
            }
        }

        return flag;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
        intent.putExtra(Constants.COMMUNITY_UUID, communityUuid);
        intent.putExtra(Constants.EVENT_DATE, day.getMillis());
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    protected void hiddenVirtualKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private void showApiError(ThrowableWithJson throwableWithJson) {
        if (throwableWithJson.getJson() != null) {
            try {
                String serverError = throwableWithJson.getJson().getString("error");
                snackbar.setText(serverError).setActionTextColor(Color.parseColor("#D32F2F")).show();

            } catch (JSONException e) {
                snackbar.setText(e.getMessage()).setActionTextColor(Color.parseColor("#D32F2F")).show();
            }
        } else {
            snackbar.setText(throwableWithJson.getThrowable().getMessage()).setActionTextColor(Color.parseColor("#D32F2F")).show();
        }
    }

    public void refreshSnackBar(){
        snackbar.setText(R.string.no_internet)
                .setActionTextColor(Color.parseColor("#D32F2F"))
                .setDuration(Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.activate, activateSnackBarCreateEvent)
                .show();
        progressBar.setVisibility(View.GONE);

    }
    private View.OnClickListener activateSnackBarCreateEvent = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            CreateAlertSetting();
        }
    };

    private void CreateAlertSetting() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.setting_info)
                .setMessage(R.string.message_internet_not_available)
                .setCancelable(false)
                .setPositiveButton(R.string.activate_wifi_message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        final WifiManager wifi =(WifiManager)getSystemService(getApplicationContext().WIFI_SERVICE);
                        wifi.setWifiEnabled(true);
                        if(checkConnection(getApplicationContext())) {
                            reloadActivity();
                        }else {
                            refreshSnackBar();
                            dialog.dismiss();
                        }
                    }
                })
                .setNegativeButton(R.string.activate_data_mobile_message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        setEnableDataMobile(true);
                        if(checkConnection(getApplicationContext())) {
                            reloadActivity();
                        }else {
                            refreshSnackBar();
                            dialog.dismiss();
                        }
                    }
                })
                .setNeutralButton(R.string.cancel_message, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        refreshSnackBar();
                        dialog.cancel();
                    }
                })
                .create()
                .show();
    }

    private void reloadActivity() {
        Intent intent = getIntent();
        intent.putExtra(Constants.EVENT_DATE, day.getMillis());
        intent.putExtra(Constants.COMMUNITY_UUID, communityUuid);
        finish();
        startActivity(intent);
    }

    public void setEnableDataMobile(boolean enable){
        // Enable data
        ConnectivityManager dataManager;
        dataManager  = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        Method dataMtd = null;
        try {
            dataMtd = ConnectivityManager.class.getDeclaredMethod("setMobileDataEnabled", boolean.class);
            dataMtd.setAccessible(true);
            dataMtd.invoke(dataManager, enable);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    private boolean checkConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

        if (activeNetworkInfo != null) { // connected to the internet
            if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_WIFI ) {
                // connected to wifi
                if(activeNetworkInfo.isAvailable() && activeNetworkInfo.isConnected()) {
                    Log.i(LOG_TAG,"type wifi");
                    return true;
                }

            } else if (activeNetworkInfo.getType() == ConnectivityManager.TYPE_MOBILE) {
                // connected to the mobile provider's data plan
                if(activeNetworkInfo.isAvailable() && activeNetworkInfo.isConnected()) {
                    Log.i(LOG_TAG, "type data");
                    return true;
                }
            }
        }
        return false;
    }
}
