package com.xinyuangongxiang.shop.custom;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.common.StringUtil;

/**
 * Created by wj on 2016/1/14.
 */
public class MyProgressDialog extends ProgressDialog {
    private AnimationDrawable mAnimation;
    private ImageView mImageView;
    private TextView mTextView;
    private String loadingTip;
    private int resid;
    private  Context context;
    /**
     * @param context 上下文对象
     * @param content 显示文字提示信息内容
     * @param resid   动画id
     */
    public MyProgressDialog(Context context, String content, int resid) {
        super(context);
        this.context=context;
        this.loadingTip = content;
        this.resid = resid;
        //点击提示框外面是否取消提示框
        setCanceledOnTouchOutside(false);
        //点击返回键是否取消提示框
        setCancelable(false);
        setIndeterminate(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.progress_dialog);
        mTextView = (TextView) findViewById(R.id.loadingTv);
        mImageView = (ImageView) findViewById(R.id.loadingIv);
        mImageView.setBackgroundResource(resid);
        // 通过ImageView对象拿到背景显示的AnimationDrawable
        mAnimation = (AnimationDrawable) mImageView.getBackground();
        mImageView.post(new Runnable() {
            @Override
            public void run() {
                mAnimation.start();
            }
        });
        mTextView.setText(loadingTip);
    }

}