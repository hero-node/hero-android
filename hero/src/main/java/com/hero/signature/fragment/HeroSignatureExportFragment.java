package com.hero.signature.fragment;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TabHost;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hero.R;
import com.hero.utils.FileUtils;
import com.hero.utils.ZxingUtils;

import org.json.JSONObject;
import org.web3j.crypto.WalletFile;

/**
 * Created by Aron on 2018/8/06.
 */
public class HeroSignatureExportFragment extends android.support.v4.app.Fragment {

    private View layout;

    private TabHost tabHost;

    private TabWidget tabWidget;

    private HeroSignatureWalletListFragment.WalletData walletData;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.hero_signature_export_fragment, container, false);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        walletData = (HeroSignatureWalletListFragment.WalletData) getArguments().getSerializable("walletData");
        initView();
        initContent();
    }

    private void initView() {
        tabHost = (TabHost) layout.findViewById(R.id.export_tabhost);
        tabWidget = (TabWidget) layout.findViewById(android.R.id.tabs);
    }

    private void initContent() {
        initTabs();
        final TextView export_keystore_tv = (TextView) layout.findViewById(R.id.export_keystore_tv);
        ImageView export_qrcode_iv = (ImageView) layout.findViewById(R.id.export_qrcode_iv);
        Button export_copy_bt = (Button) layout.findViewById(R.id.export_copy_bt);
        try {
            String fileContent = FileUtils.getKeystoreFilecontent(walletData.getName());
            export_keystore_tv.setText(fileContent);
            export_keystore_tv.setMovementMethod(ScrollingMovementMethod.getInstance());

            Bitmap qrcode = ZxingUtils.encodeAsBitmap(walletData.getAddress(), 400,400);
            export_qrcode_iv.setImageBitmap(qrcode);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),"二维码生成失败,请刷新页面", Toast.LENGTH_LONG).show();
        }

        export_copy_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText(null, export_keystore_tv.getText().toString()));
                Toast.makeText(getActivity(), "复制成功", Toast.LENGTH_LONG).show();
            }
        });
    }

    private void initTabs() {
        tabHost.setup();
        TabHost.TabSpec page1 = tabHost.newTabSpec("tab1")
                .setIndicator("Keystore 文件")
                .setContent(R.id.export_item1);
        tabHost.addTab(page1);

        TabHost.TabSpec page2 = tabHost.newTabSpec("tab2")
                .setIndicator("二维码")
                .setContent(R.id.export_item2);
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
