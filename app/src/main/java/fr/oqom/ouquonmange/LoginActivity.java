package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import fr.oqom.ouquonmange.models.AuthRepository;
import fr.oqom.ouquonmange.services.OuquonmangeApi;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout emailLayout, passwordLayout;
    private EditText emailInput, passwordInput;
    private Button loginButton;
    private TextView signUpTextView;

    private OuquonmangeApi api;
    private AuthRepository authRepository;

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

    }

    private void submitForm() {
        if (!validateEmail() && !validatePassword()) {
            Toast.makeText(getApplicationContext(), "Errors", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getApplicationContext(), "Pending", Toast.LENGTH_SHORT).show();

            api.login(emailInput.getText().toString().trim().toLowerCase(), passwordInput.getText().toString().trim(), new Callback<JSONObject>() {
                @Override
                public void apply(final JSONObject value) {
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
                                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }, new Callback2<Throwable, JSONObject>() {
                @Override
                public void apply(Throwable throwable, JSONObject error) {
                    Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private boolean validatePassword() {
        if (passwordInput.getText().toString().trim().isEmpty()) {
            passwordLayout.setError(getString(R.string.error_field_required));
            requestFocus(passwordInput);
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
            requestFocus(emailInput);
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
}
