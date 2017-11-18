package com.hackathon.smessage.controllers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.hackathon.smessage.models.Contact;
import com.hackathon.smessage.models.Message;
import com.hackathon.smessage.sqlitehelper.SqliteHelper;
import com.hackathon.smessage.utils.TimeUtils;
import com.hackathon.smessage.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Random;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class MessageOpearation {

    private static MessageOpearation sInstance = null;

    private SqliteHelper mSqliteHelper;
    private SQLiteDatabase mSqLiteDatabase;

    private ArrayList<Message> mInboxMessage, mConversationMessage;
    private boolean mIsSecuritySave;

    private String[] MESSAGE_COLUMNS = {
            Message.FIELD_ID,
            Message.FIELD_PHONE,
            Message.FIELD_BODY,
            Message.FIELD_DATE,
            Message.FIELD_READ,
            Message.FIELD_RECEIVE,
            Message.FIELD_SEND_STATUS,
            Message.FIELD_SECURITY,
            Message.FIELD_LAST
    };

    public MessageOpearation(){
        mSqliteHelper = SqliteHelper.getInstance();
        mInboxMessage = new ArrayList<>();
        mConversationMessage = new ArrayList<>();
    }

    public static MessageOpearation getInstance(){
        if(sInstance == null){
            sInstance = new MessageOpearation();
        }
        return sInstance;
    }

    public ArrayList<Message> getInbox(){
        if(mInboxMessage.size() == 0){
            loadInbox(mIsSecuritySave);
        }
        return mInboxMessage;
    }

    public ArrayList<Message> getAllMessages(boolean isSecurity){
        ArrayList<Message> list = new ArrayList<>();
        mSqLiteDatabase = mSqliteHelper.getReadableDatabase();
        Cursor cursor = mSqLiteDatabase.query(SqliteHelper.TABLE_MESSAGE, MESSAGE_COLUMNS,
                Message.FIELD_SECURITY + " = " + (isSecurity ? 1 : 0), null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                Message message = parse(cursor);
                cursor.moveToNext();

                //decrypt message
                message.decrypt();
                list.add(message);
            }
            cursor.close();
        }

        mSqLiteDatabase.close();
        mSqliteHelper.close();

        return list;
    }

    public void loadInbox(boolean isSecurity){
        mIsSecuritySave = isSecurity;
        mInboxMessage.clear();

        mSqLiteDatabase = mSqliteHelper.getReadableDatabase();
        Cursor cursor = mSqLiteDatabase.query(SqliteHelper.TABLE_MESSAGE, MESSAGE_COLUMNS,
                Message.FIELD_SECURITY + " = " + (isSecurity ? 1 : 0) +
                        " and " + Message.FIELD_LAST + " = 1", null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                Message message = parse(cursor);
                //unread number
                int unreadNum = getUnreadNumber(message);
                message.setUnreadNumber(unreadNum);

                //contact
                Contact contact = ContactOpearation.getInstance().getContactWithPhoneNumber(message.getPhone());
                message.setContact(contact);
                cursor.moveToNext();

                //decrypt message
                message.decrypt();
                mInboxMessage.add(message);
            }
            cursor.close();
        }

        mSqLiteDatabase.close();
        mSqliteHelper.close();

        Collections.sort(mInboxMessage, new Comparator<Message>() {
            @Override
            public int compare(Message message, Message t1) {
                return Utils.compareTime(t1, message);
            }
        });
    }

    public int getUnreadNumber(boolean isSecurity){
        int unread = 0;
        mSqLiteDatabase = mSqliteHelper.getReadableDatabase();
        Cursor cursor = mSqLiteDatabase.query(SqliteHelper.TABLE_MESSAGE, MESSAGE_COLUMNS,
                Message.FIELD_RECEIVE + " = 1"
                        + " and " + Message.FIELD_READ + " = 0"
                        + " and " +  Message.FIELD_SECURITY + " = " + (isSecurity ? 1 : 0), null, null, null, null);
        if(cursor != null){
            unread = cursor.getCount();
        }
        mSqLiteDatabase.close();
        mSqliteHelper.close();
        return unread;
    }

    public Message getMessage(int id){
        Message message = null;
        mSqLiteDatabase = mSqliteHelper.getReadableDatabase();
        Cursor cursor = mSqLiteDatabase.query(SqliteHelper.TABLE_MESSAGE, MESSAGE_COLUMNS,
                Message.FIELD_ID + " = " + id, null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
            message = parse(cursor);
            cursor.close();
        }

        mSqLiteDatabase.close();
        mSqliteHelper.close();

        return message;
    }

    public ArrayList<Message> getConversation(Message message){
        mConversationMessage.clear();

        mSqLiteDatabase = mSqliteHelper.getReadableDatabase();
        Cursor cursor = mSqLiteDatabase.query(SqliteHelper.TABLE_MESSAGE, MESSAGE_COLUMNS,
                Message.FIELD_PHONE + " like '%" + message.getPhone() + "%'"
                        + " and " + Message.FIELD_SECURITY + " = " + (message.isSecurity() ? 1 : 0), null, null, null, null);
        if(cursor != null){
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                Message sms = parse(cursor);
                cursor.moveToNext();

                sms.decrypt();
                mConversationMessage.add(sms);
            }
            cursor.close();
        }
        mSqLiteDatabase.close();
        mSqliteHelper.close();

        //sort time top to down
        Collections.sort(mConversationMessage, new Comparator<Message>() {
            @Override
            public int compare(Message message, Message t1) {
                return Utils.compareTime(message, t1);
            }
        });
        return mConversationMessage;
    }
    /**
     * Add new message - is last message
     * update current last message is old message
     * @param message
     * @return id of new message
     */
    public int add(Message message){
        //find last message
        Message last = findLast(message.getPhone(), message.isSecurity());

        //update last message is not
        if(last != null){
            last.setIsLast(false);
            update(last);
        }

        //add
        mSqLiteDatabase = mSqliteHelper.getWritableDatabase();

        ContentValues contentValues = createValue(message);

        int id = (int)mSqLiteDatabase.insert(SqliteHelper.TABLE_MESSAGE, null, contentValues);

        if(mConversationMessage != null){
            message.setId(id);
            mConversationMessage.add(message);
        }

        mSqLiteDatabase.close();
        mSqliteHelper.close();

        return id;
    }

    /**
     * Update message
     * @param message
     */
    public void update(Message message){
        mSqLiteDatabase = mSqliteHelper.getWritableDatabase();

        ContentValues contentValues = createValue(message);

        mSqLiteDatabase.update(SqliteHelper.TABLE_MESSAGE, contentValues, Message.FIELD_ID + " = ?", new String[]{String.valueOf(message.getId())});

        //update list
        if(mConversationMessage != null){
            for(Message sms : mConversationMessage){
                if(sms.getId() == message.getId()){
                    sms.setSendStatus(message.getSendStatus());
                    sms.setDate(message.getDate());
                }
            }
        }

        mSqLiteDatabase.close();
        mSqliteHelper.close();
    }

    /**
     * Delete a mesasge
     * @param message
     */
    public void delete(Message message){
        mSqLiteDatabase = mSqliteHelper.getWritableDatabase();

        mSqLiteDatabase.delete(SqliteHelper.TABLE_MESSAGE, Message.FIELD_ID + " = ?", new String[]{String.valueOf(message.getId())});

        //update list
        if(mConversationMessage != null){
            mConversationMessage.remove(message);
        }

        mSqLiteDatabase.close();
        mSqliteHelper.close();
    }

    /**
     * Delete all messages of a conversation with phone number
     * @param message
     */
    public void deleteConversation(Message message){
        mSqLiteDatabase = mSqliteHelper.getWritableDatabase();

        mSqLiteDatabase.delete(SqliteHelper.TABLE_MESSAGE, Message.FIELD_PHONE + " like '%" + message.getPhone() + "%'", null);

        //update list
        if(mInboxMessage != null){
            mInboxMessage.remove(message);
        }

        mSqLiteDatabase.close();
        mSqliteHelper.close();
    }

    public Message search(int id){
        mSqLiteDatabase = mSqliteHelper.getReadableDatabase();
        Cursor cursor = mSqLiteDatabase.query(SqliteHelper.TABLE_MESSAGE, MESSAGE_COLUMNS,
                Message.FIELD_ID + " = " + id, null, null, null, null);

        Message message = null;
        if(cursor != null && cursor.moveToFirst()){
            message = parse(cursor);
            cursor.close();
        }
        mSqLiteDatabase.close();
        mSqliteHelper.close();

        return message;
    }

    /**
     * Find last message
     * @param phone is phone number of message
     * @param isSecurity: is or not security
     * @return
     */
    private Message findLast(String phone, boolean isSecurity){
        mSqLiteDatabase = mSqliteHelper.getReadableDatabase();
        Cursor cursor = mSqLiteDatabase.query(SqliteHelper.TABLE_MESSAGE, MESSAGE_COLUMNS,
                Message.FIELD_PHONE + " like '%" + phone + "%'"
                        + " and " + Message.FIELD_SECURITY + " = " + (isSecurity ? 1 : 0)
                        + " and " + Message.FIELD_LAST + " = 1", null, null, null, null);

        Message message = null;
        if(cursor != null && cursor.moveToFirst()){
            message = parse(cursor);
            cursor.close();
        }
        mSqLiteDatabase.close();
        mSqliteHelper.close();

        return message;
    }

    /**
     * Parse a curse to Message
     * @param cursor
     * @return a message
     */
    private Message parse(Cursor cursor){
        int id = cursor.getInt(cursor.getColumnIndex(Message.FIELD_ID));
        String phone = cursor.getString(cursor.getColumnIndex(Message.FIELD_PHONE));
        String body = cursor.getString(cursor.getColumnIndex(Message.FIELD_BODY));
        String date = cursor.getString(cursor.getColumnIndex(Message.FIELD_DATE));
        boolean isRead = cursor.getInt(cursor.getColumnIndex(Message.FIELD_READ)) == 1;
        boolean isReceive = cursor.getInt(cursor.getColumnIndex(Message.FIELD_RECEIVE)) == 1;
        int sendStatus = cursor.getInt(cursor.getColumnIndex(Message.FIELD_SEND_STATUS));
        boolean isSecurity = cursor.getInt(cursor.getColumnIndex(Message.FIELD_SECURITY)) == 1;
        boolean isLast = cursor.getInt(cursor.getColumnIndex(Message.FIELD_LAST)) == 1;

        Message message = new Message(id, phone, body, date, isRead, isReceive, sendStatus, isSecurity, isLast);
        return message;
    }

    /**
     * Create content value from message
     * @param message
     * @return a ContentValue
     */
    private ContentValues createValue(Message message){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Message.FIELD_PHONE, message.getPhone());
        contentValues.put(Message.FIELD_BODY, message.getBody());
        contentValues.put(Message.FIELD_DATE, message.getDate());
        contentValues.put(Message.FIELD_READ, message.isRead() ? 1 : 0);
        contentValues.put(Message.FIELD_RECEIVE, message.isReceive() ? 1 : 0);
        contentValues.put(Message.FIELD_SEND_STATUS, message.getSendStatus());
        contentValues.put(Message.FIELD_SECURITY, message.isSecurity() ? 1 : 0);
        contentValues.put(Message.FIELD_LAST, message.isLast() ? 1 : 0);

        return contentValues;
    }

    private int getUnreadNumber(Message message){
        int unread = 0;
        mSqLiteDatabase = mSqliteHelper.getReadableDatabase();
        Cursor cursor = mSqLiteDatabase.query(SqliteHelper.TABLE_MESSAGE, MESSAGE_COLUMNS,
                Message.FIELD_PHONE + " like '%" + message.getPhone() + "%'"
                        + " and " + Message.FIELD_RECEIVE + " = 1"
                        + " and " + Message.FIELD_READ + " = 0"
                        + " and " +  Message.FIELD_SECURITY + " = " + (message.isSecurity() ? 1 : 0), null, null, null, null);
        if(cursor != null){
            unread = cursor.getCount();
        }
        mSqLiteDatabase.close();
        mSqliteHelper.close();
        return unread;
    }

    public void fakeData(){
        int messageCount = 2;
        int conversation = 5;
        String message = "This is a message!";
        //normal message
        Utils.LOG("Fake data normal message");
        int phone = 987654320;
        for(int i = 0; i < messageCount; i++){
            String strPhone = "0" + (phone + i);
            for(int j = 0; j < conversation; j++) {
                Utils.LOG(i + " " + j);
                boolean isReceive = new Random().nextBoolean();
                boolean isRead = new Random().nextBoolean();
                int sendStatus = new Random().nextInt(3);

                if(!isReceive){
                    isRead = true;
                }
                else{
                    sendStatus = Message.SEND_TO_STATUS_SENT;
                }
                Message msg = new Message(strPhone, message + i + "-" + j, TimeUtils.getInstance().getTimeSystem(), isRead, isReceive, sendStatus, false);
                add(msg);
            }
        }

        Utils.LOG("Fake data security message");
        phone = 987654320;
        for(int i = 0; i < messageCount; i++) {
            String strPhone = "0" + (phone + i);
            for (int j = 0; j < conversation; j++) {
                Utils.LOG(i + " " + j);
                boolean isReceive = new Random().nextBoolean();
                boolean isRead = new Random().nextBoolean();
                int sendStatus = new Random().nextInt(3);
                if (!isReceive) {
                    isRead = true;
                } else {
                    sendStatus = Message.SEND_TO_STATUS_SENT;
                }
                Message msg = new Message(strPhone, message + i + "-" + j, TimeUtils.getInstance().getTimeSystem(), isRead, isReceive, sendStatus, true);
                msg.encrypt();
                add(msg);
            }
        }
    }
}