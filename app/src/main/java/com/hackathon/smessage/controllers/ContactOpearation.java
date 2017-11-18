package com.hackathon.smessage.controllers;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;

import com.hackathon.smessage.models.Contact;
import com.hackathon.smessage.utils.PhoneNumberUtils;

import java.util.ArrayList;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class ContactOpearation {

    private static ContactOpearation sInstance = null;
    private static Context sContext;

    private ArrayList<Contact> mList;

    public ContactOpearation(){
        mList = new ArrayList<>();
    }

    public static void init(Context context){
        sContext = context;
    }

    public static boolean isInitialized(){
        return (sContext != null);
    }

    public static ContactOpearation getInstance(){
        if(sInstance == null){
            sInstance = new ContactOpearation();
        }
        return sInstance;
    }

    public Contact getContactWithName(String name){
        Contact contact = null;
        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = sContext.getContentResolver().query(uri, null,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + "='" + name + "'", null, null);

        if (cursor != null){
            if(cursor.moveToFirst()) {
                contact = parse(cursor);
            }
        }
        return contact;
    }

    public Contact getContactWithPhoneNumber(String phoneNumber){
        Contact contact = null;

        Uri uri=Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));

        String[] COLUMNS = new String[]{
                ContactsContract.PhoneLookup.CONTACT_ID,
                ContactsContract.PhoneLookup.DISPLAY_NAME,
                ContactsContract.PhoneLookup.HAS_PHONE_NUMBER,
                ContactsContract.PhoneLookup.PHOTO_URI,
        };
        Cursor cursor = sContext.getContentResolver().query(uri, COLUMNS, null, null, null);

        if (cursor != null) {
            if(cursor.moveToFirst()) {
                contact = new Contact();
                contact.setId(cursor.getString(0));
                contact.setName(cursor.getString(1));
                contact.setPhoneNumber(cursor.getString(2));
                contact.setPhotoUri(cursor.getString(3));
            }
            cursor.close();
        }
        return contact;
    }

    public ArrayList<Contact> getContacts(){
        if(mList.size() == 0){
            loadContacts();
        }
        return mList;
    }

    public void loadContacts(){
        mList.clear();

        Uri uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI;
        Cursor cursor = sContext.getContentResolver().query(uri, null, null, null, null);
        cursor.moveToFirst();

        while(!cursor.isAfterLast()){
            Contact contact = parse(cursor);
            cursor.moveToNext();
            mList.add(contact);
        }
    }

    public Contact getContactFromSystem(Context context, Intent data){
        Contact contact = null;

        Uri contactData = data.getData();
        Cursor cursor = context.getContentResolver().query(contactData, null, null, null, null);

        if(cursor.moveToFirst()){
            contact = parse(cursor);
        }
        return contact;
    }

    private Contact parse(Cursor cursor){
        Contact contact = new Contact();
        contact.setId(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)));
        contact.setName(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)));
        contact.setPhoneNumber(PhoneNumberUtils.format(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))));
        contact.setPhotoUri(cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)));
        return contact;
    }
}