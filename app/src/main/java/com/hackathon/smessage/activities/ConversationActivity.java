package com.hackathon.smessage.activities;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;

import android.view.ActionMode;
import android.view.ContextMenu;
import android.view.LayoutInflater;

import android.view.KeyEvent;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;

import android.widget.AbsListView;
import android.widget.AdapterView;

import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.hackathon.smessage.R;
import com.hackathon.smessage.adapters.ConversationArrayAdapter;
import com.hackathon.smessage.configs.AppConfigs;
import com.hackathon.smessage.configs.Defines;
import com.hackathon.smessage.controllers.BlockedOperation;
import com.hackathon.smessage.controllers.ContactOpearation;
import com.hackathon.smessage.controllers.MessageOpearation;
import com.hackathon.smessage.models.Blocked;
import com.hackathon.smessage.models.Contact;
import com.hackathon.smessage.models.Message;
import com.hackathon.smessage.utils.PhoneNumberUtils;
import com.hackathon.smessage.utils.Security;
import com.hackathon.smessage.utils.TimeUtils;
import com.hackathon.smessage.utils.Utils;

import java.util.ArrayList;
import java.util.Locale;

public class ConversationActivity extends DefaultActivity {

    private Message mCurrentMessage;
    private ArrayList<Message> mConversationList;
    private ConversationArrayAdapter mConversationAdapter;
    private ListView mLvConversation;

    private EditText mEtEnterMessage;
    private TextView mTvMessageCount;
    private Button mBtnSend;

    //
    private Message mSelectedMessage;


    private BroadcastReceiver mBroadcastSending;
    private ArrayList<Message> mSendingStack;
    private boolean isNewContact, isMute;
    private MenuItem tvOption, tvMute;
    private Contact contact;

    private SearchView mSearchView;

    private LinearLayout mlayoutPassword;
    private EditText mEtPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation);

        init();
        getWidgets();
        setWidgets();
        addWidgetsListener();


    }

    @Override
    protected void onResume() {
        super.onResume();
        if(mConversationAdapter != null){
            MessageOpearation.getInstance().getConversation(mCurrentMessage);
            updateConversation();
            contact = ContactOpearation.getInstance().getContactWithPhoneNumber(mCurrentMessage.getPhone());
            if(contact != null) {
                setTitle(contact.getName());
                mCurrentMessage.setContact(contact);
                if(isNewContact) {
                    tvOption.setTitle(R.string.view_contact);
                    isNewContact= false;
                }
            }
        }

        registerReceiver(mBroadcastSending, new IntentFilter(Defines.ACTION_SEND_SMS));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.conversation_option, menu);
        showSearch(menu);
        tvOption = menu.findItem(R.id.itemViewOrAdd);
        tvMute = menu.findItem(R.id.itemMute);
        if(!isNewContact ){
            menu.findItem(R.id.itemViewOrAdd).setTitle(R.string.view_contact);
        }else{
            menu.findItem(R.id.itemViewOrAdd).setTitle(R.string.add_contact);
        }

        if(isMute){
            menu.findItem(R.id.itemMute).setTitle(R.string.unmute);
        }else{
            menu.findItem(R.id.itemMute).setTitle(R.string.mute);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == R.id.itemViewOrAdd){
            if(!isNewContact){
                showContactDetail();
            }else{
                addNewContact(mCurrentMessage.getPhone());
            }
        }else if(id == R.id.callContact){
            dialCall(mCurrentMessage.getPhone());
        }

        if(id == R.id.itemBlockedContact){
            showConfirmBlockCallConversations();
        }

        if(id == R.id.itemBlockSMS){
            showConfirmBlockSMSConversations();
        }

        if(id == R.id.itemMute){
            AppConfigs.getInstance().setMuteContact(mCurrentMessage.getPhone(), !AppConfigs.getInstance().isMuteContact(mCurrentMessage.getPhone()));
            isMute = !isMute;

            if(isMute){
                tvMute.setTitle(R.string.unmute);
            }else{
                tvMute.setTitle(R.string.mute);
            }
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastSending);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_conversation_context_menu, menu);
        menu.setHeaderTitle("You want to");
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;

        mSelectedMessage = ((Message) mConversationAdapter.getItem(info.position));

    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.menu_copy_conversation_context_menu:
                //COPY
                break;
            case R.id.menu_delete_conversation_context_menu:
                MessageOpearation.getInstance().delete(mSelectedMessage);
                mConversationAdapter.notifyDataSetChanged();
                Toast.makeText(getApplicationContext(), R.string.deleted, Toast.LENGTH_SHORT).show();
                //DELETE
                break;
            case R.id.menu_forward_conversation_context_menu:
                //FORWARD
            case  R.id.menu_move_to_inbox_security_conversation_context_menu:
                //MOVE

                break;
        }
        return true;
    }

    private void init() {
        contact = null;
        isNewContact = false;
        Intent intent = getIntent();
        mCurrentMessage = (Message)intent.getSerializableExtra(Defines.PASS_MESSAGE_FROM_INBOX_TO_CONVERSATION);
        isMute = AppConfigs.getInstance().isMuteContact(mCurrentMessage.getPhone());

        //receive number from system
        if(mCurrentMessage == null){
            String phone = intent.getData().getSchemeSpecificPart();
            phone = PhoneNumberUtils.format(phone);
            Contact contact = ContactOpearation.getInstance().getContactWithPhoneNumber(phone);
            mCurrentMessage = new Message();
            mCurrentMessage.setPhone(phone);
            mCurrentMessage.setIsSecurity(AppConfigs.getInstance().isSecurity());
            mCurrentMessage.setContact(contact);
        }


        mConversationList = MessageOpearation.getInstance().getConversation(mCurrentMessage);
        mConversationAdapter = new ConversationArrayAdapter(this, R.layout.item_message_conversation, mConversationList);
        mSendingStack = new ArrayList<>();

        //checking is New contact
        contact = ContactOpearation.getInstance().getContactWithPhoneNumber(mCurrentMessage.getPhone());
        if(contact==null) {
            isNewContact = true;
        }else{
            isNewContact = false;
        }


        //update inbox if have SMS incoming
        mBroadcastReceivedSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //reload after receive sms
                updateConversation();
            }
        };

        //receive status
        mBroadcastSending = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (mSendingStack.size() > 0) {
                    Message message = MessageOpearation.getInstance().search(mSendingStack.get(0).getId());
                    //update time and status
                    message.setDate(TimeUtils.getInstance().getTimeSystem());
                    int result = getResultCode();
                    if (result == Activity.RESULT_OK) {
                        message.setSendStatus(Message.SEND_TO_STATUS_SENT);
                    } else {
                        message.setSendStatus(Message.SEND_TO_STATUS_FAILED);
                    }

                    MessageOpearation.getInstance().update(message);
                    updateConversation();
                    mSendingStack.remove(0);
                }
            }
        };
    }
    private void getWidgets() {
        mlayoutPassword = (LinearLayout)findViewById(R.id.layoutPassword);
        mEtPassword = (EditText)findViewById(R.id.etPassword);
        mLvConversation = (ListView)findViewById(R.id.lvConversation);
        mEtEnterMessage = (EditText)findViewById(R.id.etEnterMessage);
        mTvMessageCount = (TextView)findViewById(R.id.tvMessageCount);
        mBtnSend = (Button)findViewById(R.id.btnSend);
    }

    private void setWidgets() {
        mlayoutPassword.setVisibility(mCurrentMessage.isSecurity() ? View.VISIBLE : View.GONE);
        setTitle(mCurrentMessage.getContact().getName());
        mLvConversation.setAdapter(mConversationAdapter);
        mLvConversation.setSelection(mConversationList.size() - 1);
        mTvMessageCount.setText(AppConfigs.getInstance().isSecurity() ? String.valueOf(Message.ASCII_LENGTH - 1) : String.valueOf(Message.ASCII_LENGTH));
        mBtnSend.setEnabled(false);

    }

    private void addWidgetsListener(){
        registerForContextMenu(mLvConversation);

        mEtEnterMessage.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                int total = Message.UCS2_LENGTH;
                if(Utils.isAsciiMessage(charSequence.toString())){
                    total = Message.ASCII_LENGTH;
                }
                if(AppConfigs.getInstance().isSecurity()){
                    total--; //one character to mark Security code
                }
                int remainChar = total - (charSequence.toString().length() % total);
                int numMessage = charSequence.toString().length() / total;
                mTvMessageCount.setText(remainChar + (numMessage == 0 ? "" : ("/" + numMessage)));

                mBtnSend.setEnabled(!charSequence.toString().trim().isEmpty());
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        mBtnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendMessage();
                mEtEnterMessage.setText("");
            }
        });

        mLvConversation.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if(AppConfigs.getInstance().isSecurity()) {
                    requestDecrypt(i);
                }
            }
        });



    }

    /*private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_selected_message, null);
        builder.setView(inflater.inflate(R.layout.dialog_selected_message,null));

        ((Button)layout.findViewById(R.id.btn_copy_dialog_selected_message)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //COPY
                Toast.makeText(getApplicationContext(), "Copy", Toast.LENGTH_SHORT).show();
            }
        });((Button)layout.findViewById(R.id.btn_delete_dialog_selected_message)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //DELETE
                Toast.makeText(getApplicationContext(), "Delete", Toast.LENGTH_SHORT).show();
            }
        });((Button)layout.findViewById(R.id.btn_forward_dialog_selected_message)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //FORWARD
                Toast.makeText(getApplicationContext(), "forward", Toast.LENGTH_SHORT).show();
            }
        });((Button)layout.findViewById(R.id.btn_move_to_inbox_security_dialog_selected_message)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //MOVE TO INBOX SECURITY
                Toast.makeText(getApplicationContext(), "Move", Toast.LENGTH_SHORT).show();
            }
        });

        AlertDialog dialog = builder.create();

        dialog.show();
    }*/

    private void updateConversation(){
        mConversationAdapter.notifyDataSetChanged();
        mLvConversation.setSelection(mConversationList.size() - 1);
    }

    private void sendMessage(){
        String allMessageContent = mEtEnterMessage.getText().toString().trim();

        int smsLength = Message.UCS2_LENGTH;
        if(Utils.isAsciiMessage(allMessageContent.toString())){
            smsLength = Message.ASCII_LENGTH;
        }
        if(AppConfigs.getInstance().isSecurity()){
            smsLength--; //one character to mark Security code
        }

        int numMessage = allMessageContent.toString().length() / smsLength;
        if(allMessageContent.toString().length() % smsLength != 0){
            numMessage++;
        }

        String body = "";
        for(int i = 0; i < numMessage; i++){
            if(allMessageContent.length() > smsLength) {
                body = allMessageContent.substring(0, smsLength);
                allMessageContent = allMessageContent.substring(smsLength);
            }
            else{
                body = allMessageContent.substring(0, allMessageContent.length());
            }

            Message sendSms = new Message(mCurrentMessage.getPhone(),
                    body,
                    TimeUtils.getInstance().getTimeSystem(),
                    true,
                    false,
                    Message.SEND_TO_STATUS_SENDING,
                    mCurrentMessage.isSecurity());

            sendSms.encrypt(mEtPassword.getText().toString()); //encrypt to save and send
            int id = MessageOpearation.getInstance().add(sendSms);
            sendSms.setId(id);
            mSendingStack.add(sendSms); //push stack to update status
            sendSms.send(this);
            sendSms.decrypt(); //decrypt to show
        }
        updateConversation();
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
                mConversationAdapter.filter(query);
                return false;
            }
        });
    }

    private void showContactDetail(){
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(ContactsContract.Contacts.CONTENT_URI,""+ contact.getId()));
        startActivity(intent);
    }

    private void addNewContact(String phone){
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, phone);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }

    private void dialCall(String phone){
        String url = "tel:" + phone;
        Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse(url));
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        startActivity(intent);
    }

    private void requestDecrypt(final int index){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_enter_password, null);

        final EditText etPassword = (EditText)layout.findViewById(R.id.etPassword);
        etPassword.setHint(R.string.enter_decrypt_password);

        new AlertDialog.Builder(this)
                .setView(layout)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        String password = etPassword.getText().toString();
                        Message message = mConversationList.get(index);
                        message.encrypt();
                        message.decrypt(password);
                        showDecryptResult(message.getBody());

                        //show again
                        message.encrypt(password);
                        message.decrypt();
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .create().show();
    }


    private void showDecryptResult(String result){
        new AlertDialog.Builder(this)
                .setMessage(result)
                .setPositiveButton(android.R.string.ok, null)
                .create().show();
    }

    private void showConfirmBlockCallConversations(){
        new AlertDialog.Builder(this)
                .setTitle(R.string.block)
                .setMessage(R.string.add_block_Call_warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Blocked blocked = new Blocked(mCurrentMessage.getPhone(), false, true);
                        BlockedOperation.getInstance().add(blocked);
                        Toast.makeText(mActivity, "Block contact success !!!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create()
                .show();
    }

    private void showConfirmBlockSMSConversations(){
        new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.block)
                .setMessage(R.string.add_block_SMS_warning)
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Blocked blocked = new Blocked(mCurrentMessage.getPhone(), true, true);
                        BlockedOperation.getInstance().add(blocked);
                        MessageOpearation.getInstance().deleteConversation(mCurrentMessage);
                        Toast.makeText(getApplicationContext(),"Block SMS succes!!!", Toast.LENGTH_SHORT).show();
                        ConversationActivity.this.finish();
                    }
                })
                .setNegativeButton(android.R.string.no, null)
                .create()
                .show();
    }
}
