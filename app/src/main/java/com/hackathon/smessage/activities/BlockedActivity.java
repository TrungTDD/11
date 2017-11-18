package com.hackathon.smessage.activities;

import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.hackathon.smessage.R;
import com.hackathon.smessage.adapters.BlockedPagerAdapter;
import com.hackathon.smessage.controllers.BlockedOperation;
import com.hackathon.smessage.fragments.BlockedFragment;

public class BlockedActivity extends AppCompatActivity {

    private BlockedPagerAdapter mAdapter;
    private BlockedFragment mBlockedSmsFragment, mBlockedCallFragment;
    private ViewPager mViewPager;
    private TabLayout mTabs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocked);

        init();
        getWidgets();
        setWidgets();
        addWidgetsListener();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_blocked_option, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.menu_new_blocked:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void init(){
        mAdapter = new BlockedPagerAdapter(getSupportFragmentManager());
        mBlockedSmsFragment = new BlockedFragment();
        mBlockedCallFragment = new BlockedFragment();

        mBlockedSmsFragment.setData(BlockedOperation.getInstance().getBlockedSMS());
        mBlockedCallFragment.setData(BlockedOperation.getInstance().getBlockedCall());

        mAdapter.addFragment(mBlockedSmsFragment, getString(R.string.blocked_sms));
        mAdapter.addFragment(mBlockedCallFragment, getString(R.string.blocked_call));
    }

    private void getWidgets(){
        mViewPager = (ViewPager) findViewById(R.id.viewpager);
        mTabs = (TabLayout) findViewById(R.id.tabs);
    }

    private void setWidgets(){
        mViewPager.setAdapter(mAdapter);
        mTabs.setupWithViewPager(mViewPager);
    }

    private void addWidgetsListener(){
    }
}
