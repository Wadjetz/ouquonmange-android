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
        return inflater.inflate(R.layout.fragment_navigation_drawer, container, false);
    }

    public void setUp(int fragmentId, final DrawerLayout drawerLayout, final Toolbar toolbar) {
        this.drawerLayout = drawerLayout;
        View view = getActivity().findViewById(fragmentId);
        actionBarDrawerToggle = new ActionBarDrawerToggle(
                getActivity(),
                drawerLayout,
                toolbar,
                R.string.drower_open,
                R.string.drower_close
        ) {
            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                if (slideOffset <  0.6) {
                    toolbar.setAlpha( 1 - slideOffset / 2 );
                }
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                if (!userLearnedDrawer) {
                    userLearnedDrawer = true;
                    AppPreferences.setUserLearned(
                            getActivity(),
                            AppPreferences.KEY_USER_LEARNED,
                            AppPreferences.TRUE
                    );
                }
            }

            @Override
            public void onDrawerClosed(View drawerView) {
                AppPreferences.setUserLearned(
                        getActivity(),
                        AppPreferences.KEY_USER_LEARNED,
                        AppPreferences.TRUE
                );
            }
        };

        if (!userLearnedDrawer && !fromSavedInstanceState) {
            drawerLayout.openDrawer(view);
        }

        drawerLayout.addDrawerListener(actionBarDrawerToggle);

        drawerLayout.post(new Runnable() {
            @Override
            public void run() {
                actionBarDrawerToggle.syncState();
            }
        });
    }

    public void closeDrawer() {
        drawerLayout.closeDrawers();
    }

}
