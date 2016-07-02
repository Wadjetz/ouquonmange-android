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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;

import org.json.JSONException;
import org.json.JSONObject;

import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.services.OuquonmangeApi;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;
import fr.oqom.ouquonmange.utils.NetConnectionUtils;

public class CreateCommunityActivity extends AppCompatActivity {
    private static final String LOG_TAG = "CreateCommunityActivity";

    private Button saveAction;
    private TextInputLayout titleLayout, descriptionLayout;
    private EditText titleInput, descriptionInput;

    private OuquonmangeApi api;
    private int minLengthName = Constants.MIN_LENGTH_NAME_COMMUNITY;
    private int maxLengthName = Constants.MAX_LENGTH_NAME_COMMUNITY;
    private Snackbar snackbar;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_community);

        titleLayout = (TextInputLayout) findViewById(R.id.layout_community_title);
        titleInput = (EditText) findViewById(R.id.input_community_title);
        descriptionLayout = (TextInputLayout) findViewById(R.id.layout_community_description);
        descriptionInput = (EditText) findViewById(R.id.input_community_description);
        saveAction = (Button) findViewById(R.id.action_create_community);

        api = new OuquonmangeApi(getApplicationContext());

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorCreateCommunityLayout);
        snackbar = Snackbar.make(coordinatorLayout, "Error !", Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close), closeSnackBarCreateCommunity);

        saveAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitCommunity();
            }
        });


    }

    private View.OnClickListener closeSnackBarCreateCommunity = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            snackbar.dismiss();
        }
    };

    private void submitCommunity() {
        String name = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        hiddenVirtualKeyboard();
        if (validateForm(name, description)) {
            if (NetConnectionUtils.isConnected(getApplicationContext())) {
                api.createCommunity(name, description, new Callback<JSONObject>() {
                    @Override
                    public void apply(JSONObject value) {
                        if (value != null) {
                            Log.i(LOG_TAG, value.toString());
                            snackbar.setText(R.string.community_created).setActionTextColor(Color.parseColor("#D32F2F")).show();
                            startActivity(new Intent(getApplicationContext(), MainActivity.class));
                            finish();
                        } else {
                            snackbar.setText(R.string.error_exception).setActionTextColor(Color.parseColor("#D32F2F")).show();
                        }
                    }
                }, new Callback2<Throwable, JSONObject>() {
                    @Override
                    public void apply(Throwable throwable, JSONObject error) {
                        String err = "";
                        if (error != null) {
                            try {
                                err = error.getString("error");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            snackbar.setText(err).setActionTextColor(Color.parseColor("#D32F2F")).show();
                        } else {
                            snackbar.setText(R.string.error_exception).setActionTextColor(Color.parseColor("#D32F2F")).show();
                        }
                    }
                });
            } else {
                NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, this);
            }
        } else {
            snackbar.setText(getText(R.string.error_invalid_fields)).setActionTextColor(Color.parseColor("#D32F2F")).show();
        }

    }

    private boolean validateForm(String name, String description) {
        boolean flag = true;

        if (name.isEmpty()) {
            titleLayout.setError(getString(R.string.error_field_required));
            if (flag) {
                //requestFocus(titleInput);
            }
            flag = false;
        } else if (name.length() > maxLengthName || name.length() < minLengthName) {
            titleLayout.setError(getString(R.string.error_invalid_titleOfCommunity) + " ( beetween " + minLengthName + " and " + maxLengthName + " caracters )");
            if (flag) {
                //requestFocus(titleInput);
            }
            flag = false;
        } else {
            titleLayout.setErrorEnabled(false);
        }

        if (description.isEmpty()) {
            descriptionLayout.setError(getString(R.string.error_field_required));
            if (flag) {
                //requestFocus(descriptionInput);
            }
            flag = false;
        }
        return flag;

    }

    /*private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }*/

    protected void hiddenVirtualKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

    }
}
