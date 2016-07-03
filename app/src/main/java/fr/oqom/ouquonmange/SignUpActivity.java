package fr.oqom.ouquonmange;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ProgressBar;

import java.util.regex.Pattern;

import fr.oqom.ouquonmange.models.AuthRepository;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.SignUpUser;
import fr.oqom.ouquonmange.models.Token;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.NetConnectionUtils;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class SignUpActivity extends BaseActivity {
    private static final String LOG_TAG = "SignUnActivity";

    private TextInputLayout usernameLayoutSignup, emailLayoutSignup, passwordLayoutSignup, passwordConfirmLayoutSignup;
    private TextInputEditText usernameInputSignup, emailInputSignup, passwordInputSignup, passwordCofirmInputSignup;
    private Button signUpButton;

    private String regexPassword = Constants.REGEX_PASSWORD;
    private AuthRepository authRepository;
    private Snackbar snackbar;
    private ProgressBar progressBar;
    private CoordinatorLayout coordinatorLayout;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        initView();

        authRepository = new AuthRepository(getApplicationContext());

        snackbar = Snackbar.make(coordinatorLayout,"Error !",Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close), closeSnackBarSignin);
    }

    private void initView() {
        usernameInputSignup = (TextInputEditText) findViewById(R.id.signup_input_username);
        usernameLayoutSignup = (TextInputLayout) findViewById(R.id.signup_layout_username);
        emailInputSignup = (TextInputEditText) findViewById(R.id.signup_input_email);
        emailLayoutSignup = (TextInputLayout) findViewById(R.id.signup_layout_email);
        passwordInputSignup = (TextInputEditText) findViewById(R.id.signup_input_password);
        passwordLayoutSignup = (TextInputLayout) findViewById(R.id.signup_layout_password);
        passwordCofirmInputSignup = (TextInputEditText) findViewById(R.id.signup_input_confirm_password);
        passwordConfirmLayoutSignup = (TextInputLayout) findViewById(R.id.signup_layout_confirm_password);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorSigninLayout);
        signUpButton = (Button) findViewById(R.id.signup_button);

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }


        });
    }

    private View.OnClickListener closeSnackBarSignin = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            snackbar.dismiss();
        }
    };

    private void signUp() {
        hiddenVirtualKeyboard();
        if (validateFormCreateAccount()) {
            progressBar.setVisibility(View.VISIBLE);
            String username = usernameInputSignup.getText().toString().trim().toLowerCase();
            String email = emailInputSignup.getText().toString().trim().toLowerCase();
            String password = passwordInputSignup.getText().toString().trim();

            if (NetConnectionUtils.isConnected(getApplicationContext())) {
                ouQuOnMangeService.signUp(new SignUpUser(username, email, password))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Token>() {
                            @Override
                            public void call(Token token) {
                                authRepository.save(token.getToken(), new Callback<Void>() {
                                    @Override
                                    public void apply(Void value) {
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        finish();
                                    }
                                }, new Callback<Throwable>() {
                                    @Override
                                    public void apply(Throwable error) {
                                        Log.e(LOG_TAG, error.getMessage());
                                        snackbar.setText(R.string.error_signin);
                                    }
                                });
                            }
                        }, new Action1<Throwable>() {
                            @Override
                            public void call(Throwable throwable) {
                                throwable.printStackTrace();
                                if (throwable instanceof HttpException) {
                                    HttpException response = (HttpException) throwable;
                                    switch (response.code()) {
                                        case 400:
                                            Log.e(LOG_TAG, "signUp 400 Bad Request");
                                            snackbar.setText(R.string.error_signin).setActionTextColor(Color.parseColor("#D32F2F")).show();
                                            break;
                                        case 409:
                                            Log.e(LOG_TAG, "signUp 409 Conflict Community Already Exist");
                                            snackbar.setText(R.string.signup_error_already_exist).setActionTextColor(Color.parseColor("#D32F2F")).show();
                                    }
                                } else {
                                    snackbar.setText(throwable.getMessage()).setActionTextColor(Color.parseColor("#D32F2F")).show();
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
            } else {
                NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, this);
            }
            progressBar.setVisibility(View.GONE);
        } else {
            snackbar.setText(getText(R.string.error_invalid_fields)).setActionTextColor(Color.parseColor("#D32F2F")).show();
        }
    }

    private boolean validateFormCreateAccount() {
        String email = emailInputSignup.getText().toString().trim().toLowerCase();
        boolean flag = true;
        //check email
        if (email.isEmpty() || !isValidEmail(email)) {
            emailLayoutSignup.setError(getString(R.string.error_invalid_email));
            if (flag) {
                requestFocus(emailInputSignup);
            }
            flag = false;
        } else {
            emailLayoutSignup.setErrorEnabled(false);
        }

        //check username
        String username = usernameInputSignup.getText().toString().trim().toLowerCase();
        if (username.isEmpty()) {
            usernameLayoutSignup.setError(getString(R.string.error_invalid_username));
            if (flag) {
                requestFocus(usernameInputSignup);
            }
            flag = false;
        } else {
            usernameLayoutSignup.setErrorEnabled(false);
        }

        //check password
        String password = passwordInputSignup.getText().toString().trim();
        String passwordRepeat = passwordCofirmInputSignup.getText().toString().trim();

        if (password.isEmpty()) {
            // password vide
            passwordLayoutSignup.setError(getString(R.string.error_field_required));
            if (flag) {
                requestFocus(passwordInputSignup);
            }
            flag = false;
        } else if (passwordRepeat.isEmpty()) {
            passwordConfirmLayoutSignup.setError(getString(R.string.error_field_required));
            if (flag) {
                requestFocus(passwordCofirmInputSignup);
            }
            flag = false;
        } else if (!password.equals(passwordRepeat)) {
            // les 2 password différents
            passwordConfirmLayoutSignup.setError(getString(R.string.error_different_password));
            if (flag) {
                requestFocus(passwordInputSignup);
            }
            flag = false;
        } else if (!isValidPassword(password)) {
            //password invalide
            passwordLayoutSignup.setError(getString(R.string.error_invalid_password));
            if (flag) {
                requestFocus(passwordInputSignup);
            }
            flag = false;
        } else {
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