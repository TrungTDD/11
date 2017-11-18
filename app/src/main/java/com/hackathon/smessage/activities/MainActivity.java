package com.hackathon.smessage.activities;

import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.widget.TextView;

import com.hackathon.smessage.BuildConfig;
import com.hackathon.smessage.R;
import com.hackathon.smessage.utils.PermissionUtils;

public class MainActivity extends DefaultActivity {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;
    //use load in thread
    private Handler mHandlerThread;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firstInit();
    }
    @Override
    protected void onResume() {
        super.onResume();
    }
    @Override
    public void onBackPressed() {
        if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
            mDrawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(PermissionUtils.onRequestPermissionsResult(this, requestCode)){
            init();
        }
    }
    //-------------------------------------------
    private void showSplash(){
        setContentView(R.layout.splash_main);
        TextView tvVersion = (TextView)findViewById(R.id.textViewVersion);
        tvVersion.setText(getString(R.string.version) + " " + BuildConfig.VERSION_NAME);
    }

    private void firstInit(){
        mActivity = this;
        showSplash();

        mHandlerThread = new Handler(){
            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                    start();
            }
        };
        if(PermissionUtils.isAllPermissionAlready(this)) {
            init();
        }
        else{
            PermissionUtils.requestPermissions(this);
        }
    }
    //load
    private void init(){
        PermissionUtils.requireRegisterDefaultApp(this);

        android.os.Message msg = mHandlerThread.obtainMessage();
        mHandlerThread.sendMessage(msg);
    }

    private void start(){
        getWidgets();
        setWidgets();
        addWidgetsListener();
    }

    private void getWidgets(){
        setContentView(R.layout.activity_main);
        mToolbar = (Toolbar)findViewById(R.id.toolbar);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
    }

    private void setWidgets(){
        setSupportActionBar(mToolbar);
        mNavigationView.setItemIconTintList(null);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mActionBarDrawerToggle.syncState();
    }

    private void addWidgetsListener(){
        mDrawerLayout.addDrawerListener(mActionBarDrawerToggle);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem item) {
                if(item.isChecked()){ //skip if re-selected
                    mDrawerLayout.closeDrawer(Gravity.START);
                    return false;
                }

                switch (item.getItemId()){
                    case R.id.menu_inbox_common:

                        break;
                    case R.id.menu_inbox_security:

                        break;
                    case R.id.menu_blocked_call_sms:
                        break;
                    case R.id.menu_setting:

                        break;
                }
                mDrawerLayout.closeDrawer(Gravity.START);
                return false;
            }
        });
    }
}
