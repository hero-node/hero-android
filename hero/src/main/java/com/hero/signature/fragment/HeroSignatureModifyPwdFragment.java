package com.hero.signature.fragment;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.io.IOException;
import java.lang.ref.WeakReference;

/**
 * Created by Aron on 2018/7/26.
 */
public class HeroSignatureModifyPwdFragment extends android.support.v4.app.Fragment {

    private View layout;

    private static EditText modifypwd_fragment_oldpassword_et;

    private static EditText modifypwd_fragment_newpassword_et;

    private static EditText modifypwd_fragment_newpassword_confirm_et;

    private HeroSignatureWalletListFragment.WalletData walletData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.hero_signature_modifypwd_fragment, container, false);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        walletData = (HeroSignatureWalletListFragment.WalletData) getArguments().getSerializable("walletData");
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

                if (modifypwd_fragment_oldpassword_et.getText().toString().equals(modifypwd_fragment_newpassword_et.getText().toString())) {
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
        new MyTask((HeroSignatureActivity) getActivity(), walletData.getWalletFile(),
                modifypwd_fragment_oldpassword_et.getText().toString(),
                modifypwd_fragment_newpassword_et.getText().toString(),
                walletData.getName()).execute();
    }

    private static class MyTask extends AsyncTask {

        private WeakReference<HeroSignatureActivity> activityReference;

        private WalletFile walletFile;

        private String oldpasswordString;

        private String newpasswordString;

        private String walletName;

        // only retain a weak reference to the activity
        MyTask(HeroSignatureActivity context, WalletFile walletFile,
               String oldpasswordString, String newpasswordString,
               String walletName) {
            activityReference = new WeakReference<>(context);
            this.walletFile = walletFile;
            this.oldpasswordString = oldpasswordString;
            this.newpasswordString = newpasswordString;
            this.walletName = walletName;

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
                activity.onPostProcessed(bundle);
            }
            modifypwd_fragment_newpassword_confirm_et.setText("");
            modifypwd_fragment_newpassword_et.setText("");
            modifypwd_fragment_oldpassword_et.setText("");
            super.onPostExecute(o);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Bundle bundle = new Bundle();
            try {
                ECKeyPair keyPair = Wallet.decrypt(oldpasswordString, walletFile);
                if (keyPair != null && keyPair.getPrivateKey() != null
                        && keyPair.getPublicKey() != null) {

                    File keystoreFile = FileUtils.getKeystoreFile(walletName);

                    if (keystoreFile.exists()) {
                        keystoreFile.delete();
                    }
                    keystoreFile.createNewFile();

                    //keystore文件
                    String fileName = WalletUtils.generateWalletFile(newpasswordString, keyPair, FileUtils.getAppFileDir(), false);
                    String index = walletName;
                    index = index.replace("Keystore","");
                    index = index.replace(".json","");

                    FileUtils.renameFile(fileName, Integer.valueOf(index));

                    // 密码提示文件
                    File hintFile = FileUtils.getHintFile(walletName);

                    if (hintFile.exists()) {
                        hintFile.delete();
                    }
                    hintFile.createNewFile();

                    String fName = walletName;
                    fName = fName.replace(".json", ".txt");

                    FileUtils.writeFile(Constants.PASSWORDHINT_FILE_PATH + fName, "");
                } else {
                    throw new CipherException("旧密码错误");
                }
                bundle.putBoolean("isSucceed", true);
                bundle.putString("message", "修改密码成功");
            } catch (CipherException ce) {
                bundle.putBoolean("isSucceed", false);
                bundle.putString("message", "旧密码错误");
                ce.printStackTrace();
            } catch (Exception e) {
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
