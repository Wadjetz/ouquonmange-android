package fr.oqom.ouquonmange;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.json.JSONObject;

import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.services.OuquonmangeApi;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class CreateCommunityFragment extends Fragment {

    private static final String LOG_TAG = "CreateCommunityFragment";

    private Button saveAction;
    private TextInputLayout titleLayout;
    private EditText titleInput, descriptionInput;

    private OuquonmangeApi api;
    private int minLengthName = Constants.MIN_LENGTH_NAME_COMMUNITY;
    private int maxLengthName = Constants.MAX_LENGTH_NAME_COMMUNITY;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layoutInflater = inflater.inflate(R.layout.fragment_create_community, container, false);

        api = new OuquonmangeApi(getContext());

        titleLayout = (TextInputLayout) layoutInflater.findViewById(R.id.layout_community_title);

        ((MainActivity) getActivity()).setSubtitle(getString(R.string.create_community));

        titleInput = (EditText) layoutInflater.findViewById(R.id.input_community_title);
        descriptionInput = (EditText) layoutInflater.findViewById(R.id.input_community_description);
        saveAction = (Button) layoutInflater.findViewById(R.id.action_create_community);
        saveAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitCommunity();
            }
        });

        return layoutInflater;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(LOG_TAG, "onAttach");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(LOG_TAG, "onCreate");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(LOG_TAG, "onActivityCreated");
    }

    @Override
    public void onStart() {
        super.onStart();
        Log.d(LOG_TAG, "onStart");
    }

    @Override
    public void onResume() {
        super.onResume();
        Log.d(LOG_TAG, "onResume");
    }

    @Override
    public void onPause() {
        super.onPause();
        Log.d(LOG_TAG, "onPause");
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        Log.d(LOG_TAG, "onDestroyView");
        ((MainActivity) getActivity()).setSubtitle(getString(R.string.my_communities));
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(LOG_TAG, "onDestroy");
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(LOG_TAG, "onDetach");
    }

    private void submitCommunity() {
        String name = titleInput.getText().toString().trim();
        String description = descriptionInput.getText().toString().trim();
        if (validateName(name)) {
            api.createCommunity(name, description, new Callback<JSONObject>() {
                @Override
                public void apply(JSONObject value) {
                    Toast.makeText(getContext(), "Community Created", Toast.LENGTH_SHORT).show();
                    startActivity(new Intent(getContext(), MainActivity.class));
                }
            }, new Callback2<Throwable, JSONObject>() {
                @Override
                public void apply(Throwable throwable, JSONObject jsonObject) {
                    Toast.makeText(getContext(), throwable.getMessage() + " " + jsonObject.toString(), Toast.LENGTH_SHORT).show();
                }
            });
        }

    }

    private boolean validateName(String name) {
        if (name.isEmpty()) {
            titleLayout.setError(getString(R.string.error_field_required));
            requestFocus(titleInput);
            return false;
        }
        else if (name.length()>maxLengthName || name.length()<minLengthName ){
            titleLayout.setError(getString(R.string.error_invalid_titleOfCommunity) +" ( beetween "+minLengthName+" and "+maxLengthName+" caracters )");
            requestFocus(titleInput);
            return false;
        } else {
            titleLayout.setErrorEnabled(false);
        }
        return true;

    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }
}
