package com.guohanhealth.shop.common;

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
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.*;
import android.view.inputmethod.InputMethodManager;
import android.widget.*;

import com.alipay.sdk.app.PayTask;
import com.alipay.sdk.pay.PayDemoActivity;
import com.alipay.sdk.pay.PayResult;
import com.google.gson.JsonSyntaxException;
import com.guohanhealth.shop.R;
import com.guohanhealth.shop.bean.PayData;
import com.guohanhealth.shop.bean.PayInfos;
import com.guohanhealth.shop.bean.PayWayInfo;
import com.guohanhealth.shop.bean.WxPayInfo;
import com.guohanhealth.shop.custom.CustomDialog;
import com.guohanhealth.shop.custom.ShareButton;
import com.guohanhealth.shop.http.ObjectEvent;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.ResponseData;
import com.guohanhealth.shop.http.RxBus;
import com.guohanhealth.shop.ncinterface.CallBack;
import com.guohanhealth.shop.ncinterface.DataCallback;
import com.guohanhealth.shop.newpackage.OrderActivity;
import com.guohanhealth.shop.ui.mine.BindMobileActivity;
import com.guohanhealth.shop.ui.mine.ModifyPaypwdStep1Activity;
import com.guohanhealth.shop.ui.mine.PayMentWebActivity;
import com.guohanhealth.shop.ui.type.BuyStep1Activity;
import com.guohanhealth.shop.xrefresh.utils.LogUtils;
import com.tencent.mm.sdk.modelpay.PayReq;
import com.tencent.mm.sdk.openapi.IWXAPI;
import com.tencent.mm.sdk.openapi.WXAPIFactory;

import com.zcw.togglebutton.ToggleButton;
import okhttp3.*;
import okio.BufferedSink;
import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Pattern;

import static com.guohanhealth.shop.common.Constants.ORDERNUMBER;
import static com.guohanhealth.shop.common.Constants.ORDERTYPE;

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
        if (type.equals("3")) {
            RemoteDataHandler.asyncDataStringGet(Constants.WEIXINPAY_PREDBROADCAST + pay_sn + "&key=" + MyShopApplication.getInstance().getLoginKey(), data -> {
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
                        LogUtils.i("id=" + req.appId);
//                        Toast.makeText(context, "正常调起支付",
//                                Toast.LENGTH_SHORT).show();
                        // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
                        boolean result = api.sendReq(req);
                        LogUtils.i("唤起微信结果=" + result);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    RxBus.getDefault().post(new ObjectEvent(Utils.getValue("error", json)));
//                    ShopHelper.showApiError(context, json);
                }
            });
            return;
        }
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("pay_sn", pay_sn);
        LogUtils.i(Constants.URL_MEMBER_WX_PAYMENT);
        LogUtils.i(MyShopApplication.getInstance().getLoginKey());
        LogUtils.i(pay_sn);

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
                            LogUtils.i("id=" + req.appId);
//                            Toast.makeText(context, "正常调起支付",
//                                    Toast.LENGTH_SHORT).show();
                            // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
                            boolean result = api.sendReq(req);
                            LogUtils.i("唤起微信结果=" + result);
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    } else {
                        ShopHelper.showApiError(context, json);
                    }
                });
    }

    /**
     * 获取支付宝原生支付的参数act=member_payment_recharge&op=alipay_native_pay&pay_sn=&payment_code=alipay_native
     */
    public static void loadingAlipayNativePaymentData(final Context context, String pay_sn, String type, final DataCallback callback) {
        if (type.equals("3")) {
            RemoteDataHandler.asyncDataStringGet(Constants.ALIPAY_MEMBERPAYMENTRECHARGE + pay_sn + "&key=" + MyShopApplication.getInstance().getLoginKey(), data -> {
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
//                        PayDemoActivity payDemoActivity = new PayDemoActivity(context, signStr, callback);
//                        payDemoActivity.doPay();
                        PayTask alipay = new PayTask((Activity) context);
                        String result = alipay.pay(signStr, true);
                        RxBus.getDefault().post(new PayResult(result));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    RxBus.getDefault().post(new ObjectEvent(Utils.getValue("error", json)));
//                    ShopHelper.showApiError(context, json);
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
                        } catch (Exception e) {
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
     * 获取可用支付方式
     */
    public static void getPaymentListData(String pay_sn, final DataCallback callback) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("pay_sn", pay_sn);
        RemoteDataHandler.asyncLoginPostDataString(
                Constants.MEMBER_BUY, params, MyShopApplication.getInstance(),
                data -> callback.data(data));
    }

    public static boolean isUseHealth = false;
    public static boolean isUserPre = false;
    public static boolean isUsePrc = false;
    public static String codetype = "payment_code";
    public static Map<String, String> payment_code = new HashMap<>();
    public static PayData paydata;

    private static boolean isWxAppInstalledAndSupported(Context context) {
        IWXAPI wxApi = WXAPIFactory.createWXAPI(context, null);
        wxApi.registerApp(Constants.APP_ID);
        boolean bIsWXAppInstalledAndSupported = wxApi.isWXAppInstalled() && wxApi.isWXAppSupportAPI();
        return bIsWXAppInstalledAndSupported;
    }

    /**
     * 键盘开关
     */
    public static void openOrcloseInput(Context context, View view, boolean isopen) {
        if (!isopen) {
            // 弹出软键盘
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(view, 0);
        } else {
            InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    public static PopupWindow shopPayWindown(Context context, PayData data, String type, View.OnClickListener closelistener, CallBack callBack) {
        paydata = data;
        isUseHealth = false;
        isUsePrc = false;
        isUserPre = false;
        payment_code.put("payment_code", "alipay");
        View view = LayoutInflater.from(context).inflate(R.layout.payway, null);
        ImageView close = (ImageView) view.findViewById(R.id.close);
        close.setOnClickListener(closelistener);
        View alipay = view.findViewById(R.id.alipay);
        View wxpay = view.findViewById(R.id.wxpay);
        View emptyview = view.findViewById(R.id.emptyview);

        View viewpay1 = view.findViewById(R.id.viewpay1);
        View viewpay2 = view.findViewById(R.id.viewpay2);
        View viewpay3 = view.findViewById(R.id.viewpay3);
        View viewpay4 = view.findViewById(R.id.viewpay4);
        View viewpay5 = view.findViewById(R.id.viewpay5);

        ToggleButton toggle1 = (ToggleButton) view.findViewById(R.id.toggle1);
        ToggleButton toggle2 = (ToggleButton) view.findViewById(R.id.toggle2);
        ToggleButton toggle3 = (ToggleButton) view.findViewById(R.id.toggle3);

        TextView number1 = (TextView) view.findViewById(R.id.number1);
        TextView number2 = (TextView) view.findViewById(R.id.number2);
        TextView number3 = (TextView) view.findViewById(R.id.number3);

        TextView text1 = (TextView) view.findViewById(R.id.text1);
        TextView text2 = (TextView) view.findViewById(R.id.text2);
        TextView text3 = (TextView) view.findViewById(R.id.text3);

        TextView money = (TextView) view.findViewById(R.id.money);

        EditText pwd = (EditText) view.findViewById(R.id.pwd);
        TextView nosetpwd = (TextView) view.findViewById(R.id.nosetpwd);
        money.setText(data.pay_info.pay_amount);
        if (!isUsePrc && !isUserPre && !isUseHealth) {//显示密码框
            viewpay4.setVisibility(View.GONE);
        } else {
            viewpay4.setVisibility(View.VISIBLE);
        }
        if (data.pay_info.member_paypwd) { //是否设置支付密码 显示输入密码框
            nosetpwd.setVisibility(View.GONE);
            pwd.setVisibility(View.VISIBLE);
        } else {
            nosetpwd.setVisibility(View.VISIBLE);
            pwd.setVisibility(View.GONE);
        }
        alipay.setSelected(true);
        boolean isOnlineAli = false;
        boolean isOnlineWx = false;


        boolean isShowPrc = false;
        boolean isShowPre = false;
        boolean isShowHealth = false;


        List<PayData.PayInfo.DataBean> paylist = data.pay_info.payment_list;
        if (data != null && data.pay_info != null && paylist != null && paylist.size() > 0) {
            for (int i = 0; i < paylist.size(); i++) {
                int index = i;
                if (paylist.get(i).payment_code.equals("alipay")) {/**支付宝*/
                    if (paylist.get(i).payment_status.equals("1")) {
                        alipay.setVisibility(View.VISIBLE);
                        isOnlineAli = true;
                    } else {
                        isOnlineAli = false;
                        alipay.setVisibility(View.GONE);
                    }

                    alipay.setOnClickListener(v -> {

                        wxpay.setSelected(false);
                        payment_code.put(codetype, paylist.get(index).payment_code);
                        alipay.setSelected(true);
                    });
                }
                if (paylist.get(i).payment_code.equals("wxpay")) {/**微信*/
                    if (paylist.get(i).payment_status.equals("1")) {
                        isOnlineWx = true;
                        wxpay.setVisibility(View.VISIBLE);
                    } else {
                        isOnlineWx = false;
                        wxpay.setVisibility(View.GONE);
                    }
                    wxpay.setOnClickListener(v -> {
                        alipay.setSelected(false);
                        wxpay.setSelected(true);
                        payment_code.put(codetype, paylist.get(index).payment_code);

                    });
                }
                heal:
                if (paylist.get(i).payment_code.equals("healthbean_allow")) {/**健康豆*/
                    if (paylist.get(i).payment_status.equals("1")) {
                        viewpay1.setVisibility(View.VISIBLE);
                        isShowHealth = true;
                        number1.setText("可用健康豆余额 ￥" + data.pay_info.member_available_healthbean);
                        text1.setText(paylist.get(i).payment_name);
                        toggle1.setOnToggleChanged(on -> {
                            isUseHealth = on;
                            if (isUsePrc || isUserPre || isUseHealth) {
                                viewpay4.setVisibility(View.VISIBLE);
                            } else {
                                viewpay4.setVisibility(View.INVISIBLE);
                            }
                        });
                        break heal;
                    }
                    isShowHealth = false;
                    viewpay1.setVisibility(View.GONE);
                }
                pre:
                if (paylist.get(i).payment_code.equals("predeposit_allow")) {/**充值卡*/
                    if (paylist.get(i).payment_status.equals("1")) {
                        isShowPrc = true;
                        viewpay2.setVisibility(View.VISIBLE);
                        number2.setText("可用充值卡余额 ￥" + data.pay_info.member_available_pd);
                        text2.setText(paylist.get(i).payment_name);
                        toggle2.setOnToggleChanged(on -> {
                            isUserPre = on;
                            if (isUsePrc || isUserPre || isUseHealth) {
                                viewpay4.setVisibility(View.VISIBLE);
                            } else {
                                viewpay4.setVisibility(View.INVISIBLE);
                            }

                        });
                        break pre;
                    }
                    isShowPrc = false;
                    viewpay2.setVisibility(View.GONE);
                }
                prd:
                if (paylist.get(i).payment_code.equals("rc_balance_allow")) {/**预存款*/
                    if (paylist.get(i).payment_status.equals("1")) {
                        isShowPre = true;
                        viewpay3.setVisibility(View.VISIBLE);
                        number3.setText("可用预存款余额 ￥" + data.pay_info.member_available_rcb);
                        text3.setText(paylist.get(i).payment_name);
                        toggle3.setOnToggleChanged(on -> {
                            isUsePrc = on;
                            if (isUsePrc || isUserPre || isUseHealth) {
                                viewpay4.setVisibility(View.VISIBLE);
                            } else {
                                viewpay4.setVisibility(View.INVISIBLE);
                            }
                        });
                        break prd;
                    }
                    isShowPre = false;
                    viewpay3.setVisibility(View.GONE);
                }

            }
        }

        boolean isOnlinePay = false;/**是否存在在线支付*/
        if (!isOnlineWx && !isOnlineAli) {/**如果没有在线支付*/
            viewpay5.setVisibility(View.GONE);
            isOnlinePay = false;

        } else {
            isOnlinePay = true;
            viewpay5.setVisibility(View.VISIBLE);
        }

        boolean isLocaPay = false;/**是否存在站内支付*/
        if (!isShowHealth && !isShowPrc && !isShowPre) {/**如果没有站内支付*/
            isLocaPay = false;
        } else {
            isLocaPay = true;
        }

        Button pay = (Button) view.findViewById(R.id.pay);
        boolean onlinepay = isOnlinePay;
        boolean locapay = isLocaPay;

        pay.setOnClickListener(v -> {
            if (!onlinepay && locapay) {//只有站内支付
                if (!isUsePrc && !isUserPre && !isUseHealth) {
                    T.showShort(context, "请选择支付方式");
                    return;
                }
                if (!data.pay_info.member_paypwd) {
                    T.showShort(context, "未设置支付密码");
                    loadMobile(context);
                    return;
                } else {
                    if (isUsePrc || isUserPre || isUseHealth) {
                        if (!Utils.isEmpty(pwd)) {
                            T.showShort(context, "请输入密码");
                            return;
                        }

                    }
                }
                CheackPassword(context, Utils.getEditViewText(pwd));
            } else if (onlinepay && !locapay) {//只有在线支付
                getData(context, "");
            } else if (onlinepay && locapay) {//两种都有
                if (isUsePrc || isUserPre || isUseHealth) {
                    if (!data.pay_info.member_paypwd) {
                        T.showShort(context, "未设置支付密码");
                        loadMobile(context);
                        return;
                    } else {
                        if (!Utils.isEmpty(pwd)) {
                            T.showShort(context, "请输入密码");
                            return;
                        }

                    }
                    CheackPassword(context, Utils.getEditViewText(pwd));
                    return;
                }
                getData(context, "");
            }
        });

        PopupWindow popupWindow = new PopupWindow(view, LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setOnDismissListener(() -> {
            WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
            lp.alpha = 1; //0.0-1.0
            ((Activity) context).getWindow().setAttributes(lp);

        });
        //设置可以获取焦点，否则弹出菜单中的EditText是无法获取输入的
        popupWindow.setFocusable(true);
        //防止虚拟软键盘被弹出菜单遮住
        popupWindow.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        popupWindow.setBackgroundDrawable(new BitmapDrawable());
        if (!popupWindow.isShowing())
            popupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
        WindowManager.LayoutParams lp = ((Activity) context).getWindow().getAttributes();
        lp.alpha = 0.4f; //0.0-1.0
        ((Activity) context).getWindow().setAttributes(lp);
        return popupWindow;
    }

    private static void getData(Context context, String pwd) {
        RemoteDataHandler.asyncDataStringGet(Constants.PAY_NEW +
                        "&key=" + MyShopApplication.getInstance().getLoginKey() +
                        "&pay_sn=" + paydata.pay_info.pay_sn +
                        "&password=" + pwd +
                        "&rcb_pay=" + (isUsePrc ? "1" : "0") +
                        "&pd_pay=" + (isUserPre ? "1" : "0") +
                        "&healthbean_pay=" + (isUseHealth ? "1" : "0") +
                        "&payment_code=" + payment_code.get(codetype) +
                        "&client=" + "android"
                , data2 -> {
                    int code = data2.getCode();
                    String json = data2.getJson();
                    if (code == 200) {
                        PayInfos infos = getObject(json, PayInfos.class);
                        if (infos.payment_complete.equals("true")) {
                            if (infos.payment_code.equals("alipay")) {
//                                loadingAlipayNativePaymentData(context, infos.pay_sn, "1", obj -> {
//                                    LogUtils.i(obj);
//                                });
                                payData(context, infos.pay_sn, "alipay", "1");
                            } else if (infos.payment_code.equals("wxpay")) {
//                                loadingWXPaymentData(context, infos.pay_sn, "1");
                                if (isWxAppInstalledAndSupported(context)) {
                                    payData(context, infos.pay_sn, "wxpay", "1");
                                } else {
                                    T.showShort(context, "请安装微信客户端");
                                }
                            }

                        } else {
                            Intent it = new Intent();
                            it.putExtra(ORDERNUMBER, 0);
                            it.putExtra(ORDERTYPE, false);
                            it.setClass(context, OrderActivity.class);
                            context.startActivity(it);
                            ((Activity) context).finish();
                        }
                    } else {
                        T.showShort(context, getErrorString(json));
                    }
                });
    }


    private static void payData(Context context, String pay_sn, String type, String is_virtual) {
        String url = "";
        if (type.equals("alipay")) {
            url = (is_virtual.equals("1") ? Constants.URL_ALIPAY_NATIVE_GOODS : Constants.URL_ALIPAY_NATIVE_Virtual);
        } else if (type.equals("wxpay")) {
            url = (is_virtual.equals("1") ? Constants.URL_MEMBER_WX_PAYMENT : Constants.URL_MEMBER_WX_VPAYMENT);
        }
        LogUtils.i("请求地址：" + url + "&key=" + MyShopApplication.getInstance().getLoginKey() + "&pay_sn=" + pay_sn);
        new OkHttpClient().newCall(new Request.Builder().url(url)
                .post(new FormBody.Builder()
                        .add("key", MyShopApplication.getInstance().getLoginKey())
                        .add("pay_sn", pay_sn)
                        .build())
                .build()).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                RxBus.getDefault().post(new ObjectEvent(e.getMessage()));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    String json = ((Response) response).body().string();
                    LogUtils.i("结果=" + json);
                    if (getCode(json) == HttpStatus.SC_OK) {
                        if (type.equals("alipay")) {
                            String signStr = JSONParser.getStringFromJsonString("signStr", JSONParser.getStringFromJsonString("datas", json));
                            String notify_url = getNotifyUrl(signStr);
//                                App.getApp().setNotify_url(notify_url);
                            PayTask alipay = new PayTask((Activity) context);
                            String result = alipay.pay(signStr, true);
                            RxBus.getDefault().post(new PayResult(result));
//                            doPay(context, signStr);
                        } else if (type.equals("wxpay")) {
                            WxPayInfo info = JSONParser.JSON2Object(JSONParser.getStringFromJsonString("datas", json), WxPayInfo.class);
                            IWXAPI api = WXAPIFactory.createWXAPI(context, info.appid);
                            api.registerApp(info.appid);
                            PayReq req = new PayReq();
                            req.appId = info.appid;
                            req.partnerId = info.partnerid;
                            req.prepayId = info.prepayid;
                            req.nonceStr = info.noncestr;
                            req.timeStamp = info.timestamp;
                            req.packageValue = "Sign=WXPay";
                            req.sign = info.sign;
                            req.extData = "app data"; // optional
                            boolean result = api.sendReq(req);
                            LogUtils.i("唤起微信结果=" + result);
                        }

                    } else if (getCode(json) == HttpStatus.SC_BAD_REQUEST) {
                        RxBus.getDefault().post(new ObjectEvent(getErrorString(json)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    RxBus.getDefault().post(new ObjectEvent(getErrorString(e)));
                }
            }
        });


    }

    @NonNull
    private static String getNotifyUrl(String signStr) {
        String[] split = signStr.split("\\&");
        String urls = "";
        for (String s : split) {
            if (s.contains("notify_url")) {
                urls = s;
                break;
            }
        }
        return urls.split("\\=")[1].replace("\"", "");
    }


    /**
     * 验证支付密码
     *
     * @param password 支付密码
     */
    public static void CheackPassword(Context context, final String password) {
        String url = Constants.URL_CHECK_PASSWORD;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("password", password);

        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), data -> {
            String json = data.getJson();
            if (data.getCode() == HttpStatus.SC_OK) {
                if (json.equals("1")) {
                    getData(context, password);
                } else if (json.equals("2")) {
                    loadMobile(context);
                }
            } else {
                ShopHelper.showApiError(context, json);
            }
        });
    }

    /**
     * 获取绑定手机信息
     */
    public static void loadMobile(Context context) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        RemoteDataHandler.asyncLoginPostDataString(Constants.URL_MEMBER_ACCOUNT_GET_MOBILE_INFO, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject object = new JSONObject(json);
                        if (object.optBoolean("state")) { //绑定了手机
//                            mobile = object.optString("mobile");
                            loadPaypwdInfo(context);
                        } else { //没有绑定
                            CustomDialog.Builder builder = new CustomDialog.Builder(context);
                            builder.setTitle("提示")
                                    .setMessage("为保证您的资金安全，请先绑定手机号码后，再设置支付密码")
                                    .setPositiveButton("绑定", (dialog, which) -> {
                                        dialog.dismiss();
                                        ((Activity) context).startActivityForResult(new Intent(context, BindMobileActivity.class).putExtra("type", Constants.SETTINGPWD), Constants.RESULT_FLAG_BIND_MOBILE);
                                    })
                                    .setNegativeButton("暂不", ((dialog, which) -> {
                                        dialog.dismiss();
                                    }))
                                    .create().show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(context, json);
                }
            }
        });
    }

    /**
     * 获得是否设置支付密码信息
     */
    public static void loadPaypwdInfo(Context context) {
        HashMap<String, String> params = new HashMap<>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        RemoteDataHandler.asyncLoginPostDataString(Constants.URL_MEMBER_ACCOUNT_GET_PAYPWD_INFO, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject object = new JSONObject(json);
                        if (object.optBoolean("state")) {  //设置了密码 直接验证
//                            String password = editPasswordID.getText().toString().trim();
//                            CheackPassword(password);
                        } else { //没有设置密码
                            CustomDialog.Builder builder = new CustomDialog.Builder(context);
                            builder.setTitle("提示")
                                    .setMessage("请设置支付密码")
                                    .setPositiveButton("设置", (dialog, which) -> {
                                        dialog.dismiss();
                                        Intent intent = new Intent(context, ModifyPaypwdStep1Activity.class);
//                                        intent.putExtra("mobile", mobile);
                                        intent.putExtra("type", Constants.SETTINGPWD);
                                        ((Activity) context).startActivity(intent);
                                    })
                                    .setNegativeButton("暂不", ((dialog, which) -> {
                                        dialog.dismiss();
                                    }))
                                    .create().show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(context, json);
                }
            }
        });
    }


    public static String getEditViewText(EditText editText) {
        if (editText != null) {
            return isEmpty(editText.getText().toString().trim()) ? editText.getText().toString().trim() : "";
        }
        return "";
    }

    public static String getTextViewText(TextView textView) {
        if (textView != null) {
            return isEmpty(textView.getText().toString().trim()) ? textView.getText().toString().trim() : "";
        }
        return "";
    }

    public static String getErrorString(Exception t) {
        String errorMessage = "";
        if (t instanceof SocketException) {//请求异常
            errorMessage = "网络异常，请检查网络重试";
        } else if (t instanceof UnknownHostException) {//网络异常
            errorMessage = "请求失败，请稍后重试...";
        } else if (t instanceof SocketTimeoutException) {//请求超时
            errorMessage = "请求超时";
        } else if (t instanceof ConnectException) {
            errorMessage = "网络连接失败";
        } else if (t instanceof JsonSyntaxException) {
            errorMessage = "数据解析失败,联系管理员";
        } else if (t instanceof JSONException) {
            errorMessage = "数据转换失败,联系管理员";
        } else if (t instanceof Exception) {
            errorMessage = "系统异常，管理员正在维护";
        }
        return errorMessage;
    }

    public static String getErrorString(String json) {
        return JSONParser.getStringFromJsonString("error", JSONParser.getStringFromJsonString("datas", json));
    }

    public static String getValue(String key, String json) {
        return JSONParser.getStringFromJsonString(key, json);
    }

    public static <T> T getObject(String json, Class<T> c) {
        return JSONParser.JSON2Object(json, c);
    }

    public static <T> List<T> getObjectList(String json, Class<T> c) {
        return JSONParser.JSON2Array(json, c);
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


    public static boolean isEmpty(EditText editText) {
        if (editText != null) {
            String string = editText.getText().toString().trim();
            return isEmpty(string);
        }
        return false;
    }

    public static boolean isEmpty(List<?> list) {
        if (list != null && list.size() > 0)
            return true;
        return false;
    }

    public static boolean isEmpty(Object[] list) {
        if (list != null && list.length > 0)
            return true;
        return false;
    }

    public static boolean isEmpty(TextView textView) {
        if (textView != null) {
            String string = textView.getText().toString().trim();
            return isEmpty(string);
        }
        return false;
    }


    public static boolean isEmpty(String string) {
        if (string != null && string.length() > 0 && !"".equals(string) && !"[]".equals(string) && !"null".equals(string)) {
            return true;
        }
        return false;
    }

    public static int getCode(String json) {
        return Integer.valueOf(JSONParser.getStringFromJsonString("code", json));
    }

    public static boolean getHasMore(String json) {
        return Boolean.valueOf(JSONParser.getStringFromJsonString("hasmore", json));
    }

    public static int getPageTotal(String json) {
        return Integer.valueOf(JSONParser.getStringFromJsonString("page_total", json));
    }

    public static String getDatasString(String json) {
        return JSONParser.getStringFromJsonString("datas", json);
    }

}
