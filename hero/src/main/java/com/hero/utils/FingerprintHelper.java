package com.hero.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Build;
import android.os.CancellationSignal;
import android.security.keystore.KeyProperties;
import android.text.TextUtils;
import android.util.Base64;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;

/**
 * Created by Aron on 2018/10/9.
 */
@TargetApi(23)
public class FingerprintHelper extends FingerprintManager.AuthenticationCallback {

    public static final int FINGERPRINT_STATE_SDK_LOW = -2;
    public static final int FINGERPRINT_STATE_NOT_SUPPORT = -1;
    public static final int FINGERPRINT_STATE_NO_FINGER = 0;
    public static final int FINGERPRINT_STATE_AVAILABLE = 1;

    private FingerprintManager manager;
    private CancellationSignal mCancellationSignal;
    private SimpleAuthenticationCallback callback;
    private FingerSharedPreference mFingerSharedPreference;
    private FingerAndroidKeyStore mFingerAndroidKeyStore;
    //PURPOSE_ENCRYPT,则表示生成token，否则为取出token
    private int purpose = KeyProperties.PURPOSE_ENCRYPT;

    // 明文密码
    private String data = null;

    public FingerprintHelper(Context context) {
        manager = (FingerprintManager) context.getSystemService(Context.FINGERPRINT_SERVICE);
        mFingerSharedPreference = new FingerSharedPreference(context);
        mFingerAndroidKeyStore = new FingerAndroidKeyStore();
    }

    public void generateKey() {
        //在keystore中生成加密密钥
        mFingerAndroidKeyStore.generateKey(FingerAndroidKeyStore.keyName);
        setPurpose(KeyProperties.PURPOSE_ENCRYPT);
    }

    public boolean isKeyProtectedEnforcedBySecureHardware() {
        return mFingerAndroidKeyStore.isKeyProtectedEnforcedBySecureHardware();
    }

    /**
     *
     * @return 0 支持指纹但是没有录入指纹； 1：有可用指纹； -1：手机不支持指纹 -2：sdk版本过低不支持
     */
    public int checkFingerprintAvailable() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return FINGERPRINT_STATE_SDK_LOW;
        } else if (!isKeyProtectedEnforcedBySecureHardware()) {
            return FINGERPRINT_STATE_NOT_SUPPORT;
        } else if (!manager.isHardwareDetected()) {
            return FINGERPRINT_STATE_NOT_SUPPORT;
        } else if (!manager.hasEnrolledFingerprints()) {
            return FINGERPRINT_STATE_NO_FINGER;
        }
        return FINGERPRINT_STATE_AVAILABLE;
    }

    public boolean containsToken() {
        return mFingerSharedPreference.containsKey(mFingerSharedPreference.dataKeyName);
    }

    public void setCallback(SimpleAuthenticationCallback callback) {
        this.callback = callback;
    }

    public void setPurpose(int purpose) {
        this.purpose = purpose;
    }

    public boolean authenticate() {
        try {
            FingerprintManager.CryptoObject object;
            if (purpose == KeyProperties.PURPOSE_DECRYPT) {
                String IV = mFingerSharedPreference.getData(mFingerSharedPreference.IVKeyName);
                object = mFingerAndroidKeyStore.getCryptoObject(Cipher.DECRYPT_MODE, Base64.decode(IV, Base64.NO_PADDING));
                if (object == null) {
                    return false;
                }
            } else {
                object = mFingerAndroidKeyStore.getCryptoObject(Cipher.ENCRYPT_MODE, null);
            }
            mCancellationSignal = new CancellationSignal();
            manager.authenticate(object, mCancellationSignal, 0, this, null);
            return true;
        } catch (SecurityException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void stopAuthenticate() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
        }
        callback = null;
    }

    @Override
    public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
        if (callback == null) {
            return;
        }
        if (result.getCryptoObject() == null) {
            callback.onAuthenticationFailed();
            return;
        }
        final Cipher cipher = result.getCryptoObject().getCipher();
        if (purpose == KeyProperties.PURPOSE_DECRYPT) {
            //取出secret key并返回
            String data = mFingerSharedPreference.getData(mFingerSharedPreference.dataKeyName);
            if (TextUtils.isEmpty(data)) {
                callback.onAuthenticationFailed();
                return;
            }
            try {
                byte[] decrypted = cipher.doFinal(Base64.decode(data, Base64.NO_PADDING));
                callback.onAuthenticationSucceeded(new String(decrypted));
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
                callback.onAuthenticationFailed();
            }
        } else {
            try {
                byte[] encrypted = cipher.doFinal(data.getBytes());
                byte[] IV = cipher.getIV();
                String se = Base64.encodeToString(encrypted, Base64.NO_PADDING);
                String siv = Base64.encodeToString(IV, Base64.NO_PADDING);
                mFingerSharedPreference.storeData(mFingerSharedPreference.dataKeyName, se);
                if (mFingerSharedPreference.storeData(mFingerSharedPreference.IVKeyName, siv)) {
                    callback.onAuthenticationSucceeded(se);
                } else {
                    callback.onAuthenticationFailed();
                }
            } catch (BadPaddingException | IllegalBlockSizeException e) {
                e.printStackTrace();
                callback.onAuthenticationFailed();
            }
        }
    }

    @Override
    public void onAuthenticationError(int errorCode, CharSequence errString) {
        if (callback != null) {
            callback.onAuthenticationError(errString.toString());
        }
    }

    @Override
    public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
    }

    @Override
    public void onAuthenticationFailed() {
        if (callback != null) {
            callback.onAuthenticationFailed();
        }
    }

    public interface SimpleAuthenticationCallback {

        void onAuthenticationSucceeded(String value);

        void onAuthenticationFailed();

        void onAuthenticationError(String errString);
    }

    public void setData(String data) {
        this.data = data;
    }
}
