package fr.oqom.ouquonmange;

import android.content.Intent;
import android.graphics.Color;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

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

    private TextInputLayout emailLayout, passwordLayout;
    private EditText emailInput, passwordInput;
    private Button loginButton;
    private Snackbar snackbar;
    private ProgressBar progressBar;
    private OuQuOnMangeService ouQuOnMangeService;
    private AuthRepository authRepository;
    private CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        initView();
        getSupportActionBar().setSubtitle(R.string.login_action);

        ouQuOnMangeService = Service.getInstance(getApplicationContext());
        authRepository = new AuthRepository(getApplicationContext());

        snackbar = Snackbar.make(coordinatorLayout,"Error !",Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close),closeSnackBarLogin);
    }

    private void initView() {
        emailInput = (EditText) findViewById(R.id.login_input_email);
        emailLayout = (TextInputLayout) findViewById(R.id.login_layout_email);
        passwordInput = (EditText) findViewById(R.id.login_input_password);
        passwordLayout = (TextInputLayout) findViewById(R.id.login_layout_password);
        loginButton = (Button) findViewById(R.id.login_button);
        progressBar = (ProgressBar) findViewById(R.id.progress);
        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLoginLayout);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorLoginLayout);
        snackbar = Snackbar.make(coordinatorLayout, "Error !", Snackbar.LENGTH_LONG);
        snackbar.setAction(getText(R.string.close), closeSnackBarLogin);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitForm();
            }
        });
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

    private View.OnClickListener closeSnackBarLogin = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            snackbar.dismiss();
        }
    };

    private void submitForm() {

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
                                        snackbar.setText(R.string.error_login).setActionTextColor(Color.parseColor("#D32F2F")).show();
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
                                            snackbar.setText(R.string.login_error).setActionTextColor(Color.parseColor("#D32F2F")).show();
                                            break;
                                    }
                                    progressBar.setVisibility(View.INVISIBLE);
                                } else {
                                    snackbar.setText(throwable.getMessage()).setActionTextColor(Color.parseColor("#D32F2F")).show();
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
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

    }
}
