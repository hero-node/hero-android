package com.hero.signature;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.security.keystore.KeyProperties;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.hero.HeroActivity;
import com.hero.HeroWaitDialog;
import com.hero.R;
import com.hero.depandency.google.zxing.integration.android.IntentIntegrator;
import com.hero.depandency.google.zxing.integration.android.IntentResult;
import com.hero.signature.fragment.HeroSignatureExportFragment;
import com.hero.signature.fragment.HeroSignatureWalletListFragment;
import com.hero.signature.fragment.HeroSignatureImportFragment;
import com.hero.signature.fragment.HeroSignatureModifyPwdFragment;
import com.hero.signature.fragment.HeroSignatureQRcodeFragment;
import com.hero.signature.fragment.HeroSignatureWalletFragment;
import com.hero.utils.FileUtils;
import com.hero.utils.FingerprintHelper;
import com.hero.utils.ShareUtils;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.List;

public class HeroSignatureActivity extends FragmentActivity implements HeroSignatureWalletFragment.OnClickListener,
        FingerprintHelper.SimpleAuthenticationCallback, HeroSignatureWalletListFragment.OnClickListener{

    private HeroSignatureWalletListFragment walletListFragment = new HeroSignatureWalletListFragment();

    private HeroSignatureImportFragment importFragment = new HeroSignatureImportFragment();

    private HeroSignatureWalletFragment walletHomeFragment = new HeroSignatureWalletFragment();

    private HeroSignatureModifyPwdFragment modifyPwdFragment = new HeroSignatureModifyPwdFragment();

    private HeroSignatureExportFragment exportFragment = new HeroSignatureExportFragment();

    private HeroSignatureQRcodeFragment qRcodeFragment = new HeroSignatureQRcodeFragment();

    private HeroWaitDialog heroWaitDialog;

    private FragmentManager fragmentManager;

    private FingerprintHelper fingerprintHelper;

    private TextView fingerprint_tv;

    private AlertDialog fingerprint_alertDialog;

    private HeroSignatureWalletListFragment.WalletData walletData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //设置透明的状态栏和控制面板
        getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
                    | WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
//            window.setNavigationBarColor(Color.TRANSPARENT);
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

            // 黑色
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
        }
        fragmentManager = getSupportFragmentManager();
        fingerprintHelper = new FingerprintHelper(this);
        fingerprintHelper.setCallback(this);
        fingerprintHelper.generateKey();

        AlertDialog.Builder builder = new AlertDialog.Builder(HeroSignatureActivity.this);
        View view = View.inflate(HeroSignatureActivity.this, R.layout.fingerprint_dialog, null);
        fingerprint_tv = (TextView) view.findViewById(R.id.fingerprint_hint);
        builder.setView(view);
        // 创建对话框
        fingerprint_alertDialog = builder.create();
        fingerprint_alertDialog.setCanceledOnTouchOutside(false);
        fingerprint_alertDialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                fingerprintHelper.stopAuthenticate();
            }
        });

        super.onCreate(savedInstanceState);
        initMainContent();
        heroWaitDialog = new HeroWaitDialog(this);

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            if (intent.getExtras().containsKey("jumpType")) {
                int jump = intent.getExtras().getInt("jumpType");
                if (jump == 1) {
                    checkKeystoreFile();
                }
            }
        }
    }

    private void initMainContent() {
        setContentView(R.layout.activity_drawer);
        gotoFragment(walletListFragment, Constants.WALLETLIST_TAG);
    }

    private void gotoFragment(android.support.v4.app.Fragment fragment, String tag) {
        gotoFragment(fragment, tag, walletData);
    }

    private void gotoFragment(android.support.v4.app.Fragment fragment, String tag, HeroSignatureWalletListFragment.WalletData walletData) {
        Bundle bundle = getIntent().getExtras() == null ? new Bundle() : getIntent().getExtras();
        if (walletData != null) {
            this.walletData = walletData;
            bundle.putSerializable("walletData", walletData);
        }
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.fragment_slide_left_in, R.anim.fragment_slide_right_out)
                .replace(R.id.mainContent, fragment, tag).commitAllowingStateLoss();
    }

    private void backtoFragment(Fragment fragment, String tag) {
        backtoFragment(fragment, tag, walletData);
    }

    private void backtoFragment(Fragment fragment, String tag, HeroSignatureWalletListFragment.WalletData walletData) {
        Bundle bundle = getIntent().getExtras() == null ? new Bundle() : getIntent().getExtras();
        if (walletData != null) {
            this.walletData = walletData;
            bundle.putSerializable("walletData", walletData);
        }
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.fragment_slide_right_in, R.anim.fragment_slide_left_out)
                .replace(R.id.mainContent, fragment, tag).commitAllowingStateLoss();
    }

    @Override
    public void onItemClick(HeroSignatureWalletListFragment.WalletData walletData) {
        gotoWalletHomeFragment(walletData);
    }

    @Override
    public void onImportClick() {
        gotoImportFragment();
    }

    @Override
    public void onModifyClick(HeroSignatureWalletListFragment.WalletData walletData) {
        gotoModifyPasswordFragment(walletData);
    }

    @Override
    public void onExportKeystoreClick() {
        gotoExportFragment();
    }

    @Override
    public void onQrcodeClick() {
        gotoQRcodeFragment();
    }

    public void onPostProcessed(Bundle bundle) {
        checkKeystoreFile();
        if (bundle!= null && bundle.containsKey("password") && fingerprintHelper.checkFingerprintAvailable() == FingerprintHelper.FINGERPRINT_STATE_AVAILABLE) {
            final String password = bundle.getString("password");

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            View view = View.inflate(this, R.layout.hero_confirm_dialog, null);
            TextView content = (TextView) view.findViewById(R.id.confirm_dialog_content);
            TextView buttonConfirm = (TextView) view.findViewById(R.id.confirm_dialog_confirm);
            TextView buttonCancel = (TextView) view.findViewById(R.id.confirm_dialog_cancel);
            view.findViewById(R.id.confirm_dialog_password_et).setVisibility(View.GONE);
            view.findViewById(R.id.confirm_dialog_ok).setVisibility(View.GONE);
            content.setText("是否使用本机指纹识别");
            builder.setView(view);
            // 创建对话框
            final AlertDialog alertDialog = builder.create();
            alertDialog.setCanceledOnTouchOutside(false);
            buttonConfirm.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    fingerprintHelper.generateKey();
                    fingerprintHelper.setPurpose(KeyProperties.PURPOSE_ENCRYPT);
                    fingerprintHelper.setData(password);
                    fingerprintHelper.authenticate();
                    fingerprint_alertDialog.show();

                    alertDialog.dismiss();
                }

            });
            buttonCancel.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    alertDialog.dismiss();
                }
            });
            alertDialog.show();
        }
    }

    // 钱包详细画面
    private void gotoWalletListFragment() {
        gotoFragment(walletListFragment, Constants.WALLETLIST_TAG);
    }

    // 二维码收款码画面
    private void gotoQRcodeFragment() {
        gotoFragment(qRcodeFragment, Constants.QRCODE_TAG);
    }

    // 钱包详细画面
    private void gotoWalletHomeFragment(HeroSignatureWalletListFragment.WalletData walletData) {
        gotoFragment(walletHomeFragment, Constants.WALLETHOME_TAG, walletData);
    }

    // 修改密码页面
    private void gotoModifyPasswordFragment(HeroSignatureWalletListFragment.WalletData walletData) {
        gotoFragment(modifyPwdFragment, Constants.MODIFYPASSWORD_TAG, walletData);
    }

    // 导出页面
    private void gotoExportFragment() {
        gotoFragment(exportFragment, Constants.EXPORT_TAG);
    }

    // 导入钱包画面
    private void gotoImportFragment() {
        gotoFragment(importFragment, Constants.IMPORT_TAG);
    }

    @Override
    public void onBackPressed() {
        if ((qRcodeFragment != null && qRcodeFragment.isVisible())
                || (exportFragment != null && exportFragment.isVisible())
                || (modifyPwdFragment != null && modifyPwdFragment.isVisible())) {
            backtoFragment(walletHomeFragment, Constants.WALLETHOME_TAG);
            return;
        }
        if ((walletHomeFragment != null && walletHomeFragment.isVisible())) {
            backtoFragment(walletListFragment, Constants.WALLETLIST_TAG);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (heroWaitDialog != null && heroWaitDialog.isShowing()) {
            heroWaitDialog.dismiss();
        }
        if (fingerprintHelper != null) {
            fingerprintHelper.stopAuthenticate();
        }
        if (fingerprint_alertDialog != null && fingerprint_alertDialog.isShowing()) {
            fingerprint_alertDialog.dismiss();
        }
        super.onDestroy();
    }

    private void checkKeystoreFile() {
        if (FileUtils.hasKeystoreFile()) {
            gotoWalletListFragment();
            return;
        }
        gotoImportFragment();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 3:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    checkKeystoreFile();
                    return;
                }
        }

        // 获取到Activity下的Fragment
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments == null) {
            return;
        }
        for (Fragment fragment : fragments) {
            if (fragment != null) {

                fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                try {
                    String scanCommand = "[{command:'goto:" + result.getContents() + "'}]";
                    JSONArray tabsArray = new JSONArray(scanCommand);
//                    on(tabsArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void showWaitDialog() {
        if (heroWaitDialog == null) {
            heroWaitDialog = new HeroWaitDialog(this);
        }
        heroWaitDialog.show();
    }

    public void dismissWaitDialog() {
        if (heroWaitDialog != null && heroWaitDialog.isShowing()) {
            heroWaitDialog.dismiss();
        }
    }

    @Override
    public void onAuthenticationSucceeded(String value) {
        Toast.makeText(this, "指纹认证成功", Toast.LENGTH_SHORT).show();
        if (fingerprint_alertDialog != null && fingerprint_alertDialog.isShowing()) {
            fingerprint_alertDialog.dismiss();
        }
        ShareUtils shareUtils = ShareUtils.getInstance(HeroSignatureActivity.this);
        if (shareUtils.contains("fingerprint")) {
            String name = shareUtils.getString("fingerprint","");
            ShareUtils.getInstance(HeroSignatureActivity.this).putString("fingerprint", name + "," + walletData.getName());
        } else {
            ShareUtils.getInstance(HeroSignatureActivity.this).putString("fingerprint", walletData.getName());
        }
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