package fr.oqom.ouquonmange.navigation;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import fr.oqom.ouquonmange.R;
import fr.oqom.ouquonmange.models.AppPreferences;

public class NavigationDrawerFragment extends Fragment {
    private ActionBarDrawerToggle actionBarDrawerToggle;
    private DrawerLayout drawerLayout;

    public NavigationDrawerFragment() {}

    private boolean userLearnedDrawer;
    private boolean fromSavedInstanceState;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userLearnedDrawer = Boolean.valueOf(
                AppPreferences.hasUserLearned(
                        getActivity(),
                        AppPreferences.KEY_USER_LEARNED,
                        AppPreferences.FALSE
                )
        );

        if (savedInstanceState != null) {
            fromSavedInstanceState = true;
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return super.onCreateView(R.layout.fragment_navigation_drawer, container, savedInstanceState);
    }

    public void setUp(int fragmentId, DrawerLayout drawerLayout, final Toolbar toolbar) {
        this.drawerLayout = drawerLayout;
        View view = getActivity().findViewById(fragmentId);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                drawerLayout,
                toolbar
                
        );
    }
}
