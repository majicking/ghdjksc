package com.xinyuangongxiang.shop.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.pay.PayDemoActivity;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;
import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.bean.OrderGroupList;
import com.xinyuangongxiang.shop.custom.ShareButton;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.ResponseData;
import com.xinyuangongxiang.shop.ncinterface.DataCallback;
import com.xinyuangongxiang.shop.ui.mine.PayMentWebActivity;
import com.xinyuangongxiang.shop.xrefresh.utils.LogUtils;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {
    public static final String TAG = "PushDemoActivity";
    public static final String RESPONSE_METHOD = "method";
    public static final String RESPONSE_CONTENT = "content";
    public static final String RESPONSE_ERRCODE = "errcode";
    protected static final String ACTION_LOGIN = "com.baidu.pushdemo.action.LOGIN";
    public static final String ACTION_MESSAGE = "com.baiud.pushdemo.action.MESSAGE";
    public static final String ACTION_RESPONSE = "bccsclient.action.RESPONSE";
    public static final String ACTION_SHOW_MESSAGE = "bccsclient.action.SHOW_MESSAGE";
    protected static final String EXTRA_ACCESS_TOKEN = "access_token";
    public static final String EXTRA_MESSAGE = "message";

    public static String logStringCache = "";

    // 获取ApiKey
    public static String getMetaValue(Context context, String metaKey) {
        Bundle metaData = null;
        String apiKey = null;
        if (context == null || metaKey == null) {
            return null;
        }
        try {
            ApplicationInfo ai = context.getPackageManager()
                    .getApplicationInfo(context.getPackageName(),
                            PackageManager.GET_META_DATA);
            if (null != ai) {
                metaData = ai.metaData;
            }
            if (null != metaData) {
                apiKey = metaData.getString(metaKey);
            }
        } catch (NameNotFoundException e) {
            Log.e(TAG, "error " + e.getMessage());
        }
        return apiKey;
    }

    public static List<String> getTagsList(String originalText) {
        if (originalText == null || originalText.equals("")) {
            return null;
        }
        List<String> tags = new ArrayList<String>();
        int indexOfComma = originalText.indexOf(',');
        String tag;
        while (indexOfComma != -1) {
            tag = originalText.substring(0, indexOfComma);
            tags.add(tag);

            originalText = originalText.substring(indexOfComma + 1);
            indexOfComma = originalText.indexOf(',');
        }

        tags.add(originalText);
        return tags;
    }

    public static String getLogText(Context context) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        return sp.getString("log_text", "");
    }

    public static void setLogText(Context context, String text) {
        SharedPreferences sp = PreferenceManager
                .getDefaultSharedPreferences(context);
        Editor editor = sp.edit();
        editor.putString("log_text", text);
        editor.commit();
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics dm = context.getResources().getDisplayMetrics();
        return dm.heightPixels;
    }

    private static Pattern idcard = Pattern.compile("\\^(^[1-9]\\d{7}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])\\d{3}$)|(^[1-9]\\d{5}[1-9]\\d{3}((0\\d)|(1[0-2]))(([0|1|2]\\d)|3[0-1])((\\d{4})|\\d{3}[Xx])$)$");

    public static boolean check(Context context, String idCard) {
        if (idcard.matcher(idCard).matches()) {
            if (idCard.length() == 18) {
                //将前17位加权因子保存在数组里
                int[] idCardWi = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
                //这是除以11后，可能产生的11位余数、验证码，也保存成数组
                int[] idCardY = {1, 0, 10, 9, 8, 7, 6, 5, 4, 3, 2};
                //用来保存前17位各自乖以加权因子后的总和
                int idCardWiSum = 0;
                for (int i = 0; i < 17; i++) {
                    idCardWiSum += Integer.valueOf(idCard.substring(i, i + 1)) * idCardWi[i];
                }
                int idCardMod = idCardWiSum % 11;//计算出校验码所在数组的位置
                String idCardLast = idCard.substring(17);//得到最后一位身份证号码
                //如果等于2，则说明校验码是10，身份证号码最后一位应该是X
                if (idCardMod == 2) {
                    if (idCardLast.equalsIgnoreCase("X")) {
                        return true;
                    } else {
                        Log.i(TAG, "尾数验证错误 ");
                        T.showShort(context, "尾数验证错误");
                    }
                } else {
                    //用计算出的验证码与最后一位身份证号码匹配，如果一致，说明通过，否则是无效的身份证号码
                    if (idCardLast.equals(String.valueOf(idCardY[idCardMod]))) {
                        return true;
                    } else {
                        Log.i(TAG, "check: 身份证无效");
                        T.showShort(context, "身份证无效");
                    }
                }
            } else {
                Log.i(TAG, "长度不够 ");
                T.showShort(context, "长度不够");
            }
        } else {
            Log.i("TAG", "身份证格式错误");
            T.showShort(context, "身份证格式错误");
        }
        return false;
    }


    /**
     * 获取微信参数
     *
     * @param pay_sn 支付编号
     */
    public static void loadingWXPaymentData(final Context context, String pay_sn, String type) {
        if (type.equals("3")){
            RemoteDataHandler.asyncDataStringGet(Constants.WEIXINPAY_PREDBROADCAST+pay_sn+"&key="+MyShopApplication.getInstance().getLoginKey(),data -> {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String appid = jsonObject.getString("appid");// 微信开放平台appid
                        String noncestr = jsonObject
                                .getString("noncestr");// 随机字符串
                        String packageStr = jsonObject
                                .getString("package");// 支付内容
                        String partnerid = jsonObject
                                .getString("partnerid");// 财付通id
                        String prepayid = jsonObject
                                .getString("prepayid");// 微信预支付编号
                        String sign = jsonObject.getString("sign");// 签名
                        String timestamp = jsonObject
                                .getString("timestamp");// 时间戳

                        IWXAPI api = WXAPIFactory.createWXAPI(context, appid);

                        PayReq req = new PayReq();
                        req.appId = appid;
                        req.partnerId = partnerid;
                        req.prepayId = prepayid;
                        req.nonceStr = noncestr;
                        req.timeStamp = timestamp;
                        req.packageValue = packageStr;
                        req.sign = sign;
                        req.extData = "app data"; // optional
                        LogUtils.i("id="+req.appId);
//                        Toast.makeText(context, "正常调起支付",
//                                Toast.LENGTH_SHORT).show();
                        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
                        boolean result = api.sendReq(req);
                        LogUtils.i("唤起微信结果=" + result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    ShopHelper.showApiError(context,json);
                }
            });
            return;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("pay_sn", pay_sn);
          LogUtils.i( Constants.URL_MEMBER_WX_PAYMENT);
          LogUtils.i( MyShopApplication.getInstance().getLoginKey());
          LogUtils.i( pay_sn);

        RemoteDataHandler.asyncLoginPostDataString(
                type.equals("1") ? Constants.URL_MEMBER_WX_PAYMENT : Constants.URL_MEMBER_WX_VPAYMENT, params, MyShopApplication.getInstance(),
                data -> {
                    String json = data.getJson();
                    if (data.getCode() == HttpStatus.SC_OK) {
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String appid = jsonObject.getString("appid");// 微信开放平台appid
                            String noncestr = jsonObject
                                    .getString("noncestr");// 随机字符串
                            String packageStr = jsonObject
                                    .getString("package");// 支付内容
                            String partnerid = jsonObject
                                    .getString("partnerid");// 财付通id
                            String prepayid = jsonObject
                                    .getString("prepayid");// 微信预支付编号
                            String sign = jsonObject.getString("sign");// 签名
                            String timestamp = jsonObject
                                    .getString("timestamp");// 时间戳

                            IWXAPI api = WXAPIFactory.createWXAPI(context, appid);

                            PayReq req = new PayReq();
                            req.appId = appid;
                            req.partnerId = partnerid;
                            req.prepayId = prepayid;
                            req.nonceStr = noncestr;
                            req.timeStamp = timestamp;
                            req.packageValue = packageStr;
                            req.sign = sign;
                            req.extData = "app data"; // optional
                            LogUtils.i("id="+req.appId);
//                            Toast.makeText(context, "正常调起支付",
//                                    Toast.LENGTH_SHORT).show();
                            // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
                            boolean result = api.sendReq(req);
                            LogUtils.i("唤起微信结果=" + result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                    } else {
                        ShopHelper.showApiError(context,json);
                    }
                });
    }

    /**
     * 获取支付宝原生支付的参数act=member_payment_recharge&op=alipay_native_pay&pay_sn=&payment_code=alipay_native

     */
    public static void loadingAlipayNativePaymentData(final Context context, String pay_sn, String type, final DataCallback callback) {
        if (type.equals("3")){
            RemoteDataHandler.asyncDataStringGet(Constants.ALIPAY_MEMBERPAYMENTRECHARGE+pay_sn+"&key="+MyShopApplication.getInstance().getLoginKey(),data -> {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        String signStr = jsonObject.optString("signStr");
                        LogUtils.i(signStr);
                        String[] split = signStr.split("\\&");
                        String urls = "";
                        for (String s : split) {
                            if (s.contains("notify_url")) {
                                urls = s;
                                break;
                            }
                        }
                        MyShopApplication.getInstance().setNotify_ur(urls.split("\\=")[1].replace("\"", ""));
                        PayDemoActivity payDemoActivity = new PayDemoActivity(context, signStr, callback);
                        payDemoActivity.doPay();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(context, json);
                }
            });
            return;
        }

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("pay_sn", pay_sn);
        RemoteDataHandler.asyncLoginPostDataString(
                type.equals("1") ? Constants.URL_ALIPAY_NATIVE_GOODS : Constants.URL_ALIPAY_NATIVE_Virtual, params, MyShopApplication.getInstance(),
                data -> {
                    String json = data.getJson();
                    if (data.getCode() == HttpStatus.SC_OK) {
                        try {
                            JSONObject jsonObject = new JSONObject(json);
                            String signStr = jsonObject.optString("signStr");
                            LogUtils.i(signStr);
                            String[] split = signStr.split("\\&");
                            String urls = "";
                            for (String s : split) {
                                if (s.contains("notify_url")) {
                                    urls = s;
                                    break;
                                }
                            }
                            MyShopApplication.getInstance().setNotify_ur(urls.split("\\=")[1].replace("\"", ""));
                            PayDemoActivity payDemoActivity = new PayDemoActivity(context, signStr, callback);
                            payDemoActivity.doPay();
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        ShopHelper.showApiError(context, json);
                    }
                });
    }

    /**
     * 初始化popupwindow
     */
    public static PopupWindow initPopupWindow(final Context context, View mPopupWindowView, PopupWindow.OnDismissListener listener) {
        PopupWindow popupWindow;

        popupWindow = new PopupWindow(mPopupWindowView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
        popupWindow.setTouchable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
        popupWindow.update();
        popupWindow.setAnimationStyle(R.anim.popup_window_enter);
        popupWindow.setAnimationStyle(R.anim.popup_window_exit);
        popupWindow.setOnDismissListener(listener == null ? (PopupWindow.OnDismissListener) () -> backgroundAlpha(context, 1f) : listener);
        return popupWindow;
    }

    static class SetOnclickListener implements View.OnClickListener {

        public int id;
        Context context;
        String pay_sn;
        DataCallback callback;
        PopupWindow.OnDismissListener listener;
        String type;

        public SetOnclickListener(Context context, int clickId, String pay_sn, String type) {
            this.id = clickId;
            this.pay_sn = pay_sn;
            this.context = context;
            this.type = type;
        }

        public SetOnclickListener(Context context, int clickId, String pay_sn, String type, DataCallback callback, PopupWindow.OnDismissListener listener) {
            this.id = clickId;
            this.pay_sn = pay_sn;
            this.context = context;
            this.callback = callback;
            this.listener = listener;
            this.type = type;
        }

        @Override
        public void onClick(View view) {

            if (pop != null && pop.isShowing()) {
                pop.dismiss();
            } else {
                pop = Utils.initPopupWindow(context, view, listener);
                pop.dismiss();
            }
            Intent intent;
            switch (id) {
                case R.id.social_sb_wechat:// "微信"
//                    loadingWXPaymentData(Qbean.getOrder_sn());
                    loadingWXPaymentData(context, pay_sn, type);
                    break;
                case R.id.social_sb_zhifubao:// "获取支付宝原生支付的参数"
//                    loadingAlipayNativePaymentData(Qbean.getOrder_sn());
                    loadingAlipayNativePaymentData(context, pay_sn, type, callback);
                    break;

                case R.id.social_sb_wzhifubao:// "web支付宝
//                    intent = new Intent(context, PayMentWebActivity.class);
//                    intent.putExtra("order_sn", Qbean.getOrder_sn());
//                    context.startActivity(intent);
                    intent = new Intent(context,
                            PayMentWebActivity.class);
                    intent.putExtra("pay_sn", pay_sn);
                    context.startActivity(intent);
                    break;
              /*  case R.id.pay_miss:
                    if (pop != null) {
                        pop.dismiss();
                    } else {
                        pop = Utils.initPopupWindow(context, view,);
                        pop.dismiss();
                    }
                    break;*/
            }
        }
    }

    static PopupWindow pop;

    /**
     * 获取可用支付方式
     */
    public static void loadingPaymentListData(final DataCallback callback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        RemoteDataHandler.asyncLoginPostDataString(
                Constants.URL_ORDER_PAYMENT_LIST, params, MyShopApplication.getInstance(),
                data -> callback.data(data));
    }


    /**
     * 初始化 支付window
     */
    public static View initPayWindowView(Context context, int size, JSONArray arr, String pay_sn, String type, DataCallback callback, View.OnClickListener listener, PopupWindow.OnDismissListener dismissListener) {
        View mPopupWindowView = LayoutInflater.from(context).inflate(R.layout.es_activity_social_share, null);
        TextView pay_miss = (TextView) mPopupWindowView.findViewById(R.id.pay_miss);
        pay_miss.setOnClickListener(listener == null ? new SetOnclickListener(context, R.id.pay_miss, pay_sn, type) : listener);
        ShareButton social_share_sb_wechat = (ShareButton) mPopupWindowView.findViewById(R.id.social_sb_wechat);
        social_share_sb_wechat.setOnClickListener(new SetOnclickListener(context, R.id.social_sb_wechat, pay_sn, type));
        ShareButton social_share_sb_zhifubao = (ShareButton) mPopupWindowView.findViewById(R.id.social_sb_zhifubao);
        social_share_sb_zhifubao.setOnClickListener(new SetOnclickListener(context, R.id.social_sb_zhifubao, pay_sn, type, callback, dismissListener));
        ShareButton social_sb_wzhifubao = (ShareButton) mPopupWindowView.findViewById(R.id.social_sb_wzhifubao);
        social_sb_wzhifubao.setOnClickListener(new SetOnclickListener(context, R.id.social_sb_wzhifubao, pay_sn, type));
        try {
            for (int i = 0; i < size; i++) {
                String Values = arr.getString(i);
                HashMap<String, Object> map = new HashMap<String, Object>();
                if (Values.equals("wxpay")) {
                    social_share_sb_wechat.setVisibility(View.VISIBLE);
//                } else if (Values.equals("alipay")) {
//                    social_sb_wzhifubao.setVisibility(View.VISIBLE);
                } else if (Values.equals("alipay_native")) {//TODO Modify 支付宝原生支付
                    social_share_sb_zhifubao.setVisibility(View.VISIBLE);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mPopupWindowView;
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public static void backgroundAlpha(Context context, float bgAlpha) {
        WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        ((Activity) context).getWindow().setAttributes(lp);
    }
}
