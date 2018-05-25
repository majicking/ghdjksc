package com.guohanhealth.shop.ui.type;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Html;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.pay.PayResult;
import com.guohanhealth.shop.BaseActivity;
import com.guohanhealth.shop.R;
import com.guohanhealth.shop.adapter.RpacketListSpinnerAdapter;
import com.guohanhealth.shop.adapter.StoreVoucherListViewAdapter;
import com.guohanhealth.shop.bean.AddressDetails;
import com.guohanhealth.shop.bean.BuyStep1;
import com.guohanhealth.shop.bean.CartList;
import com.guohanhealth.shop.bean.InvoiceInFO;
import com.guohanhealth.shop.bean.ManSongRulesInFo;
import com.guohanhealth.shop.bean.PlayGoodsList;
import com.guohanhealth.shop.bean.RpacketInfo;
import com.guohanhealth.shop.bean.StoreVoucherList;
import com.guohanhealth.shop.bean.UpdateAddress;
import com.guohanhealth.shop.common.AnimateFirstDisplayListener;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.JSONParser;
import com.guohanhealth.shop.common.MyExceptionHandler;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.common.StringUtils;
import com.guohanhealth.shop.common.SystemHelper;
import com.guohanhealth.shop.common.T;
import com.guohanhealth.shop.common.Utils;
import com.guohanhealth.shop.custom.CustomDialog;
import com.guohanhealth.shop.http.HttpHelper;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.RemoteDataHandler.Callback;
import com.guohanhealth.shop.http.ResponseData;
import com.guohanhealth.shop.ncinterface.DataCallback;
import com.guohanhealth.shop.newpackage.OrderActivity;
import com.guohanhealth.shop.newpackage.ProgressDialog;
import com.guohanhealth.shop.ui.mine.BindMobileActivity;
import com.guohanhealth.shop.ui.mine.ModifyPaypwdStep1Activity;
import com.guohanhealth.shop.xrefresh.utils.LogUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.orhanobut.logger.Logger;
import com.zcw.togglebutton.ToggleButton;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

import static com.guohanhealth.shop.common.Constants.ORDERNUMBER;
import static com.guohanhealth.shop.common.Constants.ORDERTYPE;

/**
 * 购买第一步
 *
 * @author dqw
 * @Time 2015/8/19
 */
public class BuyStep1Activity extends BaseActivity implements OnClickListener {
    private FrameLayout flMain;

    private String is_fcode;//是否为F码商品 1是 0否
    private String goods_id;
    private String ifcart;//购物车购买标志 1购物车 0不是

    private String cart_id;//购买参数

//    private boolean showAvailableRCBalance = false;//标识是否显示充值卡
//
//    private boolean showAvailablePredeposit = false;//标识是否显示预存款

    private double goods_total = 0.00;//总价

    private double goods_freight = 0.00;//运费

    private double goods_voucher = 0.00;//折扣价格


    private double rpacket = 0.00;

    private String rpacketId = "";

    private String freight_hash; //记录运费hash

    private String offpay_hash; //货到付款hash

    private String offpay_hash_batch; //店铺是否支持货到付款hash

    private String inv_id;//记录发票ID

    private String address_id;//记录收货地址ID

    private String vat_hash;//记录发票信息hash

    private String if_pd_pay = "0";//记录是否充值卡支付  1-使用 0-不使用

    private String if_rcb_pay = "0";//记录是否预存款支付 1-使用 0-不使用

    private String healthbean_pay = "0";//记录是否健康豆支付 1-使用 0-不使用

    private String pay_name = "online";//记录付款方式，可选值 online(线上付款) offline(货到付款)

    private MyShopApplication myApplication;

    private TextView areaInfoID, addressID, trueNameID, mobPhoneID, invInfoID, noAreaInfoID, tvGoodsFreight, textViewGoodsTotal, textVoucher, textviewAllPrice, tvRpacket, tvRpacketButton;

    private RadioButton ifshowOnpayID, ifshowOffpayID;

    private boolean ifshow_offpay = false;
    private boolean allow_offpay = false;

    private LinearLayout predepositLayoutID, storeCartListID, addressInFoLayoutID, llRpacket;

//    private CheckBox availablePredepositID, availableRCBalanceID;

    private Button commitID;

    private EditText editPasswordID, editFCodeID;
    private EditText fcode_ed_text;
    private PopupWindow popupWindow; // 声明PopupWindow对象的引用

    private HashMap<String, StoreVoucherList> storeVoucherLists = new HashMap<String, StoreVoucherList>();//记录选中代金券

    private ArrayList<RpacketInfo> rpacketList;
    private ArrayList<RpacketInfo> rpacketListUseable;
    private RpacketListSpinnerAdapter rpacketAdapter;

    protected ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions options = SystemHelper.getDisplayImageOptions();
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    JSONObject jsonObj;
    PopupWindow pop;
    private EditText playgoods1_view_message;
    private String playgoods1_store_id;
    private ArrayList<String> noSendId = new ArrayList<String>();
    private AlertDialog alertDialog;
    private View mPopupWindowView;
    private String distri_id;
    private View textstatu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.buy_step1_view);
        MyExceptionHandler.getInstance().setContext(this);
        myApplication = (MyShopApplication) getApplicationContext();

        flMain = (FrameLayout) findViewById(R.id.flMain);
//        flMain.getForeground().setAlpha(0);

        is_fcode = getIntent().getStringExtra("is_fcode");
        int ic = getIntent().getIntExtra("ifcart", 0);
        ifcart = String.valueOf(ic);
        cart_id = getIntent().getStringExtra("cart_id");
        goods_id = getIntent().getStringExtra("goods_id");
        distri_id = getIntent().getStringExtra("distri_id");

        initViewID();

        setCommonHeader("确认订单");

        if ("1".equals(is_fcode)) {
            initPop();
        }

        alertDialog = new AlertDialog.Builder(BuyStep1Activity.this, AlertDialog.THEME_HOLO_LIGHT)
                .setTitle("提示")
                .setMessage("请先设置收货地址")
                .setPositiveButton("好", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent noAddressIntent = new Intent(BuyStep1Activity.this, AddressListActivity.class);
                        noAddressIntent.putExtra("addressFlag", "1");//1是提交订单跳转过去的 0或者没有是 个人中心
                        noAddressIntent.putExtra("addressFlag", "1");//1是提交订单跳转过去的 0或者没有是 个人中心
                        startActivityForResult(noAddressIntent, 5);
                    }
                })
                .setNegativeButton("取消", (dialogInterface, i) -> {
                })
                .create();


    }


    public void initPop() {
        mPopupWindowView = initPopupWindowView(getApplicationContext());
        pop = initPopupWindow(getApplicationContext(), mPopupWindowView);


        flMain.post(() -> {
            if (null != pop) {
                fcode_ed_text.setFocusable(true);
                fcode_ed_text.setFocusableInTouchMode(true);
                fcode_ed_text.requestFocus();
                pop.showAtLocation(flMain, Gravity.CENTER, 0, 0);
                backgroundAlpha(0.5f);
            }
        });

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
        editFCodeID = (EditText) findViewById(R.id.editFCodeID);
        areaInfoID = (TextView) findViewById(R.id.areaInfoID);
        addressID = (TextView) findViewById(R.id.addressID);
        trueNameID = (TextView) findViewById(R.id.trueNameID);
        mobPhoneID = (TextView) findViewById(R.id.mobPhoneID);
        invInfoID = (TextView) findViewById(R.id.invInfoID);
        noAreaInfoID = (TextView) findViewById(R.id.noAreaInfoID);
        textVoucher = (TextView) findViewById(R.id.textVoucher);
        editPasswordID = (EditText) findViewById(R.id.editPasswordID);
        textviewAllPrice = (TextView) findViewById(R.id.textviewAllPrice);
        tvGoodsFreight = (TextView) findViewById(R.id.tvGoodsFreight);
        textViewGoodsTotal = (TextView) findViewById(R.id.textViewGoodsTotal);
        ifshowOffpayID = (RadioButton) findViewById(R.id.ifshowOffpayID);
        ifshowOnpayID = (RadioButton) findViewById(R.id.ifshowOnpayID);
        predepositLayoutID = (LinearLayout) findViewById(R.id.predepositLayoutID);
        storeCartListID = (LinearLayout) findViewById(R.id.storeCartListID);
        addressInFoLayoutID = (LinearLayout) findViewById(R.id.addressInFoLayoutID);
//        availablePredepositID = (CheckBox) findViewById(R.id.availablePredepositID);
//        availableRCBalanceID = (CheckBox) findViewById(R.id.availableRCBalanceID);
        textstatu = findViewById(R.id.textstatu);
        LinearLayout fCodeLayoutID = (LinearLayout) findViewById(R.id.fCodeLayoutID);

        commitID = (Button) findViewById(R.id.commitID);

        if (is_fcode != null && is_fcode.equals("1")) {
            fCodeLayoutID.setVisibility(View.VISIBLE);
            editFCodeID.setVisibility(View.VISIBLE);
        } else {
            fCodeLayoutID.setVisibility(View.GONE);
            editFCodeID.setVisibility(View.GONE);
        }

        MyifshowOnpayRadioButtonClickListener onpayRadioButtonClickListener = new MyifshowOnpayRadioButtonClickListener();
        ifshowOffpayID.setOnClickListener(onpayRadioButtonClickListener);
        ifshowOnpayID.setOnClickListener(onpayRadioButtonClickListener);

        commitID.setOnClickListener(this);
        invInfoID.setOnClickListener(this);
        noAreaInfoID.setOnClickListener(this);
        addressInFoLayoutID.setOnClickListener(this);

        loadingBuyStep1Data();//加载购买一数据

//        availablePredepositID.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    if_pd_pay = "1";
//                } else {
//                    if_pd_pay = "0";
//                }
//            }
//        });

//        availableRCBalanceID.setOnCheckedChangeListener(new OnCheckedChangeListener() {
//            @Override
//            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
//                if (isChecked) {
//                    if_rcb_pay = "1";
//                } else {
//                    if_rcb_pay = "0";
//                }
//            }
//        });

        //红包
        llRpacket = (LinearLayout) findViewById(R.id.llRpacket);
        tvRpacket = (TextView) findViewById(R.id.tvRpacket);
        tvRpacketButton = (TextView) findViewById(R.id.tvRpacketButton);
        rpacketListUseable = new ArrayList<RpacketInfo>();
        tvRpacketButton.setOnClickListener(view -> showRpacketWindow());
        toggle_yck.setOnToggleChanged(on -> {
            isyck = on;
            if (on) {
                if_pd_pay = "1";
            } else {
                if_pd_pay = "0";
            }
            showEiditPassword();

        });
        toggle_czk.setOnToggleChanged(on -> {
            isczk = on;

            if (on) {
                if_rcb_pay = "1";
            } else {
                if_rcb_pay = "0";
            }
            showEiditPassword();
        });
        toggle_jkd.setOnToggleChanged(on -> {
            isjkd = on;
            if (on) {
                healthbean_pay = "1";
            } else {
                healthbean_pay = "0";
            }
//                showToast(healthbean_pay);
            showEiditPassword();
        });
        num_yck = (TextView) findViewById(R.id.textview_yck);
        num_czk = (TextView) findViewById(R.id.textview_czk);
        num_jkd = (TextView) findViewById(R.id.textview_jkd);

    }

    public void showEiditPassword() {
        if (isczk || isjkd || isyck) {
            textstatu.setActivated(true);
            editPasswordID.setHint("请输入支付密码");
            editPasswordID.setEnabled(true);
        } else {
            editPasswordID.setHint("其他方式支付订单");
            editPasswordID.setEnabled(false);
            textstatu.setActivated(false);
        }
    }

    //红包选择弹出窗口
    private void showRpacketWindow() {
        View popupView = BuyStep1Activity.this.getLayoutInflater().inflate(R.layout.popupwindow_rpacket_view, null);

        final PopupWindow mPopupWindow = new PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT, true);

        //点空白关闭窗口
        mPopupWindow.setTouchable(true);
        mPopupWindow.setOutsideTouchable(true);
        mPopupWindow.setBackgroundDrawable(new BitmapDrawable(getResources(), (Bitmap) null));

        ListView llRpacketList = (ListView) popupView.findViewById(R.id.lvRpacketList);
        rpacketAdapter = new RpacketListSpinnerAdapter(BuyStep1Activity.this);
        rpacketAdapter.setRpacketLists(rpacketListUseable);
        llRpacketList.setAdapter(rpacketAdapter);
        llRpacketList.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RpacketInfo rpacketInfo = rpacketListUseable.get(i);
                rpacket = rpacketInfo.getRpacketPrice();
                rpacketId = rpacketInfo.getRpacketId();
                tvRpacketButton.setText(rpacketInfo.getRpacketDesc());
                upPriceUIData();
                mPopupWindow.dismiss();
            }
        });

        mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                flMain.getForeground().setAlpha(0);
            }
        });

        mPopupWindow.showAtLocation(popupView, Gravity.CENTER, 0, 0);
        flMain.getForeground().setAlpha(150);
    }

    /**
     * 加载购买一数据
     */
    public void loadingBuyStep1Data() {
        String url = Constants.URL_BUY_STEP1;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("cart_id", cart_id);
        params.put("ifcart", ifcart);
        if (!StringUtils.isEmpty(distri_id)) {
            params.put("dis_id", distri_id);
        }
        Logger.d(params.toString());

        Dialog dialogs = ProgressDialog.showLoadingProgress(mActivity, "数据加载中...");
        dialogs.show();
        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                ProgressDialog.dismissDialog(dialogs);
                if (data.getCode() == HttpStatus.SC_OK) {
                    BuyStep1 buyStep1 = BuyStep1.newInstanceList(json);
                    if (buyStep1 != null) {
                        AddressDetails addressDetails = AddressDetails.newInstanceDetails(buyStep1.getAddress_info());
                        //记录运费hash
                        freight_hash = buyStep1.getFreight_hash();
                        //记录发票hash
                        vat_hash = buyStep1.getVat_hash();
                        //判断是否显示货到付款
                        if (buyStep1.getIfshow_offpay().equals("true")) {
                            ifshow_offpay = true;
//                            ifshowOffpayID.setVisibility(View.VISIBLE);
                        } else {
                            ifshow_offpay = false;
//                            ifshowOffpayID.setVisibility(View.GONE);
                        }
                        //判断显示隐藏收货地址
                        if (addressDetails != null) {
                            noAreaInfoID.setVisibility(View.GONE);
                            addressInFoLayoutID.setVisibility(View.VISIBLE);
                            //记录地址ID
                            address_id = addressDetails.getAddress_id();
                            //显示收货信息
                            areaInfoID.setText(addressDetails.getArea_info() == null ? "" : addressDetails.getArea_info());
                            addressID.setText(addressDetails.getAddress() == null ? "" : addressDetails.getAddress());
                            trueNameID.setText(addressDetails.getTrue_name() == null ? "" : addressDetails.getTrue_name());
                            mobPhoneID.setText(addressDetails.getMob_phone() == null ? "" : addressDetails.getMob_phone());
                            //更新收货地址
                            updataAddress(addressDetails.getCity_id(), addressDetails.getArea_id());
                        } else {
                            noAreaInfoID.setVisibility(View.VISIBLE);
                            addressInFoLayoutID.setVisibility(View.GONE);
                            if (!alertDialog.isShowing()) {
                                alertDialog.show();
                            }
                        }
                        InvoiceInFO inv_info = InvoiceInFO.newInstanceList(buyStep1.getInv_info());
                        if (inv_info != null) {
                            //记录发票ID
                            inv_id = inv_info.getInv_id() == null ? "0" : inv_info.getInv_id();
                            //显示发票信息
                            invInfoID.setText(inv_info.getContent() == null ? "" : inv_info.getContent());
                        }
                        //显示预存款 充值卡 健康豆
                        String Available_predeposit = buyStep1.getAvailable_predeposit();
                        String Available_Rcb_pay = buyStep1.getAvailable_rc_balance();
                        String member_available_healthbean = buyStep1.getMember_available_healthbean();
                        num_yck.setText("可用余额:￥" + (Available_predeposit == null || Available_predeposit.equals("") || Available_predeposit.equals("null") ? "0.00" : buyStep1.getAvailable_predeposit()));
                        num_czk.setText("可用余额:￥" + (Available_Rcb_pay == null | Available_Rcb_pay.equals("") || Available_Rcb_pay.equals("null") ? "0.00" : buyStep1.getAvailable_rc_balance()));
                        num_jkd.setText("可用余额:￥" + (member_available_healthbean == null | member_available_healthbean.equals("") || member_available_healthbean.equals("null") ? "0.00" : buyStep1.getMember_available_healthbean()));
                        if (!buyStep1.getHealthbean_allow().equals("1")) {
                            toggle_jkd.setOnClickListener(new OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    Toast.makeText(mActivity, "无法使用健康豆", Toast.LENGTH_SHORT).show();
                                    toggle_jkd.setToggleOff();
                                }
                            });
                        }
                        //显示购买商品列表
                        try {
                            jsonObj = new JSONObject(buyStep1.getStore_cart_list());
                            showGoodsList();
                        } catch (JSONException e1) {
                            e1.printStackTrace();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    //判断是否显示红包
                    if (buyStep1.getRpt_list() != null && !buyStep1.getRpt_list().equals("null") && !buyStep1.getRpt_list().equals("")) {
                        rpacketList = RpacketInfo.newInstanceList(buyStep1.getRpt_list());
                        updateRpacketUseable();
                    }
                    //更新价格UI
                    upPriceUIData();
                } else {
                    ShopHelper.showApiError(BuyStep1Activity.this, json);
                }
            }
        });
    }

    private void showGoodsList() {
        try {

            storeCartListID.removeAllViews();
            goods_freight = 0.00;
            goods_total = 0.00;

            Iterator<?> iterator = jsonObj.keys();
            ArrayList<PlayGoodsList> storeCartLists = new ArrayList<PlayGoodsList>();

            while (iterator.hasNext()) {
                String storeID = iterator.next().toString();
                String Value = jsonObj.getString(storeID);
                PlayGoodsList storecart = PlayGoodsList.newInstanceList(Value);
                ArrayList<CartList> goodList = CartList.newInstanceList(storecart.getGoods_list());
                storecart.setStore_id(storeID);

                //添加显示店铺信息
                LinearLayout playListView = (LinearLayout) getLayoutInflater().inflate(R.layout.buy_step1_playgoods_view, null);
                LinearLayout goodsListLayoutID = (LinearLayout) playListView.findViewById(R.id.goodsListLayoutID);
                final Button selectVoucheID = (Button) playListView.findViewById(R.id.selectVoucheID);
                final TextView voucherPriceID = (TextView) playListView.findViewById(R.id.voucherPriceID);
                final TextView manJianID = (TextView) playListView.findViewById(R.id.manJianID);

                TextView storeNameID = (TextView) playListView.findViewById(R.id.storeNameID);
                storeNameID.setText(storecart.getStore_name() == null ? "" : storecart.getStore_name());

                //判断显示优惠券
                String storeVoucher = storecart.getStore_voucher_list();
                if (!storeVoucher.contains("[]")) {
                    selectVoucheID.setVisibility(View.VISIBLE);
                } else {
                    selectVoucheID.setVisibility(View.GONE);
                }

                if (storeVoucher != null && !"".equals(storeVoucher) && !storeVoucher.contains("[]") && !"null".equals(storeVoucher)) {
                    JSONObject jsonVoucher = new JSONObject(storeVoucher);
                    Iterator<?> iteratorVoucher = jsonVoucher.keys();
                    final ArrayList<StoreVoucherList> Voucherlist = new ArrayList<StoreVoucherList>();
                    Voucherlist.add(new StoreVoucherList("0", storecart.getStore_id(), "0", "暂时不使用"));
                    while (iteratorVoucher.hasNext()) {
                        String voucherID = iteratorVoucher.next().toString();
                        String voucherValue = jsonVoucher.getString(voucherID);
                        StoreVoucherList Voucherbean = StoreVoucherList.newInstanceList(voucherValue);
                        Voucherlist.add(Voucherbean);
                    }

                    voucherPriceID.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getPopupWindow(v, Voucherlist, selectVoucheID, voucherPriceID);//获取PopupWindow实例
                        }
                    });

                    selectVoucheID.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            getPopupWindow(v, Voucherlist, selectVoucheID, voucherPriceID);//获取PopupWindow实例
                        }
                    });

                }

                //添加显示购买商品
                for (int i = 0; i < goodList.size(); i++) {

                    CartList bean = goodList.get(i);

                    LinearLayout playListItem = (LinearLayout) getLayoutInflater().inflate(R.layout.buy_step1_playgoods_view_item, null);
                    TextView goodsNameID = (TextView) playListItem.findViewById(R.id.goodsNameID);
                    TextView goodsPriceID = (TextView) playListItem.findViewById(R.id.goodsPriceID);
                    TextView goodsNumID = (TextView) playListItem.findViewById(R.id.goodsNumID);
                    ImageView goodsPicID = (ImageView) playListItem.findViewById(R.id.goodsPicID);
                    ImageView zengpinID = (ImageView) playListItem.findViewById(R.id.zengpinID);
                    TextView tvNoSend = (TextView) playListItem.findViewById(R.id.tvNoSend);

                    goodsNameID.setText(bean.getGoods_name() == null ? "" : bean.getGoods_name());
                    goodsPriceID.setText("价格：￥" + (bean.getGoods_price() == null ? "" : bean.getGoods_price()));
                    goodsNumID.setText("数量：" + (bean.getGoods_num() == null ? "" : bean.getGoods_num()));
                    imageLoader.displayImage(bean.getGoods_image_url(), goodsPicID, options, animateFirstListener);

                    if (bean.getPremiums().equals("true")) {
                        zengpinID.setVisibility(View.VISIBLE);
                    } else {
                        zengpinID.setVisibility(View.GONE);
                    }

                    String transportId = bean.getTransport_id();
                    tvNoSend.setVisibility(View.INVISIBLE);
                    for (String tId : noSendId) {
                        if (tId.equals(transportId)) {
                            tvNoSend.setVisibility(View.VISIBLE);
                        }
                    }

                    goodsListLayoutID.addView(playListItem);

                }

                storeCartListID.addView(playListView);

                ManSongRulesInFo bean = ManSongRulesInFo.newInstanceList(storecart.getStore_mansong_rule_list());
                double allprice = Double.parseDouble(storecart.getStore_goods_total());
                double price = Double.parseDouble(bean.getPrice() == null ? "0" : bean.getPrice());
                double discount = Double.parseDouble(bean.getDiscount() == null ? "0" : bean.getDiscount());

                if (bean != null && price > 0 && discount > 0) {
                    manJianID.setText(Html.fromHtml("订单满<font color='#FF3300'>" + price + "元</font>，立减现金<font color='#339900'>" + discount + "元</font>"));
                    manJianID.setVisibility(View.VISIBLE);
                } else {
                    manJianID.setVisibility(View.GONE);
                }

                if (allprice >= price) {
                    allprice = allprice - discount;
                }

                goods_total += allprice;
                storeCartLists.add(storecart);
            }
            if ("0".equals(ifcart) || jsonObj.length() <= 1) {
                LinearLayout message = (LinearLayout) getLayoutInflater().inflate(R.layout.buy_step1_playgoods_view_message, null);
                playgoods1_view_message = (EditText) message.findViewById(R.id.message);
                storeCartListID.addView(message);
                Iterator keys = jsonObj.keys();
                while (keys.hasNext()) {
                    playgoods1_store_id = keys.next().toString();
                }
            }

//							aStoreCartListViewAdapter.setStoreCartLists(storeCartLists);
//							aStoreCartListViewAdapter.notifyDataSetChanged();
        } catch (JSONException e1) {
            e1.printStackTrace();
        }


    }

    //控制红包显示
    private void updateRpacketUseable() {
        rpacketListUseable.clear();
        RpacketInfo rpacketInfo = new RpacketInfo(0.0, 0.0, "", "不使用红包");
        rpacketListUseable.add(0, rpacketInfo);
        double totalPrice = goods_total + goods_freight - goods_voucher;
        for (RpacketInfo info : rpacketList) {
            if (totalPrice > info.getRpacketLimit()) {
                rpacketListUseable.add(info);
            }
        }

        if (rpacketListUseable.size() > 1) {
            llRpacket.setVisibility(View.VISIBLE);
        } else {
            llRpacket.setVisibility(View.GONE);
        }

        rpacket = 0.0;
        rpacketId = "";
        tvRpacketButton.setText("不使用红包");
        upPriceUIData();
    }

    @Override
    protected void onResume() {
        super.onResume();


    }

    /**
     * 更新收货地址
     */
    public void updataAddress(String city_id, String area_id) {
        String url = Constants.URL_UPDATE_ADDRESS;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("city_id", city_id);
        params.put("area_id", area_id);
        params.put("freight_hash", freight_hash);
        Dialog dialog = ProgressDialog.showLoadingProgress(BuyStep1Activity.this, "数据加载...");
        dialog.show();
        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                ProgressDialog.dismissDialog(dialog);
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    UpdateAddress updateAddress = UpdateAddress.newInstanceList(json);
                    noSendId.clear();
                    String noSendIdString = updateAddress.getNo_send_tpl_ids();
                    if (!noSendIdString.equals("[]")) {
                        try {
                            JSONArray noSendIdArray = new JSONArray(noSendIdString);
                            int size = null == noSendIdArray ? 0 : noSendIdArray.length();
                            for (int i = 0; i < size; i++) {
                                noSendId.add((String) noSendIdArray.get(i));
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }catch (Exception e){
                            e.printStackTrace();
                        }
                    }

                    if (updateAddress != null) {
                        //判断是否显示货到付款
                        if (updateAddress.getAllow_offpay().equals("1") && ifshow_offpay) {
                            ifshowOffpayID.setVisibility(View.VISIBLE);
                        } else {
                            ifshowOffpayID.setVisibility(View.GONE);
                        }

                        //记录货到付款hash
                        offpay_hash = updateAddress.getOffpay_hash();

                        //店铺是否支持货到付款hash
                        offpay_hash_batch = updateAddress.getOffpay_hash_batch();

                        //运费
                        try {
                            goods_freight = 0.00;

                            JSONObject jsonObj = new JSONObject(updateAddress.getContent());

                            Iterator<?> iterator = jsonObj.keys();

                            while (iterator.hasNext()) {
                                String storeID = iterator.next().toString();
                                String Value = jsonObj.getString(storeID);
                                goods_freight += Double.parseDouble(Value);
                            }

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //不能配送区域
                        updateRpacketUseable();
                        goods_voucher = 0.0;
                        upPriceUIData();//更新价格UI
//						updateVoucher();
                    }
                } else {
                    ShopHelper.showApiError(BuyStep1Activity.this, json);
                }
            }
        });
    }

    /**
     * 创建PopupWindow
     */
    protected void initPopuptWindow(View view, ArrayList<StoreVoucherList> Voucherlist, final Button selectVoucheID, final TextView voucherPriceID) {
        // 获取自定义布局文件activity_popupwindow_left.xml的视图
        View popupWindow_view = getLayoutInflater().inflate(R.layout.popupwindow_vouche_view, null, false);
        final ListView listViewID = (ListView) popupWindow_view.findViewById(R.id.listViewID);

        StoreVoucherListViewAdapter adapter = new StoreVoucherListViewAdapter(this);

        adapter.setDatas(Voucherlist);

        listViewID.setAdapter(adapter);

        adapter.notifyDataSetChanged();

        // 创建PopupWindow实例,200,LayoutParams.MATCH_PARENT分别是宽度和高度
        popupWindow = new PopupWindow(popupWindow_view, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, true);
        // 设置动画效果
//	    popupWindow.setAnimationStyle(R.style.PopupVoucherAnimation);
        popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);
        // 点击其他地方消失
        popupWindow_view.setOnTouchListener(new OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // TODO Auto-generated method stub
                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }
                return false;
            }
        });

        listViewID.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                    long arg3) {

                if (popupWindow != null && popupWindow.isShowing()) {
                    popupWindow.dismiss();
                    popupWindow = null;
                }

                StoreVoucherList bean = (StoreVoucherList) listViewID.getItemAtPosition(arg2);

                if (bean != null) {
                    if (bean.getVoucher_t_id().equals("0")) {
                        storeVoucherLists.remove(bean.getStore_id());
                        selectVoucheID.setVisibility(View.VISIBLE);
                        voucherPriceID.setVisibility(View.GONE);
                    } else {
                        selectVoucheID.setVisibility(View.GONE);
                        voucherPriceID.setVisibility(View.VISIBLE);
                        storeVoucherLists.put(bean.getStore_id(), bean);
                        ;
                        voucherPriceID.setText("￥ " + (bean.getVoucher_price() == null ? "0" : bean.getVoucher_price()));
                    }
                }

                //记录折扣价格
                goods_voucher = 0.00;
                Iterator iterator = storeVoucherLists.keySet().iterator();
                while (iterator.hasNext()) {

                    String store_id = (String) iterator.next();
                    StoreVoucherList vbean = storeVoucherLists.get(store_id);

                    if (vbean != null) {
                        goods_voucher += Double.parseDouble(vbean.getVoucher_price() == null ? "0.00" : vbean.getVoucher_price());
                    }
                }

                upPriceUIData();//更新价格UI
            }
        });
    }


    PopupWindow payPopupWindow;

    /**
     * 购买第二布 提交订单
     *
     * @param password 用户支付密码，启动预存款支付时需要提交
     */
    public void sendBuyStep2Data(String password) {
        String url = Constants.URL_BUY_STEP2;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("cart_id", cart_id);
        params.put("ifcart", ifcart);
        params.put("address_id", address_id);
        params.put("vat_hash", vat_hash);
        params.put("offpay_hash", offpay_hash);
        params.put("offpay_hash_batch", offpay_hash_batch);
        params.put("pay_name", pay_name);
        params.put("invoice_id", inv_id);
        params.put("pd_pay", if_pd_pay);
        params.put("rcb_pay", if_rcb_pay);
        params.put("healthbean_pay", healthbean_pay);
        params.put("password", TextUtils.isEmpty(password) ? "" : password);
        params.put("client", "android");
        if (!rpacketId.equals("")) {
            params.put("rpt", rpacketId + "|" + rpacket);
        }

        if (is_fcode != null && is_fcode.equals("1")) {
            String fcode = editFCodeID.getText().toString();
            params.put("fcode", fcode);
        }

        if (storeVoucherLists.size() > 0) {
            String voucher = "";
            Iterator<?> iteratorVoucher = storeVoucherLists.keySet().iterator();
            while (iteratorVoucher.hasNext()) {
                String voucherID = iteratorVoucher.next().toString();
                StoreVoucherList voucherbean = storeVoucherLists.get(voucherID);
                voucher += "," + voucherbean.getVoucher_t_id() + "|" + voucherbean.getStore_id() + "|" + voucherbean.getVoucher_price();
            }
            voucher = voucher.replaceFirst("", voucher);
            params.put("voucher", voucher);
        }

        if ("0".equals(ifcart) || jsonObj.length() <= 1) {
            String message = playgoods1_view_message.getText().toString().trim();
            params.put("pay_message", playgoods1_store_id + "|" + message);
        }
        Logger.d(params.toString());
        showProgressDialog(mActivity, "正在生成订单...");
        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                final String jsons = data.getJson();
                dismissProgressDialog();
                if (data.getCode() == HttpStatus.SC_OK) {
                    if (JSONParser.getStringFromJsonString("pay_info", jsons).equals("true")) {
                        Utils.loadingPaymentListData(
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
                                            OrderPay(size, arr, JSONParser.getStringFromJsonString("pay_sn", jsons));
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    } else {
                                        ShopHelper.showApiError(BuyStep1Activity.this, json);
                                    }
                                }
                        );

                    } else {
                        showToast("订单生成成功！");
                        Intent it = new Intent();
                        it.putExtra(ORDERNUMBER, 1);
                        it.putExtra(ORDERTYPE, false);
                        it.setClass(BuyStep1Activity.this, OrderActivity.class);
                        startActivity(it);
                        finish();
                    }
                    //提交订单成功则广播显示购物车数量
                    Intent intent = new Intent(Constants.SHOW_CART_NUM);
                    BuyStep1Activity.this.sendBroadcast(intent);
                } else {
                    ShopHelper.showApiError(mActivity, jsons);
                }
            }
        });
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                if (msg.obj != null) {
                    Toast.makeText(BuyStep1Activity.this, "支付成功", Toast.LENGTH_SHORT).show();
                    LogUtils.i((String) msg.obj);
                    Intent it = new Intent();
                    it.putExtra(ORDERNUMBER, 0);
                    it.putExtra(ORDERTYPE, false);
                    it.setClass(BuyStep1Activity.this, OrderActivity.class);
                    startActivity(it);
                    finish();
                }
            } else if (msg.what == 2) {
                Toast.makeText(BuyStep1Activity.this, "支付成功", Toast.LENGTH_SHORT).show();
                LogUtils.i((String) msg.obj);
                Intent it = new Intent();
                it.putExtra(ORDERNUMBER, 0);
                it.putExtra(ORDERTYPE, false);
                it.setClass(BuyStep1Activity.this, OrderActivity.class);
                startActivity(it);
                finish();
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
            } catch (Exception e) {
                ms.what = 2;
                e.printStackTrace();
            } finally {
                handler.sendMessage(ms);
            }

        }).start();
    }

    DataCallback callback = o -> {
        switch (((Message) o).what) {
            case 1: {
                PayResult payResult = new PayResult((String) ((Message) o).obj);
                LogUtils.i("payResult------>" + payResult.toString());
                // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                String resultInfo = payResult.getResult();
                String resultStatus = payResult.getResultStatus();
                // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                if (TextUtils.equals(resultStatus, "9000")) {
                    PayResult(resultInfo);
                } else {
                    if (TextUtils.equals(resultStatus, "8000")) {
                        Toast.makeText(mActivity, "支付结果确认中",
                                Toast.LENGTH_SHORT).show();
                        showToast("订单生成成功！");
                        Intent it = new Intent();
                        it.putExtra(ORDERNUMBER, 1);
                        it.putExtra(ORDERTYPE, false);
                        it.setClass(BuyStep1Activity.this, OrderActivity.class);
                        startActivity(it);
                        finish();
                    } else {
                        showToast("支付失败，已生成订单！");
                        Intent it = new Intent();
                        it.putExtra(ORDERNUMBER, 1);
                        it.putExtra(ORDERTYPE, false);
                        it.setClass(BuyStep1Activity.this, OrderActivity.class);
                        startActivity(it);
                        finish();
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
    };

    PopupWindow.OnDismissListener listener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            Utils.backgroundAlpha(mActivity, 1f);
            if (payPopupWindow != null && payPopupWindow.isShowing()) {
                payPopupWindow.dismiss();
            }
            showToast("订单生成成功");
            Intent it = new Intent();
            it.putExtra(ORDERNUMBER, 1);
            it.putExtra(ORDERTYPE, false);
            it.setClass(BuyStep1Activity.this, OrderActivity.class);
            startActivity(it);
            finish();
        }
    };

    String pay_sn;

    private void OrderPay(int size, JSONArray arr, final String pay_sn) {
        this.pay_sn = pay_sn;
        View view = Utils.initPayWindowView(mActivity, size, arr, pay_sn, "1", callback, new OnClickListener() {
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

        if (payPopupWindow != null && !payPopupWindow.isShowing()) {
            payPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            Utils.backgroundAlpha(mActivity, 0.5f);
        }


    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    String mobile;

    /**
     * 获得是否设置支付密码信息
     */
    private void loadPaypwdInfo() {
        HashMap<String, String> params = new HashMap<>();
        params.put("key", myApplication.getLoginKey());
        RemoteDataHandler.asyncLoginPostDataString(Constants.URL_MEMBER_ACCOUNT_GET_PAYPWD_INFO, params, myApplication, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject object = new JSONObject(json);
                        if (object.optBoolean("state")) {  //设置了密码 直接验证
                            String password = editPasswordID.getText().toString().trim();
                            CheackPassword(password);
                        } else { //没有设置密码
                            CustomDialog.Builder builder = new CustomDialog.Builder(mActivity);
                            builder.setTitle("提示")
                                    .setMessage("请设置支付密码")
                                    .setPositiveButton("设置", (dialog, which) -> {
                                        dialog.dismiss();
                                        Intent intent = new Intent(mActivity, ModifyPaypwdStep1Activity.class);
                                        intent.putExtra("mobile", mobile);
                                        intent.putExtra("type", Constants.SETTINGPWD);
                                        startActivity(intent);
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
                    ShopHelper.showApiError(mActivity, json);
                }
            }
        });
    }


    /**
     * 获取绑定手机信息
     */
    private void loadMobile() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        RemoteDataHandler.asyncLoginPostDataString(Constants.URL_MEMBER_ACCOUNT_GET_MOBILE_INFO, params, myApplication, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject object = new JSONObject(json);
                        if (object.optBoolean("state")) { //绑定了手机
                            mobile = object.optString("mobile");
                            loadPaypwdInfo();
                        } else { //没有绑定
                            CustomDialog.Builder builder = new CustomDialog.Builder(mActivity);
                            builder.setTitle("提示")
                                    .setMessage("为保证您的资金安全，请先绑定手机号码后，再设置支付密码")
                                    .setPositiveButton("绑定", (dialog, which) -> {
                                        dialog.dismiss();
                                        startActivityForResult(new Intent(mActivity, BindMobileActivity.class).putExtra("type", Constants.SETTINGPWD), Constants.RESULT_FLAG_BIND_MOBILE);
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
                    ShopHelper.showApiError(mActivity, json);
                }
            }
        });
    }

    /**
     * 验证支付密码
     *
     * @param password 支付密码
     */
    public void CheackPassword(final String password) {
        String url = Constants.URL_CHECK_PASSWORD;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("password", password);
        showProgressDialog(mActivity, "密码验证...");
        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                dismissProgressDialog();
                if (data.getCode() == HttpStatus.SC_OK) {
                    if (json.equals("1")) {
                        sendBuyStep2Data(password);
                    } else if (json.equals("2")) {
                        loadMobile();
//                        setPwd();
                    }
                } else {
                    ShopHelper.showApiError(myApplication, json);
                }
            }
        });
    }


    /**
     * 获取PopupWindow实例
     */
    private void getPopupWindow(View view, ArrayList<StoreVoucherList> voucherlist, Button selectVoucheID, TextView voucherPriceID) {
        if (null != popupWindow) {
            popupWindow.dismiss();
            return;
        } else {
            initPopuptWindow(view, voucherlist, selectVoucheID, voucherPriceID);
        }
    }

    /**
     * 更新价格UI
     */
    public void upPriceUIData() {

        //显示折扣价格
        textVoucher.setText("-￥" + goods_voucher);

        //显示红包
        tvRpacket.setText("-￥" + rpacket);

        //显示运费
        tvGoodsFreight.setText(" +￥" + goods_freight);

        //显示商品总价
        textViewGoodsTotal.setText(" ￥" + goods_total);

        //显示总价
        textviewAllPrice.setText("￥" + (goods_total + goods_freight - goods_voucher - rpacket));
    }

    class MyifshowOnpayRadioButtonClickListener implements OnClickListener {
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

                    if_pd_pay = "0";
                    if_rcb_pay = "0";

                    pay_name = "offline";//online(线上付款) offline(货到付款)

                    break;
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Constants.SELECT_INVOICE) {
            inv_id = data.getStringExtra("inv_id");
            String inv_context = data.getStringExtra("inv_context");
            invInfoID.setText(inv_context == null ? "" : inv_context);
        } else if (resultCode == Constants.SELECT_ADDRESS) {
            address_id = data.getStringExtra("address_id");
            String city_id = data.getStringExtra("city_id");
            String area_id = data.getStringExtra("area_id");
            String tureName = data.getStringExtra("tureName");
            String addressInFo = data.getStringExtra("addressInFo");
            String address = data.getStringExtra("address");
            String mobPhone = data.getStringExtra("mobPhone");

            //显示收货信息
            areaInfoID.setText(addressInFo == null ? "" : addressInFo);
            addressID.setText(address == null ? "" : address);
            trueNameID.setText(tureName == null ? "" : tureName);
            mobPhoneID.setText(mobPhone == null ? "" : mobPhone);
            addressInFoLayoutID.setVisibility(View.VISIBLE);
            noAreaInfoID.setVisibility(View.GONE);
            //更新收货地址
            updataAddress(city_id, area_id);
        } else if (resultCode == Constants.SELECT_ADDRESS_NULL) {//添加地址为空
            addressInFoLayoutID.setVisibility(View.GONE);
            noAreaInfoID.setVisibility(View.VISIBLE);
            if (!alertDialog.isShowing()) {
                alertDialog.show();
            }
        } else if (resultCode == Constants.BUNDERMOBILE) {//设置密码
            loadMobile();
        } else if (resultCode == Constants.BUNDERPAYPWD) {
            T.showShort(mActivity, data == null ? "没有设置密码" : data.getStringExtra("pwd"));
        }

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {

            case R.id.invInfoID:

                Intent intent = new Intent(BuyStep1Activity.this, InvoiceListActivity.class);
                startActivityForResult(intent, 2);

                break;

            case R.id.commitID:

                //判断是否使用预存款或者充值卡如果使用验证密码
                if (isyck || isjkd || isczk) {

                    String password = editPasswordID.getText().toString().trim();
//                    if (StringUtil.isNoEmpty(password)) {
                    CheackPassword(password);
//                    } else {
//                        showToast("亲,支付密码不能为空哟!");
//                    }
//                } else {
//                    sendBuyStep2Data("");
                } else {
                    sendBuyStep2Data("");
                }

                break;

            case R.id.noAreaInfoID:

                Intent noAddressIntent = new Intent(BuyStep1Activity.this, AddressListActivity.class);
                noAddressIntent.putExtra("addressFlag", "1");//1是提交订单跳转过去的 0或者没有是 个人中心
                startActivityForResult(noAddressIntent, 5);

                break;

            case R.id.addressInFoLayoutID:

                Intent addressIntent = new Intent(BuyStep1Activity.this, AddressListActivity.class);
                addressIntent.putExtra("addressFlag", "1");//1是提交订单跳转过去的 0或者没有是 个人中心
                startActivityForResult(addressIntent, 5);

                break;

        }
    }

    /**
     * 初始化popupwindow
     */
    public PopupWindow initPopupWindow(Context context, View mPopupWindowView) {
        PopupWindow popupWindow;

        popupWindow = new PopupWindow(mPopupWindowView, ActionBar.LayoutParams.WRAP_CONTENT, ActionBar.LayoutParams.WRAP_CONTENT);
        popupWindow.setFocusable(true);
        popupWindow.setOutsideTouchable(true);
        popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
        popupWindow.update();
        popupWindow.setAnimationStyle(R.anim.popup_window_enter);
        popupWindow.setAnimationStyle(R.anim.popup_window_exit);
        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                backgroundAlpha(1f);
            }
        });
        return popupWindow;
    }


    /**
     * 初始化popupwindowView
     */
    public View initPopupWindowView(Context context) {
        View mPopupWindowView = LayoutInflater.from(context).inflate(R.layout.popwindow_fcode, null);
        fcode_ed_text = (EditText) mPopupWindowView.findViewById(R.id.fcode_ed_text);
        TextView fcode_miss = (TextView) mPopupWindowView.findViewById(R.id.fcode_miss);
        fcode_miss.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        TextView fcode_sure = (TextView) mPopupWindowView.findViewById(R.id.fcode_sure);
        fcode_sure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String fcode = fcode_ed_text.getText().toString().trim();
                if (!StringUtils.isEmpty(fcode)) {
                    CheackFcode(fcode);
                } else {
                    T.showShort(BuyStep1Activity.this, "请输入F码");
                }
            }
        });
        return mPopupWindowView;
    }

    public void CheackFcode(final String fcode) {
        String url = Constants.URL_CHECK_FCODE;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("goods_id", goods_id);
        params.put("fcode", fcode);
        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    if (json.equals("1")) {
                        pop.dismiss();
                        backgroundAlpha(1f);
                        editFCodeID.setText(fcode);
                    }
                } else {
                    ShopHelper.showApiError(BuyStep1Activity.this, json);
                }
            }
        });
    }

    /**
     * 设置添加屏幕的背景透明度
     *
     * @param bgAlpha
     */
    public void backgroundAlpha(float bgAlpha) {
        WindowManager.LayoutParams lp = getWindow().getAttributes();
        lp.alpha = bgAlpha; //0.0-1.0
        getWindow().setAttributes(lp);
    }

}
