
package com.guohanhealth.shop.wxapi;

import android.os.Bundle;

import com.umeng.socialize.weixin.view.WXCallbackActivity;
import com.guohanhealth.shop.xrefresh.utils.LogUtils;

public class WXEntryActivity extends WXCallbackActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            LogUtils.i("------");
            super.onCreate(savedInstanceState);
        }catch (Exception e){
            finish();
        }
    }
}