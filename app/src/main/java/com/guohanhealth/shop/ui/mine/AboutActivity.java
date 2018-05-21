package com.guohanhealth.shop.ui.mine;

import android.os.Bundle;

import com.guohanhealth.shop.BaseActivity;
import com.guohanhealth.shop.R;
import com.guohanhealth.shop.common.MyExceptionHandler;

/**
 * 关于我们页面
 * @author KingKong-HE
 * @Time 2015-2-1
 * @Email KingKong@QQ.COM
 */
public class AboutActivity extends BaseActivity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_about);
		MyExceptionHandler.getInstance().setContext(this);
		setCommonHeader("关于我们");
	}
}
