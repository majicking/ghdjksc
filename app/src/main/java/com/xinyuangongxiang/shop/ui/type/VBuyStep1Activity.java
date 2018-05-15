package com.xinyuangongxiang.shop.ui.type;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.pay.PayResult;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xinyuangongxiang.shop.BaseActivity;
import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.bean.VirtualGoodsInFo;
import com.xinyuangongxiang.shop.common.AnimateFirstDisplayListener;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.JSONParser;
import com.xinyuangongxiang.shop.common.MyExceptionHandler;
import com.xinyuangongxiang.shop.common.MyShopApplication;
import com.xinyuangongxiang.shop.common.StringUtil;
import com.xinyuangongxiang.shop.common.SystemHelper;
import com.xinyuangongxiang.shop.common.Utils;
import com.xinyuangongxiang.shop.http.HttpHelper;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.RemoteDataHandler.Callback;
import com.xinyuangongxiang.shop.http.ResponseData;
import com.xinyuangongxiang.shop.ncinterface.DataCallback;
import com.xinyuangongxiang.shop.newpackage.OrderActivity;
import com.xinyuangongxiang.shop.newpackage.ProgressDialog;
import com.xinyuangongxiang.shop.xrefresh.utils.LogUtils;
import com.zcw.togglebutton.ToggleButton;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import static com.xinyuangongxiang.shop.common.Constants.ORDERNUMBER;
import static com.xinyuangongxiang.shop.common.Constants.ORDERTYPE;

/**
 * 虚拟购买一步界面
 *
 * @author KingKong-HE
 * @Time 2015-1-15
 * @Email KingKong@QQ.COM
 */
public class VBuyStep1Activity extends BaseActivity implements OnClickListener {

    private String is_fcode;//是否为F码商品 1是 0否

    private String ifcart;//购物车购买标志 1购物车 0不是

    private String cart_id;//购买参数

    private String goodscount;

    private boolean showAvailableRCBalance = false;//标识是否显示充值卡

    private boolean showAvailablePredeposit = false;//标识是否显示预存款

    private double goods_total = 0.00;//总价

    private double goods_freight = 0.00;//运费

    private double goods_voucher = 0.00;//折扣价格

    private String if_pd_pay = "0";//记录是否充值卡支付  1-使用 0-不使用

    private String if_rcb_pay = "0";//记录是否预存款支付 1-使用 0-不使用
    private String healthbean_pay = "0";//记录是否健康豆支付 1-使用 0-不使用

    private String pay_name = "online";//记录付款方式，可选值 online(线上付款) offline(货到付款)

    private MyShopApplication myApplication;

    private TextView textViewGoodsFreight, textViewGoodsTotal, textVoucher, textviewAllPrice, goodsNameID, goodsPriceID, goodsNumID, storeNameID;

    private RadioButton ifshowOnpayID, ifshowOffpayID;

    private LinearLayout predepositLayoutID;

//    private CheckBox availablePredepositID, availableRCBalanceID;

    private Button commitID;

    private ImageView goodsPicID;

    private EditText editPasswordID, editPhoneID;

    protected ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions options = SystemHelper.getDisplayImageOptions();
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    private Dialog dialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vbuy_step1_view);
        MyExceptionHandler.getInstance().setContext(this);
        myApplication = (MyShopApplication) getApplicationContext();

        ifcart = getIntent().getStringExtra("ifcart");
        cart_id = getIntent().getStringExtra("cart_id");
        goodscount = getIntent().getStringExtra("goodscount");

        initViewID();

    }

    View howtopay;
    ToggleButton toggle_yck, toggle_czk, toggle_jkd;
    boolean isyck, isczk, isjkd;
    TextView num_yck, num_czk, num_jkd;

    /**
     * 初始化注册控件ID
     */
    public void initViewID() {
        howtopay = findViewById(R.id.layout_pay);
        toggle_yck = (ToggleButton) findViewById(R.id.toggle_useyck);
        toggle_czk = (ToggleButton) findViewById(R.id.toggle_useczk);
        toggle_jkd = (ToggleButton) findViewById(R.id.toggle_usejkd);
        ImageView imageBack = (ImageView) findViewById(R.id.imageBack);

        editPhoneID = (EditText) findViewById(R.id.editPhoneID);

        editPasswordID = (EditText) findViewById(R.id.editPasswordID);

        textviewAllPrice = (TextView) findViewById(R.id.textviewAllPrice);

        goodsNameID = (TextView) findViewById(R.id.goodsNameID);
        goodsPriceID = (TextView) findViewById(R.id.goodsPriceID);
        goodsNumID = (TextView) findViewById(R.id.goodsNumID);
        storeNameID = (TextView) findViewById(R.id.storeNameID);

        textViewGoodsFreight = (TextView) findViewById(R.id.textViewGoodsFreight);

        textViewGoodsTotal = (TextView) findViewById(R.id.textViewGoodsTotal);

        ifshowOffpayID = (RadioButton) findViewById(R.id.ifshowOffpayID);

        ifshowOnpayID = (RadioButton) findViewById(R.id.ifshowOnpayID);

        predepositLayoutID = (LinearLayout) findViewById(R.id.predepositLayoutID);
//
//        availablePredepositID = (CheckBox) findViewById(R.id.availablePredepositID);
//
//        availableRCBalanceID = (CheckBox) findViewById(R.id.availableRCBalanceID);

        goodsPicID = (ImageView) findViewById(R.id.goodsPicID);

        commitID = (Button) findViewById(R.id.commitID);

        MyifshowOnpayRadioButtonClickListener onpayRadioButtonClickListener = new MyifshowOnpayRadioButtonClickListener();
        ifshowOffpayID.setOnClickListener(onpayRadioButtonClickListener);
        ifshowOnpayID.setOnClickListener(onpayRadioButtonClickListener);

        imageBack.setOnClickListener(this);

        commitID.setOnClickListener(this);

        loadingBuyStep1Data();//加载购买一数据
/*
        availablePredepositID.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if_pd_pay = "1";
                } else {
                    if_pd_pay = "0";
                }
            }
        });

        availableRCBalanceID.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    if_rcb_pay = "1";
                } else {
                    if_rcb_pay = "0";
                }
            }
        });*/
        toggle_yck.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                isyck = on;
                showEiditPassword();
                if (on) {
                    if_pd_pay = "1";
                } else {
                    if_pd_pay = "0";
                }

            }
        });
        toggle_czk.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                isczk = on;
                showEiditPassword();
                if (on) {
                    if_rcb_pay = "1";
                } else {
                    if_rcb_pay = "0";
                }

            }
        });
        toggle_jkd.setOnToggleChanged(new ToggleButton.OnToggleChanged() {
            @Override
            public void onToggle(boolean on) {
                isjkd = on;
                showEiditPassword();

                if (on) {
                    healthbean_pay = "1";
                } else {
                    healthbean_pay = "0";
                }
            }
        });
        num_yck = (TextView) findViewById(R.id.textview_yck);
        num_czk = (TextView) findViewById(R.id.textview_czk);
        num_jkd = (TextView) findViewById(R.id.textview_jkd);
    }

    public void showEiditPassword() {
        if (isczk || isjkd || isyck) {
            editPasswordID.setHint("请输入支付密码");
            editPasswordID.setEnabled(true);
        } else {
            editPasswordID.setHint("不需要支付密码");
            editPasswordID.setEnabled(false);
        }
    }

    /**
     * 加载购买一数据
     */
    public void loadingBuyStep1Data() {
        String url = Constants.URL_MEMBER_VR_BUY;
        ;//index.php?act=member_buy&op=test
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("goods_id", cart_id);
        params.put("quantity", goodscount);

        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                Log.i("msg", "result=" + json);
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject jsonObj = new JSONObject(json);
                        String goods_info = jsonObj.getString("goods_info");
                        String member_info = jsonObj.getString("member_info");
                        JSONObject member_infojsonObj = new JSONObject(member_info);
                        String Available_predeposit = member_infojsonObj.getString("available_predeposit");
                        String member_available_healthbean = member_infojsonObj.getString("member_available_healthbean");
                        String Available_Rcb_pay = member_infojsonObj.getString("available_rc_balance");
                        String member_mobile = member_infojsonObj.getString("member_mobile");
                        String healthbean_allow = member_infojsonObj.getString("healthbean_allow");

                        if (member_mobile != null && !member_mobile.equals("null") && !member_mobile.equals("")) {
                            editPhoneID.setText(member_mobile);
                        }


                        num_yck.setText("可用余额:￥" + Available_predeposit == null || Available_predeposit.equals("") || Available_predeposit.equals("null") ? "0.00" : Available_predeposit);
                        num_czk.setText("可用余额:￥" + Available_Rcb_pay == null || Available_Rcb_pay.equals("") || Available_Rcb_pay.equals("null") ? "0.00" : Available_Rcb_pay);
                        num_jkd.setText("可用余额:￥" + member_available_healthbean == null || member_available_healthbean.equals("") || member_available_healthbean.equals("null") ? "0.00" : member_available_healthbean);
//                toggle_jkd.setEnabled(!buyStepInfo.getHealthbean_allow().equals("1"));
                        if (!healthbean_allow.equals("1")) {
                            toggle_jkd.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(mActivity, "无法使用健康豆", Toast.LENGTH_SHORT).show();
                                    toggle_jkd.setToggleOff();
                                }
                            });
                        }

                        VirtualGoodsInFo bean = VirtualGoodsInFo.newInstanceList(goods_info);

                        goodsNameID.setText(bean.getGoods_name() == null ? "" : bean.getGoods_name());
                        goodsPriceID.setText("价格：￥" + (bean.getGoods_price() == null ? "0.00" : bean.getGoods_price()));
                        goodsNumID.setText("数量：" + (bean.getQuantity() == null ? "0" : bean.getQuantity()));
                        storeNameID.setText(bean.getStore_name() == null ? "" : bean.getStore_name());
                        textViewGoodsTotal.setText("￥" + (bean.getGoods_total() == null ? "0" : bean.getGoods_total()));
                        textviewAllPrice.setText("￥" + (bean.getGoods_total() == null ? "0" : bean.getGoods_total()));
                        imageLoader.displayImage(bean.getGoods_image_url(), goodsPicID, options, animateFirstListener);

//                        //判断是否显示预存款
//                        if (Available_predeposit != null && !Available_predeposit.equals("null") && !Available_predeposit.equals("") && !Available_predeposit.equals("0") && !Available_predeposit.equals("0.00")) {
//                            showAvailablePredeposit = true;
//                            availablePredepositID.setVisibility(View.VISIBLE);
//                        } else {
//                            showAvailablePredeposit = false;
//                            availablePredepositID.setVisibility(View.GONE);
//                        }
//
//                        //判断是否显示充值卡
//                        if (Available_Rcb_pay != null && !Available_Rcb_pay.equals("null") && !Available_Rcb_pay.equals("") && !Available_Rcb_pay.equals("0") && !Available_Rcb_pay.equals("0.00")) {
//                            showAvailableRCBalance = true;
//                            availableRCBalanceID.setVisibility(View.VISIBLE);
//                        } else {
//                            showAvailableRCBalance = false;
//                            availableRCBalanceID.setVisibility(View.GONE);
//                        }

//                        if (showAvailablePredeposit || showAvailableRCBalance) {
//                            predepositLayoutID.setVisibility(View.VISIBLE);
//                        } else {
//                            predepositLayoutID.setVisibility(View.GONE);
//                        }


                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {

                    try {
                        JSONObject obj = new JSONObject(json);
                        String error = obj.getString("error");
                        if (error != null) {
                            Toast.makeText(VBuyStep1Activity.this, error, Toast.LENGTH_SHORT).show();
                            ;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 购买第二布 提交订单
     * <p>
     * //     * @param key当前登录令牌
     * //     * @param goods_id    商品编号
     * //     * @param quantity    购买数量
     * //     * @param buyer_phone 接收手机
     * //     * @param pd_pay      是否使用预存款 1-是 0-否
     *
     * @param password 支付密码(可以提前使用“验证支付密码”接口进行验证)
     */
    public void sendBuyStep2Data(String password, String buyer_phone) {
        String url = Constants.URL_MEMBER_VR_BUY3;


        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("goods_id", cart_id);
        params.put("quantity", goodscount);
        params.put("buyer_phone", buyer_phone);
        params.put("pd_pay", if_pd_pay);
        params.put("rcb_pay", if_rcb_pay);
        params.put("password", password);
        params.put("healthbean_pay", healthbean_pay);
        params.put("client", "android");
        dialog = ProgressDialog.showLoadingProgress(this, "订单正在生成...");
        dialog.show();
        Log.i("msg", "请求参数：" + params.toString());
        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                ProgressDialog.dismissDialog(dialog);
                final String jsons = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {

                    if (JSONParser.getStringFromJsonString("pay_info", jsons).equals("true")) {
                        Utils.loadingPaymentListData(
//                                mActivity,JSONParser.getStringFromJsonString("pay_sn",json)
                                o -> {
                                    ResponseData data1 = (ResponseData) o;
                                    String json = data1.getJson();
                                    if (data1.getCode() == HttpStatus.SC_OK) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(json);
                                            String JosnObj = jsonObject
                                                    .getString("payment_list");
                                            JSONArray arr = new JSONArray(JosnObj);
                                            LogUtils.i(arr.toString());
                                            int size = null == arr ? 0 : arr.length();
                                            if (size < 1) {
                                                showToast("没有支付方式，请后台配置");
                                                return;
                                            }
                                            LogUtils.i("订单号--->" + JSONParser.getStringFromJsonString("pay_sn", jsons));
                                            pay_sn = JSONParser.getStringFromJsonString("pay_sn", jsons);
                                            OrderPay(size, arr, JSONParser.getStringFromJsonString("pay_sn", jsons));
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }

                                    } else {
                                        try {
                                            JSONObject obj2 = new JSONObject(json);
                                            String error = obj2.getString("error");
                                            if (error != null) {
                                                showToast(error);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                        );

                    } else {
//
                        showToast("订单生成成功！");
                        startListActivity();
                    }
                } else {
                    try {
                        JSONObject obj = new JSONObject(jsons);
                        String error = obj.getString("error");
                        if (error != null) {
                            Toast.makeText(VBuyStep1Activity.this, error, Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void startListActivity() {
        Intent it = new Intent();
        it.putExtra(ORDERNUMBER, 0);
        it.putExtra(ORDERTYPE, true);
        it.setClass(VBuyStep1Activity.this, OrderActivity.class);
        startActivity(it);
        finish();
    }

    public String pay_sn;

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (msg.obj != null) {
                    Toast.makeText(VBuyStep1Activity.this, "支付成功", Toast.LENGTH_SHORT).show();
                    LogUtils.i((String) msg.obj);
                    startListActivity();
                }
            } else if (msg.what == 2) {
                Toast.makeText(VBuyStep1Activity.this, "请求失败", Toast.LENGTH_SHORT).show();
                LogUtils.i((String) msg.obj);
                startListActivity();
            }
        }
    };


    private void PayResult(String resultInfo) {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("payresult", resultInfo);
        params.put("pay_sn", pay_sn);
        new Thread(() -> {
            Message ms = new Message();
            String url = MyShopApplication.getInstance().getNotify_ur();
            try {
                String json = HttpHelper.post(url, params);
                ms.what = 1;
                ms.obj = json;
            } catch (IOException e) {
                e.printStackTrace();
                ms.what = 2;
            } finally {
                handler.sendMessage(ms);
            }

        }).start();
    }

    DataCallback callback = new DataCallback() {
        @Override
        public void data(Object o) {
            switch (((Message) o).what) {
                case 1: {
                    PayResult payResult = new PayResult((String) ((Message) o).obj);

                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                    String resultInfo = payResult.getResult();

                    String resultStatus = payResult.getResultStatus();

                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        PayResult(resultInfo);

//                        Toast.makeText(mActivity, "支付成功",
//                                Toast.LENGTH_SHORT).show();
                    } else {
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(mActivity, "支付结果确认中",
                                    Toast.LENGTH_SHORT).show();
                            showToast("订单生成成功！");
                            startListActivity();
                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
//                            Toast.makeText(mActivity, "支付失败",
//                                    Toast.LENGTH_SHORT).show();
                            showToast("支付失败，已生成订单！");
                            startListActivity();

                        }
                    }
                    break;
                }
                case 2: {
                    Toast.makeText(mActivity, "检查结果为：" + ((Message) o).obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };
    PopupWindow payPopupWindow;
    PopupWindow.OnDismissListener listener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
//            showToast("popdismiss");
            Utils.backgroundAlpha(mActivity, 1f);
            if (payPopupWindow != null && payPopupWindow.isShowing()) {
                payPopupWindow.dismiss();

            }
            showToast("订单生成成功");
            startListActivity();
        }
    };

    private void OrderPay(int size, JSONArray arr, final String pay_sn) {
        View view = Utils.initPayWindowView(mActivity, size, arr, pay_sn, "2", callback, new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.backgroundAlpha(mActivity, 1f);
                if (payPopupWindow != null && payPopupWindow.isShowing()) {
                    payPopupWindow.dismiss();
                }
            }
        }, listener);
        if (payPopupWindow == null) {
            payPopupWindow = Utils.initPopupWindow(mActivity, view, listener);
        }

        if (!payPopupWindow.isShowing()) {
            payPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            Utils.backgroundAlpha(mActivity, 0.5f);
        }


    }


    /**
     * 验证支付密码
     * <p>
     * //     * @param key  登录返回标识
     * //     * @param 支付密码
     */
    public void CheackPassword(final String password, final String buyer_phone) {
        String url = Constants.URL_CHECK_PASSWORD;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("password", password);
        dialog = ProgressDialog.showLoadingProgress(this, "密码验证");
        dialog.show();
        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, data -> {
            String json = data.getJson();
            ProgressDialog.dismissDialog(dialog);
            if (data.getCode() == HttpStatus.SC_OK) {
                if (json.equals("1")) {
                    sendBuyStep2Data(password, buyer_phone);
                }

            } else {
                try {
                    JSONObject obj = new JSONObject(json);
                    String error = obj.getString("error");
                    if (error != null) {
                        Toast.makeText(VBuyStep1Activity.this, error, Toast.LENGTH_SHORT).show();
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * 更新价格UI
     */
    public void upPriceUIData() {

        //显示折扣价格
        textVoucher.setText("-￥" + goods_voucher);

        //显示运费
        textViewGoodsFreight.setText(" +￥" + goods_freight);

        //显示商品总价
        textViewGoodsTotal.setText(" ￥" + goods_total);

        //显示总价
        textviewAllPrice.setText("￥" + (goods_total + goods_freight - goods_voucher));
    }

    class MyifshowOnpayRadioButtonClickListener implements View.OnClickListener {
        public void onClick(View v) {
            RadioButton btn = (RadioButton) v;
            switch (btn.getId()) {
                case R.id.ifshowOnpayID:

//                    if (showAvailablePredeposit || showAvailableRCBalance) {
//                        predepositLayoutID.setVisibility(View.VISIBLE);
//                    }

                    pay_name = "online";//online(线上付款) offline(货到付款)
                    break;
                case R.id.ifshowOffpayID:

//                    predepositLayoutID.setVisibility(View.GONE);

                    pay_name = "offline";//online(线上付款) offline(货到付款)

                    break;
            }
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.imageBack:

                finish();

                break;

            case R.id.commitID:

                String buyer_phone = editPhoneID.getText().toString();

                if (buyer_phone == null || buyer_phone.equals("") || buyer_phone.equals("null")) {
                    Toast.makeText(this, "请输入手机号", Toast.LENGTH_SHORT).show();
                    return;
                }
                //判断是否使用预存款或者充值卡如果使用验证密码
                if (isyck || isjkd || isczk) {

                    String password = editPasswordID.getText().toString().trim();

                    if (password != null && !password.equals("") && !password.equals("null") && StringUtil.isNoEmpty(password)) {
                        CheackPassword(password, buyer_phone);
                    } else {
                        Toast.makeText(VBuyStep1Activity.this, "支付密码不能为空", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    sendBuyStep2Data("", buyer_phone);
                }

                break;

            default:
                break;
        }
    }
}
