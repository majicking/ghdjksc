package com.xinyuangongxiang.shop.custom;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.xinyuangongxiang.shop.R;

/**
 * 空列表背景
 *
 * dqw
 * 2015/8/25
 */
public class MyListEmpty extends FrameLayout{
    private ImageView ivListEmpty;
    private TextView tvListEmptyTitle;
    private TextView tvListEmptySubTitle;
    public MyListEmpty(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater.from(context).inflate(R.layout.my_list_empty, this);
        ivListEmpty = (ImageView) findViewById(R.id.ivListEmpty);
        tvListEmptyTitle = (TextView) findViewById(R.id.tvListEmptyTitle);
        tvListEmptySubTitle = (TextView) findViewById(R.id.tvListEmptySubTitle);
    }

    public void setListEmpty(int resId, String title, String subTitle) {
        ivListEmpty.setImageResource(resId);
        tvListEmptyTitle.setText(title);
        tvListEmptySubTitle.setText(subTitle);
    }
}
