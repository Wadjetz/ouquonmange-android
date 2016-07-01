package fr.oqom.ouquonmange;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
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
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.services.OuquonmangeApi;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class CreateCommunityActivity extends AppCompatActivity {
    private static final String LOG_TAG = "CreateCommunityActivity";

    private Button saveAction;
    private TextInputLayout titleLayout;
    private EditText titleInput, descriptionInput;

    private OuquonmangeApi api;
    private int minLengthName = Constants.MIN_LENGTH_NAME_COMMUNITY;
    private int maxLengthName = Constants.MAX_LENGTH_NAME_COMMUNITY;
    private Snackbar snackbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_community);

        titleLayout = (TextInputLayout) findViewById(R.id.layout_community_title);
        titleInput = (EditText) findViewById(R.id.input_community_title);
        descriptionInput = (EditText) findViewById(R.id.input_community_description);
        saveAction = (Button) findViewById(R.id.action_create_community);

        api = new OuquonmangeApi(getApplicationContext());
        
        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorCreateCommunityLayout);
        snackbar = Snackbar.make(coordinatorLayout,"Error !",Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close),closeSnackBarCreateCommunity);

        saveAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitCommunity();
            }
        });


    }
    private View.OnClickListener closeSnackBarCreateCommunity = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            snackbar.dismiss();
        }
    };
    private void submitCommunity() {
        String name = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        if (validateName(name)) {
            hiddenVirtualKeyboard();
            if(checkConnection(getApplicationContext())) {
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
            }else{
                refreshSnackBar();
            }
        }else{
            snackbar.setText(getText(R.string.error_invalid_fields)).setActionTextColor(Color.parseColor("#D32F2F")).show();
        }

    }

    private boolean checkConnection(Context context) {
        final ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetworkInfo = connMgr.getActiveNetworkInfo();

        if (activeNetworkInfo != null) { // connected to the internet
            Toast.makeText(context, activeNetworkInfo.getTypeName(), Toast.LENGTH_SHORT).show();

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

    protected void hiddenVirtualKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

    }

    public void refreshSnackBar(){
        snackbar.setText(R.string.no_internet)
                .setActionTextColor(Color.parseColor("#D32F2F"))
                .setDuration(Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.refresh, refreshSnackBarCreateCom)
                .show();

    }
    private View.OnClickListener refreshSnackBarCreateCom = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    };
}
