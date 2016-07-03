package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;

import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.services.OuQuOnMangeService;
import fr.oqom.ouquonmange.services.Service;
import fr.oqom.ouquonmange.utils.NetConnectionUtils;
import retrofit2.adapter.rxjava.HttpException;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class CreateCommunityActivity extends AppCompatActivity {
    private static final String LOG_TAG = "CreateCommunityActivity";

    private OuQuOnMangeService ouQuOnMangeService;

    private TextInputLayout titleLayout, descriptionLayout;
    private EditText titleInput, descriptionInput;

    private CoordinatorLayout coordinatorLayout;
    private ProgressBar progressBar;

    private int communityType = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_community);
        initView();

        ouQuOnMangeService = Service.getInstance(getApplicationContext());
    }

    private void initView() {
        titleLayout = (TextInputLayout) findViewById(R.id.layout_community_title);
        titleInput = (EditText) findViewById(R.id.input_community_title);
        descriptionLayout = (TextInputLayout) findViewById(R.id.layout_community_description);
        descriptionInput = (EditText) findViewById(R.id.input_community_description);

        coordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinatorCreateCommunityLayout);
        progressBar = (ProgressBar) findViewById(R.id.progress);

        // Init Community Type Spinner
        Spinner typeInput = (Spinner) findViewById(R.id.input_community_typ);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.community_types, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeInput.setAdapter(adapter);
        typeInput.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.d(LOG_TAG, "typeInput onItemSelected " + position);
                communityType = position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                communityType = 0;
            }
        });

        // Init Save Button
        Button saveAction = (Button) findViewById(R.id.action_create_community);
        saveAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitCommunity();
            }
        });
    }

    private void showErrorSnackBar(CharSequence message) {
        Snackbar.make(coordinatorLayout, message, Snackbar.LENGTH_LONG).show();
    }

    private String getCommunityType(int position) {
        switch (position) {
            case 0: return "public";
            case 1: return "private";
            case 2: return "closed";
            default: throw new ArrayIndexOutOfBoundsException("Unknown Community Type");
        }
    }

    private void submitCommunity() {
        String name = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        hiddenVirtualKeyboard();
        if (validateForm(name, description)) {
            if (NetConnectionUtils.isConnected(getApplicationContext())) {
                progressBar.setVisibility(View.VISIBLE);
                ouQuOnMangeService.createCommunity(new Community(name, description, getCommunityType(communityType)))
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Action1<Community>() {
                            @Override
                            public void call(Community community) {
                                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                intent.putExtra(Constants.CREATED_COMMUNITY, community);
                                intent.putExtra(Constants.FROM_MENU, Constants.FROM_MENU);
                                startActivity(intent);
                                finish();
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
                                            showErrorSnackBar(getText(R.string.error_invalid_fields));
                                            break;
                                        case 409:
                                            Log.e(LOG_TAG, "Login 409 Conflict Community Already Exist");
                                            showErrorSnackBar(getText(R.string.create_community_error_already_exist));
                                        case 401:
                                            Log.e(LOG_TAG, "Login 401 Unauthorized");
                                            Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
                                            startActivity(intent);
                                            finish();
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
        } else {
            showErrorSnackBar(getText(R.string.error_invalid_fields));
        }
    }

    private boolean validateForm(String name, String description) {
        boolean flag = true;

        int minLengthName = Constants.MIN_LENGTH_NAME_COMMUNITY;
        int maxLengthName = Constants.MAX_LENGTH_NAME_COMMUNITY;

        if (name.isEmpty()) {
            titleLayout.setError(getString(R.string.error_field_required));
            if (flag) {
                //requestFocus(titleInput);
            }
            flag = false;
        } else if (name.length() > maxLengthName || name.length() < minLengthName) {
            titleLayout.setError(getString(R.string.error_invalid_titleOfCommunity) + " ( beetween " + minLengthName + " and " + maxLengthName + " caracters )");
            if (flag) {
                //requestFocus(titleInput);
            }
            flag = false;
        } else {
            titleLayout.setErrorEnabled(false);
        }

        if (description.isEmpty()) {
            descriptionLayout.setError(getString(R.string.error_field_required));
            if (flag) {
                //requestFocus(descriptionInput);
            }
            flag = false;
        }
        return flag;

    }

    /*private void requestFocus(View view) {
        if (view.requestFocus()) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }*/

    protected void hiddenVirtualKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
    }
}
