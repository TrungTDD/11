package com.hackathon.smessage.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.SearchView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.hackathon.smessage.R;
import com.hackathon.smessage.adapters.ConversationArrayAdapter;
import com.hackathon.smessage.configs.AppConfigs;
import com.hackathon.smessage.configs.Defines;
import com.hackathon.smessage.controllers.ContactOpearation;
import com.hackathon.smessage.controllers.MessageOpearation;
import com.hackathon.smessage.models.Contact;
import com.hackathon.smessage.models.Message;
import com.hackathon.smessage.utils.PhoneNumberUtils;
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

    private BroadcastReceiver mBroadcastSending;
    private ArrayList<Message> mSendingStack;

    private SearchView mSearchView;

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
        }
        registerReceiver(mBroadcastSending, new IntentFilter(Defines.ACTION_SEND_SMS));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.activity_main_option, menu);
        showSearch(menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mBroadcastSending);
    }

    private void init() {
        Intent intent = getIntent();
        mCurrentMessage = (Message)intent.getSerializableExtra(Defines.PASS_MESSAGE_FROM_INBOX_TO_CONVERSATION);


        mConversationList = MessageOpearation.getInstance().getConversation(mCurrentMessage);
        mConversationAdapter = new ConversationArrayAdapter(this, R.layout.item_message_conversation, mConversationList);
        mSendingStack = new ArrayList<>();

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
        mLvConversation = (ListView)findViewById(R.id.lvConversation);
        mEtEnterMessage = (EditText)findViewById(R.id.etEnterMessage);
        mTvMessageCount = (TextView)findViewById(R.id.tvMessageCount);
        mBtnSend = (Button)findViewById(R.id.btnSend);
    }

    private void setWidgets() {
        setTitle(mCurrentMessage.getContact().getName());
        mLvConversation.setAdapter(mConversationAdapter);
        mLvConversation.setSelection(mConversationList.size() - 1);
        mTvMessageCount.setText(AppConfigs.getInstance().isSecurity() ? String.valueOf(Message.ASCII_LENGTH - 1) : String.valueOf(Message.ASCII_LENGTH));
        mBtnSend.setEnabled(false);
    }

    private void addWidgetsListener(){
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


    }
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

            sendSms.encrypt(); //encrypt to save and send
            int id = MessageOpearation.getInstance().add(sendSms);
            sendSms.setId(id);
            mSendingStack.add(sendSms); //push stack to update status
            sendSms.send(this);
            Utils.LOG(sendSms.toString());
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
}
