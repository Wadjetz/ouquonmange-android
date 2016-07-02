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
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

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
                .setAction(R.string.activate, activateSnackBarLogin)
                .show();
        progressBar.setVisibility(View.GONE);

    }
    private View.OnClickListener activateSnackBarLogin = new View.OnClickListener(){
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
