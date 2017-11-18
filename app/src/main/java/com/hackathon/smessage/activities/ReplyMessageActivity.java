package com.hackathon.smessage.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hackathon.smessage.R;
import com.hackathon.smessage.adapters.PopupMessageAdapter;
import com.hackathon.smessage.configs.AppConfigs;
import com.hackathon.smessage.configs.Defines;
import com.hackathon.smessage.controllers.ContactOpearation;
import com.hackathon.smessage.controllers.MessageOpearation;
import com.hackathon.smessage.models.Contact;
import com.hackathon.smessage.models.Message;
import com.hackathon.smessage.utils.TimeUtils;
import com.hackathon.smessage.utils.Utils;

import java.util.ArrayList;

public class ReplyMessageActivity extends DefaultActivity {

    private AlertDialog mReplyPopup;
    private View mLayout;

    private ViewPager vpConversation;
    private TabLayout tabDots;
    private ArrayList<Message> mQueueList;
    private PopupMessageAdapter mAdapter;
    private int mCurrentIndex;

    private EditText mEtEnterMessage;
    private TextView mTvMessageCount;
    private Button mBtnSend;
    private ProgressBar mPbWWaiting;

    private BroadcastReceiver mBroadcastSending;
    private ArrayList<Message> mSendingStack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature (Window.FEATURE_NO_TITLE);

        createDialog();
        init();
        getWidgets();
        setWidgets();
        addWidgetsListener();
    }

    @Override
    protected void onResume() {
        super.onResume();
        AppConfigs.getInstance().setIsPopupShowing(true);
        registerReceiver(mBroadcastSending, new IntentFilter(Defines.ACTION_SEND_SMS));
    }

    @Override
    protected void onPause() {
        super.onPause();
        AppConfigs.getInstance().setIsPopupShowing(false);
        unregisterReceiver(mBroadcastSending);
    }

    private void createDialog(){
        LayoutInflater inflater = getLayoutInflater();
        mLayout = inflater.inflate(R.layout.dialog_reply_message, null);

        View headerLayout = inflater.inflate(R.layout.dialog_title_reply_message, null);
        Button btnClose = (Button)headerLayout.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mActivity.finish();
            }
        });
        mReplyPopup = new AlertDialog.Builder(this)
                .setCancelable(false)
                .setCustomTitle(headerLayout)
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if(i == KeyEvent.KEYCODE_BACK){
                            finish();
                            return true;
                        }
                        return false;
                    }
                })
                .setView(mLayout).create();
    }

    private void init(){
        mActivity = this;
        mCurrentIndex = 0;
        mQueueList = new ArrayList<>();
        Intent intent = getIntent();
        Message message = (Message)intent.getSerializableExtra(Defines.PASS_MESSAGE_FROM_RECEIVER);
        addMessage(message);

        mBroadcastReceivedSMS = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                //reload after receive sms
                Message message = (Message)intent.getSerializableExtra(Defines.PASS_MESSAGE_FROM_RECEIVER);
                addMessage(message);
                setViewPaper();
            }
        };

        //receive status
        mSendingStack = new ArrayList<>();
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
                    mSendingStack.remove(0);
                }

                if(mSendingStack.size() == 0){
                    //close pop up
                    mActivity.finish();
                }
            }
        };
    }

    private void getWidgets(){
        vpConversation = (ViewPager) mLayout.findViewById(R.id.vpConversation);
        tabDots = (TabLayout) mLayout.findViewById(R.id.tabDots);
        mEtEnterMessage = (EditText)mLayout.findViewById(R.id.etEnterMessage);
        mTvMessageCount = (TextView)mLayout.findViewById(R.id.tvMessageCount);
        mBtnSend = (Button)mLayout.findViewById(R.id.btnSend);
        mPbWWaiting = (ProgressBar)mLayout.findViewById(R.id.pbWWaiting);
    }

    private void setWidgets(){
        tabDots.setupWithViewPager(vpConversation, true);
        mTvMessageCount.setText(AppConfigs.getInstance().isSecurity() ? String.valueOf(Message.ASCII_LENGTH - 1) : String.valueOf(Message.ASCII_LENGTH));
        mBtnSend.setEnabled(false);
        mPbWWaiting.setVisibility(View.GONE);
        setViewPaper();

        if(AppConfigs.getInstance().isEnablePassword(false)){
            showEnterPassword(false);
        }
        else{
            mReplyPopup.show();
        }
    }

    private void addWidgetsListener(){
        vpConversation.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }

            @Override
            public void onPageSelected(int position) {
                mCurrentIndex = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

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
                mPbWWaiting.setVisibility(View.VISIBLE);
                mBtnSend.setVisibility(View.GONE);
            }
        });
    }

    private void setViewPaper(){
        mAdapter = new PopupMessageAdapter(this, R.layout.item_popup_message, mQueueList);
        vpConversation.setAdapter(mAdapter);
        vpConversation.setCurrentItem(mCurrentIndex);
    }

    private void addMessage(Message message){
        Contact contact = ContactOpearation.getInstance().getContactWithPhoneNumber(message.getPhone());
        message.setContact(contact);
        mQueueList.add(message);
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

            Message mCurrentMessage = mQueueList.get(mCurrentIndex);

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
    }

    private void showEnterPassword(final boolean isSecurity){
        LayoutInflater inflater = getLayoutInflater();
        View layout = inflater.inflate(R.layout.dialog_enter_password, null);

        final EditText etPassword = (EditText)layout.findViewById(R.id.etPassword);

        final android.app.AlertDialog dialog = new android.app.AlertDialog.Builder(this)
                .setTitle(R.string.app_name)
                .setCancelable(false)
                .setIcon(R.drawable.inbox_big_icon)
                .setView(layout)
                .setPositiveButton(android.R.string.ok, null)
                .setNegativeButton(R.string.exit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                })
                .setOnKeyListener(new DialogInterface.OnKeyListener() {
                    @Override
                    public boolean onKey(DialogInterface dialogInterface, int i, KeyEvent keyEvent) {
                        if(i == KeyEvent.KEYCODE_BACK){
                            finish();
                            return true;
                        }
                        return false;
                    }
                })
                .create();
        dialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface dialogInterface) {
                Button btnOK = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
                btnOK.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        String currentPass = AppConfigs.getInstance().getPassword(isSecurity);
                        String pass = etPassword.getText().toString();
                        if(currentPass.equals(pass)) {
                            mReplyPopup.show();
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
