package com.xinyuangongxiang.shop.custom;

import com.xinyuangongxiang.shop.R;
import android.app.Dialog;
import android.content.Context;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;


/**
 * 等待对话框
 * 
 * @author HJGANG
 */
public class MyUpdateDialog extends Dialog {
	public Button btu_on;
	public Button btu_off;
	public ProgressBar progress;
	public TextView shuzhi;
	public MyUpdateDialog(Context context) {
		super(context, R.style.MyProgressDialog);
		this.setContentView(R.layout.my_update);

		btu_on = (Button) findViewById(R.id.btu_on);
		btu_off = (Button) findViewById(R.id.btu_off);
		progress = (ProgressBar) findViewById(R.id.progress);
		shuzhi = (TextView) findViewById(R.id.shuzhi);
	}
}
