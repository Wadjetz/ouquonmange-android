package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.Calendar;
import java.util.TimeZone;

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
                        dateStartInput.setText(Constants.simpleDateFormat.format(dateStart.getTime()));
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
                        dateEndInput.setText(Constants.simpleDateFormat.format(dateEnd.getTime()));
                    }
                });
                dateTimePickerDialog.show(getFragmentManager(), "date_time_end_picker");
            }
        });
    }

    private void submitEvent() {
        progressBar.setVisibility(View.VISIBLE);
        // TODO validate Data
        final String name = titleInput.getText().toString();
        String description = descriptionInput.getText().toString();

        if(validateTitle(name) && validateDate(dateStart,dateEnd)){
            api.createEvent(communityUuid, name, description, dateStart, dateEnd, new Callback<JSONObject>() {
                @Override
                public void apply(JSONObject jsonObject) {
                    Toast.makeText(getApplicationContext(), "Event Created", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                    intent.putExtra(Constants.COMMUNITY_UUID, communityUuid);
                    intent.putExtra(Constants.EVENT_DATE, day.getTimeInMillis());
                    startActivity(intent);
                    finish();
                }
            }, new Callback2<Throwable, JSONObject>() {
                @Override
                public void apply(Throwable throwable, JSONObject jsonObject) {
                    Toast.makeText(getApplicationContext(), "Event Created", Toast.LENGTH_SHORT).show();
                    if (jsonObject != null) {
                        Log.e(LOG_TAG, jsonObject.toString());
                        Toast.makeText(getApplicationContext(), jsonObject.toString(), Toast.LENGTH_SHORT).show();
                    }
                    Log.e(LOG_TAG, throwable.getMessage());
                    Toast.makeText(getApplicationContext(), throwable.getMessage(), Toast.LENGTH_SHORT).show();
                    progressBar.setVisibility(View.GONE);
                }
            });

        }else{
            Toast.makeText(getApplicationContext(), "Error in create", Toast.LENGTH_SHORT).show();
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

    private boolean validateTitle(String title) {
        if (title.isEmpty()) {
            titleLayout.setError(getString(R.string.error_field_required));
            requestFocus(titleInput);
            return false;
        }
        else if (title.length() > maxLengthName || title.length() < minLengthName ){
            titleLayout.setError(getString(R.string.error_invalid_titleOfCommunity) + " ( between " + minLengthName + " and " + maxLengthName + " characters )");
            requestFocus(titleInput);
            return false;
        } else {
            titleLayout.setErrorEnabled(false);
        }
        return true;

    }
    private boolean validateDate(Calendar dateStart, Calendar dateEnd) {
        Calendar dateNow = Calendar.getInstance();
        dateNow.setTimeZone(TimeZone.getTimeZone("GMT+2"));
        int dateStartHours = dateStart.getTime().getHours();
        int dateEndHours = dateEnd.getTime().getHours();
        int dateNowHours = dateNow.getTime().getHours() + 2 ; // GMT + 2
        int dateStartMinutes = dateStart.getTime().getMinutes();
        int dateEndMinutes = dateEnd.getTime().getMinutes();
        int dateNowMinutes = dateNow.getTime().getMinutes();
        layoutDateStart.setErrorEnabled(false);
        layoutDateEnd.setErrorEnabled(false);
        if((dateStartHours < dateNowHours) || (dateStartHours == dateNowHours && dateStartMinutes < dateNowMinutes)){
            layoutDateStart.setError(getString(R.string.error_invalid_date));
            requestFocus(layoutDateStart);
            return false;
        }
        if ((dateEndHours < dateNowHours) || (dateEndHours == dateNowHours && dateEndMinutes < dateNowMinutes)){
            layoutDateStart.setErrorEnabled(false);
            layoutDateEnd.setError(getString(R.string.error_invalid_date));
            requestFocus(layoutDateEnd);
            return false;
        }
        if( (dateEndHours<dateStartHours) || (dateStartHours == dateEndHours && dateEndMinutes < dateStartMinutes)){
            layoutDateEnd.setError(getString(R.string.error_invalid_date));
            requestFocus(layoutDateEnd);
            return false;
        }

        return true;




    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
}
