package fr.oqom.ouquonmange;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

public class CalendarActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Intent intent = getIntent();
        String uuid =  intent.getStringExtra("uuid_community");
        Toast.makeText(getApplicationContext(), uuid, Toast.LENGTH_LONG).show();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_drawer,menu);
        MenuItem menuSearchCommunity = menu.findItem(R.id.nav_search_communities);
        MenuItem menuTools = menu.findItem(R.id.nav_manage);
        MenuItem menuCommunities = menu.findItem(R.id.nav_communities);
        MenuItem menuCommunicate = menu.findItem(R.id.nav_communicate);
        menuSearchCommunity.setVisible(false);
        menuTools.setVisible(false);
        menuCommunities.setVisible(false);
        menuCommunicate.setVisible(false);

        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_calendar:
                setDate();
                //Toast.makeText(getApplicationContext(), "calendar", Toast.LENGTH_LONG).show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void setDate() {
        Toast.makeText(getApplicationContext(), "calendar", Toast.LENGTH_LONG).show();
        PickerDialogs pickerDialogs = new PickerDialogs();
        pickerDialogs.show(getFragmentManager(),"date_picker");
    }
}
