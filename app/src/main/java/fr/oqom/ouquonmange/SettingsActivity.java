package fr.oqom.ouquonmange;

import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class SettingsActivity extends BaseActivity {

    private static final String LOG_TAG = "SettingsActivity";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initNav();
        Log.d(LOG_TAG, "onCreate");

        toolbar.setSubtitle(R.string.action_settings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.empty_menu, menu);
        return true;
    }
}
