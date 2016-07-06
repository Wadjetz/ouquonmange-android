package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import java.util.regex.Pattern;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.oqom.ouquonmange.repositories.Repository;
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

    @BindView(R.id.signup_layout_username)         TextInputLayout usernameLayoutSignup;
    @BindView(R.id.signup_layout_email)            TextInputLayout emailLayoutSignup;
    @BindView(R.id.signup_layout_password)         TextInputLayout passwordLayoutSignup;
    @BindView(R.id.signup_layout_confirm_password) TextInputLayout passwordConfirmLayoutSignup;

    @BindView(R.id.signup_input_username)         TextInputEditText usernameInputSignup;
    @BindView(R.id.signup_input_email)            TextInputEditText emailInputSignup;
    @BindView(R.id.signup_input_password)         TextInputEditText passwordInputSignup;
    @BindView(R.id.signup_input_confirm_password) TextInputEditText passwordCofirmInputSignup;

    @BindView(R.id.progress)                ProgressBar progressBar;
    @BindView(R.id.coordinatorSigninLayout) CoordinatorLayout coordinatorLayout;

    private Repository repository;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        ButterKnife.bind(this);

        repository = new Repository(getApplicationContext());
    }

    @OnClick(R.id.signup_button)
    public void signUp() {
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
                                repository.save(token.getToken(), new Callback<Void>() {
                                    @Override
                                    public void apply(Void value) {
                                        startActivity(new Intent(getApplicationContext(), MainActivity.class));
                                        finish();
                                    }
                                }, new Callback<Throwable>() {
                                    @Override
                                    public void apply(Throwable error) {
                                        Log.e(LOG_TAG, error.getMessage());
                                        showErrorSnackBar(getText(R.string.error_signin));
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
                                            showErrorSnackBar(getText(R.string.error_signin));
                                            break;
                                        case 409:
                                            Log.e(LOG_TAG, "signUp 409 Conflict Community Already Exist");
                                            showErrorSnackBar(getText(R.string.signup_error_already_exist));
                                    }
                                } else {
                                    showErrorSnackBar(throwable.getMessage());
                                }
                                progressBar.setVisibility(View.INVISIBLE);
                            }
                        });
            } else {
                NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, this);
            }
            progressBar.setVisibility(View.GONE);
        } else {
            showErrorSnackBar(getText(R.string.error_invalid_fields));
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
        return Pattern.compile(Constants.REGEX_PASSWORD).matcher(password).matches();
    }

    private void showErrorSnackBar(CharSequence message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }
}