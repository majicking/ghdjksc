package com.xinyuangongxiang.shop.ui.mine;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;

import com.xinyuangongxiang.shop.BaseActivity;
import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.MyExceptionHandler;

/**
 * 在线帮助界面
 * 
 * @author KingKong·HE
 * @Time 2014年2月14日 下午4:10:35
 * @E-mail hjgang@bizpoer.com
 */
public class HelpActivity extends BaseActivity {
	private WebView webviewHelp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.more_help);
		MyExceptionHandler.getInstance().setContext(this);
		webviewHelp = (WebView) findViewById(R.id.webviewHelp);
		webviewHelp.getSettings().setSupportZoom(true);
		webviewHelp.getSettings().setBuiltInZoomControls(true);
		WebSettings settings = webviewHelp.getSettings();
		settings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
		webviewHelp.loadUrl(Constants.URL_HELP);
		webviewHelp.setWebViewClient(new WebViewClient() {
			public boolean shouldOverrideUrlLoading(WebView view, String url) {
				view.loadUrl(url);
				return true;
			}

			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {
				super.onPageStarted(view, url, favicon);
			}

			@Override
			public void onPageFinished(WebView view, String url) {
				super.onPageFinished(view, url);
			}

			@Override
			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				view.loadUrl("file:///android_asset/error.html");
			}
		});

		setCommonHeader("在线帮助");
	}
}
