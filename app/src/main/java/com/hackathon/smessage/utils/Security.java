package com.hackathon.smessage.utils;

import android.os.Environment;

import com.hackathon.smessage.models.Message;

/**
 * Created by tai.nguyenduc on 11/18/2017.
 */

public class Security {
    private static int MATRIX_COLUMN 			= 10;
    private static int MATRIX_MIN_ROW 			= 7; //max sms for UCS2 = 7 x 10 = 70 chatacter
    private static char MATRIX_DEFAULT_VALUE	= ' '; //space

    //character will encrypt
    private static String CHARACTER            = " !\"#$%&')*+,-./0123456789:;<=>?@ABCDEFGHIJKLMNOPQRSTUVWXYZ[]^_`abcdefghijklmnopqrstuvwxyz{|}~";

    private static String VIGENERE_DEFAULT_KEY  = "xNYUWmL8mDzM9poJrL810IArfvgoehvB2f6ZSdyHAVNyLCSktgpJnELszUGKATGZC71ySY";

    private static Security sInstance = null;

    private Security(){

    }

    public static Security getInstance(){
        if(sInstance == null){
            sInstance = new Security();
        }
        return sInstance;
    }

    public native String encrypt(String message, String password);

    public native String decrypt(String message, String password);

    public boolean isSecurity(String message){
        String tmpDecrypt = decrypt(message, "");
        if(tmpDecrypt == null || tmpDecrypt.equals("")){
            return false;
        }
        return tmpDecrypt.charAt(0) == Message.SECURITY_CODE;
    }

    public static String getDownloadPath(String passs){
        return "getDownloadPath " +passs;
    }

    /** Encrypt with Vigenere
     * Ban ro: P
     * Ma hoa: C
     * Khoa: K
     * C = [P + C � 1] mod total
     */
    public static String vigenereEncrypt(String message, String password){
        //create key
        String key = VIGENERE_DEFAULT_KEY.charAt(0) + password + VIGENERE_DEFAULT_KEY.substring(1);
        while(key.length() < message.length()){
            key += key;
        }

        key = key.substring(0, message.length());

        String encyptMgs = "";
        for(int i = 0; i < message.length(); i++){
            char ch = message.charAt(i);
            if(CHARACTER.indexOf(ch) != -1){ //ch is character will encrypt
                int P = CHARACTER.indexOf(ch);
                int K = CHARACTER.indexOf(key.charAt(i));
                int C = (P + K - 1) % CHARACTER.length();
                encyptMgs += CHARACTER.charAt(C);
            }
            else{
                encyptMgs += ch;
            }
        }
        return encyptMgs;
    }

    /** Decrypt with Vigenere
     * Ban ro: P
     * Ma hoa: C
     * Khoa: K
     * P = C - K + 1
     * if P < 0 then P += length
     */
    public static String vigenereDecrypt(String message, String password){
        //Ban ro: P
        //Ma hoa: C
        //Khoa: K
        // P = C - K + 1

        //create key
        String key = VIGENERE_DEFAULT_KEY.charAt(0) + password + VIGENERE_DEFAULT_KEY.substring(1);
        while(key.length() < message.length()){
            key += key;
        }

        key = key.substring(0, message.length());

        String decryptMgs = "";
        for(int i = 0; i < message.length(); i++){
            char ch = message.charAt(i);
            if(CHARACTER.indexOf(ch) != -1){ //ch is character will encrypt
                int C = CHARACTER.indexOf(ch);
                int K = CHARACTER.indexOf(key.charAt(i));
                int P = C - K + 1;
                if (P < 0){
                    P += CHARACTER.length();
                }
                decryptMgs += CHARACTER.charAt(P);
            }
            else{
                decryptMgs += ch;
            }
        }
        return decryptMgs;
    }

    /**
     * put message into a matrix row x 10 (min row is 7 == UCS message)
     * order
     * --------------->
     * --------------->
     * end out put
     * |
     * |
     * |
     */
    public static String matrixEncrypt(String message){
        //make standard String
        message = message.trim();

        int col = MATRIX_COLUMN;
        int row = MATRIX_MIN_ROW;
        if(message.length() > row * col){
            row = message.length() / MATRIX_MIN_ROW;
            if(message.length() % MATRIX_MIN_ROW != 0){
                row++;
            }
        }

        //init each element is 10 (enter)
        char [][]matrix = new char[row][col];

        //push each character into matrix if character is not enough push 10 (enter)
        int index = 0;
        for(int i = 0; i < row; i++){
            for(int j = 0; j < col; j++){
                if(index < message.length()){
                    matrix[i][j] = message.charAt(index);
                    index++;
                }
                else{
                    matrix[i][j] = MATRIX_DEFAULT_VALUE;
                }
            }
        }

        String newMessage = "";
        for(int j = 0; j < col; j++){
            for(int i = 0; i < row; i++){
                newMessage += matrix[i][j];
            }
        }
        return newMessage;
    }

    /**
     * put message into a matrix row x 10 (min row is 7 == UCS message)
     * order
     * |
     * |
     * |
     * end out put
     * --------------->
     * --------------->
     */
    public static String matrixDecrypt(String message){
        int col = MATRIX_COLUMN;
        int row = message.length()/MATRIX_COLUMN;

        char [][]matrix = new char[row][col];

        //push each character into matrix if character is not enough push 10 (enter)
        int index = 0;
        for(int j = 0; j < col; j++){
            for(int i = 0; i < row; i++){
                if(index < message.length()) {
                    matrix[i][j] = message.charAt(index);
                    index++;
                }
                else{
                    matrix[i][j] = MATRIX_DEFAULT_VALUE;
                }
            }
        }

        //output and MATRIX_DEFAULT_VALUE
        String newString = "";
        for(int i = 0; i < row; i++) {
            for (int j = 0; j < col; j++) {
                newString += matrix[i][j];
            }
        }

        //remove MATRIX_DEFAULT_VALUE (space)
        newString = newString.trim();
        return newString;
    }
}
