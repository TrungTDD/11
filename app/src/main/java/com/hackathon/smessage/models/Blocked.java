package com.hackathon.smessage.models;

/**
 * Created by ADMIN on 18/11/2017.
 */

public class Blocked {
    public static String FIELD_ID = "id";
    public static String FIELD_CONTENT = "content";
    public static String FIELD_IS_SMS = "is_sms";
    public static String FIELD_IS_CONTACT = "iscontact";

    private int mId;
    private String mContent; //content: contact or message
    private boolean mIsMessage; //true : blocked sms
    private boolean mIsContact; //true: block sms with phone number

    public Blocked() {
    }

    public Blocked(int mId, String mContent, boolean mIsMessage, boolean mIsContact) {
        this.mId = mId;
        this.mContent = mContent;
        this.mIsMessage = mIsMessage;
        this.mIsContact = mIsContact;
    }

    public Blocked(String mContent, boolean mIsMessage, boolean mIsContact) {
        this.mContent = mContent;
        this.mIsMessage = mIsMessage;
        this.mIsContact = mIsContact;
    }

    public int getId() {
        return mId;
    }

    public void setId(int mId) {
        this.mId = mId;
    }

    public String getContent() {
        return mContent;
    }

    public void setContent(String mContent) {
        this.mContent = mContent;
    }

    public boolean isMessage() {
        return mIsMessage;
    }

    public void setIsMessage(boolean mIsMessage) {
        this.mIsMessage = mIsMessage;
    }

    public boolean isContact() {
        return mIsContact;
    }

    public void setIsContact(boolean mIsContact) {
        this.mIsContact = mIsContact;
    }

    @Override
    public String toString() {
        return "Blocked{" +
                "mId=" + mId +
                ", mContent='" + mContent + '\'' +
                ", mIsMessage=" + mIsMessage +
                ", mIsContact=" + mIsContact +
                '}';
    }
}
