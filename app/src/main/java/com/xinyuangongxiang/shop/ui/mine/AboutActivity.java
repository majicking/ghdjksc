package com.xinyuangongxiang.shop.ui.mine;

import android.os.Bundle;

import com.xinyuangongxiang.shop.BaseActivity;
import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.common.MyExceptionHandler;

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
