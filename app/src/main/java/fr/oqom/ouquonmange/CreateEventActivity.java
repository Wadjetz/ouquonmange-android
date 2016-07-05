package fr.oqom.ouquonmange;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.json.JSONException;

import fr.oqom.ouquonmange.dialogs.DatePickerDialogs;
import fr.oqom.ouquonmange.dialogs.DateTimePickerDialog;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.Event;
import fr.oqom.ouquonmange.services.OuQuOnMangeService;
import fr.oqom.ouquonmange.services.Service;
import fr.oqom.ouquonmange.services.ThrowableWithJson;
import fr.oqom.ouquonmange.utils.Callback2;
import fr.oqom.ouquonmange.utils.Callback3;
import fr.oqom.ouquonmange.utils.DateTimeUtils;
import fr.oqom.ouquonmange.utils.NetConnectionUtils;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class CreateEventActivity extends AppCompatActivity {

    private static final String LOG_TAG = "CreateEventActivity";

    private TextInputLayout titleLayout, layoutDateStart, layoutDateEnd, layoutDayStart, layoutDayEnd;
    private EditText titleInput, descriptionInput, dateStartInput, dateEndInput, dayStartInput, dayEndInput;
    private Button saveEventAction;
    private CoordinatorLayout coordinatorLayout;

    private DateTime day = DateTimeUtils.now();
    private DateTime dateStart = DateTimeUtils.now();
    private DateTime dateEnd = DateTimeUtils.now();

    private OuQuOnMangeService ouQuOnMangeService;

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
        initView();
        Intent intent = getIntent();
        communityUuid = intent.getStringExtra(Constants.COMMUNITY_UUID);
        day = DateTimeUtils.getDateTime(intent.getLongExtra(Constants.EVENT_DATE, DateTimeUtils.now().getMillis()));

        ouQuOnMangeService = Service.getInstance(getApplicationContext());

        this.dateStart = day.toDateTime();
        this.dateEnd = day.toDateTime();

        this.dayStartInput.setText(DateTimeUtils.printDate(day, getApplicationContext()));
        this.dayEndInput.setText(DateTimeUtils.printDate(day, getApplicationContext()));

        snackbar = Snackbar.make(coordinatorLayout, R.string.no_internet, Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close), closeSnackBarEvent);

        progressBar.setVisibility(View.GONE);

        dateStartInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DateTimePickerDialog dateTimePickerDialog = new DateTimePickerDialog();
                dateTimePickerDialog.setCallback(new Callback2<Integer, Integer>() {
                    @Override
                    public void apply(Integer hours, Integer minutes) {
                        dateStart = dateStart.withHourOfDay(hours).withMinuteOfHour(minutes);
                        dateStartInput.setText(DateTimeUtils.printTime(dateStart, getApplicationContext()));
                    }
                });
                dateTimePickerDialog.show(getFragmentManager(), "date_time_start_picker");
            }
        });

        showDialogWhenHasFocus(dateStartInput);

        dayStartInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatePickerDialogs datePickerDialogs = new DatePickerDialogs();
                datePickerDialogs.setCallback(new Callback3<Integer, Integer, Integer>() {
                    @Override
                    public void apply(Integer year, Integer month, Integer day) {
                        dateStart = dateStart.withYear(year).withMonthOfYear(month+1).withDayOfMonth(day);
                        dayStartInput.setText(DateTimeUtils.printDate(dateStart,getApplicationContext()));
                    }
                });
                datePickerDialogs.show(getFragmentManager(), "day_start_picker");
            }
        });

        showDialogWhenHasFocus(dayStartInput);

        dateEndInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DateTimePickerDialog dateTimePickerDialog = new DateTimePickerDialog();
                dateTimePickerDialog.setCallback(new Callback2<Integer, Integer>() {
                    @Override
                    public void apply(Integer hours, Integer minutes) {
                        dateEnd = dateEnd.withHourOfDay(hours).withMinuteOfHour(minutes);
                        dateEndInput.setText(DateTimeUtils.printTime(dateEnd, getApplicationContext()));
                    }
                });
                dateTimePickerDialog.show(getFragmentManager(), "date_time_end_picker");
            }
        });

        showDialogWhenHasFocus(dateEndInput);

        dayEndInput.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final DatePickerDialogs datePickerDialogs = new DatePickerDialogs();
                datePickerDialogs.setCallback(new Callback3<Integer, Integer, Integer>() {
                    @Override
                    public void apply(Integer year, Integer month, Integer day) {
                        dateEnd = dateEnd.withYear(year).withMonthOfYear(month+1).withDayOfMonth(day);
                        dayEndInput.setText(DateTimeUtils.printDate(dateEnd,getApplicationContext()));
                    }
                });
                datePickerDialogs.show(getFragmentManager(), "day_end_picker");
            }
        });

        showDialogWhenHasFocus(dayEndInput);

        snackbar = Snackbar.make(coordinatorLayout, R.string.no_internet, Snackbar.LENGTH_LONG);

        snackbar.setAction(getText(R.string.close), closeSnackBarEvent);

    }

    private void showDialogWhenHasFocus(final EditText input) {
        input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(hasFocus && input.getText().length() == 0){
                    input.callOnClick();
                }
            }
        });
    }

    private void initView() {
        titleLayout = (TextInputLayout) findViewById(R.id.layout_event_title);
        layoutDateStart = (TextInputLayout) findViewById(R.id.layout_event_date_start);
        layoutDateEnd = (TextInputLayout) findViewById(R.id.layout_event_date_end);

        layoutDayEnd = (TextInputLayout) findViewById(R.id.layout_event_day_end);
        layoutDayStart = (TextInputLayout) findViewById(R.id.layout_event_day_start);

        titleInput = (EditText) findViewById(R.id.input_event_title);
        descriptionInput = (EditText) findViewById(R.id.input_event_description);
        dateStartInput = (EditText) findViewById(R.id.input_event_date_start);
        dateEndInput = (EditText) findViewById(R.id.input_event_date_end);

        dayStartInput = (EditText) findViewById(R.id.input_event_day_date_start);
        dayEndInput = (EditText) findViewById(R.id.input_event_day_date_end);

        saveEventAction = (Button) findViewById(R.id.action_create_event);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);
        progressBar = (ProgressBar) findViewById(R.id.progress);

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
        hiddenVirtualKeyboard();
        if (validateFormCreateEvent()) {
            if (NetConnectionUtils.isConnected(getApplicationContext())) {
                progressBar.setVisibility(View.VISIBLE);
                ouQuOnMangeService.createEvent(communityUuid, new Event(name, description, dateStart.getMillis(), dateEnd.getMillis()))
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
                                throwable.printStackTrace();
                                if (throwable instanceof HttpException) {
                                    HttpException response = (HttpException) throwable;
                                    int code = response.code();
                                    Log.e(LOG_TAG, "RETROFIT ERROR code = " + response.response().errorBody().toString());

                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            } else {
                NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, this);
            }

        } else {
            snackbar.setText(getText(R.string.create_event_error_validation)).setActionTextColor(Color.parseColor("#D32F2F")).show();
        }
    }

    private boolean validateFormCreateEvent() {
        boolean flag = true, timeStartIsEmpty = false , timeEndIsEmpty = false,
        dayStartIsEmpty = false , dayEndIsEmpty = false;

        String title = titleInput.getText().toString();
        String timeStart = dateStartInput.getText().toString();
        String timeEnd = dateEndInput.getText().toString();
        String dayStart = dayStartInput.getText().toString();
        String dayEnd = dayEndInput.getText().toString();

        DateTime dateNow = DateTimeUtils.now();

        int minLengthName = Constants.MIN_LENGTH_NAME_COMMUNITY;
        int maxLengthName = Constants.MAX_LENGTH_NAME_COMMUNITY;
        if (title.isEmpty()) {
            titleLayout.setError(getString(R.string.error_field_required));
            if (flag) {
                requestFocus(titleInput);
            }
            flag = false;
        } else if (title.length() > maxLengthName || title.length() < minLengthName) {
            titleLayout.setError(getString(R.string.error_invalid_titleOfCommunity_length));
            if (flag) {
                requestFocus(titleInput);
            }
            flag = false;
        } else {
            titleLayout.setErrorEnabled(false);
        }

        if(dayStart.isEmpty()){
            layoutDayStart.setError(getString(R.string.error_field_required));
            /*if(flag){
                requestFocus(dayStartInput);
            }*/
            dayStartIsEmpty = false;
            flag = false;
        }

        if (timeStart.isEmpty()) {
            layoutDateStart.setError(getString(R.string.error_field_required));
            /*if (flag) {
                requestFocus(dateStartInput);
            }*/
            timeStartIsEmpty = true;
            flag = false;
        } else {
            layoutDateStart.setErrorEnabled(false);
        }

        if(dayEnd.isEmpty()){
            layoutDayEnd.setError(getString(R.string.error_field_required));
            /*if(flag){
                requestFocus(dayEndInput);
            }*/
            timeEndIsEmpty = true;
            flag = false;
        }

        if (timeEnd.isEmpty()) {
            layoutDateEnd.setError(getString(R.string.error_field_required));
            /*if (flag) {
                requestFocus(dateEndInput);
            }*/
            timeEndIsEmpty = true;
            flag = false;
        } else {
            layoutDateEnd.setErrorEnabled(false);
        }

        if (!dayStartIsEmpty && !timeStartIsEmpty) {
            if (this.dateStart.isBefore(dateNow)) {
                layoutDateStart.setError(getString(R.string.error_start_date_in_the_past));
                /*if (flag) {
                    requestFocus(layoutDateStart);
                }*/
                flag = false;
            } else {
                layoutDateStart.setErrorEnabled(false);
            }
        }

        if (!dayEndIsEmpty && !timeEndIsEmpty) {
            if (this.dateEnd.isBefore(dateNow)) {
                layoutDateEnd.setError(getString(R.string.error_end_date_in_the_past));
                /*if (flag) {
                    requestFocus(layoutDateEnd);
                }*/
                flag = false;
            } else {
                layoutDateEnd.setErrorEnabled(false);
            }
        }
        boolean allTrue = !dayEndIsEmpty && !dayStartIsEmpty && !timeEndIsEmpty && !timeStartIsEmpty;
        if (allTrue) {
            if (this.dateStart.isAfter(this.dateEnd)) {
                layoutDateEnd.setError(getString(R.string.error_end_date_prior_start));
                /*if (flag) {
                    requestFocus(layoutDateEnd);
                }*/
                flag = false;
            } else {
                layoutDateEnd.setErrorEnabled(false);
            }
        }

        if(allTrue){
            Period period = new Period(dateStart,dateEnd);
            long diff = Constants.DIFFERENT_HOURS;
            long per  = period.toStandardSeconds().getSeconds();
            if( diff <= per ){
                layoutDateEnd.setError(getString(R.string.error_different_number_of_hours_between_dates));
            }else{
                Log.e(LOG_TAG,diff+" - "+per);
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

    protected void hiddenVirtualKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
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

    private void showErrorSnackBar(CharSequence message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }
}
