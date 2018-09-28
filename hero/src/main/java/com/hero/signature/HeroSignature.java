package com.hero.signature;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
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
import com.hero.HeroActivity;
import com.hero.HeroView;
import com.hero.IHero;
import com.hero.IHeroContext;
import com.hero.R;
import com.hero.utils.FileUtils;

import org.apache.http.util.EncodingUtils;
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

/**
 * Created by Aron on 2018/7/9.
 */
public class HeroSignature extends View implements IHero {

    private Integer transactionType = TRAN_MESSAGE;
    private static final int TRAN_MESSAGE = 1;
    private static final int TRAN_TRANSFER = 2;
    private static JSONObject jsonObject;

    public HeroSignature(Context c) {
        super(c);
    }

    @Override
    public void on(JSONObject jsonObject) throws JSONException {
        HeroView.on(this, jsonObject);
        if (jsonObject.has("message"))
        {
            System.out.println(jsonObject.toString());
            this.jsonObject = jsonObject;
            if (jsonObject.has("transactionType")) {
                this.transactionType = jsonObject.getInt("transactionType");
            }
            View contentView = LayoutInflater.from(getContext()).inflate(R.layout.hero_sign_popwindow, null, false);
            try {
                if (transactionType == TRAN_TRANSFER) {
                    contentView.findViewById(R.id.sign_content_transfer_ll).setVisibility(View.VISIBLE);
                    contentView.findViewById(R.id.sign_content_message_ll).setVisibility(View.INVISIBLE);
                    if (!FileUtils.getKeystoreFile().exists()) {
                        Intent intent = new Intent(getContext(), HeroSignatureActivity.class);
                        intent.putExtra("jumptype", "1");
                        getContext().startActivity(intent);
                    }
                } else {
                    contentView.findViewById(R.id.sign_content_transfer_ll).setVisibility(View.INVISIBLE);
                    contentView.findViewById(R.id.sign_content_message_ll).setVisibility(View.VISIBLE);
                    if (!FileUtils.getKeystoreFile().exists()) {
                        Intent intent = new Intent(getContext(), HeroSignatureActivity.class);
                        intent.putExtra("jumptype", "1");
                        getContext().startActivity(intent);
                    }
                }
            } catch (IOException e) {


            }

            initSignView(contentView, jsonObject);
        }
    }

    private void initSignView(View view, JSONObject object) throws JSONException {
        ((TextView)view.findViewById(R.id.sign_tra_data_tv)).setMovementMethod(ScrollingMovementMethod.getInstance());
        final PopupWindow popupWindow = new PopupWindow(view, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
        // 设置PopupWindow是否能响应外部点击事件
        popupWindow.setOutsideTouchable(false);
        popupWindow.setTouchable(true);
        popupWindow.setAnimationStyle(R.style.ActionSheetDialogAnimation);
        popupWindow.showAtLocation(view, Gravity.BOTTOM,0,0);
        JSONObject jsonObject = object.getJSONObject("transactionData");

        //字段处理
        if (jsonObject.has("hash")) {
            ((TextView)view.findViewById(R.id.sign_tra_hash_tv)).setText(jsonObject.getString("hash"));
        }
        if (jsonObject.has("nonce") && jsonObject.has("position")) {
            ((TextView)view.findViewById(R.id.sign_tra_nonce_tv)).setText(
                    jsonObject.getInt("nonce") + "I﹛"+ jsonObject.getInt("position") + "﹜");
        }
        if (jsonObject.has("position")) {
            ((TextView)view.findViewById(R.id.sign_tra_data_tv)).setText(jsonObject.getString("position"));
        }
        if (jsonObject.has("receiveAddress")) {
            ((TextView)view.findViewById(R.id.sign_tra_toaddress_tv)).setText(jsonObject.getString("receiveAddress"));

        }
        if (jsonObject.has("sendAddress")) {
            ((TextView)view.findViewById(R.id.sign_tra_fromaddress_tv)).setText(jsonObject.getString("sendAddress"));
        }
        if (jsonObject.has("value")) {
            ((TextView)view.findViewById(R.id.sign_tra_value_tv)).setText(jsonObject.getString("value"));
        }
        if (jsonObject.has("data")) {
            ((TextView)view.findViewById(R.id.sign_tra_data_tv)).setText(jsonObject.getString("value"));
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
                new MyTask((HeroSignatureActivity) getContext(), password_et.getText().toString()).execute();
                popupWindow.dismiss();

            }
        });

    }

    private static class MyTask extends AsyncTask {

        private WeakReference<HeroSignatureActivity> activityReference;

        private String passwordString;

        // only retain a weak reference to the activity
        MyTask(HeroSignatureActivity context, String passwordString) {
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
            HeroSignatureActivity activity = activityReference.get();
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
                File file = new File("/data/user/0/com.hero.sample/files/keystore.json");
                String fileString;
                FileInputStream fis = new FileInputStream(file);
                int length = fis.available();
                byte [] buffer = new byte[length];
                fis.read(buffer);
                fileString = EncodingUtils.getString(buffer, "UTF-8");
                fis.close();

                ObjectMapper mapper = new ObjectMapper();
                WalletFile walletFile = mapper.readValue(fileString, WalletFile.class);

                ECKeyPair keyPair = Wallet.decrypt(passwordString, walletFile);
                if (keyPair != null && keyPair.getPrivateKey() != null
                        && keyPair.getPublicKey() != null) {
                    Sign.SignatureData signatureData = Sign.signMessage(fileString.getBytes(),keyPair);
                    JSONObject signatureDataObject = new JSONObject();
                    signatureDataObject.put("R", Numeric.toHexString(signatureData.getR()));
                    signatureDataObject.put("S", Numeric.toHexString(signatureData.getS()));
                    signatureDataObject.put("V",(char) signatureData.getV());
                    jsonObject.put("signatureData", signatureDataObject);
                } else {
                    throw new CipherException("旧密码错误");
                }
                bundle.putBoolean("isSucceed", true);
                bundle.putString("message", "签名成功");
            }catch (CipherException ce) {
                bundle.putBoolean("isSucceed", false);
                bundle.putString("message", "签名失败");
                ce.printStackTrace();
            }
            catch (Exception e) {
                bundle.putBoolean("isSucceed", false);
                bundle.putString("message", "签名失败");
                e.printStackTrace();
            }
            return bundle;
        }
    }

}