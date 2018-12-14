package com.hero.signature.fragment;

import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hero.HeroFragment;
import com.hero.R;
import com.hero.utils.ZxingUtils;

import org.web3j.crypto.WalletFile;

/**
 * Created by Aron on 2018/08/05.
 */
public class HeroSignatureQRcodeFragment extends android.support.v4.app.Fragment {

    private View layout;

    private HeroSignatureWalletListFragment.WalletData walletData;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.hero_signature_qrcode_fragment, container, false);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        walletData = (HeroSignatureWalletListFragment.WalletData) getArguments().getSerializable("walletData");

        initView();
    }

    private void initView() {
        ImageView qrcode_iv = (ImageView) layout.findViewById(R.id.qrcode_iv);
        Button qrcode_copy_bt = (Button) layout.findViewById(R.id.qrcode_copy_bt);
        TextView qrcode_hash_tv = (TextView) layout.findViewById(R.id.qrcode_hash_tv);
        try {
            qrcode_hash_tv.setText(walletData.getAddress());
            Bitmap qrcode = ZxingUtils.encodeAsBitmap(walletData.getAddress(), 400,400);
            qrcode_iv.setImageBitmap(qrcode);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(getActivity(),"二维码生成失败,请刷新页面", Toast.LENGTH_LONG).show();
        }
        qrcode_copy_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipboardManager cm = (ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                cm.setPrimaryClip(ClipData.newPlainText(null, walletData.getWalletFile().toString()));
                Toast.makeText(getActivity(), "复制成功", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
