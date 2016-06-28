package fr.oqom.ouquonmange;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import java.util.Calendar;

import fr.oqom.ouquonmange.models.AuthRepository;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.services.Config;
import fr.oqom.ouquonmange.services.OuquonmangeApi;
import fr.oqom.ouquonmange.utils.Callback;

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
                String defaultCommunityUuid = Config.getDefaultCommunity(getApplicationContext());
                if (defaultCommunityUuid != null && !defaultCommunityUuid.isEmpty()) {
                    Intent intent = new Intent(getApplicationContext(), CalendarActivity.class);
                    intent.putExtra(Constants.COMMUNITY_UUID, defaultCommunityUuid);
                    startActivity(intent);
                    finish();
                } else {
                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.nav_communities:
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.putExtra(Constants.FROM_MENU, Constants.FROM_MENU);
                startActivity(intent);
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

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(R.string.logout_confirm_message)
                        .setCancelable(false)
                        .setPositiveButton(R.string.yes_message, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                authRepository.deleteToken(new Callback<Void>() {
                                    @Override
                                    public void apply(Void aVoid) {
                                    startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                        finish();
                                    }
                                });
                            }
                        })
                        .setNegativeButton(R.string.no_message, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        })
                        .create()
                        .show();

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
            Intent intentLogin = new Intent(this, LoginActivity.class);
            startActivity(intentLogin);
            finish();
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

    protected void hiddenVirtualKeyboard(){
        InputMethodManager imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

    }
}
