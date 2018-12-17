package com.hero.signature.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hero.R;
import com.hero.utils.FileUtils;

import org.web3j.crypto.WalletFile;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


/**
 * Created by Aron on 2018/5/7.
 */
public class HeroSignatureWalletListFragment extends android.support.v4.app.Fragment {

    private View layout;

    private ListView listView;

    private MyWalletListAdapter myWalletListAdapter;

    private List<WalletData> walletDataList = new ArrayList<>();

    private HeroSignatureWalletListFragment.OnClickListener mOnClickListener;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (!(context instanceof FragmentActivity)) return;

            mOnClickListener = (HeroSignatureWalletListFragment.OnClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnClickListener");
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.hero_home_fragment, container, false);
        return layout;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {
        ArrayList<File> files = FileUtils.getKeystroeFilesWithoutDefault();
        initData(files);
        listView = (ListView) layout.findViewById(R.id.wallet_item_lv);
        myWalletListAdapter = new MyWalletListAdapter(getContext(), walletDataList);
        listView.setAdapter(myWalletListAdapter);

        layout.findViewById(R.id.wallet_item_add_bt).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mOnClickListener.onImportClick();
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mOnClickListener.onItemClick(walletDataList.get(position));
            }
        });
    }

    private void initData(ArrayList<File> files) {
        walletDataList.clear();
        for (int i = 0; i < files.size(); i++) {
            WalletData walletData = new WalletData();
            walletData.setImageid(R.drawable.wallet_icon);
            String fileName = files.get(i).getName();
            // 设置钱包名
            walletData.setName(fileName);
            try {
                String walletString = FileUtils.getKeystoreFilecontent(fileName);
                ObjectMapper mapper = new ObjectMapper();
                WalletFile walletFile = mapper.readValue(walletString, WalletFile.class);
                walletData.setWalletFile(walletFile);
                walletData.address = walletData.getWalletFile().getAddress();
            } catch (IOException e) {
                e.printStackTrace();
            }
            walletDataList.add(walletData);
        }
    }

    public class WalletData implements Serializable {

        private String name;

        private String address;

        private int imageid;

        private WalletFile walletFile;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getAddress() {
            return address;
        }

        public void setAddress(String address) {
            this.address = address;
        }

        public int getImageid() {
            return imageid;
        }

        public void setImageid(int imageid) {
            this.imageid = imageid;
        }

        public WalletFile getWalletFile() {
            return walletFile;
        }

        public void setWalletFile(WalletFile walletFile) {
            this.walletFile = walletFile;
        }
    }


    private class MyWalletListAdapter extends BaseAdapter {
        private Context context;
        private List<WalletData> list;

        public MyWalletListAdapter(Context context, List<WalletData> list) {
            this.context = context;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list == null ? 0 : list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup viewGroup) {
            MyViewHolder holder = null;
            if (convertView == null) {
                convertView = LayoutInflater.from(context).inflate(R.layout.wallet_item,viewGroup,false);
                holder = new MyViewHolder();
                holder.iv = (ImageView) convertView.findViewById(R.id.wallet_item_imageView);
                holder.tv = (TextView) convertView.findViewById(R.id.wallet_item_textView);
                holder.tvdetail = (TextView) convertView.findViewById(R.id.wallet_item_textViewDetail);
                convertView.setTag(holder);
            } else {
                holder = (MyViewHolder) convertView.getTag();
            }
            WalletData data = (WalletData) getItem(position);
            holder.iv.setImageResource(R.drawable.wallet_icon);
            holder.tv.setText("钱包" + (position + 1));
            holder.tvdetail.setText(data.getAddress());
            return convertView;
        }

        public class MyViewHolder {
            ImageView iv;
            TextView tv;
            TextView tvdetail;
        }
    }


    public interface OnClickListener {

        void onImportClick();

        void onItemClick(WalletData walletData);

    }


}
