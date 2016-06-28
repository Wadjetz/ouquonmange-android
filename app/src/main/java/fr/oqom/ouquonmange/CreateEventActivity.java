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
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Calendar;

import fr.oqom.ouquonmange.dialogs.DateTimePickerDialog;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.services.OuquonmangeApi;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class CreateEventActivity extends AppCompatActivity {

    private static final String LOG_TAG = "CreateEventActivity";

    private int minLengthName = Constants.MIN_LENGTH_NAME_COMMUNITY;
    private int maxLengthName = Constants.MAX_LENGTH_NAME_COMMUNITY;

    private TextInputLayout titleLayout, layoutDateStart, layoutDateEnd;
    private EditText titleInput, descriptionInput, dateStartInput, dateEndInput;
    private Button saveEventAction;
    private Calendar day = Calendar.getInstance();
    private Calendar dateStart = Calendar.getInstance();
    private Calendar dateEnd = Calendar.getInstance();

    private OuquonmangeApi api;

    private String communityUuid;

    private ProgressBar progressBar;
    private Snackbar snackbar;

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putLong(Constants.EVENT_DATE, day.getTimeInMillis());
        outState.putString(Constants.COMMUNITY_UUID, communityUuid);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        day = Calendar.getInstance();
        day.setTimeInMillis(savedInstanceState.getLong(Constants.EVENT_DATE));
        communityUuid = savedInstanceState.getString(Constants.COMMUNITY_UUID);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        Intent intent = getIntent();
        communityUuid =  intent.getStringExtra(Constants.COMMUNITY_UUID);
        day.setTimeInMillis(intent.getLongExtra(Constants.EVENT_DATE, Calendar.getInstance().getTimeInMillis()));

        this.dateStart.setTimeInMillis(day.getTimeInMillis());
        this.dateEnd.setTimeInMillis(day.getTimeInMillis());

        initLayoutWidgets();
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
                        dateStart.set(Calendar.HOUR_OF_DAY, hours);
                        dateStart.set(Calendar.MINUTE, minutes);
                        dateStartInput.setText(Constants.timeFormat.format(dateStart.getTime()));
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
                        dateEnd.set(Calendar.HOUR_OF_DAY, hours);
                        dateEnd.set(Calendar.MINUTE, minutes);
                        dateEndInput.setText(Constants.timeFormat.format(dateEnd.getTime()));
                    }
                });
                dateTimePickerDialog.show(getFragmentManager(), "date_time_end_picker");
            }
        });

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLayout);

        snackbar = Snackbar.make(coordinatorLayout, R.string.no_internet, Snackbar.LENGTH_LONG);

        snackbar.setAction(getText(R.string.close), closeSnackBarEvent);

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
            api.createEvent(communityUuid, name, description, dateStart, dateEnd, new Callback<JSONObject>() {
                @Override
                public void apply(JSONObject jsonObject) {
                    Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                    intent.putExtra(Constants.COMMUNITY_UUID, communityUuid);
                    intent.putExtra(Constants.EVENT_DATE, day.getTimeInMillis());
                    startActivity(intent);
                    finish();
                }
            }, new Callback2<Throwable, JSONObject>() {
                @Override
                public void apply(Throwable throwable, JSONObject jsonObject) {
                    if (jsonObject != null) {
                        Log.e(LOG_TAG, jsonObject.toString());
                        snackbar.setText(R.string.event_created).setActionTextColor(Color.parseColor("#D32F2F")).show();
                    } else {
                        Log.e(LOG_TAG, throwable.getMessage());
                        snackbar.setText(throwable.getMessage()).setActionTextColor(Color.parseColor("#D32F2F")).show();
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });

        } else {
            snackbar.setText(getText(R.string.create_event_error_validation)).setActionTextColor(Color.parseColor("#D32F2F")).show();
        }
    }

    private void initLayoutWidgets() {
        titleLayout = (TextInputLayout) findViewById(R.id.layout_event_title);
        layoutDateStart = (TextInputLayout) findViewById(R.id.layout_event_date_start);
        layoutDateEnd = (TextInputLayout) findViewById(R.id.layout_event_date_end);

        titleInput = (EditText) findViewById(R.id.input_event_title);
        descriptionInput = (EditText) findViewById(R.id.input_event_description);
        dateStartInput = (EditText) findViewById(R.id.input_event_date_start);
        dateEndInput = (EditText) findViewById(R.id.input_event_date_end);

        saveEventAction = (Button) findViewById(R.id.action_create_event);

        saveEventAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitEvent();
            }
        });
    }

    private boolean validateFormCreateEvent() {
        boolean flag = true, dateStartIsEmpty = true , dateEndIsEmpty = true;

        String title = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();
        String dateStart = dateStartInput.getText().toString();
        String dateEnd = dateEndInput.getText().toString();
        Calendar dateNow = Calendar.getInstance();

        if (title.isEmpty()) {
            titleLayout.setError(getString(R.string.error_field_required));
            if (flag) {
                requestFocus(titleInput);
            }
            flag = false;
        } else if (title.length() > maxLengthName || title.length() < minLengthName ){
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
            if (this.dateStart.before(dateNow)) {
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
            if (this.dateEnd.before(dateNow)) {
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
            if (this.dateStart.after(this.dateEnd)) {
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
        intent.putExtra(Constants.EVENT_DATE, day.getTimeInMillis());
        startActivity(intent);
        finish();
        super.onBackPressed();
    }

    protected void hiddenVirtualKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

    }
}
