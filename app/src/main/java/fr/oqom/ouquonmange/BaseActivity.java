package fr.oqom.ouquonmange;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.widget.Toast;

public class BaseActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int id = item.getItemId();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);

        switch (id) {
            case R.id.nav_calendar:
                Toast.makeText(getApplicationContext(), "Calendar", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_communities:
                Toast.makeText(getApplicationContext(), "Communities", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_search_communities:
                Toast.makeText(getApplicationContext(), "Search Communities", Toast.LENGTH_SHORT).show();
                break;
        }

        if (drawer != null) {
            drawer.closeDrawer(GravityCompat.START);
        }

        return true;
    }
}
