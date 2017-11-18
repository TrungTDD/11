package com.hackathon.smessage.activities;

import android.content.DialogInterface;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.hackathon.smessage.R;
import com.hackathon.smessage.adapters.BlockedPagerAdapter;
import com.hackathon.smessage.controllers.BlockedOperation;
import com.hackathon.smessage.fragments.BlockedFragment;
import com.hackathon.smessage.models.Blocked;
import com.hackathon.smessage.utils.PhoneNumberUtils;

import java.util.ArrayList;
import java.util.Locale;

public class BlockedActivity extends DefaultActivity {

    private BlockedPagerAdapter mAdapter;
    private BlockedFragment mBlockedSmsFragment, mBlockedCallFragment;
    private ViewPager mViewPager;
    private TabLayout mTabs;
    private BlockedOperation mBlockedOperation;
    private EditText mEditText;
    private RadioButton mRadioBtSMS, mRadioBtCall, mRadioBtContact, mRadioBtcontent;
    private RadioGroup mRadioGroup;
    private SearchView mSearchView;


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
        showSearch(menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        createChooseBlockDialog(item);
        mAdapter.notifyDataSetChanged();
        return super.onOptionsItemSelected(item);
    }
    public void checkRadioAll(View view){
        switch (view.getId()){
            case R.id.radiobtCall:
                mRadioBtContact.setVisibility(View.GONE);
                mRadioBtcontent.setVisibility(View.GONE);
                mEditText.setHint(R.string.phon_number);
                mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                break;
            case R.id.radiobtSms:
                mRadioBtContact.setVisibility(View.VISIBLE);
                mRadioBtcontent.setVisibility(View.VISIBLE);
                mEditText.setHint(R.string.text);
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                break;
            case R.id.radiobtCentent:
                mEditText.setInputType(InputType.TYPE_CLASS_TEXT);
                mEditText.setHint(R.string.text); break;
            case R.id.radiobtContact:
                mEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
                mEditText.setHint(R.string.phon_number);
        }
    }
    private void createChooseBlockDialog(MenuItem item){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_add_blocked, null);
        mRadioBtSMS = (RadioButton)layout.findViewById(R.id.radiobtSms);
        mRadioBtCall = (RadioButton)layout.findViewById(R.id.radiobtCall);
        mRadioBtcontent = (RadioButton)layout.findViewById(R.id.radiobtCentent);
        mRadioBtContact = (RadioButton)layout.findViewById(R.id.radiobtContact);
        mEditText = (EditText)layout.findViewById(R.id.edBlock);
        mRadioGroup = (RadioGroup)layout.findViewById(R.id.radioGroupBlock);
        mRadioBtSMS.setChecked(true);
        mRadioBtcontent.setChecked(true);
        switch (item.getItemId()){
            case R.id.menu_new_blocked:
                final AlertDialog dialog = new AlertDialog.Builder(this)
                        .setTitle(R.string.blocked)
                        .setIcon(R.drawable.ic_close_normal)
                        .setView(layout)
                        .setIcon(R.drawable.ic_blocked_call_sms)
                        .setPositiveButton(R.string.save, null)
                        .setNegativeButton(R.string.cancel, null)
                        .create();
                dialog.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialogInterface) {
                        final Button button = ((AlertDialog) dialog).getButton(AlertDialog.BUTTON_POSITIVE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                chooseRadioButon( dialog);
                            }
                        });
                    }
                });
                dialog.show();
                mAdapter.notifyDataSetChanged();
                break;
        }
    }
    private void chooseRadioButon(AlertDialog dialog){
        switch (mRadioGroup.getCheckedRadioButtonId()){
            case R.id.radiobtSms:
                if(mEditText.getText().toString().equals("") == false
                        && mRadioBtcontent.isChecked()){
                    BlockedOperation.getInstance().add(new Blocked(mEditText.getText().toString(), true, mRadioBtContact.isChecked()));
                    mBlockedSmsFragment.setData(BlockedOperation.getInstance().getBlockedSMS());
                    dialog.dismiss();
                    break;
                }
                if(mEditText.getText().toString().equals("") == false
                        && mRadioBtContact.isChecked()&& PhoneNumberUtils.isValid(mEditText.getText().toString())){
                    BlockedOperation.getInstance().add(new Blocked(mEditText.getText().toString(), true, mRadioBtContact.isChecked()));
                    mBlockedSmsFragment.setData(BlockedOperation.getInstance().getBlockedSMS());
                    dialog.dismiss();
                }
                else Toast.makeText(getApplicationContext(), R.string.error_text, Toast.LENGTH_SHORT).show();
                break;
            case R.id.radiobtCall:
                if(!PhoneNumberUtils.isValid(mEditText.getText().toString())
                        || mEditText.getText().toString().equals("") == true){
                    Toast.makeText(getApplicationContext(), R.string.invalid_phone_warn, Toast.LENGTH_SHORT).show();
                }
                else{
                    BlockedOperation.getInstance().add(new Blocked(mEditText.getText().toString(), false, true));
                    mBlockedCallFragment.setData(BlockedOperation.getInstance().getBlockedCall());
                    dialog.dismiss();
                }break;
        }
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
    private void showSearch(Menu menu){
        mSearchView = (SearchView) menu.findItem(R.id.menu_block_search).getActionView();
        mSearchView.setQueryHint(getString(R.string.search_hint));
        mSearchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                String query = newText.toLowerCase(Locale.getDefault());
                ArrayList<String> listSMS = new ArrayList<>();
                ArrayList<String> listCall = new ArrayList<>();
                ArrayList<Blocked> blockedSmsList = mBlockedSmsFragment.getList();
                ArrayList<Blocked> blockCallList = mBlockedCallFragment.getList();
                for(Blocked blocked : blockedSmsList){
                    if(blocked.getContent().toLowerCase(Locale.getDefault()).contains(query)){
                        listSMS.add(blocked.getContent());
                    }
                }

                for(Blocked blocked : blockCallList){
                    if(blocked.getContent().toLowerCase(Locale.getDefault()).contains(query)){
                        listCall.add(blocked.getContent());
                    }
                }
                mBlockedSmsFragment.getAdapter().setFilter(listSMS);
                mBlockedCallFragment.getAdapter().setFilter(listCall);
                return false;
            }
        });
    }
}
