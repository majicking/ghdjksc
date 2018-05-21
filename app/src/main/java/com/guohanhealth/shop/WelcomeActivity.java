package com.guohanhealth.shop;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.guohanhealth.shop.R;

import com.guohanhealth.shop.common.MyExceptionHandler;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.common.StringUtils;
import com.guohanhealth.shop.common.T;
import com.guohanhealth.shop.newpackage.OrderActivity;
import com.guohanhealth.shop.ui.fenxiao.ZhiBoActivity;

import static com.guohanhealth.shop.common.Constants.ORDERNUMBER;
import static com.guohanhealth.shop.common.Constants.ORDERTYPE;

/**
 * 软件启动界面
 * @author KingKong-HE
 * @Time 2014-12-30
 * @Email KingKong@QQ.COM
 */
public class WelcomeActivity extends Activity {

	private int i;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.welcome_view);
		MyExceptionHandler.getInstance().setContext(this);

    }

	@Override
	protected void onStart() {
		super.onStart();
		onLoad();
		if(i>=2){
			finish();
		}
	}

	private void onLoad() {
		Intent intent = getIntent();
		String scheme = intent.getScheme();
		Uri uri = intent.getData();
		System.out.println("scheme:"+scheme);
		if (uri != null) {
			String host = uri.getHost();
			String dataString = intent.getDataString();
			String id = uri.getQueryParameter("id");
			String url = uri.getQueryParameter("url");
			String path = uri.getPath();
			String path1 = uri.getEncodedPath();
			String queryString = uri.getQuery();
			System.out.println("host:"+host);
			System.out.println("dataString:"+dataString);
			System.out.println("id:"+id);
			System.out.println("url:"+url);
			System.out.println("path:"+path);
			System.out.println("path1:"+path1);
			System.out.println("queryString:"+queryString);
			if(!StringUtils.isEmpty(id)){
				if (ShopHelper.isLogin(WelcomeActivity.this, MyShopApplication.getInstance().getLoginKey())) {
					Intent intent1 = new Intent(getApplicationContext(), ZhiBoActivity.class);
					intent1.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
					intent1.putExtra("live_id", id);
					intent1.putExtra("live_url", url);
					startActivity(intent1);
					finish();
				}else {
//					finish();
					i++;
					T.showShort(getApplicationContext(),"请登录查看");
				}

			}else {
				Welcome();
			}
		}else {
			Welcome();
		}
	}

	private void Welcome() {
		//加入定时器 睡眠 2000毫秒 自动跳转页面
		new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent it=new Intent();

//				it.setClass(WelcomeActivity.this,StartActivity.class);
                it.setClass(WelcomeActivity.this,MainFragmentManager.class);
//				it.putExtra(ORDERNUMBER,2);
//				it.putExtra(ORDERTYPE,true);
//				it.setClass(WelcomeActivity.this,OrderActivity.class);
                startActivity(it);
                WelcomeActivity.this.finish();
            }
        }, 1000);
	}
}
