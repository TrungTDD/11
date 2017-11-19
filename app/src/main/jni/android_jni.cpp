#include <jni.h>
#include <cstdio>

JavaVM* javaVM;

//need  to init jni
int JNI_OnLoad(JavaVM* vm, void* reserved) {
    JNIEnv* env;
    javaVM = vm;
    if ((vm)->GetEnv((void**)&env, JNI_VERSION_1_6) != JNI_OK) {
        return  -1;
    }
    return JNI_VERSION_1_6;
}

const char* GetDownloadPath(const char* pass)
{
    JNIEnv* env;
    (javaVM)->GetEnv((void**)&env, JNI_VERSION_1_6);
    //get lớp MainActivity
    jclass classMainActivity = (jclass)env->FindClass("com/hackathon/smessage/utils/Security");

    //get method id - get id hàm bên java (Lớp, tên hàm, kiểu dữ liệu)
    jmethodID  mID = env->GetStaticMethodID(classMainActivity, "getDownloadPath", "(Ljava/lang/String;)Ljava/lang/String;");

    if (mID == NULL) {
        return ""; /* exception thrown */
    }
    jstring jStringParam = env->NewStringUTF( "DMM" );
    if( !jStringParam )
    {
        return "";
    };

    //call updateTextView method static from java
    jstring rv = (jstring)env->CallStaticObjectMethod(classMainActivity, mID, jStringParam);
    const char *strReturn = (env)->GetStringUTFChars( rv, 0);

    env->ReleaseStringUTFChars(rv, strReturn);
    return strReturn;
}

//--------------CALL JAVA---------------------
const char* vigenereEncrypt(const char* message, const char* password)
{
    JNIEnv* env;
    (javaVM)->GetEnv((void**)&env, JNI_VERSION_1_6);
    //get lớp MainActivity
    jclass javaClass = (jclass)env->FindClass("com/hackathon/smessage/utils/Security");

    //get method id - get id hàm bên java (Lớp, tên hàm, kiểu dữ liệu)
    jmethodID  mID = env->GetStaticMethodID(javaClass, "vigenereEncrypt", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");

    if (mID == NULL) {
        return ""; /* exception thrown */
    }
    jstring jStringMessage = env->NewStringUTF(message);
    jstring jStringPassword = env->NewStringUTF(password);
    if( !jStringMessage || !jStringPassword )
    {
        return "";
    };
    //call updateTextView method static from java
    jstring rv = (jstring)env->CallStaticObjectMethod(javaClass, mID, jStringMessage, jStringPassword);
    const char *strReturn = (env)->GetStringUTFChars( rv, 0);

    env->ReleaseStringUTFChars(rv, strReturn);
    return strReturn;
}

const char* vigenereDecrypt(const char* message, const char* password)
{
    JNIEnv* env;
    (javaVM)->GetEnv((void**)&env, JNI_VERSION_1_6);
    //get lớp MainActivity
    jclass javaClass = (jclass)env->FindClass("com/hackathon/smessage/utils/Security");

    //get method id - get id hàm bên java (Lớp, tên hàm, kiểu dữ liệu)
    jmethodID  mID = env->GetStaticMethodID(javaClass, "vigenereDecrypt", "(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;");

    if (mID == NULL) {
        return ""; /* exception thrown */
    }
    jstring jStringMessage = env->NewStringUTF(message);
    jstring jStringPassword = env->NewStringUTF(password);
    if( !jStringMessage || !jStringPassword )
    {
        return "";
    };
    //call updateTextView method static from java
    jstring rv = (jstring)env->CallStaticObjectMethod(javaClass, mID, jStringMessage, jStringPassword);
    const char *strReturn = (env)->GetStringUTFChars( rv, 0);

    env->ReleaseStringUTFChars(rv, strReturn);
    return strReturn;
}

const char* matrixEncrypt(const char* message)
{
    JNIEnv* env;
    (javaVM)->GetEnv((void**)&env, JNI_VERSION_1_6);
    //get lớp MainActivity
    jclass javaClass = (jclass)env->FindClass("com/hackathon/smessage/utils/Security");

    //get method id - get id hàm bên java (Lớp, tên hàm, kiểu dữ liệu)
    jmethodID  mID = env->GetStaticMethodID(javaClass, "matrixEncrypt", "(Ljava/lang/String;)Ljava/lang/String;");

    if (mID == NULL) {
        return ""; /* exception thrown */
    }
    jstring jStringMessage = env->NewStringUTF(message);
    if( !jStringMessage )
    {
        return "";
    };

    //call updateTextView method static from java
    jstring rv = (jstring)env->CallStaticObjectMethod(javaClass, mID, jStringMessage);
    const char *strReturn = (env)->GetStringUTFChars( rv, 0);

    env->ReleaseStringUTFChars(rv, strReturn);
    return strReturn;
}

const char* matrixDecrypt(const char* message)
{
    JNIEnv* env;
    (javaVM)->GetEnv((void**)&env, JNI_VERSION_1_6);
    //get lớp MainActivity
    jclass javaClass = (jclass)env->FindClass("com/hackathon/smessage/utils/Security");

    //get method id - get id hàm bên java (Lớp, tên hàm, kiểu dữ liệu)
    jmethodID  mID = env->GetStaticMethodID(javaClass, "matrixDecrypt", "(Ljava/lang/String;)Ljava/lang/String;");

    if (mID == NULL) {
        return ""; /* exception thrown */
    }
    jstring jStringMessage = env->NewStringUTF(message);
    if( !jStringMessage )
    {
        return "";
    };
    //call updateTextView method static from java
    jstring rv = (jstring)env->CallStaticObjectMethod(javaClass, mID, jStringMessage);
    const char *strReturn = (env)->GetStringUTFChars( rv, 0);

    env->ReleaseStringUTFChars(rv, strReturn);
    return strReturn;
}

extern "C" {
JNIEXPORT jstring JNICALL
Java_com_hackathon_smessage_utils_Security_encrypt(JNIEnv *env, jobject instance, jstring message_, jstring password_);

JNIEXPORT jstring JNICALL
Java_com_hackathon_smessage_utils_Security_decrypt(JNIEnv *env, jobject instance, jstring message_, jstring password_);

};


JNIEXPORT jstring JNICALL
Java_com_hackathon_smessage_utils_Security_encrypt(JNIEnv *env, jobject instance, jstring message_,
                                                   jstring password_) {
    const char *message = env->GetStringUTFChars(message_, 0);
    const char *password = env->GetStringUTFChars(password_, 0);

    const char* encryptMatrix = matrixEncrypt(message);

    env->ReleaseStringUTFChars(message_, message);
    env->ReleaseStringUTFChars(password_, password);

    return env->NewStringUTF(vigenereEncrypt(encryptMatrix, password));
}

JNIEXPORT jstring JNICALL
Java_com_hackathon_smessage_utils_Security_decrypt(JNIEnv *env, jobject instance, jstring message_,
                                                   jstring password_) {
    const char *message = env->GetStringUTFChars(message_, 0);
    const char *password = env->GetStringUTFChars(password_, 0);

    const char* decryptVigenere = vigenereDecrypt(message, password);

    env->ReleaseStringUTFChars(message_, message);
    env->ReleaseStringUTFChars(password_, password);

    return env->NewStringUTF(matrixDecrypt(decryptVigenere));
}