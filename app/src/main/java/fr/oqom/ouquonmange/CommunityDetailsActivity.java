package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.utils.OnCalendarSelected;
import fr.oqom.ouquonmange.utils.OnRedirectFragmentWithCommunity;
import fr.oqom.ouquonmange.utils.OnRedirectFragmentWithString;

public class CommunityDetailsActivity extends AppCompatActivity implements OnCalendarSelected, OnRedirectFragmentWithString, OnRedirectFragmentWithCommunity {

    private static final String LOG_TAG = "CommunityDetailsActy";

    private Community community;

    protected Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_details);

        Intent intent = getIntent();
        community = intent.getParcelableExtra(Constants.COMMUNITY);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra(Constants.FROM_MENU, Constants.FROM_MENU);
                startActivity(intent);
                finish();
            }
        });

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, CommunityDetailsFragment.newInstance(community));
        //transaction.replace(R.id.fragment_container, MyCommunitiesFragment.newInstance());
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Log.d(LOG_TAG, "onSaveInstanceState");
        super.onSaveInstanceState(outState);
        outState.putParcelable(Constants.COMMUNITY, community);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "onRestoreInstanceState");
        super.onRestoreInstanceState(savedInstanceState);
        community = savedInstanceState.getParcelable(Constants.COMMUNITY);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_search:
                startActivity(new Intent(getApplicationContext(), SearchCommunityActivity.class));
            default:
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(getApplicationContext(), MainActivity.class);
        intent.putExtra(Constants.FROM_MENU, Constants.FROM_MENU);
        startActivity(intent);
        finish();
    }

    @Override
    public void onRedirectFragmentWithString(String name, String arg) {
        Log.d(LOG_TAG, "onRedirectFragment = " + name + " arg = " + arg);
        //Intent intent = new Intent(getContext(), CalendarActivity.class);
        //intent.putExtra(Constants.COMMUNITY_UUID, community.uuid);
        //startActivity(intent);
        //finish();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, CommunityDetailsFragment.newInstance(community));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onRedirectFragmentWithCommunity(String name, Community community) {
        Log.d(LOG_TAG, "onRedirectFragment = " + name + " arg = " + community);
        //Intent intent = new Intent(getApplicationContext(), CommunityDetailsActivity.class);
        //intent.putExtra(Constants.COMMUNITY, community);
        //startActivity(intent);
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, CommunityDetailsFragment.newInstance(community));
        transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    public void onCalendarSelected(Community community) {
        Log.d(LOG_TAG, "onCalendarSelected community = " + community);
    }
}
