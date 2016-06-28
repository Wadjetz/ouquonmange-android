package fr.oqom.ouquonmange;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import fr.oqom.ouquonmange.models.AuthRepository;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.services.OuquonmangeApi;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class CreateAccountUserActivity extends BaseActivity {
    private static final String LOG_TAG = "SigninActivity";

    private TextInputLayout usernameLayoutSignup, emailLayoutSignup, passwordLayoutSignup, passwordConfirmLayoutSignup;
    private EditText usernameInputSignup, emailInputSignup, passwordInputSignup, passwordCofirmInputSignup;
    private Button signUpButton;
    private String regexPassword = Constants.REGEX_PASSWORD;
    private OuquonmangeApi api;
    private AuthRepository authRepository;
    private Snackbar snackbar;
    private ProgressBar progressBar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.create_account_user_activity);
        usernameInputSignup = (EditText) findViewById(R.id.signup_input_username);
        usernameLayoutSignup = (TextInputLayout) findViewById(R.id.signup_layout_username);
        emailInputSignup = (EditText) findViewById(R.id.signup_input_email);
        emailLayoutSignup = (TextInputLayout) findViewById(R.id.signup_layout_email);
        passwordInputSignup = (EditText) findViewById(R.id.signup_input_password);
        passwordLayoutSignup = (TextInputLayout) findViewById(R.id.signup_layout_password);
        passwordCofirmInputSignup = (EditText) findViewById(R.id.signup_input_confirm_password);
        passwordConfirmLayoutSignup = (TextInputLayout) findViewById(R.id.signup_layout_confirm_password);

        signUpButton = (Button) findViewById(R.id.signup_button);

        api = new OuquonmangeApi(getApplicationContext());
        authRepository = new AuthRepository(getApplicationContext());

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }


        });

        CoordinatorLayout coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorSigninLayout);
        snackbar = Snackbar.make(coordinatorLayout,"Error !",Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close), closeSnackBarSignin);

        progressBar = (ProgressBar) findViewById(R.id.progressCreateAccountUser);
        progressBar.setVisibility(View.GONE);

    }

    private View.OnClickListener closeSnackBarSignin = new View.OnClickListener(){
        @Override
        public void onClick(View v) {
            snackbar.dismiss();
        }
    };
    private void signUp() {
        if(validateFormCreateAccount()) {
            progressBar.setVisibility(View.VISIBLE);
            String username = usernameInputSignup.getText().toString().trim().toLowerCase();
            String email = emailInputSignup.getText().toString().trim().toLowerCase();
            String password = passwordInputSignup.getText().toString().trim();
            hiddenVirtualKeyboard();
            api.createAccountUser(username,email ,password , new Callback<JSONObject>() {
                @Override
                public void apply(final JSONObject value) {
                    if(value != null) {
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
                                    snackbar.setText(R.string.error_Signin);
                                }
                            });
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }else{
                        snackbar.setText(R.string.error_exception).setActionTextColor(Color.parseColor("#D32F2F")).show();
                    }
                }
            }, new Callback2<Throwable, JSONObject>() {
                @Override
                public void apply(Throwable throwable, JSONObject error) {
                    String err = "";
                    if(error != null) {
                        try {
                            err = error.getString("error");
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                        snackbar.setText(err).setActionTextColor(Color.parseColor("#D32F2F")).show();
                    }else{
                        snackbar.setText(R.string.error_exception).setActionTextColor(Color.parseColor("#D32F2F")).show();
                    }
                }
            });
            progressBar.setVisibility(View.GONE);
        }else{
            snackbar.setText(getText(R.string.error_invalid_fields)).setActionTextColor(Color.parseColor("#D32F2F")).show();
        }

    }

    private boolean validateFormCreateAccount(){
        String email = emailInputSignup.getText().toString().trim().toLowerCase();
        boolean flag = true;
        //check email
        if (email.isEmpty() || !isValidEmail(email)) {
            emailLayoutSignup.setError(getString(R.string.error_invalid_email));
            if(flag){
                requestFocus(emailInputSignup);
            }
            flag = false;
        } else {
            emailLayoutSignup.setErrorEnabled(false);
        }

        //check username
        String username = usernameInputSignup.getText().toString().trim().toLowerCase();
        if(username.isEmpty()){
            usernameLayoutSignup.setError(getString(R.string.error_invalid_username));
            if(flag){
                requestFocus(usernameInputSignup);
            }
            flag = false;
        }else {
            usernameLayoutSignup.setErrorEnabled(false);
        }

        //check password
        String password = passwordInputSignup.getText().toString().trim();
        String passwordRepeat = passwordCofirmInputSignup.getText().toString().trim();

        if(password.isEmpty()) {
            // password vide
            passwordLayoutSignup.setError(getString(R.string.error_field_required));
            if(flag){
                requestFocus(passwordInputSignup);
            }
            flag = false;
        }else if(passwordRepeat.isEmpty()){
            passwordConfirmLayoutSignup.setError(getString(R.string.error_field_required));
            if(flag) {
                requestFocus(passwordCofirmInputSignup);
            }
            flag = false;
        }else if(!password.equals(passwordRepeat)){
            // les 2 password différents
            passwordConfirmLayoutSignup.setError(getString(R.string.error_different_password));
            if(flag){
                requestFocus(passwordInputSignup);
            }
            flag = false;
        }else if(!isValidPassword(password)){
            //password invalide
            passwordLayoutSignup.setError(getString(R.string.error_invalid_password));
            if(flag){
                requestFocus(passwordInputSignup);
            }
            flag = false;
        }else{
            passwordLayoutSignup.setErrorEnabled(false);
            passwordConfirmLayoutSignup.setErrorEnabled(false);
        }

        return flag;
    }


    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean isValidPassword(String password) {
        return Pattern.compile(regexPassword).matcher(password).matches();
    }

}
