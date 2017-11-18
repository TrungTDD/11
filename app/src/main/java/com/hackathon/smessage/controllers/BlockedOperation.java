package com.hackathon.smessage.controllers;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import com.hackathon.smessage.models.Blocked;
import com.hackathon.smessage.models.Message;
import com.hackathon.smessage.sqlitehelper.SqliteHelper;

import java.util.ArrayList;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class BlockedOperation {

    private static BlockedOperation sInstance = null;

    private SqliteHelper mSqliteHelper;
    private SQLiteDatabase mSqLiteDatabase;

    private ArrayList<Blocked> mBlockedCallList, mBlockedSMSList;

    private String[] BLOCKED_COLUMNS = {
            Blocked.FIELD_ID,
            Blocked.FIELD_CONTENT,
            Blocked.FIELD_IS_SMS,
            Blocked.FIELD_IS_CONTACT,
    };

    public BlockedOperation() {
        mSqliteHelper = SqliteHelper.getInstance();

        mBlockedCallList = new ArrayList<>();
        mBlockedSMSList = new ArrayList<>();
    }

    public static BlockedOperation getInstance(){
        if(sInstance == null){
            sInstance = new BlockedOperation();
        }
        return sInstance;
    }

    public void loadBlocked(){
        mSqLiteDatabase = mSqliteHelper.getReadableDatabase();
        Cursor cursor = mSqLiteDatabase.query(SqliteHelper.TABLE_BLOCKED, BLOCKED_COLUMNS,
                null, null, null, null, null);

        mBlockedCallList.clear();
        mBlockedSMSList.clear();
        if(cursor != null){
            cursor.moveToFirst();
            while(!cursor.isAfterLast()){
                Blocked blocked = parse(cursor);
                if(blocked.isMessage()){
                    mBlockedSMSList.add(blocked);
                }
                else {
                    mBlockedCallList.add(blocked);
                }
                cursor.moveToNext();
            }
            cursor.close();
        }
        mSqLiteDatabase.close();
        mSqliteHelper.close();
    }

    public boolean isBlockedMessage(Message message, boolean isBlockedPhone){

        String filter = Blocked.FIELD_IS_SMS + " = 1 and "
                + Blocked.FIELD_IS_CONTACT + " = " + (isBlockedPhone ? 1 : 0) + " and " + Blocked.FIELD_CONTENT + " like '%" + (isBlockedPhone ? message.getPhone() : message.getBody()) + "%'";

        mSqLiteDatabase = mSqliteHelper.getReadableDatabase();
        Cursor cursor = mSqLiteDatabase.query(SqliteHelper.TABLE_BLOCKED, BLOCKED_COLUMNS,
                filter, null, null, null, null);

        if(cursor != null && cursor.moveToNext()){
            return true;
        }
        return false;
    }

    public boolean isBlockedCall(String phoneNumber){

        String filter = Blocked.FIELD_IS_SMS + " = 0 and " + Blocked.FIELD_CONTENT + " like '%" + phoneNumber + "%'";

        mSqLiteDatabase = mSqliteHelper.getReadableDatabase();
        Cursor cursor = mSqLiteDatabase.query(SqliteHelper.TABLE_BLOCKED, BLOCKED_COLUMNS,
                filter, null, null, null, null);

        if(cursor != null && cursor.moveToNext()){
            return true;
        }
        return false;
    }

    public ArrayList<Blocked> getBlockedCall(){
        if(mBlockedCallList.size() == 0){
            loadBlocked();
        }
        return mBlockedCallList;
    }

    public ArrayList<Blocked> getBlockedSMS(){
        if(mBlockedSMSList.size() == 0){
            loadBlocked();
        }
        return mBlockedSMSList;
    }

    public int add(Blocked blocked){
        //add
        mSqLiteDatabase = mSqliteHelper.getWritableDatabase();

        ContentValues contentValues = createValue(blocked);

        int id = (int)mSqLiteDatabase.insert(SqliteHelper.TABLE_BLOCKED, null, contentValues);

        blocked.setId(id);
        if(blocked.isMessage()){
            mBlockedSMSList.add(blocked);
        }
        else {
            mBlockedCallList.add(blocked);
        }

        mSqLiteDatabase.close();
        mSqliteHelper.close();

        return id;
    }

    public void delete(Blocked blocked){
        mSqLiteDatabase = mSqliteHelper.getWritableDatabase();

        mSqLiteDatabase.delete(SqliteHelper.TABLE_BLOCKED, Blocked.FIELD_ID + " = ?", new String[]{String.valueOf(blocked.getId())});

        //update list
        if(blocked.isMessage()){
            mBlockedSMSList.remove(blocked);
        }
        else {
            mBlockedCallList.remove(blocked);
        }

        mSqLiteDatabase.close();
        mSqliteHelper.close();
    }

    private Blocked parse(Cursor cursor){
        int id = cursor.getInt(cursor.getColumnIndex(Blocked.FIELD_ID));
        String content = cursor.getString(cursor.getColumnIndex(Blocked.FIELD_CONTENT));
        boolean isSMS = cursor.getInt(cursor.getColumnIndex(Blocked.FIELD_IS_SMS)) == 1;
        boolean isContact = cursor.getInt(cursor.getColumnIndex(Blocked.FIELD_IS_CONTACT)) == 1;

        Blocked blocked = new Blocked(id, content, isSMS, isContact);
        return blocked;
    }

    private ContentValues createValue(Blocked blocked){
        ContentValues contentValues = new ContentValues();
        contentValues.put(Blocked.FIELD_CONTENT, blocked.getContent());
        contentValues.put(Blocked.FIELD_IS_SMS, blocked.isMessage() ? 1 : 0);
        contentValues.put(Blocked.FIELD_IS_CONTACT, blocked.isContact() ? 1 : 0);
        return contentValues;
    }

    public void fakeData(){
        add(new Blocked("0987654321", false, true));
        add(new Blocked("0987654322", true, true));
        add(new Blocked("abcd", true, false));
        add(new Blocked("QA", true, false));
    }
}
