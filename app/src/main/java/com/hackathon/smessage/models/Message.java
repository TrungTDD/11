package com.hackathon.smessage.models;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.telephony.SmsManager;

import com.hackathon.smessage.configs.Defines;
import com.hackathon.smessage.utils.Security;
import com.hackathon.smessage.utils.Utils;

import java.io.Serializable;
import java.util.HashMap;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class Message implements Serializable {

    public static int SEND_TO_STATUS_SENDING = 0;
    public static int SEND_TO_STATUS_SENT = 1;
    public static int SEND_TO_STATUS_FAILED = 2;

    public static final int ASCII_LENGTH = 160; //last for type of encrypt
    public static final int UCS2_LENGTH = 70; //last for type of encrypt
    public static final char SECURITY_CODE = '_';

    public static String FIELD_ID = "id";
    public static String FIELD_PHONE = "phone";
    public static String FIELD_BODY = "body";
    public static String FIELD_DATE = "date";
    public static String FIELD_READ = "read";
    public static String FIELD_RECEIVE = "receive";
    public static String FIELD_SEND_STATUS = "send_status";
    public static String FIELD_SECURITY = "security";
    public static String FIELD_LAST = "last";

    private int mId;
    private String mPhone;
    private String mBody;
    private String mDate;
    private boolean mIsRead; //true is read, false is unread
    private boolean mIsReceive; //true is receive, false is send to
    private int mSendStatus; //0: is sending, 1: sent, 2: failed
    private boolean mIsSecurity; //true is security message, false is nornal message
    private boolean mIsLast; //true is lase message, false is message before

    private static HashMap<String, Integer> sUnreadNumber = new HashMap<>();
    private static HashMap<String, Contact> sContacts = new HashMap<>();

    private void create(int mId, String mPhone, String mBody, String mDate, boolean isRead, boolean mIsReceive, int mSendStatus, boolean isSecurity, boolean isLast) {
        this.mId = mId;
        this.mPhone = mPhone;
        this.mBody = mBody;
        this.mDate = mDate;
        this.mIsRead = isRead;
        this.mIsReceive = mIsReceive;
        this.mSendStatus = mSendStatus;
        this.mIsSecurity = isSecurity;
        this.mIsLast = isLast;
    }

    public Message(){

    }

    public Message(int mId, String mPhone, String mBody, String mDate, boolean isRead, boolean mIsReceive, int mSendStatus, boolean isSecurity, boolean isLast) {
        create(mId, mPhone, mBody, mDate, isRead, mIsReceive, mSendStatus, isSecurity, isLast);
    }

    /**
     * create new message ==> last message: mIsLast = true
     * @param mId
     * @param mPhone
     * @param mBody
     * @param mDate
     * @param mIsRead
     * @param mIsReceive
     * @param mSendStatus
     * @param isSecurity
     */
    public Message(int mId, String mPhone, String mBody, String mDate, boolean mIsRead, boolean mIsReceive, int mSendStatus, boolean isSecurity) {
        create(mId, mPhone, mBody, mDate, mIsRead, mIsReceive, mSendStatus, isSecurity, true);
    }

    //no Id :create new

    /**
     * create new message ==> last message: mIsLast = true
     * id = -1
     * @param mPhone
     * @param mBody
     * @param mDate
     * @param mIsRead
     * @param mIsReceive
     * @param mSendStatus
     * @param isSecurity
     */
    public Message(String mPhone, String mBody, String mDate, boolean mIsRead, boolean mIsReceive, int mSendStatus, boolean isSecurity) {
        create(-1, mPhone, mBody, mDate, mIsRead, mIsReceive, mSendStatus, isSecurity, true);
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public String getPhone() {
        return mPhone;
    }

    public void setPhone(String mPhone) {
        this.mPhone = mPhone;
    }

    public String getBody() {
        return mBody;
    }

    public void setBody(String mBody) {
        this.mBody = mBody;
    }

    public String getDate() {
        return mDate;
    }

    public void setDate(String mDate) {
        this.mDate = mDate;
    }

    public boolean isRead() {
        return mIsRead;
    }

    public void setIsRead(boolean mIsRead) {
        this.mIsRead = mIsRead;
    }

    public boolean isReceive() {
        return mIsReceive;
    }

    public void setIsReceive(boolean mIsReceive) {
        this.mIsReceive = mIsReceive;
    }

    public int getSendStatus() {
        return mSendStatus;
    }

    public void setSendStatus(int mSendStatus) {
        this.mSendStatus = mSendStatus;
    }

    public boolean isSecurity() {
        return mIsSecurity;
    }

    public void setIsSecurity(boolean security) {
        mIsSecurity = security;
    }

    public boolean isLast() {
        return mIsLast;
    }

    public void setIsLast(boolean isLast) {
        mIsLast = isLast;
    }

    public void setContact(Contact contact){
        sContacts.put(mPhone, contact);
    }

    public Contact getContact(){
        Contact contact = sContacts.get(mPhone);
        if(contact == null){
            contact = new Contact(mPhone, mPhone, null);
        }
        return contact;
    }

    public void setUnreadNumber(int num){
        sUnreadNumber.put(mPhone, num);
    }

    public int getUnreadNumber(){
        Integer num = sUnreadNumber.get(mPhone);
        if (num == null){
            num = 0;
        }
        return num;
    }

    public void encrypt(){
        encrypt("");
    }

    public void encrypt(String password){
        if(mIsSecurity){
            mBody = SECURITY_CODE + mBody;
            mBody = Security.getInstance().encrypt(mBody, password);
        }
    }

    public void decrypt(){
        decrypt("");
    }

    public void decrypt(String password){
        if(mIsSecurity){
            mBody = Security.getInstance().decrypt(mBody, password);
            mBody = mBody.substring(1);
        }
    }

    public void send(Activity activity){
        SmsManager smsManager = SmsManager.getDefault();
        Intent msgSent = new Intent(Defines.ACTION_SEND_SMS);
        msgSent.putExtra(Defines.PASS_MESSAGE_FROM_CONVERSATION_TO_SEND, this);
        PendingIntent pendingMsgSent = PendingIntent.getBroadcast(activity, 0, msgSent, 0);
        smsManager.sendTextMessage(mPhone, null, mBody, pendingMsgSent, null);
        Utils.LOG(mBody);
    }

    @Override
    public String toString() {
        return "Message{" +
                "mId=" + mId +
                ", mPhone='" + mPhone + '\'' +
                ", mBody='" + mBody + '\'' +
                ", mDate='" + mDate + '\'' +
                ", mIsRead='" + mIsRead + '\'' +
                ", mIsReceive=" + mIsReceive +
                ", mSendStatus=" + mSendStatus +
                ", mIsSecurity=" + mIsSecurity +
                ", mIsLast=" + mIsLast +
                '}';
    }
}
