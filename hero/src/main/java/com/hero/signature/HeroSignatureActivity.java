package com.hero.signature;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import com.hero.HeroDrawerActivity;
import com.hero.HeroFragment;
import com.hero.HeroWaitDialog;
import com.hero.R;
import com.hero.depandency.google.zxing.integration.android.IntentIntegrator;
import com.hero.depandency.google.zxing.integration.android.IntentResult;
import com.hero.signature.fragment.HeroSignatureExportFragment;
import com.hero.signature.fragment.HeroSignatureHomeFragment;
import com.hero.signature.fragment.HeroSignatureImportFragment;
import com.hero.signature.fragment.HeroSignatureModifyPwdFragment;
import com.hero.signature.fragment.HeroSignatureQRcodeFragment;
import com.hero.signature.fragment.HeroSignatureWalletFragment;
import com.hero.utils.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class HeroSignatureActivity extends HeroDrawerActivity implements HeroSignatureHomeFragment.OnClickListener, HeroSignatureWalletFragment.OnClickListener{

    private HeroSignatureHomeFragment homeFragment = new HeroSignatureHomeFragment();

    private HeroSignatureImportFragment importFragment = new HeroSignatureImportFragment();

    private HeroSignatureWalletFragment walletHomeFragment = new HeroSignatureWalletFragment();

    private HeroSignatureModifyPwdFragment modifyPwdFragment = new HeroSignatureModifyPwdFragment();

    private HeroSignatureExportFragment exportFragment = new HeroSignatureExportFragment();

    private HeroSignatureQRcodeFragment qRcodeFragment = new HeroSignatureQRcodeFragment();

//    private WebViewFragment webViewFragment = new WebViewFragment();

    private HeroFragment heroFragment = new HeroFragment();

    private HeroWaitDialog heroWaitDialog;

    private FragmentManager fragmentManager;

    private String walletFileString = null;

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

            //隐藏底部导航栏
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
        fragmentManager = getSupportFragmentManager();
        super.onCreate(savedInstanceState);
        initMainContent();
        heroWaitDialog = new HeroWaitDialog(this);

        Intent intent = getIntent();
        if (intent != null) {
            int jump = intent.getExtras().getInt("jumpType");
            if (jump == 1) {
                gotoFragment(walletHomeFragment, Constants.WALLETHOME_TAG);
            }
        }
    }

    private void initMainContent() {
        gotoFragment(homeFragment, Constants.HOME_TAG);
//        Intent intent = new Intent();
//        intent.putExtra("url", "http://10.0.0.26:3000/home.html");
//        intent.putExtra("headBarVisible", true);
//        heroFragment.setArguments(intent.getExtras());
//        fragmentManager.beginTransaction().add(R.id.mainContent, heroFragment, "test").hide(heroFragment).commitAllowingStateLoss();
    }
//
//    private void initPopupWindow() {
//        View contentView = LayoutInflater.from(this).inflate(R.layout.sign_popwindow, null, false);
//        ((TextView)contentView.findViewById(R.id.sign_tra_data_tv)).setMovementMethod(ScrollingMovementMethod.getInstance());
//        PopupWindow popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
//        popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
//        // 设置PopupWindow是否能响应外部点击事件
//        popupWindow.setOutsideTouchable(false);
//        popupWindow.setTouchable(true);
//        popupWindow.setAnimationStyle(R.style.ActionSheetDialogAnimation);
//        popupWindow.showAtLocation(contentView, Gravity.BOTTOM,0,0);
//    }

    private void gotoFragment(HeroFragment fragment, String tag) {
        Bundle bundle = getIntent().getExtras() == null ? new Bundle() : getIntent().getExtras();
        if (walletFileString != null && !walletFileString.equals("")) {
            bundle.putString("walletString", walletFileString);
        }
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.fragment_slide_left_in, R.anim.fragment_slide_right_out)
                .replace(R.id.mainContent, fragment, tag).commitAllowingStateLoss();
    }

    private void backtoFragment(HeroFragment fragment, String tag) {
        Bundle bundle = getIntent().getExtras() == null ? new Bundle() : getIntent().getExtras();
        if (walletFileString != null && !walletFileString.equals("")) {
            bundle.putString("walletString", walletFileString);
        }
        fragment.setArguments(bundle);
        fragmentManager.beginTransaction().setCustomAnimations(R.anim.fragment_slide_right_in, R.anim.fragment_slide_left_out)
                .replace(R.id.mainContent, fragment, tag).commitAllowingStateLoss();
    }

    @Override
    public void onModifyClick() {
        gotoModifyPasswordFragment();
    }

    @Override
    public void onExportKeystoreClick() {
        gotoExportFragment();
    }

    @Override
    public void onQrcodeClick() {
        gotoQRcodeFragment();
    }

    // 签名入口
    @Override
    public void onSignClick() {
//        initPopupWindow();
    }

    public void onPostProcessed() {
        checkKeystoreFile();
//        gotoFragment(walletHomeFragment, Constants.WALLETHOME_TAG);
    }

    // 跳转钱包入口
    @Override
    public void onWalletClick() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            //申请WRITE_EXTERNAL_STORAGE权限
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                    3);
            return;
        } else {
            checkKeystoreFile();
            return;
        }
    }

    // 二维码收款码画面
    private void gotoQRcodeFragment() {
        gotoFragment(qRcodeFragment, Constants.QRCODE_TAG);
    }

    // 钱包详细画面
    private void gotoWalletHomeFragment() {
        gotoFragment(walletHomeFragment, Constants.WALLETHOME_TAG);
    }

    // 修改密码页面
    private void gotoModifyPasswordFragment() {
        gotoFragment(modifyPwdFragment, Constants.MODIFYPASSWORD_TAG);
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
    public HeroFragment getCurrentFragment() {
        return (HeroFragment) getSupportFragmentManager().findFragmentByTag(Constants.HOME_TAG);
    }

    @Override
    public void onBackPressed() {
        if ((qRcodeFragment != null && qRcodeFragment.isVisible())
                || (exportFragment != null && exportFragment.isVisible())
                || (modifyPwdFragment != null && modifyPwdFragment.isVisible())) {
            backtoFragment(walletHomeFragment, Constants.WALLETHOME_TAG);
            return;
        }
        if ((importFragment != null && importFragment.isVisible())
                || (walletHomeFragment != null && walletHomeFragment.isVisible())) {
            backtoFragment(homeFragment, Constants.HOME_TAG);
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        if (heroWaitDialog != null && heroWaitDialog.isShowing()) {
            heroWaitDialog.dismiss();
        }
        super.onDestroy();
    }

    @Override
    protected void startLoading() {

    }

    @Override
    protected void finishLoading() {

    }

    private void checkKeystoreFile() {
        try {
            File f = FileUtils.getKeystoreFile();
            if (f.exists()) {
                walletFileString = FileUtils.getKeystoreFilecontent();
                gotoWalletHomeFragment();
                return;
            }
            gotoImportFragment();
        }  catch (IOException IOEx) {
            IOEx.printStackTrace();
            Toast.makeText(this,"文件处理失败",Toast.LENGTH_LONG).show();
        }
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
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
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

}