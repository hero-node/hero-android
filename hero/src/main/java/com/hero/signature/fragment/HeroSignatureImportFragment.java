package com.hero.signature.fragment;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hero.R;
import com.hero.signature.Constants;
import com.hero.signature.HeroSignatureActivity;
import com.hero.utils.FileUtils;
import com.hero.utils.FingerprintHelper;
import com.hero.utils.ShareUtils;

import org.json.JSONObject;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.crypto.WalletUtils;
import org.web3j.utils.Numeric;

import java.io.File;
import java.lang.ref.WeakReference;

import static com.hero.signature.Constants.AES_128_CTR;
import static com.hero.signature.Constants.CIPHER;
import static com.hero.signature.Constants.CURRENT_VERSION;
import static com.hero.signature.Constants.PROCESS_TYPE_KEYSTORE;
import static com.hero.signature.Constants.PROCESS_TYPE_PRIVATEKEY;
import static com.hero.signature.Constants.SCRYPT;

/**
 * Created by Aron on 2018/7/17.
 */
public class HeroSignatureImportFragment extends android.support.v4.app.Fragment {

    private View layout;

    private TabHost tabHost;

    private TabWidget tabWidget;

    // 私钥页面控件
    private static EditText private_key_data_et;

    private static EditText private_key_password_et;

    private EditText private_key_password_confirm_et;

    private static EditText private_key_hint_et;

    private CheckBox private_key_agrselect_cb;

    // keystore页面控件
    private static EditText official_wallet_keystore_et;

    private static EditText official_wallet_keystore_password_et;

    private CheckBox official_wallet_agrselect_cb;

    private FingerprintHelper fingerprintHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.hero_import_fragment, container, false);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
        fingerprintHelper = new FingerprintHelper(getActivity());
    }

    private void initView() {
        initTabs();
        initPrivateKeyPage();
        initOfficialWallet();
    }

    private void initOfficialWallet() {
        official_wallet_keystore_et = (EditText) layout.findViewById(R.id.official_wallet_keystore_et);
        official_wallet_keystore_password_et = (EditText) layout.findViewById(R.id.official_wallet_keystore_password_et);
        Button official_wallet_confirm_bt = (Button) layout.findViewById(R.id.official_wallet_confirm_bt);
        official_wallet_agrselect_cb = (CheckBox) layout.findViewById(R.id.official_wallet_agrselect_cb);
        TextView official_wallet_service_tv = (TextView) layout.findViewById(R.id.official_wallet_service_tv);
        TextView official_wallet_help_tv = (TextView) layout.findViewById(R.id.official_wallet_help_tv);

        official_wallet_confirm_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!official_wallet_agrselect_cb.isChecked()) {
                    Toast.makeText(getActivity(),"请查看服务及隐私条例", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isNull(official_wallet_keystore_et)) {
                    Toast.makeText(getActivity(),"请输入正确的keystore", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isNull(official_wallet_keystore_password_et)) {
                    Toast.makeText(getActivity(),"请输入正确的keystore密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!checkKeystore()) {
                    Toast.makeText(getActivity(),"请输入正确的keystore", Toast.LENGTH_SHORT).show();
                    return;
                } else {

                    if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            != PackageManager.PERMISSION_GRANTED) {
                        //申请WRITE_EXTERNAL_STORAGE权限
                        ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                                2);
                    } else {
                        // 导入keystore
                        processKeystore();
                    }
                }

            }
        });

        official_wallet_service_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"暂未开放", Toast.LENGTH_SHORT).show();
            }
        });
        official_wallet_help_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"暂未开放", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private static class MyTask extends AsyncTask {

        private WeakReference<HeroSignatureActivity> activityReference;

        int processType;

        // only retain a weak reference to the activity
        MyTask(HeroSignatureActivity context, int processType) {
            activityReference = new WeakReference<>(context);
            this.processType = processType;
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
                if (processType == PROCESS_TYPE_KEYSTORE) {
                    bundle.putString("password", official_wallet_keystore_password_et.getText().toString());
                } else if (processType == PROCESS_TYPE_PRIVATEKEY) {
                    bundle.putString("password", private_key_password_et.getText().toString());
                }
                activity.onPostProcessed(bundle);
            }
            super.onPostExecute(o);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Bundle bundle = new Bundle();
            //获取现在有几个keystore钱包
            int number = FileUtils.getNumbersOfKeystore();
            ShareUtils shareUtils = ShareUtils.getInstance(activityReference.get());

            if (processType == PROCESS_TYPE_KEYSTORE) {
                String keystore = official_wallet_keystore_et.getText().toString();

                try {
                    ObjectMapper mapper = new ObjectMapper();
                    WalletFile walletFile = mapper.readValue(keystore, WalletFile.class);

                    if (walletFile.getVersion() != CURRENT_VERSION) {
                        throw new CipherException("钱包版本不支持");
                    }

                    if (!walletFile.getCrypto().getCipher().equals(CIPHER)) {
                        throw new CipherException("钱包Cipher不支持");
                    }

                    if (!walletFile.getCrypto().getKdf().equals(AES_128_CTR) && !walletFile.getCrypto().getKdf().equals(SCRYPT)) {
                        throw new CipherException("KDF类型不支持");
                    }

                    ECKeyPair keyPair = Wallet.decrypt(official_wallet_keystore_password_et.getText().toString(), walletFile);
                    if (keyPair != null && keyPair.getPrivateKey() != null
                            && keyPair.getPublicKey() != null) {

                        if (shareUtils.getBoolean(Keys.getAddress(keyPair), false)) {
                            throw new CipherException("钱包已存在");
                        }

                        File keystoreFile = FileUtils.getKeystoreFile("Keystore" + number);
                        shareUtils.putString("Keystore" + number + ".json", "Keystore" + number + ".json");
                        shareUtils.putBoolean(Keys.getAddress(keyPair), true);

                        if (keystoreFile.exists()) {
                            keystoreFile.delete();
                        }
                        keystoreFile.createNewFile();
                        FileUtils.writeFile(Constants.KEYSTORE_FILE_PATH + "Keystore" + number + ".json", official_wallet_keystore_et.getText().toString());
                    } else {
                        throw new CipherException("keystore密码不正确");
                    }
                    bundle.putBoolean("isSucceed", true);
                    bundle.putString("message", "钱包导入成功");
                } catch (CipherException ce) {
                    bundle.putBoolean("isSucceed", false);
                    bundle.putString("message", ce.getMessage());
                    ce.printStackTrace();
                } catch (Exception e) {
                    bundle.putBoolean("isSucceed", false);
                    bundle.putString("message", "keystore处理异常");
                    e.printStackTrace();
                }
            } else if (processType == PROCESS_TYPE_PRIVATEKEY) {
                try {
                    ECKeyPair keyPair = ECKeyPair.create(Numeric.toBigInt(private_key_data_et.getText().toString()));
                    if (shareUtils.getBoolean(Keys.getAddress(keyPair), false)) {
                        throw new CipherException("钱包已存在");
                    }

                    //keystore文件
                    String fileName = WalletUtils.generateWalletFile(private_key_password_et.getText().toString(),
                            keyPair, FileUtils.getAppFileDir(), false);

                    //重命名文件
                    FileUtils.renameFile(fileName, number);
                    shareUtils.putString("Keystore" + number + ".json", "Keystore" + number + ".json");
                    shareUtils.putBoolean(Keys.getAddress(keyPair), true);

                    // 密码提示文件
                    File hintFile = FileUtils.getHintFile("Hint" + number);

                    if (hintFile.exists()) {
                        hintFile.delete();
                    }
                    hintFile.createNewFile();

                    FileUtils.writeFile(Constants.PASSWORDHINT_FILE_PATH + "Hint" + number + ".txt", private_key_hint_et.getText().toString());
                    bundle.putBoolean("isSucceed", true);
                    bundle.putString("message", "钱包导入成功");
                } catch (CipherException ce) {
                    bundle.putBoolean("isSucceed", false);
                    bundle.putString("message", ce.getMessage());
                    ce.printStackTrace();
                } catch (Exception e) {
                    bundle.putBoolean("isSucceed", false);
                    bundle.putString("message", "keystore处理异常");
                    e.printStackTrace();
                }
            } else {
                bundle.putBoolean("isSucceed", false);
                bundle.putString("message", "处理类型不支持");
            }

            try {
                //如果是唯一一个钱包设置为默认钱包
                if (number == 0) {
                    FileUtils.copyFile(Constants.KEYSTORE_FILE_PATH + "Keystore0.json",
                            Constants.KEYSTORE_FILE_PATH + "default.json");
                }
                shareUtils.putString("default", "Keystore0.json");
            } catch (Exception e) {
                e.printStackTrace();
            }
            return bundle;
        }
    }

    private void processKeystore() {
        new MyTask((HeroSignatureActivity)getActivity(), PROCESS_TYPE_KEYSTORE).execute();
    }


    private boolean checkKeystore() {
        String keystore = official_wallet_keystore_et.getText().toString();
        try {
            JSONObject jsonObject = new JSONObject(keystore);
            if (jsonObject.has("address") && jsonObject.has("crypto")
                    && jsonObject.has("id") && jsonObject.has("version")) {
                JSONObject crypto = jsonObject.getJSONObject("crypto");
                if (crypto.has("cipher") && crypto.has("cipherparams")
                        && crypto.has("ciphertext") && crypto.has("kdf")
                        && crypto.has("kdfparams") && crypto.has("mac")) {
                    JSONObject cipherparams = crypto.getJSONObject("cipherparams");
                    JSONObject kdfparams = crypto.getJSONObject("kdfparams");
                    if (cipherparams.has("iv") && kdfparams.has("dklen")
                            && kdfparams.has("n") && kdfparams.has("p")
                            && kdfparams.has("r") && kdfparams.has("salt")) {
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } catch (Exception e){
            Toast.makeText(getActivity(),"keystore处理异常", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    private void initPrivateKeyPage() {
        private_key_data_et = (EditText) layout.findViewById(R.id.private_key_data_et);
        private_key_password_et = (EditText) layout.findViewById(R.id.private_key_password_et);
        private_key_password_confirm_et = (EditText) layout.findViewById(R.id.private_key_password_confirm_et);
        private_key_hint_et = (EditText) layout.findViewById(R.id.private_key_hint_et);
        private_key_agrselect_cb = (CheckBox) layout.findViewById(R.id.private_key_agrselect_cb);
        Button private_key_confirm_bt = (Button) layout.findViewById(R.id.private_key_confirm_bt);
        TextView private_key_service_tv = (TextView) layout.findViewById(R.id.private_key_service_tv);
        TextView private_key_help_tv =  (TextView) layout.findViewById(R.id.private_key_help_tv);

        private_key_confirm_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!private_key_agrselect_cb.isChecked()) {
                    Toast.makeText(getActivity(),"请查看服务及隐私条例", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isNull(private_key_data_et)) {
                    Toast.makeText(getActivity(),"请输入正确的明文私钥", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isNull(private_key_password_et)) {
                    Toast.makeText(getActivity(),"请输入正确的密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (isNull(private_key_password_confirm_et)) {
                    Toast.makeText(getActivity(),"请重复输入密码", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!private_key_password_et.getText().toString().equals(private_key_password_confirm_et.getText().toString())) {
                    Toast.makeText(getActivity(),"两次输入的密码不一致，请重新输入", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    //申请WRITE_EXTERNAL_STORAGE权限
                    ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                            1);
                } else {
                    // 导入
                    processKey();
                }
            }
        });

        private_key_service_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"暂未开放", Toast.LENGTH_SHORT).show();
            }
        });
        private_key_help_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getActivity(),"暂未开放", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void processKey() {
        new MyTask((HeroSignatureActivity)getActivity(), PROCESS_TYPE_PRIVATEKEY).execute();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 1:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //创建文件夹
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        processKey();
                    }
                    break;
                }
            case 2:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //创建文件夹
                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        processKeystore();
                    }
                    break;
                }
        }
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

    private void initTabs() {
        tabHost = (TabHost) layout.findViewById(R.id.import_tabhost);
        tabWidget = (TabWidget) layout.findViewById(android.R.id.tabs);

        tabHost.setup();
        TabHost.TabSpec page1 = tabHost.newTabSpec("tab1")
                .setIndicator("官方钱包")
                .setContent(R.id.import_item1);
        tabHost.addTab(page1);

        TabHost.TabSpec page2 = tabHost.newTabSpec("tab2")
                .setIndicator("私钥")
                .setContent(R.id.import_item2);
        tabHost.addTab(page2);
        changeColor();

        tabHost.setOnTabChangedListener(new TabHost.OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                changeColor();
            }
        });
    }

    private void changeColor() {
        for (int i = 0; i < tabWidget.getChildCount(); i++) {
            TextView tView = (TextView) tabWidget.getChildAt(i).findViewById(
                    android.R.id.title);
            //修改背景
            tabWidget.getChildAt(i).setBackgroundResource(
                    R.drawable.wallet_tab_select);

            if (tabHost.getCurrentTab() == i) {
                tView.setTextColor(getResources().getColorStateList(
                        R.color.tab_blue));
            } else {
                tView.setTextColor(getResources().getColorStateList(
                        R.color.tab_gray));
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
