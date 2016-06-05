package fr.oqom.ouquonmange;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.services.OuquonmangeApi;

public class CreateEventActivity extends AppCompatActivity {

    private int minLengthName = Constants.MIN_LENGTH_NAME_COMMUNITY;
    private int maxLengthName = Constants.MAX_LENGTH_NAME_COMMUNITY;

    private TextInputLayout titleLayout, layoutDateStart, layoutDateEnd;
    private EditText titleInput, descriptionInput, dateStartInput, dateEndInput;
    private Button saveEventAction;

    private OuquonmangeApi api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_event);
        initLayoutWidgets();
        api = new OuquonmangeApi(getApplicationContext());
    }

    private void submitEvent() {
        Toast.makeText(getApplicationContext(), "TODO Event Created", Toast.LENGTH_SHORT).show();
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

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
}
