package com.xinyuangongxiang.shop.common;

import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xinyuangongxiang.shop.xrefresh.utils.LogUtils;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class AppRegister extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		final IWXAPI msgApi = WXAPIFactory.createWXAPI(context, null);
		boolean isregister =msgApi.registerApp(Constants.APP_ID);
		LogUtils.i("isregister"+isregister);
	}
}
