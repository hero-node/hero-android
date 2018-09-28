package com.hero.signature.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.hero.HeroCustomCaptureActivity;
import com.hero.HeroDrawerActivity;
import com.hero.R;
import com.hero.depandency.google.zxing.integration.android.IntentIntegrator;
import com.hero.HeroFragment;
import com.hero.signature.entity.DataBean;
import com.hero.signature.entity.GridViewAdapter;
import com.hero.signature.entity.GridViewPageAdapter;

import org.json.JSONArray;
import org.json.JSONException;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by Aron on 2018/5/7.
 */
public class HeroSignatureHomeFragment extends HeroFragment {

    private EditText mEditText;

    private static final int ITEM_COUNT_OF_PAGE = 10;

    private static final int ITEM_COUNT_OF_LINE = 5;

    private View layout;

    private HeroSignatureHomeFragment.OnClickListener mOnClickListener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.hero_home_fragment, container, false);
        return layout;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (!(context instanceof HeroDrawerActivity)) return;

            mOnClickListener = (HeroSignatureHomeFragment.OnClickListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnClickListener");
        }

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        initView();
    }

    private void initView() {
        initPageGridView();
        initEditText();
        fakeButton();
        initMenuButton();
    }

    private void initEditText() {
        mEditText = (EditText) layout.findViewById(R.id.login_hash_ev);
        Drawable drawableRight = getResources().getDrawable(R.drawable.login_scanning_icon);
        drawableRight.setBounds(0, 0, 52, 52);
        mEditText.setCompoundDrawablePadding(16);
        mEditText.setCompoundDrawables(null, null, drawableRight, null);

        mEditText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (mEditText.getCompoundDrawables()[2] == null){
                    return false;
                }
                //这里一定要对点击事件类型做一次判断，否则你的点击事件会被执行2次
                if (event.getAction() != MotionEvent.ACTION_UP){
                    return false;
                } if (event.getX() > mEditText.getWidth() - mEditText.getCompoundDrawables()[2].getBounds().width()) {
                    startCaptureActivity();
                    return true;
                }
                return false;
            }
        });
    }

    private void startCaptureActivity() {
//        IntentIntegrator.forSupportFragment(this).initiateScan();
        new IntentIntegrator(getActivity()).setCaptureActivity(HeroCustomCaptureActivity.class)
                .setDesiredBarcodeFormats(IntentIntegrator.QR_CODE)// 扫码的类型,可选：一维码，二维码，一/二维码
                .setPrompt("将二维码放入框内，即可自动扫描")// 设置提示语
                .setCameraId(0)// 选择摄像头,可使用前置或者后置
                .setBeepEnabled(true)// 是否开启声音,扫完码之后会"哔"的一声
                .setBarcodeImageEnabled(false)// 扫完码之后生成二维码的图片
                .initiateScan();
    }

    private void initPageGridView () {
        ViewPager mViewPager = (ViewPager) layout.findViewById(R.id.icon_viewpager);
        ArrayList<DataBean> mDataList = new ArrayList<>();

        mDataList.add(new DataBean("Hero Node" ,R.drawable.login_heronode_icon," " ));
        mDataList.add(new DataBean("云币网" ,R.drawable.login_yunbi_icon," " ));
        mDataList.add(new DataBean("开眼看" ,R.drawable.login_kaiyankan1_icon," " ));
        mDataList.add(new DataBean("赛比我" ,R.drawable.login_saibiwo_icon," " ));
        mDataList.add(new DataBean("赛比我" ,R.drawable.login_saibiwo2_icon," " ));
        mDataList.add(new DataBean("Ehtereum" ,R.drawable.login_ehter_icon," " ));
        mDataList.add(new DataBean("云币网" ,R.drawable.login_yunbi2_icon," " ));
        mDataList.add(new DataBean("开眼看" ,R.drawable.login_kaiyankan_icon," " ));
        mDataList.add(new DataBean("赛比我" ,R.drawable.login_saibiwo4_icon," " ));
        mDataList.add(new DataBean("赛比我" ,R.drawable.login_saibiwo3_icon," " ));

        GridViewAdapter mGridViewAdapter = new GridViewAdapter(mDataList);

        List<View> gridViews = new ArrayList<View>();

        int count = mDataList.size() / ITEM_COUNT_OF_PAGE;
        if (mDataList.size() % ITEM_COUNT_OF_PAGE != 0) {
            count++;
        }

        List<DataBean> pageItem;
        for (int i = 0; i < count; i++) {
            GridView gridView = new GridView(getActivity());
            if(i == count - 1){
                pageItem = mDataList.subList(i * ITEM_COUNT_OF_PAGE, mDataList.size());
                GridViewAdapter adapter = new GridViewAdapter(pageItem);
                gridView.setAdapter(adapter);
            } else {
                pageItem = mDataList.subList(i * ITEM_COUNT_OF_PAGE, (i + 1) * ITEM_COUNT_OF_PAGE);
                GridViewAdapter adapter = new GridViewAdapter(pageItem);
                gridView.setAdapter(adapter);
            }
            gridView.setNumColumns(ITEM_COUNT_OF_LINE);
            gridViews.add(gridView);

        }

        GridViewPageAdapter mGridViewPageAdapter = new GridViewPageAdapter(gridViews);
        mViewPager.setAdapter(mGridViewPageAdapter);
    }


    private void fakeButton() {
        Button fakeButton = (Button) layout.findViewById(R.id.fake_button);
        fakeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if (mOnClickListener != null) {
//                    mOnClickListener.onSignClick();
//                }
                try {
                    JSONArray tabsArray = new JSONArray(getGotoCommand("test"));
                    on(tabsArray);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    private void initMenuButton() {
        ImageView imageViewMenu = (ImageView) layout.findViewById(R.id.home_menu_iv);
        imageViewMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                View contentView = LayoutInflater.from(getActivity()).inflate(R.layout.menu_popwindow, null, false);
                final PopupWindow popupWindow = new PopupWindow(contentView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupWindow.setOutsideTouchable(false);
                popupWindow.setTouchable(true);
                popupWindow.setAnimationStyle(R.style.PopMenuAnimTranslate);
                popupWindow.showAtLocation(contentView, Gravity.TOP,212,312);
                ImageView imageViewWallet = (ImageView) contentView.findViewById(R.id.home_wallet_iv);
                imageViewWallet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mOnClickListener != null) {
                            mOnClickListener.onWalletClick();
                            popupWindow.dismiss();
                        }
                    }
                });

                ImageView imageViewNode = (ImageView) contentView.findViewById(R.id.home_node_iv);
                imageViewNode.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Toast.makeText(getActivity(),"暂未开放",Toast.LENGTH_LONG).show();
                    }
                });


            }
        });
    }

    private void initButton() {
//        ImageButton imageButton = layout.findViewById(R.id.login_go);
        ImageButton imageButton = new ImageButton(getActivity());
        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hashcode = mEditText.getText().toString();
                if (hashcode != null && hashcode.length() > 0) {
                    try {
                        JSONArray tabsArray = new JSONArray(getGotoCommand(hashcode));
                        on(tabsArray);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(getActivity(),"请输入正确的Hash",Toast.LENGTH_LONG).show();
                }

            }
        });
    }

    public String getGotoCommand(String hashcode) {
//        String url = "http://106.14.187.240/_/ipfs/files/";
//        return "[{command:'goto:" + url + hashcode + "/#/'}]";
        return "[{command:'goto:" + "http://10.0.0.26:3000/home.html" + "'}]";
//        return "[{command:'goto:" + "http://10.0.0.35:3000/_/ipfs/files/" + "QmYeVNmPu7fHwwHZUiaGqxPo5mEHp2LXa6mTSJsjGcHE5Q" + "/#/'}]";
    }

    public interface OnClickListener {

        void onSignClick();

        void onWalletClick();

    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }

}
