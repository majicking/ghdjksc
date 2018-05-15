package com.xinyuangongxiang.shop.ui.type;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.common.MyExceptionHandler;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

/**
 * 选择规格界面
 * @author KingKong-HE
 * @Time 2015-1-6
 * @Email KingKong@QQ.COM
 */
public class SpecActivity extends Activity implements OnClickListener{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.spec_view);
		MyExceptionHandler.getInstance().setContext(this);
		ImageView imageBack = (ImageView) findViewById(R.id.imageBack);
		
		imageBack.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.imageBack:
			
			finish();
			
			break;

		default:
			break;
		}
	}
}
