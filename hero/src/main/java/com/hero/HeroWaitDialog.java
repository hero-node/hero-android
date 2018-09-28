package com.hero;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;

/**
 * Created by Aron on 2018/8/14.
 */
public class HeroWaitDialog extends Dialog {

    private Context context;

    public HeroWaitDialog(@NonNull Context context) {
        super(context, R.style.CustomWaitDialog);
        this.context = context;
    }

    public HeroWaitDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
        this.context = context;
    }

    protected HeroWaitDialog(@NonNull Context context, boolean cancelable, @Nullable OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.context = context;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = View.inflate(context, R.layout.hero_wait_dialog,null);
        setContentView(view);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
    }

    @Override
    protected void onStop() {
        super.onStop();
    }
}
