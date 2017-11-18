package com.hackathon.smessage.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.ActionMode;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.hackathon.smessage.BuildConfig;
import com.hackathon.smessage.R;
import com.hackathon.smessage.adapters.InboxArrayAdapter;
import com.hackathon.smessage.adapters.SearchAllMessageAdapter;
import com.hackathon.smessage.configs.AppConfigs;
import com.hackathon.smessage.configs.Defines;
import com.hackathon.smessage.controllers.BlockedOperation;
import com.hackathon.smessage.controllers.ContactOpearation;
import com.hackathon.smessage.controllers.MessageOpearation;
import com.hackathon.smessage.models.Blocked;
import com.hackathon.smessage.models.Contact;
import com.hackathon.smessage.models.Message;
import com.hackathon.smessage.utils.PermissionUtils;
import com.hackathon.smessage.utils.PhoneNumberUtils;

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

    //Contextual ation mode
    private ActionMode mActionMode;
    private MenuItem mItemCheckedAll;
    //use load in thread
    private Handler mHandlerThread;

    //choose contact to create new convesation
    private AlertDialog mChooseContactDialog;
    private AutoCompleteTextView mAcPhone;
    private Contact mChooseContact;
    // SearchView
    private SearchView mSearchView;
    private SearchAllMessageAdapter mSearchAllMessageAdapter;
    private ArrayList<Message> mListAllMessage;


    private interface ActionListenter{
        void positiveAction();
        void negativeAction();
    }


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
            case R.id.menu_new_message:
                mChooseContactDialog.show();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode){
            case Defines.REQUEST_PICK_CONTACT:
                if(resultCode == RESULT_OK){
                    Contact contact = ContactOpearation.getInstance().getContactFromSystem(this, data);
                    if(contact != null){
                        mAcPhone.setText(contact.format());
                        mChooseContact = contact;
                    }
                }
                break;
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
                if(AppConfigs.getInstance().isEnablePassword(AppConfigs.getInstance().isSecurity())){
                    showEnterPassword(AppConfigs.getInstance().isSecurity(), new ActionListenter() {
                        @Override
                        public void positiveAction() {
                            start();
                        }

                        @Override
                        public void negativeAction() {
                            finish();
                        }
                    });
                }
                else {
                    start();
                }
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
        ContactOpearation.getInstance().loadContacts();
        BlockedOperation.getInstance().loadBlocked();
        PermissionUtils.requireRegisterDefaultApp(this);
        MessageOpearation.getInstance().loadInbox(AppConfigs.getInstance().isSecurity());
        if(MessageOpearation.getInstance().getInbox().size() == 0 && MessageOpearation.getInstance().getInbox().size() == 0) {
            MessageOpearation.getInstance().fakeData();
        }
        if(BlockedOperation.getInstance().getBlockedCall().size() == 0){
            BlockedOperation.getInstance().fakeData();
        }

        mInboxList = MessageOpearation.getInstance().getInbox();
        mInboxAdapter = new InboxArrayAdapter(this, R.layout.item_message_inbox, mInboxList);

        mListAllMessage = MessageOpearation.getInstance().getAllMessages(false);
        mSearchAllMessageAdapter = new SearchAllMessageAdapter(MainActivity.this, mListAllMessage);
        createChooseContactDialog();

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

        //set MUTIL CHOIDE MODAL for LISTVIEW
        mLvInbox.setChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE_MODAL);
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
                        if(AppConfigs.getInstance().isEnablePassword(false)) {
                            showEnterPassword(false, new ActionListenter() {

                                @Override
                                public void positiveAction() {
                                    updateInbox(false);
                                }

                                @Override
                                public void negativeAction() {

                                }
                            });
                        }
                        else{
                            updateInbox(false);
                        }
                        break;
                    case R.id.menu_inbox_security:
                        if(AppConfigs.getInstance().isEnablePassword(true)) {
                            showEnterPassword(true, new ActionListenter() {

                                @Override
                                public void positiveAction() {
                                    updateInbox(true);
                                }

                                @Override
                                public void negativeAction() {

                                }
                            });
                        }
                        else{
                            updateInbox(true);
                        }
                        break;
                    case R.id.menu_blocked_call_sms:
                        startActivity(new Intent(MainActivity.this, BlockedActivity.class));
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


        //SET CONTEXTUAL ACTION BAR
        mLvInbox.setMultiChoiceModeListener(new AbsListView.MultiChoiceModeListener() {
            @Override
            public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
                mInboxAdapter.setItemChecked(position, checked);
                setViewCheckedAll(mInboxAdapter.isAllChecked());
                    mActionMode.setTitle(mInboxAdapter.getCheckedCount() + " " + R.string.selected);

            }

            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                mActionMode = mode;
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.activity_main_contextual_action_mode, menu);
                mItemCheckedAll = (MenuItem)menu.findItem(R.id.menu_checked_all);
                setViewCheckedAll(false);
                return true;
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                return false;
            }

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_checked_all:
                        int numSelected = mInboxAdapter.getCheckedCount();
                        boolean isChecked = !(numSelected == mInboxList.size());
                        setViewCheckedAll(isChecked);
                        item.setChecked(isChecked);
                        for (int i = 0; i < mInboxList.size(); i++) {
                            mLvInbox.setItemChecked(i, isChecked);
                        }
                        mInboxAdapter.setAllChecked(isChecked);
                        break;
                    case R.id.menu_delete:
                        //delete
                        showConfirmDeleteConversation();
                        break;
                    case R.id.menu_block_sms:
                        //block sms
                        showConfirmBlockSMSConversations();
                        break;
                    case R.id.menu_block_call:
                        //block call
                        showConfirmBlockCallConversations();
                        break;
                }
                return false;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                mInboxAdapter.setAllChecked(false);
            }
        });
    }
    private void showConfirmBlockSMSConversations(){
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.block)
                .setMessage(R.string.add_block_SMS_warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int count = 0;
                        for(int  index = mInboxList.size() - 1; index >= 0; index--){
                            if(mInboxAdapter.isItemChecked(index)){
                                Message message = mInboxList.get(index);
                                //MessageOpearation.getInstance().deleteConversation(mInboxList.get(index));
                                Blocked blocked = new Blocked(message.getPhone(), true, true);
                                BlockedOperation.getInstance().add(blocked);
                                MessageOpearation.getInstance().deleteConversation(mInboxList.get(index));
                                count++;
                            }
                        }
                        Toast.makeText(getApplicationContext(),"Blocked: " + count, Toast.LENGTH_SHORT).show();
                        mInboxAdapter.clear();
                        mInboxAdapter.notifyDataSetChanged();
                        mActionMode.finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create()
                .show();
    }private void showConfirmBlockCallConversations(){
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.block)
                .setMessage(R.string.add_block_Call_warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        int count = 0;
                        for(int index = mInboxList.size() - 1; index >= 0; index--){
                            if(mInboxAdapter.isItemChecked(index)){
                                Message message = mInboxList.get(index);
                                //MessageOpearation.getInstance().deleteConversation(mInboxList.get(index));
                                Blocked blocked = new Blocked(message.getPhone(), false, true);
                                BlockedOperation.getInstance().add(blocked);
                                count++;
                            }
                        }
                        Toast.makeText(getApplicationContext(),"Blocked: " + count, Toast.LENGTH_SHORT).show();
                        mInboxAdapter.clear();
                        mInboxAdapter.notifyDataSetChanged();
                        mActionMode.finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create()
                .show();
    }
    private void showConfirmDeleteConversation(){
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.delete)
                .setMessage(R.string.delete_conversation_warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        for(int index = mInboxList.size() - 1; index >= 0; index--){
                            if(mInboxAdapter.isItemChecked(index)){
                                MessageOpearation.getInstance().deleteConversation(mInboxList.get(index));
                            }
                        }
                        mInboxAdapter.clear();
                        mInboxAdapter.notifyDataSetChanged();
                        mActionMode.finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create()
                .show();
    }
    private void setViewCheckedAll(boolean isAll){
        if(isAll){
            mItemCheckedAll.setIcon(android.R.drawable.checkbox_on_background);
        }
        else{
            mItemCheckedAll.setIcon(android.R.drawable.checkbox_off_background);
        }
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
    private void createChooseContactDialog(){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_choose_contact, null);
        mAcPhone = (AutoCompleteTextView)layout.findViewById(R.id.acPhone);
        Button btnContact = (Button)layout.findViewById(R.id.btnContact);

        ArrayList<Contact> list = ContactOpearation.getInstance().getContacts();
        String[] nameList = new String[list.size()];
        for(int i = 0; i < list.size(); i++){
            Contact contact = list.get(i);
            nameList[i] = contact.getName();
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, nameList);
        mAcPhone.setAdapter(adapter);

        mAcPhone.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mChooseContact = ContactOpearation.getInstance().getContactWithName(mAcPhone.getText().toString());
                if(mChooseContact != null){
                    mAcPhone.setText(mChooseContact.format());
                }
            }
        });

        btnContact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                mActivity.startActivityForResult(intent, Defines.REQUEST_PICK_CONTACT);
            }
        });

        mChooseContactDialog =  new AlertDialog.Builder(this)
                .setIcon(R.drawable.ic_contact)
                .setTitle(R.string.title_choose_contact)
                .setView(layout)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, null)
                .create();
        mChooseContactDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button button = ((AlertDialog) mChooseContactDialog).getButton(AlertDialog.BUTTON_POSITIVE);
                button.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View view) {
                        //check phone is valid or not
                        if(mChooseContact == null && !PhoneNumberUtils.isValid(mAcPhone.getText().toString())){
                            Toast.makeText(getApplicationContext(), R.string.invalid_phone_warn, Toast.LENGTH_SHORT).show();
                        }
                        else{
                            String phone = mChooseContact != null ? mChooseContact.getPhoneNumber() : mAcPhone.getText().toString();
                            Message message = new Message();
                            message.setPhone(phone);
                            message.setIsSecurity(AppConfigs.getInstance().isSecurity());
                            gotoConversation(message);
                            mAcPhone.setText("");
                            mChooseContactDialog.dismiss();
                        }

                    }
                });
            }
        });
    }

    private void showEnterPassword(final boolean isSecurity, final ActionListenter actionListenter){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_enter_password, null);

        final EditText etPassword = (EditText)layout.findViewById(R.id.etPassword);

        int titleId = isSecurity ? R.string.inbox_security : R.string.inbox_common;

        final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle(titleId)
                .setIcon(R.drawable.ic_dialog_locked)
                .setView(layout)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        if(actionListenter != null){
                            actionListenter.negativeAction();
                        }
                    }
                })
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnOK = dialog.getButton(android.support.v7.app.AlertDialog.BUTTON_POSITIVE);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String currentPass = AppConfigs.getInstance().getPassword(isSecurity);
                        String pass = etPassword.getText().toString();
                        if(currentPass.equals(pass)) {
                            if(actionListenter != null){
                                actionListenter.positiveAction();
                            }
                            dialog.dismiss();
                        }
                        else{
                            Toast.makeText(getApplicationContext(), getString(R.string.wrong_password), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
        dialog.show();
    }
}
