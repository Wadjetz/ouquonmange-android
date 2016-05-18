package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import org.json.JSONObject;
import java.util.regex.Pattern;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.services.OuquonmangeApi;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

/**
 * Created by hedhili on 18/05/2016.
 */
public class CreateAccountUserActivity extends BaseActivity {
    private TextInputLayout usernameLayoutSignup, emailLayoutSignup, passwordLayoutSignup, passwordConfirmLayoutSignup;
    private EditText usernameInputSignup, emailInputSignup, passwordInputSignup, passwordCofirmInputSignup;
    private Button signUpButton;
    private String regexPassword = Constants.REGEX_PASSWORD;
    private OuquonmangeApi api;

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

        signUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signUp();
            }


        });

    }
    private void signUp() {
        if( !(validateEmail() && validatePassword() && validateUserName())) {
            Toast.makeText(getApplicationContext(), "Errors", Toast.LENGTH_SHORT).show();
        }else{
            Toast.makeText(getApplicationContext(), "Pending", Toast.LENGTH_SHORT).show();
            String username = usernameInputSignup.getText().toString().trim().toLowerCase();
            String email = emailInputSignup.getText().toString().trim().toLowerCase();
            String password = passwordInputSignup.getText().toString().trim();
            api.createAccountUser(username,email ,password , new Callback<JSONObject>() {
                @Override
                public void apply(final JSONObject value) {
                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                }
            }, new Callback2<Throwable, JSONObject>() {
                @Override
                public void apply(Throwable throwable, JSONObject error) {
                    Toast.makeText(getApplicationContext(), error.toString(), Toast.LENGTH_SHORT).show();
                }
            });

        }
    }

    private boolean validateUserName() {
        String username = usernameInputSignup.getText().toString().trim().toLowerCase();
        if(username.isEmpty()){
            usernameLayoutSignup.setError(getString(R.string.error_invalid_username));
            requestFocus(usernameInputSignup);
            return false;
        }else {
            usernameLayoutSignup.setErrorEnabled(false);
        }
        return true;
    }

    private boolean validateEmail() {
        String email = emailInputSignup.getText().toString().trim().toLowerCase();
        if (email.isEmpty() || !isValidEmail(email)) {
            emailLayoutSignup.setError(getString(R.string.error_invalid_email));
            requestFocus(emailInputSignup);
            return false;
        } else {
            emailLayoutSignup.setErrorEnabled(false);
        }
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private static boolean isValidEmail(String email) {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    private boolean validatePassword(){
        String password = passwordInputSignup.getText().toString().trim();
        String passwordRepeat = passwordCofirmInputSignup.getText().toString().trim();
        boolean result = false;

        if(password.isEmpty()) {
            // password vide
            passwordLayoutSignup.setError(getString(R.string.error_field_required));
            requestFocus(passwordInputSignup);
        }else if(passwordRepeat.isEmpty()){
            passwordConfirmLayoutSignup.setError(getString(R.string.error_field_required));
            requestFocus(passwordCofirmInputSignup);
        }else if(!password.equals(passwordRepeat)){
            // les 2 password différents
            passwordConfirmLayoutSignup.setError(getString(R.string.error_different_password));
            requestFocus(passwordInputSignup);
        }else if(!isValidPassword(password)){
                    //password invalide
            passwordLayoutSignup.setError(getString(R.string.error_invalid_password));
            requestFocus(passwordInputSignup);
        }else{
            result=true;
            passwordLayoutSignup.setErrorEnabled(false);
            passwordConfirmLayoutSignup.setErrorEnabled(false);
        }
        return result;
    }

    private boolean isValidPassword(String password) {
        return Pattern.compile(regexPassword).matcher(password).matches();
    }

    private boolean EmailIsNotExist(){
        return true;
    }

}
