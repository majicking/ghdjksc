package com.guohanhealth.shop;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.guohanhealth.shop.newpackage.ProgressDialog;

import butterknife.ButterKnife;


/**
 * Created by dqw on 2015/5/25.
 */
public class BaseActivity extends Activity {
    protected ImageButton btnBack;
    protected TextView tvCommonTitle;
    protected TextView tvCommonTitleBorder;
    private LinearLayout llListEmpty;
    public Activity mActivity;
    public Dialog loadingDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mActivity = this;
        ButterKnife.bind(this);
    }

    public Dialog showProgressDialog(Context context, String string) {
        if (loadingDialog == null) {
            loadingDialog = ProgressDialog.showLoadingProgress(context, TextUtils.isEmpty(string) ? "正在加载..." : string);
        }
        return loadingDialog;
    }

    public void dismissProgressDialog() {
        ProgressDialog.dismissDialog(loadingDialog);
    }

    /**
     * 设置Activity通用标题文字
     */
    protected void setCommonHeader(String title) {
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        tvCommonTitle = (TextView) findViewById(R.id.tvCommonTitle);
        tvCommonTitleBorder = (TextView) findViewById(R.id.tvCommonTitleBorder);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tvCommonTitle.setText(title);
    }

    private Toast toast;

    public void showToast(String str) {
        if (toast == null) {
            toast = Toast.makeText(mActivity, str,
                    Toast.LENGTH_SHORT);
        } else {
            toast.setText(str);
        }
        toast.show();
    }

    /**
     * 空列表背景
     */
    protected void setListEmpty(int resId, String title, String subTitle) {
        llListEmpty = (LinearLayout) findViewById(R.id.llListEmpty);
        ImageView ivListEmpty = (ImageView) findViewById(R.id.ivListEmpty);
        ivListEmpty.setImageResource(resId);
        TextView tvListEmptyTitle = (TextView) findViewById(R.id.tvListEmptyTitle);
        TextView tvListEmptySubTitle = (TextView) findViewById(R.id.tvListEmptySubTitle);
        tvListEmptyTitle.setText(title);
        tvListEmptySubTitle.setText(subTitle);
    }

    /**
     * 显示空列表背景
     */
    protected void showListEmpty() {
        llListEmpty.setVisibility(View.VISIBLE);
    }

    /**
     * 隐藏空列表背景
     */
    protected void hideListEmpty() {
        llListEmpty.setVisibility(View.GONE);
    }

    /**
     * 隐藏返回按钮
     */
    protected void hideBack() {
        btnBack.setVisibility(View.INVISIBLE);
    }

    /**
     * 隐藏分隔线
     */
    protected void hideCommonHeaderBorder() {
        tvCommonTitleBorder.setVisibility(View.INVISIBLE);
    }

    public void initData(String json) {
    }

}
