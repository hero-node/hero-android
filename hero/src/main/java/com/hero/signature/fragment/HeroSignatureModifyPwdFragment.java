package com.hero.signature.fragment;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hero.HeroDrawerActivity;
import com.hero.HeroFragment;
import com.hero.R;
import com.hero.signature.Constants;
import com.hero.signature.HeroSignatureActivity;
import com.hero.utils.FileUtils;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Aron on 2018/7/26.
 */
public class HeroSignatureModifyPwdFragment extends HeroFragment {

    private View layout;

    private EditText modifypwd_fragment_oldpassword_et;

    private EditText modifypwd_fragment_newpassword_et;

    private EditText modifypwd_fragment_newpassword_confirm_et;

    private String walletFileString;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.hero_signature_modifypwd_fragment, container, false);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        walletFileString = getArguments().getString("walletString");
        initView();
    }

    private void initView() {
        TextView save_tv = (TextView) layout.findViewById(R.id.wallet_save_tv);
        modifypwd_fragment_oldpassword_et = (EditText) layout.findViewById(R.id.modifypwd_fragment_oldpassword_et);
        modifypwd_fragment_newpassword_confirm_et = (EditText)layout.findViewById(R.id.modifypwd_fragment_newpassword_confirm_et);
        modifypwd_fragment_newpassword_et = (EditText) layout.findViewById(R.id.modifypwd_fragment_newpassword_et);
        save_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (isNull(modifypwd_fragment_oldpassword_et)) {
                    Toast.makeText(getActivity(),"请输入旧密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isNull(modifypwd_fragment_newpassword_et)) {
                    Toast.makeText(getActivity(),"请输入正确的密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isNull(modifypwd_fragment_newpassword_confirm_et)) {
                    Toast.makeText(getActivity(),"请重复输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!modifypwd_fragment_newpassword_et.getText().toString().equals(modifypwd_fragment_newpassword_confirm_et.getText().toString())) {
                    Toast.makeText(getActivity(),"两次输入的密码不一致，请重新输入", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!modifypwd_fragment_oldpassword_et.getText().toString().equals(modifypwd_fragment_newpassword_et.getText().toString())) {
                    Toast.makeText(getActivity(),"新旧密码不能相同", Toast.LENGTH_SHORT).show();
                    return;
                }

                processModify();
            }
        });

    }

    private boolean isNull (EditText editText) {
        if (editText.getText() == null
                | editText.getText().equals("")
                | editText.getText().length() == 0) {
            return true;
        } else {
            return false;
        }
    }

    private void processModify() {
        new MyTask((HeroSignatureActivity) getActivity(), walletFileString, modifypwd_fragment_oldpassword_et.getText().toString(),
                modifypwd_fragment_newpassword_et.getText().toString()).execute();
    }

    private static class MyTask extends AsyncTask {

        private WeakReference<HeroSignatureActivity> activityReference;

        private String walletFileString;

        private String oldpasswordString;

        private String newpasswordString;

        // only retain a weak reference to the activity
        MyTask(HeroSignatureActivity context, String walletFileString, String oldpasswordString, String newpasswordString) {
            activityReference = new WeakReference<>(context);
            this.walletFileString = walletFileString;
            this.oldpasswordString = oldpasswordString;
            this.newpasswordString = newpasswordString;
        }
        @Override
        protected void onPreExecute() {
            activityReference.get().showWaitDialog();
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
            activity.dismissWaitDialog();
            Toast.makeText(activity, bundle.getString("message"), Toast.LENGTH_SHORT).show();
            if (bundle.getBoolean("isSucceed")) {
                activity.onPostProcessed();
            }
            super.onPostExecute(o);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Bundle bundle = new Bundle();
            try {
                ObjectMapper mapper = new ObjectMapper();
                WalletFile walletFile = mapper.readValue(walletFileString, WalletFile.class);

                ECKeyPair keyPair = Wallet.decrypt(oldpasswordString, walletFile);
                if (keyPair != null && keyPair.getPrivateKey() != null
                        && keyPair.getPublicKey() != null) {

                    File keystoreFile = FileUtils.getKeystoreFile();

                    if (keystoreFile.exists()) {
                        keystoreFile.delete();
                    }
                    keystoreFile.createNewFile();

                    //keystore文件
                    String fileName = WalletUtils.generateWalletFile(newpasswordString, keyPair, FileUtils.getAppFileDir(), false);

                    FileUtils.renameFile(fileName);
                    // 密码提示文件
                    File hintFile = FileUtils.getHintFile();

                    if (hintFile.exists()) {
                        hintFile.delete();
                    }
                    hintFile.createNewFile();

                    FileUtils.writeFile(Constants.PASSWORDHINT_FILE_PATH, "");
                } else {
                    throw new CipherException("旧密码错误");
                }
                bundle.putBoolean("isSucceed", true);
                bundle.putString("message", "修改密码成功");
            }catch (CipherException ce) {
                bundle.putBoolean("isSucceed", false);
                bundle.putString("message", "旧密码错误");
                ce.printStackTrace();
            }
            catch (Exception e) {
                bundle.putBoolean("isSucceed", false);
                bundle.putString("message", "修改密码失败");
                e.printStackTrace();
            }
            return bundle;
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
