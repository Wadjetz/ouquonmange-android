package fr.oqom.ouquonmange;

import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import fr.oqom.ouquonmange.services.OuquonmangeApi;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class CreateCommunityActivity extends AppCompatActivity {

    private Button saveAction;
    private TextInputLayout titleLayout, descriptionLayout;
    private EditText titleInput, descriptionInput;

    private OuquonmangeApi api;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_community);

        titleLayout = (TextInputLayout) findViewById(R.id.layout_community_title);
        descriptionLayout = (TextInputLayout) findViewById(R.id.layout_community_description);

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
        // TODO Error checks
        String name = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        api.createCommunity(name, description, new Callback<JSONObject>() {
            @Override
            public void apply(JSONObject value) {
                Toast.makeText(getApplicationContext(),"Community Created", Toast.LENGTH_SHORT).show();
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
