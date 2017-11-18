package com.hackathon.smessage.sqlitehelper;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.hackathon.smessage.models.Blocked;
import com.hackathon.smessage.models.Message;
import com.hackathon.smessage.utils.Utils;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class SqliteHelper extends SQLiteOpenHelper{

    private static final String DATABASE_NAME = "awdrgyjilplijygrdwa";
    private static final int DATABASE_VERSION = 2;

    public static final String TABLE_MESSAGE= "zsxdcfvgbhnjmklijyhtfesq";
    public static final String TABLE_BLOCKED= "poiuytrewq";

    private static Context sContext;
    private static SqliteHelper sInstance = null;

    public static void init(Context context){
        sContext = context;
    }

    public static boolean isInitialized(){
        return (sContext != null);
    }

    public static SqliteHelper getInstance(){
        if(sInstance == null){
            sInstance = new SqliteHelper();
        }
        return sInstance;
    }

    public Context getContext(){
        return sContext;
    }

    private SqliteHelper() {
        super(sContext, DATABASE_NAME, null, DATABASE_VERSION);
        if(sContext == null){
            throw new RuntimeException("Please call init method first. Should call at Main Activity");
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTables(sqLiteDatabase);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        deleteTables(sqLiteDatabase);
        createTables(sqLiteDatabase);
    }

    //------------------------
    private void createTables(SQLiteDatabase sqLiteDatabase){
        String sqlTableMessage = "CREATE TABLE " + TABLE_MESSAGE
                + " ( "
                + Message.FIELD_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Message.FIELD_PHONE       + " TEXT, "
                + Message.FIELD_BODY        + " TEXT, "
                + Message.FIELD_DATE        + " TEXT, "
                + Message.FIELD_READ        + " INTEGER, "
                + Message.FIELD_RECEIVE     + " INTEGER, "
                + Message.FIELD_SEND_STATUS + " INTEGER, "
                + Message.FIELD_SECURITY    + " INTEGER, "
                + Message.FIELD_LAST        + " INTEGER "
                + " )";

        Utils.LOG(sqlTableMessage);
        sqLiteDatabase.execSQL(sqlTableMessage);

        String sqlTableBlockedSMS = "CREATE TABLE " + TABLE_BLOCKED
                + " ( "
                + Blocked.FIELD_ID          + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + Blocked.FIELD_CONTENT     + " TEXT, "
                + Blocked.FIELD_IS_SMS      + " INTEGER, "
                + Blocked.FIELD_IS_CONTACT  + " INTEGER "
                + " )";

        Utils.LOG(sqlTableBlockedSMS);
        sqLiteDatabase.execSQL(sqlTableBlockedSMS);
    }

    private void deleteTables(SQLiteDatabase sqLiteDatabase){
        Utils.LOG("Delete table");
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " + TABLE_MESSAGE);
    }
}
