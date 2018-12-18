package com.hero.signature.fragment;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hero.R;
import com.hero.signature.Constants;
import com.hero.signature.HeroSignatureActivity;
import com.hero.utils.FileUtils;
import com.hero.utils.ShareUtils;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Wallet;
import org.web3j.crypto.WalletFile;
import org.web3j.utils.Numeric;

import java.io.File;
import java.lang.ref.WeakReference;

/**
 * Created by Aron on 2018/7/24.
 */
public class HeroSignatureWalletFragment extends android.support.v4.app.Fragment {

    private View layout;

    private HeroSignatureWalletFragment.OnClickListener mOnClickListener;

    private HeroSignatureWalletListFragment.WalletData walletData;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (!(context instanceof FragmentActivity)) return;

            mOnClickListener = (HeroSignatureWalletFragment.OnClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnClickListener");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.hero_signature_wallet_fragment, container, false);
        return layout;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        walletData = (HeroSignatureWalletListFragment.WalletData) getArguments().getSerializable("walletData");
    }

    @Override
    public void onResume() {
        super.onResume();
        initView();
    }

    private void initView() {
        initHash();
        initExport();
        initModifyPassword();
        initDefaultWallet();
    }

    private void initDefaultWallet() {
        ShareUtils shareUtils = ShareUtils.getInstance(getContext());
        String defaultWallet = shareUtils.getString("default", "");
        if (defaultWallet.equals(walletData.getName())) {
            ((Switch)layout.findViewById(R.id.wallet_export_default_sw)).setChecked(true);
            layout.findViewById(R.id.wallet_export_default_sw).setClickable(false);
        } else {
            ((Switch)layout.findViewById(R.id.wallet_export_default_sw)).setChecked(false);
            ((Switch)layout.findViewById(R.id.wallet_export_default_sw)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(final CompoundButton buttonView, boolean isChecked) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                    View view = View.inflate(getActivity(), R.layout.hero_confirm_dialog, null);
                    TextView content = (TextView) view.findViewById(R.id.confirm_dialog_content);
                    TextView buttonConfirm = (TextView) view.findViewById(R.id.confirm_dialog_confirm);
                    TextView buttonCancel = (TextView) view.findViewById(R.id.confirm_dialog_cancel);
                    view.findViewById(R.id.confirm_dialog_password_et).setVisibility(View.GONE);
                    view.findViewById(R.id.confirm_dialog_ok).setVisibility(View.GONE);
                    content.setText("是否设置为默认钱包");
                    builder.setView(view);
                    // 创建对话框
                    final AlertDialog alertDialog = builder.create();
                    alertDialog.setCanceledOnTouchOutside(false);
                    buttonConfirm.setOnClickListener(new View.OnClickListener() {

                        @Override
                        public void onClick(View v) {
                            new MyDefaultTask((HeroSignatureActivity) getActivity(), walletData.getName()).execute();
                            buttonView.setChecked(true);
                            alertDialog.dismiss();
                        }

                    });
                    buttonCancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            buttonView.setChecked(false);
                            alertDialog.dismiss();
                        }
                    });
                    alertDialog.show();
                }
            });
        }
    }

    private void initExport() {
        TextView wallet_export_keystore_tv = (TextView) layout.findViewById(R.id.wallet_export_keystore_tv);

        wallet_export_keystore_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickListener.onExportKeystoreClick();
            }
        });

        TextView wallet_export_privatekey_tv = (TextView)layout.findViewById(R.id.wallet_export_privatekey_tv);
        wallet_export_privatekey_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View view = View.inflate(getActivity(), R.layout.hero_confirm_dialog, null);
                TextView content = (TextView) view.findViewById(R.id.confirm_dialog_content);
                TextView buttonConfirm = (TextView) view.findViewById(R.id.confirm_dialog_confirm);
                TextView buttonCancel = (TextView) view.findViewById(R.id.confirm_dialog_cancel);
                final EditText editTextPassword = (EditText) view.findViewById(R.id.confirm_dialog_password_et);
                view.findViewById(R.id.confirm_dialog_password_et).setVisibility(View.VISIBLE);
                view.findViewById(R.id.confirm_dialog_ok).setVisibility(View.GONE);
                content.setText("请输入密码");
                builder.setView(view);
                // 创建对话框
                final AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                buttonConfirm.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        String password = editTextPassword.getText().toString();
                        if (password == null || password.equals("")) {
                            Toast.makeText(getActivity(), "请输入密码", Toast.LENGTH_LONG).show();
                            return;
                        }
                        alertDialog.dismiss();
                        new MyWalletTask((HeroSignatureActivity) getActivity(), walletData.getWalletFile().toString(), password).execute();
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
        });

    }

    private static class MyWalletTask extends AsyncTask {

        private WeakReference<HeroSignatureActivity> activityReference;

        private String walletFileString;

        private String passwordString;

        // only retain a weak reference to the activity
        MyWalletTask(HeroSignatureActivity context, String walletFileString, String passwordString) {
            activityReference = new WeakReference<>(context);
            this.walletFileString = walletFileString;
            this.passwordString = passwordString;
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
                showPopupWindow(bundle.getString("key"));
            }
            super.onPostExecute(o);
        }


        private void showPopupWindow(String privatekey) {
            View contentView = LayoutInflater.from(activityReference.get()).inflate(R.layout.hero_export_popwindow, null, false);
            final TextView export_privatekey_tv = (TextView) contentView.findViewById(R.id.export_privatekey_tv);
            try {
                export_privatekey_tv.setText(privatekey);
                export_privatekey_tv.setMovementMethod(ScrollingMovementMethod.getInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
            Button export_copykeyystore_bt = (Button) contentView.findViewById(R.id.export_copyprivatekey_bt);
            export_copykeyystore_bt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ClipboardManager cm = (ClipboardManager) activityReference.get().getSystemService(Context.CLIPBOARD_SERVICE);
                    cm.setPrimaryClip(ClipData.newPlainText(null, export_privatekey_tv.getText().toString()));
                    Toast.makeText(activityReference.get(), "复制成功", Toast.LENGTH_LONG).show();
                }
            });

            PopupWindow popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
            popupWindow.setBackgroundDrawable(new ColorDrawable(Color.WHITE));
            // 设置PopupWindow是否能响应外部点击事件
            popupWindow.setOutsideTouchable(false);
            popupWindow.setTouchable(true);
            popupWindow.setAnimationStyle(R.style.ActionSheetDialogAnimation);
            popupWindow.showAtLocation(contentView, Gravity.BOTTOM,0,0);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Bundle bundle = new Bundle();
            try {
                ObjectMapper mapper = new ObjectMapper();
                WalletFile walletFile = mapper.readValue(walletFileString, WalletFile.class);

                ECKeyPair keyPair = Wallet.decrypt(passwordString, walletFile);
                if (keyPair != null && keyPair.getPrivateKey() != null
                        && keyPair.getPublicKey() != null) {
                    bundle.putString("key", Numeric.toHexStringNoPrefix(keyPair.getPrivateKey()));
                } else {
                    throw new CipherException("密码错误");
                }
                bundle.putBoolean("isSucceed", true);
                bundle.putString("message", "密码正确");
            }catch (CipherException ce) {
                bundle.putBoolean("isSucceed", false);
                bundle.putString("message", "密码错误");
                ce.printStackTrace();
            }
            catch (Exception e) {
                bundle.putBoolean("isSucceed", false);
                bundle.putString("message", "处理错误");
                e.printStackTrace();
            }
            return bundle;
        }
    }

    private void initHash() {
        ImageView wallet_hash_qrcode_iv = (ImageView) layout.findViewById(R.id.wallet_hash_qrcode_iv);
        TextView  wallet_hash_tv = (TextView) layout.findViewById(R.id.wallet_hash_tv);
        wallet_hash_tv.setText(walletData.getWalletFile().getAddress());


        wallet_hash_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickListener.onQrcodeClick();
            }
        });
        wallet_hash_qrcode_iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickListener.onQrcodeClick();
            }
        });

        Button wallet_home_delete_bt = (Button) layout.findViewById(R.id.wallet_home_delete_bt);
        wallet_home_delete_bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                View view = View.inflate(getActivity(), R.layout.hero_confirm_dialog, null);
                TextView content = (TextView) view.findViewById(R.id.confirm_dialog_content);
                TextView buttonConfirm = (TextView) view.findViewById(R.id.confirm_dialog_confirm);
                TextView buttonCancel = (TextView) view.findViewById(R.id.confirm_dialog_cancel);
                view.findViewById(R.id.confirm_dialog_password_et).setVisibility(View.GONE);
                view.findViewById(R.id.confirm_dialog_ok).setVisibility(View.GONE);
                content.setText("是否删除钱包");
                builder.setView(view);
                // 创建对话框
                final AlertDialog alertDialog = builder.create();
                alertDialog.setCanceledOnTouchOutside(false);
                buttonConfirm.setOnClickListener(new View.OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                        new MyTask((HeroSignatureActivity) getActivity(), walletData.getName(), walletData.getAddress()).execute();
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
        });
    }

    private static class MyTask extends AsyncTask {

        private WeakReference<HeroSignatureActivity> activityReference;

        private String fileName;

        private String address;

        // only retain a weak reference to the activity
        MyTask(HeroSignatureActivity context, String walletName, String address) {
            this.activityReference = new WeakReference<>(context);
            this.fileName = walletName;
            this.address = address;
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
            super.onPostExecute(o);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Bundle bundle = new Bundle();
            try {
                ShareUtils shareUtils = ShareUtils.getInstance(activityReference.get());
                File keystoreFile = FileUtils.getKeystoreFile(fileName);
                if (keystoreFile.exists()) {
                    keystoreFile.delete();
                }
                //如果是默认钱包的话一起删除默认钱包
                if (shareUtils.getString("default","").equals(fileName)) {
                    shareUtils.remove("default");
                    File defaultFile = FileUtils.getKeystoreFile("default");
                    if (defaultFile.exists()) {
                        defaultFile.delete();
                    }
                }

                if (shareUtils.contains("fingerprint")) {
                    String name = shareUtils.getString("fingerprint","");
                    if (name.contains("," + fileName)) {
                        name.replace("," + fileName,"");
                    } else if (name.contains(fileName)){
                        name.replace(fileName,"");
                    }
                    shareUtils.putString("fingerprint", name);
                }

                shareUtils.remove(fileName);
                shareUtils.remove(address);
                File hintFile = FileUtils.getHintFile(fileName.replace(".json",".txt").replace("Keystore","Hint"));

                if (hintFile.exists()) {
                    hintFile.delete();
                }
                bundle.putBoolean("isSucceed", true);
                bundle.putString("message", "钱包删除成功");
            } catch (Exception e) {
                bundle.putBoolean("isSucceed", false);
                bundle.putString("message", "钱包删除失败");
                e.printStackTrace();
            }
            return bundle;
        }
    }

    private void initModifyPassword() {
        TextView wallet_modify_password_tv = (TextView) layout.findViewById(R.id.wallet_modify_password_tv);

        wallet_modify_password_tv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickListener.onModifyClick(walletData);
            }
        });
    }

    public interface OnClickListener {

        void onModifyClick(HeroSignatureWalletListFragment.WalletData walletData);

        void onExportKeystoreClick();

        void onQrcodeClick();

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

    private static class MyDefaultTask extends AsyncTask {

        private WeakReference<HeroSignatureActivity> activityReference;

        private String fileName;

        // only retain a weak reference to the activity
        MyDefaultTask(HeroSignatureActivity context, String walletName) {
            this.activityReference = new WeakReference<>(context);
            this.fileName = walletName;
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
            super.onPostExecute(o);
        }

        @Override
        protected Object doInBackground(Object[] objects) {
            Bundle bundle = new Bundle();
            try {
                ShareUtils shareUtils = ShareUtils.getInstance(activityReference.get());
                //设置默认钱包
                if (!shareUtils.getString("default","").equals(fileName)) {
                    shareUtils.remove("default");
                    shareUtils.putString("default", fileName);
                    File defaultFile = FileUtils.getKeystoreFile("default");
                    if (defaultFile.exists()) {
                        defaultFile.delete();
                    }
                    FileUtils.copyFile(Constants.KEYSTORE_FILE_PATH + fileName,
                            Constants.KEYSTORE_FILE_PATH + "default.json");
                }

                bundle.putBoolean("isSucceed", true);
                bundle.putString("message", "设置默认钱包成功");
            } catch (Exception e) {
                bundle.putBoolean("isSucceed", false);
                bundle.putString("message", "设置默认钱包失败");
                e.printStackTrace();
            }
            return bundle;
        }
    }

}
