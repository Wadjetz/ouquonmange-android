package fr.oqom.ouquonmange;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import java.util.regex.Pattern;

import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.services.OuquonmangeApi;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class CreateCommunityActivity extends AppCompatActivity {

    private Button saveAction;
    private TextInputLayout titleLayout;
    private EditText titleInput, descriptionInput;

    private OuquonmangeApi api;
    private int minLengthName = Constants.MIN_LENGTH_NAME_COMMUNITY;
    private int maxLengthName = Constants.MAX_LENGTH_NAME_COMMUNITY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_community);

        titleLayout = (TextInputLayout) findViewById(R.id.layout_community_title);

        titleInput = (EditText) findViewById(R.id.input_community_title);
        descriptionInput = (EditText) findViewById(R.id.input_community_description);

        saveAction = (Button) findViewById(R.id.action_create_community);

        api = new OuquonmangeApi(getApplicationContext());

        saveAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitCommunity();
            }
        });
    }

    private void submitCommunity() {
        String name = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        if (validateName(name)) {
            api.createCommunity(name, description, new Callback<JSONObject>() {
                @Override
                public void apply(JSONObject value) {
                    Toast.makeText(getApplicationContext(), "Community Created", Toast.LENGTH_SHORT).show();
                    onBackPressed();
                }
            }, new Callback2<Throwable, JSONObject>() {
                @Override
                public void apply(Throwable throwable, JSONObject jsonObject) {
                    Toast.makeText(getApplicationContext(), throwable.getMessage() + " " + jsonObject.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private boolean validateName(String name) {
        if (name.isEmpty()) {
            titleLayout.setError(getString(R.string.error_field_required));
            requestFocus(titleInput);
            return false;
        }
        else if (name.length()>maxLengthName || name.length()<minLengthName ){
            titleLayout.setError(getString(R.string.error_invalid_titleOfCommunity) +" ( beetween "+minLengthName+" and "+maxLengthName+" caracters )");
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
