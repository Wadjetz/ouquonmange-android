package fr.oqom.ouquonmange;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import butterknife.BindView;
import butterknife.ButterKnife;
import fr.oqom.ouquonmange.models.Community;
import fr.oqom.ouquonmange.models.Constants;
import fr.oqom.ouquonmange.models.Message;
import fr.oqom.ouquonmange.repositories.Repository;
import fr.oqom.ouquonmange.services.OuQuOnMangeService;
import fr.oqom.ouquonmange.services.Service;
import fr.oqom.ouquonmange.utils.Callback;
import fr.oqom.ouquonmange.utils.NetConnectionUtils;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class CommunityDetailsActivity extends AppCompatActivity {

    private static final String LOG_TAG = "CommunityDetailsActy";

    private Community community;
    private Repository repository;
    private OuQuOnMangeService ouQuOnMangeService;

    @BindView(R.id.toolbar) Toolbar toolbar;
    @BindView(R.id.coordinatorMainLayout) CoordinatorLayout coordinatorLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_community_details);
        ButterKnife.bind(this);

        Intent intent = getIntent();
        community = intent.getParcelableExtra(Constants.COMMUNITY);

        repository = new Repository(getApplicationContext());
        ouQuOnMangeService = Service.getInstance(getApplicationContext());

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
        getMenuInflater().inflate(R.menu.activity_community_details_menu, menu);
        return true;
    }

    private void quitCommunity() {
        if (NetConnectionUtils.isConnected(getApplicationContext())) {
            ouQuOnMangeService.quitCommunity(community.uuid)
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Action1<Message>() {
                        @Override
                        public void call(Message message) {
                            repository.deleteMyCommunities(new Callback<Boolean>() {
                                @Override
                                public void apply(Boolean aBoolean) {
                                    Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                                    intent.putExtra(Constants.FROM_MENU, Constants.FROM_MENU);
                                    startActivity(intent);
                                    finish();
                                }
                            });
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) {
                            Snackbar.make(coordinatorLayout, throwable.getMessage(), Snackbar.LENGTH_LONG).show();
                        }
                    });
        } else {
            NetConnectionUtils.showNoConnexionSnackBar(coordinatorLayout, this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_community_quit:
                quitCommunity();
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
}
