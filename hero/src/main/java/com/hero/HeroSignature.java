package com.hero;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.fingerprint.FingerprintManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.security.keystore.KeyProperties;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.hero.HeroView;
import com.hero.IHero;
import com.hero.IHeroContext;
import com.hero.R;
import com.hero.depandency.StringUtil;
import com.hero.signature.HeroSignatureActivity;
import com.hero.utils.FileUtils;
import com.hero.utils.FingerprintHelper;
import com.hero.utils.ShareUtils;

import org.apache.http.util.EncodingUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.utils.Numeric;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;

import static com.hero.signature.Constants.KEYSTORE_FILE_PATH;

/**
 * Created by Aron on 2018/7/9.
 */
public class HeroSignature extends View implements IHero, FingerprintHelper.SimpleAuthenticationCallback {

    private static JSONObject jsonObject;

    private FingerprintHelper fingerprintHelper;

    private TextView fingerprint_tv;

    private AlertDialog fingerprint_alertDialog;

    private Context context;

    private PopupWindow popupWindow;

    private boolean isCancel = false;

    public HeroSignature(Context c) {
        super(c);
        this.context = c;
        this.fingerprintHelper = new FingerprintHelper(context);
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        View contentView = LayoutInflater.from(getContext()).inflate(R.layout.hero_sign_popwindow, null, false);
        if (jsonObject.has("accounts")){
            final JSONArray jsonArray = new JSONArray();

            ArrayList<File> fileArrayList = FileUtils.getKeystroeFilesWithoutDefault();
            for (int i = 0; i <fileArrayList.size(); i++) {
                try {
                    String walletString = FileUtils.getKeystoreFilecontent(fileArrayList.get(i).getName());
                    ObjectMapper mapper = new ObjectMapper();
                    WalletFile walletFile = mapper.readValue(walletString, WalletFile.class);
                    jsonArray.put("0x"+walletFile.getAddress());

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            if (jsonObject.has("isNpc")) {
                ((HeroFragmentActivity)context).getCurrentFragment().mWebview.evaluateJavascript("window['HeroSignature"+"callback']("+ jsonArray.toString() +")",null);
            } else {
                ((HeroFragmentActivity)context).on(jsonArray);
            }
        }

        if (jsonObject.has("wallet")){
            Intent intent = new Intent(context, HeroSignatureActivity.class);
            intent.putExtra("jumpType", 1);
            context.startActivity(intent);
            return;
        }
        if (jsonObject.has("transaction")){
            contentView.findViewById(R.id.sign_content_transfer_ll).setVisibility(View.VISIBLE);
            contentView.findViewById(R.id.sign_content_message_ll).setVisibility(View.INVISIBLE);
            this.jsonObject = jsonObject;
            initSignView(contentView, jsonObject);
        }
        if (jsonObject.has("message")) {
            contentView.findViewById(R.id.sign_content_transfer_ll).setVisibility(View.INVISIBLE);
            contentView.findViewById(R.id.sign_content_message_ll).setVisibility(View.VISIBLE);
            System.out.println(jsonObject.toString());
            this.jsonObject = jsonObject;
            initSignView(contentView, jsonObject);
        }
    }

    private void initSignView(View view, JSONObject object) throws JSONException {
        try {
            if (!FileUtils.getKeystoreFile("default").exists()) {
                if (FileUtils.getNumbersOfKeystore() == 0) {
                    view.findViewById(R.id.sign_no_keystore_ll).setVisibility(View.VISIBLE);
                    view.findViewById(R.id.sign_password_ll).setVisibility(View.INVISIBLE);
                } else {
                    view.findViewById(R.id.sign_no_keystore_ll).setVisibility(View.INVISIBLE);
                    view.findViewById(R.id.sign_password_ll).setVisibility(View.VISIBLE);
                }
            } else {
                view.findViewById(R.id.sign_no_keystore_ll).setVisibility(View.INVISIBLE);
                view.findViewById(R.id.sign_password_ll).setVisibility(View.VISIBLE);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        boolean defaultHasFingerprint = false;
        ShareUtils shareUtils = ShareUtils.getInstance(context);
        if (shareUtils.contains("default")) {
            String fileName = shareUtils.getString("default","");
            String fingerprint = shareUtils.getString("fingerprint","");
            if (fingerprint.contains(fileName)){
                defaultHasFingerprint = true;
            }
        }

        if (defaultHasFingerprint && (fingerprintHelper.checkFingerprintAvailable() == FingerprintHelper.FINGERPRINT_STATE_AVAILABLE)) {
            view.findViewById(R.id.sign_fingerprint_ll).setVisibility(View.VISIBLE);
            view.findViewById(R.id.sign_fingerprint_line).setVisibility(View.VISIBLE);
            view.findViewById(R.id.sign_fingerprint_ll).setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    fingerprintHelper.setPurpose(KeyProperties.PURPOSE_DECRYPT);
                    fingerprintHelper.setCallback(HeroSignature.this);
                    fingerprintHelper.authenticate();

                    if (fingerprint_alertDialog == null) {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        View view = View.inflate(context, R.layout.fingerprint_dialog, null);
                        fingerprint_tv = (TextView) view.findViewById(R.id.fingerprint_hint);
                        builder.setView(view);
                        // 创建对话框
                        fingerprint_alertDialog = builder.create();
                        fingerprint_alertDialog.setCanceledOnTouchOutside(true);
                        fingerprint_alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                fingerprintHelper.stopAuthenticate();
                            }
                        });
                    }
                    fingerprint_tv.setText("指纹识别中");
                    fingerprint_alertDialog.show();
                }
            });
        } else {
            view.findViewById(R.id.sign_fingerprint_ll).setVisibility(View.GONE);
            view.findViewById(R.id.sign_fingerprint_line).setVisibility(View.GONE);
        }

        ((TextView)view.findViewById(R.id.sign_tra_data_tv)).setMovementMethod(ScrollingMovementMethod.getInstance());
        popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        // 设置PopupWindow是否能响应外部点击事件
        popupWindow.setOutsideTouchable(false);
        popupWindow.setTouchable(true);
        popupWindow.setAnimationStyle(R.style.ActionSheetDialogAnimation);
        popupWindow.showAtLocation(view, Gravity.BOTTOM,0,0);
        isCancel = true;
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                if (isCancel) {
                    try {
                        JSONObject jsonObject = new JSONObject();
                        jsonObject.put("npc","fail");
                        jsonObject.put("desc","User denied transcation signature");

                        ((HeroFragmentActivity)context).getCurrentFragment().mWebview.evaluateJavascript("window['HeroSignature"+"callback']("+ jsonObject.toString() +")",null);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        if (object.has("transaction")) {
            JSONObject jsonObject = object.getJSONObject("transaction");
            if (jsonObject.has("to")) {
                ((TextView)view.findViewById(R.id.sign_tra_toaddress_tv)).setText(jsonObject.getString("to"));

            }
            if (jsonObject.has("from")) {
                ((TextView)view.findViewById(R.id.sign_tra_fromaddress_tv)).setText(jsonObject.getString("from"));
            }

            if (jsonObject.has("nonce")) {
                String value = String.valueOf(Long.parseLong(jsonObject.getString("nonce").substring(2, jsonObject.getString("nonce").length()), 16));
                ((TextView)view.findViewById(R.id.sign_tra_nonce_tv)).setText(value);
            }
            if (jsonObject.has("value")) {
                String value = String.valueOf(Long.parseLong(jsonObject.getString("value").substring(2, jsonObject.getString("value").length()), 16));
                ((TextView)view.findViewById(R.id.sign_tra_value_tv)).setText(value);
            }
            //inputData
            if (jsonObject.has("data") && !jsonObject.getString("data").equals("")) {
                ((TextView)view.findViewById(R.id.sign_tra_data_tv)).setText(jsonObject.getString("data"));
            } else {
                ((TextView)view.findViewById(R.id.sign_tra_data_tv)).setText("0x");
            }

            if (jsonObject.has("gas")) {
                String value = String.valueOf(Long.parseLong(jsonObject.getString("gas").substring(2, jsonObject.getString("gas").length()), 16));
                ((TextView)view.findViewById(R.id.sign_gaslimit_tv)).setText(value);
            }
            if (jsonObject.has("gasPrice")) {
                String value = String.valueOf(Long.parseLong(jsonObject.getString("gasPrice").substring(2, jsonObject.getString("gasPrice").length()), 16));
                ((TextView)view.findViewById(R.id.sign_gasPrice_tv)).setText(value);
            }
        }

        if (object.has("message")) {
            JSONObject jsonObject = object.getJSONObject("message");
            //inputData
            if (jsonObject.has("data") && !jsonObject.getString("data").equals("")) {
                ((TextView)view.findViewById(R.id.sign_tra_data_tv)).setText(jsonObject.getString("data"));
            } else {
                ((TextView)view.findViewById(R.id.sign_tra_data_tv)).setText("0x");
            }
        }

        final EditText password_et = (EditText) view.findViewById(R.id.sign_password_et);

        view.findViewById(R.id.sign_confirm_bt).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (password_et.getText() == null | password_et.getText().equals("")
                        | password_et.getText().length() == 0) {
                    Toast.makeText(getContext(),"请输入正确的密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                new MyTask((HeroActivity) context, password_et.getText().toString()).execute();
                isCancel = false;
                popupWindow.dismiss();

            }
        });

        view.findViewById(R.id.sign_import_bt).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                isCancel = false;
                popupWindow.dismiss();
                Intent intent = new Intent(getContext(), HeroSignatureActivity.class);
                intent.putExtra("jumptype", "1");
                getContext().startActivity(intent);
            }
        });

    }

    private static class MyTask extends AsyncTask {

        private WeakReference<HeroActivity> activityReference;

        private String passwordString;

        // only retain a weak reference to the activity
        MyTask(HeroActivity context, String passwordString) {
            activityReference = new WeakReference<>(context);
            this.passwordString = passwordString;
        }
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected void onPostExecute(Object o) {
            if (!(o instanceof Bundle)) {
                return;
            }
            Bundle bundle = (Bundle) o;
            HeroActivity activity = activityReference.get();
            if (activity == null || activity.isFinishing())
                return;
            Toast.makeText(activity, bundle.getString("message"), Toast.LENGTH_SHORT).show();
            try {
                jsonObject.put("signMessage",true);
                if (bundle.getBoolean("isSucceed")) {
                    jsonObject.put("isSucceed",bundle.getBoolean("isSucceed"));
                    ((IHeroContext) (activityReference.get())).on(jsonObject);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            super.onPostExecute(o);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Bundle bundle = new Bundle();
            try {
                String content = "";
                if (!FileUtils.getKeystoreFile("default").exists()) {
                    content = FileUtils.getKeystoreFilecontent("default");
                } else {
                    ArrayList<File> fileArrayList = FileUtils.getKeystroeFilesWithoutDefault();
                    content = FileUtils.getKeystoreFilecontent(fileArrayList.get(0).getName());
                }

                ObjectMapper mapper = new ObjectMapper();
                WalletFile walletFile = mapper.readValue(content, WalletFile.class);

                ECKeyPair keyPair = Wallet.decrypt(passwordString, walletFile);
                if (keyPair != null && keyPair.getPrivateKey() != null
                        && keyPair.getPublicKey() != null) {
                    Sign.SignatureData signatureData = Sign.signMessage(content.getBytes(),keyPair);
                    JSONObject signatureDataObject = new JSONObject();
                    signatureDataObject.put("R", Numeric.toHexString(signatureData.getR()));
                    signatureDataObject.put("S", Numeric.toHexString(signatureData.getS()));
                    signatureDataObject.put("V",(char) signatureData.getV());
                    jsonObject.put("signatureData", signatureDataObject);
                } else {
                    throw new CipherException("密码错误");
                }
                bundle.putBoolean("isSucceed", true);
                bundle.putString("message", "签名成功");
            } catch (CipherException ce) {
                bundle.putBoolean("isSucceed", false);
                bundle.putString("message", "签名失败");
                ce.printStackTrace();
            } catch (Exception e) {
                bundle.putBoolean("isSucceed", false);
                bundle.putString("message", "签名失败");
                e.printStackTrace();
            }
            return bundle;
        }
    }

    @Override
    public void onAuthenticationSucceeded(String value) {
        Toast.makeText(context, "指纹认证成功", Toast.LENGTH_SHORT).show();
        new Handler().post(new Runnable() {
            @Override
            public void run() {
                if (fingerprint_alertDialog != null && fingerprint_alertDialog.isShowing()) {
                    fingerprint_alertDialog.dismiss();
                }
                isCancel = false;
                popupWindow.dismiss();
            }
        });

        new MyTask((HeroActivity) getContext(), value).execute();

    }

    @Override
    public void onAuthenticationFailed() {
        fingerprint_tv.setText("指纹识别失败，请重试");
    }

    @Override
    public void onAuthenticationError(String errString) {
        fingerprint_tv.setText(errString);
    }
}