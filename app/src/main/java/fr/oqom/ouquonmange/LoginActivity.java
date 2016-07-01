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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import org.json.JSONException;
import org.json.JSONObject;
import fr.oqom.ouquonmange.models.AuthRepository;
import fr.oqom.ouquonmange.services.OuquonmangeApi;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class LoginActivity extends AppCompatActivity {
    private static final String LOG_TAG = "LoginActivity";

    private TextInputLayout emailLayout, passwordLayout;
    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView signUpTextView;
    private OuquonmangeApi api;
    private AuthRepository authRepository;
    private Snackbar snackbar;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailInput = (EditText) findViewById(R.id.login_input_email);
        emailLayout = (TextInputLayout) findViewById(R.id.login_layout_email);
        passwordInput = (EditText) findViewById(R.id.login_input_password);
        passwordLayout = (TextInputLayout) findViewById(R.id.login_layout_password);
        loginButton = (Button) findViewById(R.id.login_button);
        signUpTextView = (TextView) findViewById(R.id.signUpTextView);

        api = new OuquonmangeApi(getApplicationContext());
        authRepository = new AuthRepository(getApplicationContext());

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLoginLayout);
        snackbar = Snackbar.make(coordinatorLayout,"Error !",Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close),closeSnackBarLogin);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayCreateAccountView();
            }


        });

        progressBar = (ProgressBar) findViewById(R.id.progressLogin);
        progressBar.setVisibility(View.GONE);
    }

    private View.OnClickListener closeSnackBarLogin = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            snackbar.dismiss();
        }
    };

    private void submitForm() {
        if (validateEmail() && validatePassword()) {
            progressBar.setVisibility(View.VISIBLE);
            hiddenVirtualKeyboard();
            if(checkConnection(getApplicationContext())) {
                api.login(emailInput.getText().toString().trim().toLowerCase(), passwordInput.getText().toString().trim(), new Callback<JSONObject>() {
                    @Override
                    public void apply(final JSONObject value) {
                        if (value != null) {
                            try {
                                String token = value.getString("token");
                                authRepository.save(token, new Callback<Void>() {
                                    @Override
                                    public void apply(Void value) {
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        finish();
                                    }
                                }, new Callback<Throwable>() {
                                    @Override
                                    public void apply(Throwable error) {
                                        Log.e(LOG_TAG, error.getMessage());
                                        snackbar.setText(R.string.error_login).setActionTextColor(Color.parseColor("#D32F2F")).show();
                                    }
                                });
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        } else {
                            snackbar.setText(R.string.error_exception).setActionTextColor(Color.parseColor("#D32F2F")).show();
                        }

                        progressBar.setVisibility(View.GONE);

                    }
                }, new Callback2<Throwable, JSONObject>() {
                    @Override
                    public void apply(Throwable throwable, JSONObject error) {
                        if (error != null) {
                            String err = "";
                            try {
                                err = error.getString("error");
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                            snackbar.setText(err).setActionTextColor(Color.parseColor("#D32F2F")).show();
                        } else {
                            Log.e(LOG_TAG, throwable.getMessage());
                            snackbar.setText(R.string.error_exception).setActionTextColor(Color.parseColor("#D32F2F")).show();
                        }

                        progressBar.setVisibility(View.GONE);

                    }
                });
            }else{
                refreshSnackBar();
            }
        }else{
            snackbar.setText(getText(R.string.error_invalid_fields)).setActionTextColor(Color.parseColor("#D32F2F")).show();
        }
    }

    private boolean validatePassword() {
        if (passwordInput.getText().toString().trim().isEmpty()) {
            passwordLayout.setError(getString(R.string.error_field_required));
            //requestFocus(passwordInput);
            return false;
        } else {
            passwordLayout.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateEmail() {
        String email = emailInput.getText().toString().trim().toLowerCase();
        if (email.isEmpty() || !isValidEmail(email)) {
            emailLayout.setError(getString(R.string.error_invalid_email));
            //requestFocus(emailInput);
            return false;
        } else {
            emailLayout.setErrorEnabled(false);
        }
        return true;
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private void displayCreateAccountView() {
        Intent intent = new Intent(getApplicationContext(), CreateAccountUserActivity.class);
        startActivity(intent);
    }

    protected void hiddenVirtualKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

    }

    public void refreshSnackBar(){
        snackbar.setText(R.string.no_internet)
                .setActionTextColor(Color.parseColor("#D32F2F"))
                .setDuration(Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.refresh, refreshSnackBarLogin)
                .show();

    }
    private View.OnClickListener refreshSnackBarLogin = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            Intent intent = getIntent();
            finish();
            startActivity(intent);
        }
    };
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



}
