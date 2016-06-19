package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Toast;

import fr.oqom.ouquonmange.models.AuthRepository;
import fr.oqom.ouquonmange.services.OuquonmangeApi;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    protected OuquonmangeApi api;
    protected AuthRepository authRepository;

    protected NavigationView navigationView;
    protected DrawerLayout drawer;
    protected Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        api = new OuquonmangeApi(getApplicationContext());
        authRepository = new AuthRepository(getApplicationContext());
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        switch (id) {
            case R.id.nav_calendar:
                // TODO get default community and start new activity
                Toast.makeText(getApplicationContext(), "Calendar", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_communities:
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
                finish();
                break;
            case R.id.nav_search_communities:
                startActivity(new Intent(getApplicationContext(), SearchCommunityActivity.class));
                break;
            /*
            case R.id.nav_settings:
                Toast.makeText(getApplicationContext(), "TODO Settings", Toast.LENGTH_SHORT).show();
                break;
            */
            case R.id.nav_logout:
                Toast.makeText(getApplicationContext(), "Logout", Toast.LENGTH_SHORT).show();
                authRepository.deleteToken(null);
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                break;
        }

        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }

        return true;
    }

    protected void initNav() {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }

    protected void checkAuth() {
        if(authRepository.getToken() == null){
            Intent intentLogin = new Intent(this,LoginActivity.class);
            startActivity(intentLogin);
        }
    }

    protected void initNavSearch() {
        toolbar = (Toolbar) findViewById(R.id.toolbar_search);
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout_search);
        navigationView = (NavigationView) findViewById(R.id.nav_view_search);

        setSupportActionBar(toolbar);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView.setNavigationItemSelectedListener(this);
    }
}
