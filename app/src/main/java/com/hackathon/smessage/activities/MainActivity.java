package com.hackathon.smessage.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hackathon.smessage.BuildConfig;
import com.hackathon.smessage.R;
import com.hackathon.smessage.adapters.InboxArrayAdapter;
import com.hackathon.smessage.adapters.SearchAllMessageAdapter;
import com.hackathon.smessage.configs.AppConfigs;
import com.hackathon.smessage.configs.Defines;
import com.hackathon.smessage.controllers.ContactOpearation;
import com.hackathon.smessage.controllers.MessageOpearation;
import com.hackathon.smessage.models.Message;
import com.hackathon.smessage.utils.PermissionUtils;

import java.util.ArrayList;
import java.util.Locale;
import java.util.PropertyPermission;

public class MainActivity extends DefaultActivity {

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private NavigationView mNavigationView;

    //MenuItem
    private MenuItem mMenuItemInboxCommon;
    private MenuItem mMenuItemInboxSecurity;
    //Inbox List
    private RelativeLayout mLayoutNoConversation;
    private ListView mLvInbox;
    private InboxArrayAdapter mInboxAdapter;
    private ArrayList<Message> mInboxList;
    //use load in thread
    private Handler mHandlerThread;

    // SearchView
    private SearchView mSearchView;
    private SearchAllMessageAdapter mSearchAllMessageAdapter;
    private ArrayList<Message> mListAllMessage;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        firstInit();
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(mLvInbox != null){
            updateInbox(AppConfigs.getInstance().isSecurity());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_option, menu);
        showSearch(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id){
            case R.id.menu_search:

                return true;
            case R.id.menu_new_message:

                return true;
            default:
                return false;
        }
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
        AppConfigs.getInstance().setIsSecurity(false);
        showSplash();

        mHandlerThread = new Handler(){
            @Override
            public void handleMessage(android.os.Message msg) {
                super.handleMessage(msg);
                    start();
            }
        };

        //update inbox if have SMS incoming
        mBroadcastReceivedSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //reload after receive sms
                updateInbox(AppConfigs.getInstance().isSecurity());
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
        MessageOpearation.getInstance().loadInbox(AppConfigs.getInstance().isSecurity());
        if(MessageOpearation.getInstance().getInbox().size() == 0 && MessageOpearation.getInstance().getInbox().size() == 0) {
            MessageOpearation.getInstance().fakeData();
        }


        mInboxList = MessageOpearation.getInstance().getInbox();
        mInboxAdapter = new InboxArrayAdapter(this, R.layout.item_message_inbox, mInboxList);

        android.os.Message msg = mHandlerThread.obtainMessage();
        mHandlerThread.sendMessage(msg);

        mListAllMessage = MessageOpearation.getInstance().getAllMessages(false);
        mSearchAllMessageAdapter = new SearchAllMessageAdapter(MainActivity.this, mListAllMessage);
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
        mLvInbox = (ListView)findViewById(R.id.lvInbox);
        mLayoutNoConversation = (RelativeLayout)findViewById(R.id.layout_no_conversation);

        Menu menu = mNavigationView.getMenu();
        mMenuItemInboxCommon = menu.findItem(R.id.menu_inbox_common);
        mMenuItemInboxSecurity = menu.findItem(R.id.menu_inbox_security);


    }

    private void setWidgets(){
        setSupportActionBar(mToolbar);
        mNavigationView.setItemIconTintList(null);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar,
                                    R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mActionBarDrawerToggle.syncState();
        mLvInbox.setAdapter(mInboxAdapter);

        //default selected Inbox common
        mMenuItemInboxCommon.setChecked(true);
        updateInbox(AppConfigs.getInstance().isSecurity());
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
                        updateInbox(false);
                        break;
                    case R.id.menu_inbox_security:
                        updateInbox(true);
                        break;
                    case R.id.menu_blocked_call_sms:
                        break;
                    case R.id.menu_setting:
                        startActivity(new Intent(MainActivity.this, SettingsActivity.class));
                        break;
                }
                mDrawerLayout.closeDrawer(Gravity.START);
                return false;
            }
        });

        mLvInbox.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Message message = mInboxList.get(i);
                gotoConversation(message);
            }
        });
    }

    private void gotoConversation(Message message){
        Intent intent = new Intent(MainActivity.this, ConversationActivity.class);
        intent.putExtra(Defines.PASS_MESSAGE_FROM_INBOX_TO_CONVERSATION, message);
        startActivity(intent);
    }

    private void updateInbox(boolean isSecurity){
        AppConfigs.getInstance().setIsSecurity(isSecurity);
        MessageOpearation.getInstance().loadInbox(isSecurity);
        mInboxAdapter.notifyDataSetChanged();

        //show no message or not
        if(mInboxList.size() == 0){
            mLayoutNoConversation.setVisibility(View.VISIBLE);
            mLvInbox.setVisibility(View.GONE);
        }
        else{
            mLayoutNoConversation.setVisibility(View.GONE);
            mLvInbox.setVisibility(View.VISIBLE);
        }

        //update action bar title
        int titleID = isSecurity ? R.string.inbox_security : R.string.inbox_common;
        setTitle(titleID);

        //set unread message
        //common
        int unreadSms = MessageOpearation.getInstance().getUnreadNumber(false);
        mMenuItemInboxCommon.setTitle(getString(R.string.inbox_common) + (unreadSms == 0 ? "" : " (" + unreadSms + ")"));

        //security
        unreadSms = MessageOpearation.getInstance().getUnreadNumber(true);
        mMenuItemInboxSecurity.setTitle(getString(R.string.inbox_security) + (unreadSms == 0 ? "" : " (" + unreadSms + ")"));

        mMenuItemInboxSecurity.setChecked(isSecurity);
        mMenuItemInboxCommon.setChecked(!mMenuItemInboxSecurity.isChecked());
    }

    private void showSearch(Menu menu){
        mSearchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String query = newText.toLowerCase(Locale.getDefault());
                if(query.length() == 0){
                    mLvInbox.setAdapter(mInboxAdapter);
                }
                else {
                    mLvInbox.setAdapter(mSearchAllMessageAdapter);
                    mSearchAllMessageAdapter.filter(query);
                }
                return true;
            }
        });
    }
}
