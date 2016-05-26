package fr.oqom.ouquonmange;

import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import fr.oqom.ouquonmange.models.AuthRepository;
import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.models.EventOfCommunity;
import fr.oqom.ouquonmange.models.ListEventAdapter;
import fr.oqom.ouquonmange.services.OuquonmangeApi;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.Callback2;

public class CalendarActivity extends AppCompatActivity {
    private static String LOG_TAG = "CalendarActivity";
    private OuquonmangeApi api;
    private AuthRepository authRepository;
    private RecyclerView.Adapter eventsAdapter;
    private List<EventOfCommunity> eventOfCommunities = new ArrayList<>();
    private RecyclerView eventsRecyclerView;
    private LinearLayoutManager eventsLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);
        Intent intent = getIntent();
        String uuid =  intent.getStringExtra("uuid_community");
        Toast.makeText(getApplicationContext(), uuid, Toast.LENGTH_LONG).show();
        api = new OuquonmangeApi(getApplicationContext());
        authRepository = new AuthRepository(getApplicationContext());

        //Creating listview
        eventsAdapter = new ListEventAdapter(eventOfCommunities, new Callback<EventOfCommunity>() {
            @Override
            public void apply(EventOfCommunity eventOfCommunity) {
                //TODO
            }
        });

        eventsRecyclerView = (RecyclerView) findViewById(R.id.events_list);
        eventsLayoutManager = new LinearLayoutManager(this);
        eventsRecyclerView.setHasFixedSize(true);
        eventsRecyclerView.setLayoutManager(eventsLayoutManager);
        eventsRecyclerView.setAdapter(eventsAdapter);

        if(authRepository.getToken() == null){
            Intent intentLogin = new Intent(this,LoginActivity.class);
            startActivity(intentLogin);
        }else{
            this.fetchEvents(uuid);
        }
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.create_events_fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), "Add Event TODO", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getApplicationContext(), CreateEventActivity.class));
            }
        });

    }

    private void fetchEvents(String uuid) {
        api.getEventsByUUID(uuid,new Callback<JSONArray>() {
            @Override
            public void apply(JSONArray value) {
                try {
                    eventOfCommunities.addAll(EventOfCommunity.fromJson(value));
                    eventsAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), eventOfCommunities.toString(), Toast.LENGTH_SHORT).show();
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(getApplicationContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.i(LOG_TAG, value.toString());
            }
        }, new Callback2<Throwable, JSONObject>() {
            @Override
            public void apply(Throwable throwable, JSONObject jsonObject) {
                if (jsonObject != null) {
                    Log.e(LOG_TAG, jsonObject.toString());
                }
                Log.e(LOG_TAG, throwable.getMessage());
            }
        });

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
