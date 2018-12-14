package com.hero.signature.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.hero.R;


/**
 * Created by Aron on 2018/5/7.
 */
public class HeroSignatureHomeFragment extends android.support.v4.app.Fragment {


    private View layout;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        layout = inflater.inflate(R.layout.hero_home_fragment, container, false);
        return layout;
    }

}
