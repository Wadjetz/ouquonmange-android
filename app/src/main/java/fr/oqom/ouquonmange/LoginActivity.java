package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ProgressBar;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import fr.oqom.ouquonmange.models.AuthRepository;
import fr.oqom.ouquonmange.models.Login;
import fr.oqom.ouquonmange.models.Token;
import fr.oqom.ouquonmange.services.OuQuOnMangeService;
import fr.oqom.ouquonmange.services.Service;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.NetConnectionUtils;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class LoginActivity extends AppCompatActivity {
    private static final String LOG_TAG = "LoginActivity";

    @BindView(R.id.login_layout_email)     TextInputLayout emailLayout;
    @BindView(R.id.login_layout_password)  TextInputLayout passwordLayout;
    @BindView(R.id.login_input_email)      EditText emailInput;
    @BindView(R.id.login_input_password)   EditText passwordInput;
    @BindView(R.id.progress)               ProgressBar progressBar;
    @BindView(R.id.coordinatorLoginLayout) CoordinatorLayout coordinatorLayout;

    private OuQuOnMangeService ouQuOnMangeService;
    private AuthRepository authRepository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        ButterKnife.bind(this);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setSubtitle(R.string.login_action);
        }

        ouQuOnMangeService = Service.getInstance(getApplicationContext());
        authRepository = new AuthRepository(getApplicationContext());
    }

    @OnClick(R.id.login_button)
    public void submit() {
        hiddenVirtualKeyboard();
        if (validateEmail() && validatePassword()) {
            if (NetConnectionUtils.isConnected(getApplicationContext())) {
                progressBar.setVisibility(View.VISIBLE);
                String email = emailInput.getText().toString().trim().toLowerCase();
                String password = passwordInput.getText().toString().trim();

                ouQuOnMangeService.login(new Login(email, password))
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
                                        showErrorSnackBar(getText(R.string.error_login));
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
                                            Log.e(LOG_TAG, "Login 400 Bad Request");
                                            showErrorSnackBar(getText(R.string.login_error));
                                            break;
                                    }
                                    progressBar.setVisibility(View.INVISIBLE);
                                } else {
                                    showErrorSnackBar(throwable.getMessage());
                                }
                            }
                        });
            } else {
                NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, this);
            }
        } else {
            showErrorSnackBar(getText(R.string.error_invalid_fields));
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

    protected void hiddenVirtualKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        if(getCurrentFocus().getWindowToken() != null)
            imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }

    private void showErrorSnackBar(CharSequence message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_login_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_signup:
                startActivity(new Intent(getApplicationContext(), SignUpActivity.class));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
