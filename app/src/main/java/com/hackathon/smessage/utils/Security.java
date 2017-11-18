package com.hackathon.smessage.utils;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class Security {

    private static Security sInstance = null;

    private Security(){

    }

    public static Security getInstance(){
        if(sInstance == null){
            sInstance = new Security();
        }
        return sInstance;
    }

    public String encrypt(String message){
        return new StringBuffer(message).reverse().toString();
    }

    public String decrypt(String message){
        return new StringBuffer(message).reverse().toString();
    }
}
