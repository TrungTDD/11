package com.hackathon.smessage.models;

/**
 * Created by segaw on 11/18/2017.
 */

public class Contact {
    private String mId;
    private String mName;
    private String mPhoneNumber;
    private String mPhotoUri;

    public Contact(){
        mId = "";
    }

    public Contact(String name, String phone, String photoUri){
        mId = "";
        mName = name;
        mPhoneNumber = phone;
        mPhotoUri = photoUri;
    }

    public Contact(String id, String name, String phone, String photoUri){
        mId = id;
        mName = name;
        mPhoneNumber = phone;
        mPhotoUri = photoUri;
    }

    public void setId(String id){
        mId = id;
    }

    public String getId(){
        return mId;
    }

    public void setName(String name){
        mName = name;
    }

    public String getName(){
        return mName;
    }

    public void setPhoneNumber(String phone){
        mPhoneNumber = phone;
    }

    public String getPhoneNumber(){
        return mPhoneNumber;
    }

    public void setPhotoUri(String photo){
        mPhotoUri = photo;
    }

    public String getPhotoUri(){
        return mPhotoUri;
    }

    public String format(){
        return mName + " - " + mPhoneNumber;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "mName='" + mName + '\'' +
                ", mPhoneNumber='" + mPhoneNumber + '\'' +
                ", mPhotoUri='" + mPhotoUri + '\'' +
                '}';
    }

}
