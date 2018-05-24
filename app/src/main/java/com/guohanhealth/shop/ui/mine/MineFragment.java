package com.guohanhealth.shop.ui.mine;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.bean.Mine;
import com.guohanhealth.shop.common.AnimateFirstDisplayListener;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.JSONParser;
import com.guohanhealth.shop.common.MyExceptionHandler;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.common.StringUtils;
import com.guohanhealth.shop.common.SystemHelper;
import com.guohanhealth.shop.common.T;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.ResponseData;
import com.guohanhealth.shop.newpackage.OrderActivity;
import com.guohanhealth.shop.newpackage.PredepositActivity;
import com.guohanhealth.shop.ui.fenxiao.ApplyLiveActivity;
import com.guohanhealth.shop.ui.fenxiao.BeginLiveActivity;
import com.guohanhealth.shop.ui.fenxiao.FenxiaoAllActivity;
import com.guohanhealth.shop.ui.fenxiao.FenxiaoGoodsActivity;
import com.guohanhealth.shop.ui.fenxiao.FenxiaoOrderActivity;
import com.guohanhealth.shop.ui.fenxiao.FenxiaoSettlementActivity;
import com.guohanhealth.shop.ui.fenxiao.FenxiaoTixianActivity;
import com.guohanhealth.shop.ui.type.AddressListActivity;
import com.guohanhealth.shop.ui.type.GoodsBrowseActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

import static com.guohanhealth.shop.common.Constants.ORDERNUMBER;
import static com.guohanhealth.shop.common.Constants.ORDERTYPE;

/**
 * 我的
 *
 * @author dqw
 * @Time 2015-7-6
 */
public class MineFragment extends Fragment {
    private MyShopApplication myApplication;
    private LinearLayout llLogin;
    private LinearLayout llMemberInfo;
    private ImageView ivMemberAvatar;
    private TextView tvMemberName;
    private ImageView ivFavGoods;
    private TextView tvFavGoodsCount;
    private ImageView ivFavStore;
    private TextView tvFavStoreCount;
    private TextView tv_Member_v;
    private TextView daifukuan, daishouhuo, daiziti, daipingjia, tuikuang;
    protected ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions options = SystemHelper.getRoundedBitmapDisplayImageOptions(100);
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
    /*分销*/
    private LinearLayout fenxiao_llOrderNoeval, fenxiao_llOrderNotakes, fenxiao_llOrderSend, fenxiao_llOrderNew, ll_fenxiao;
    private Button fenxiao_btnOrderAll;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View viewLayout = inflater.inflate(R.layout.main_mine_view, container, false);
        MyExceptionHandler.getInstance().setContext(getActivity());
        myApplication = (MyShopApplication) getActivity().getApplicationContext();
        initSettingButton(viewLayout);
        initMemberButton(viewLayout);
        initOrderButton(viewLayout);
        initAssetButton(viewLayout);
        initFenxiaoLinearlayout(viewLayout);
        return viewLayout;
    }

    private void initFenxiaoLinearlayout(View viewLayout) {
        fenxiao_llOrderNoeval = (LinearLayout) viewLayout.findViewById(R.id.fenxiao_llOrderNoeval);
        fenxiao_llOrderNotakes = (LinearLayout) viewLayout.findViewById(R.id.fenxiao_llOrderNotakes);
        fenxiao_llOrderSend = (LinearLayout) viewLayout.findViewById(R.id.fenxiao_llOrderSend);
        fenxiao_llOrderNew = (LinearLayout) viewLayout.findViewById(R.id.fenxiao_llOrderNew);
        LinearLayout fenxiao_llOrdertixian = (LinearLayout) viewLayout.findViewById(R.id.fenxiao_llOrdertixian);
        fenxiao_btnOrderAll = (Button) viewLayout.findViewById(R.id.fenxiao_btnOrderAll);
        ll_fenxiao = (LinearLayout) viewLayout.findViewById(R.id.ll_fenxiao);
        /*直播*/
        fenxiao_llOrderNoeval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                TODO
                if (StringUtils.isEmpty(movie_msg)) {
                    ApplyVerifyMovie();
                } else {
                    T.showShort(getActivity(), movie_msg);
                }
            }
        });
        /*全部*/
        fenxiao_btnOrderAll.setOnClickListener(v -> {
            startActivity(new Intent(getActivity(), FenxiaoAllActivity.class));
        });
        /*分销结算*/
        fenxiao_llOrderNotakes.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), FenxiaoSettlementActivity.class)));
        /*分销订单*/
        fenxiao_llOrderSend.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), FenxiaoOrderActivity.class)));
        /*分销商品 */
        fenxiao_llOrderNew.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), FenxiaoGoodsActivity.class)));
        /*分销提现列表*/
        fenxiao_llOrdertixian.setOnClickListener(v ->
                startActivity(new Intent(getActivity(), FenxiaoTixianActivity.class)));
    }

    /**
     * 我的二维码
     */
    private PopupWindow popupWindow;
    private View viewPopScreen;

    private void showPopWindow() {
        if (popupWindow == null) {
            viewPopScreen = LayoutInflater.from(getActivity()).inflate(R.layout.nc_activity_popwindow1, null);
            popupWindow = new PopupWindow(viewPopScreen, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            popupWindow.setTouchable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new BitmapDrawable(getActivity().getResources(), (Bitmap) null));
            popupWindow.update();
            ImageView imageView = (ImageView) viewPopScreen.findViewById(R.id.img);
            AVLoadingIndicatorView loadingIndicatorView = (AVLoadingIndicatorView) viewPopScreen.findViewById(R.id.loading);
            //带缓存的商品图
            String url = Constants.URL_CONTEXTPATH + "act=qr&op=create&key=" + MyShopApplication.getInstance().getLoginKey();
            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    //通过message，拿到字节数组
                    byte[] Picture = (byte[]) msg.obj;
                    //使用BitmapFactory工厂，把字节数组转化为bitmap
                    Bitmap bitmap = BitmapFactory.decodeByteArray(Picture, 0, Picture.length);
                    //通过imageview，设置图片
                    imageView.setImageBitmap(bitmap);
                    loadingIndicatorView.setVisibility(View.GONE);
                }
            };
            RemoteDataHandler.asyncDataStringGet(url, data -> {
                if (data != null && !StringUtils.isEmpty(data.getJson())) {
                    try {
                        //1.创建一个okhttpclient对象
                        OkHttpClient okHttpClient = new OkHttpClient();
                        //2.创建Request.Builder对象，设置参数，请求方式如果是Get，就不用设置，默认就是Get
                        Request request = new Request.Builder()
                                .url(JSONParser.getStringFromJsonString("recommend_qr", data.getJson()))
                                .build();
                        //3.创建一个Call对象，参数是request对象，发送请求
                        Call call = okHttpClient.newCall(request);
                        //4.异步请求，请求加入调度
                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Request request, IOException e) {
                            }
                            //得到从网上获取资源，转换成我们想要的类型
                            @Override
                            public void onResponse(Response response) throws IOException {
                                byte[] Picture_bt = response.body().bytes();
                                //通过handler更新UI
                                Message message = handler.obtainMessage();
                                message.obj = Picture_bt;
                                message.what = 1;
                                handler.sendMessage(message);
                            }

                        });
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    T.showShort(getActivity(), "数据加载失败");
                    popupWindow.dismiss();
                }
            });
            Button btnConfirm = (Button) viewPopScreen.findViewById(R.id.btnConfirm);
            FrameLayout flBack = (FrameLayout) viewPopScreen.findViewById(R.id.flBack);
            btnConfirm.setOnClickListener(view -> popupWindow.dismiss());
            popupWindow.setOnDismissListener(() -> {
                popupWindow = null;
            });
        }
        //设置出现位置
        popupWindow.showAtLocation(viewPopScreen, Gravity.CENTER, 0, 0);
    }

    /**
     * 初始化设置相关按钮
     *
     * @param viewLayout
     */
    private void initSettingButton(View viewLayout) {
        //设置
        Button btnSetting = (Button) viewLayout.findViewById(R.id.btnSetting);
        btnSetting.setOnClickListener(view -> {
            if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                startActivity(new Intent(getActivity(), SettingActivity.class));
            }
        });
        //设置2
        RelativeLayout rlSetting = (RelativeLayout) viewLayout.findViewById(R.id.rlSetting);
        rlSetting.setOnClickListener(view -> {
            if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                startActivity(new Intent(getActivity(), SettingActivity.class));
            }
        });
        //IM
        Button btnIm = (Button) viewLayout.findViewById(R.id.btnIm);
        btnIm.setOnClickListener(view -> {
            if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                startActivity(new Intent(getActivity(), IMNewListActivity.class));
            }
        });
        //登录
        llLogin = (LinearLayout) viewLayout.findViewById(R.id.llLogin);
        llLogin.setOnClickListener(view ->
                startActivity(new Intent(getActivity(), LoginActivity.class)));
        llMemberInfo = (LinearLayout) viewLayout.findViewById(R.id.llMemberInfo);
        ivMemberAvatar = (ImageView) viewLayout.findViewById(R.id.ivMemberAvatar);
        tvMemberName = (TextView) viewLayout.findViewById(R.id.tvMemberName);
        daifukuan = (TextView) viewLayout.findViewById(R.id.daifukuang_num);
        daishouhuo = (TextView) viewLayout.findViewById(R.id.daishouhuo_num);
        daiziti = (TextView) viewLayout.findViewById(R.id.daiziti_num);
        daipingjia = (TextView) viewLayout.findViewById(R.id.daipingjia_num);
        tuikuang = (TextView) viewLayout.findViewById(R.id.tuikuan_num);
        viewLayout.findViewById(R.id.twocode).setOnClickListener(v -> {
            if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                showPopWindow();
            }
        });
    }
    /**
     * 初始化用户信息相关按钮
     *
     * @param viewLayout
     */
    private void initMemberButton(View viewLayout) {
        //商品收藏
        LinearLayout llFavGoods = (LinearLayout) viewLayout.findViewById(R.id.llFavGoods);
        llFavGoods.setOnClickListener(view -> {
            if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                startActivity(new Intent(getActivity(), FavGoodsListActivity.class));
            }
        });
        ivFavGoods = (ImageView) viewLayout.findViewById(R.id.ivFavGoods);
        tvFavGoodsCount = (TextView) viewLayout.findViewById(R.id.tvFavGoodsCount);
        //收藏店铺
        LinearLayout llFavStore = (LinearLayout) viewLayout.findViewById(R.id.llFavStore);
        llFavStore.setOnClickListener(view -> {
            if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                startActivity(new Intent(getActivity(), FavStoreListActivity.class));
            }
        });
        ivFavStore = (ImageView) viewLayout.findViewById(R.id.ivFavStore);
        tvFavStoreCount = (TextView) viewLayout.findViewById(R.id.tvFavStoreCount);
        tv_Member_v = (TextView) viewLayout.findViewById(R.id.tv_Member_v);
        //我的足迹
        LinearLayout llZuji = (LinearLayout) viewLayout.findViewById(R.id.llZuji);
        llZuji.setOnClickListener(view -> {
            if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                startActivity(new Intent(getActivity(), GoodsBrowseActivity.class));
            }
        });
        //收货地址
        RelativeLayout rlAddress = (RelativeLayout) viewLayout.findViewById(R.id.rlAddress);
        rlAddress.setOnClickListener(view -> {
            if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                Intent intent = new Intent(getActivity(), AddressListActivity.class);
                intent.putExtra("addressFlag", "0");
                startActivity(intent);
            }
        });
    }
    /**
     * 初始化订单相关按钮
     *
     * @param viewLayout
     */
    private void initOrderButton(View viewLayout) {
        //全部订单
        Button btnOrderAll = (Button) viewLayout.findViewById(R.id.btnOrderAll);
        btnOrderAll.setOnClickListener(view -> showOrderList(""));
        //待付款订单
        LinearLayout llOrderNew = (LinearLayout) viewLayout.findViewById(R.id.llOrderNew);
        setOrderButtonEvent(llOrderNew, "state_new");
        //待收货
        LinearLayout llOrderSend = (LinearLayout) viewLayout.findViewById(R.id.llOrderSend);
        setOrderButtonEvent(llOrderSend, "state_send");
        //待自提订单
        LinearLayout llOrderNotakes = (LinearLayout) viewLayout.findViewById(R.id.llOrderNotakes);
        setOrderButtonEvent(llOrderNotakes, "state_notakes");
        //待评价订单
        LinearLayout llOrderNoeval = (LinearLayout) viewLayout.findViewById(R.id.llOrderNoeval);
        setOrderButtonEvent(llOrderNoeval, "state_noeval");
        //退款退货
        LinearLayout llRefund = (LinearLayout) viewLayout.findViewById(R.id.llRefund);
        llRefund.setOnClickListener(view ->
                startActivity(new Intent(getActivity(), OrderExchangeListActivity.class)));
    }

    /**
     * 初始化财产相关按钮
     *
     * @param viewLayout
     */
    private void initAssetButton(View viewLayout) {
        //全部财产
        Button btnMyAsset = (Button) viewLayout.findViewById(R.id.btnMyAsset);
        btnMyAsset.setOnClickListener(view -> {
            if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {

                startActivity(new Intent(getActivity(), MyAssetActivity.class));
            }
        });

        //预存款
        LinearLayout llPredeposit = (LinearLayout) viewLayout.findViewById(R.id.llPredeposit);
        llPredeposit.setOnClickListener(view -> {
            if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {

                startActivity(new Intent(getActivity(), PredepositActivity.class));
            }
        });

        //充值卡
        LinearLayout llRechargeCard = (LinearLayout) viewLayout.findViewById(R.id.llRechargeCard);
        llRechargeCard.setOnClickListener(view -> {
            if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                startActivity(new Intent(getActivity(), RechargeCardLogActivity.class));
            }
        });

        //代金券
        LinearLayout llVoucherList = (LinearLayout) viewLayout.findViewById(R.id.llVoucherList);
        llVoucherList.setOnClickListener(view -> {
            if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                startActivity(new Intent(getActivity(), VoucherListActivity.class));
            }
        });

        //红包
        LinearLayout llRedpacket = (LinearLayout) viewLayout.findViewById(R.id.llRedpacketList);
        llRedpacket.setOnClickListener(view -> {
            if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                startActivity(new Intent(getActivity(), RedpacketListActivity.class));
            }
        });

        //积分
        LinearLayout llPointLog = (LinearLayout) viewLayout.findViewById(R.id.llPointLog);
        llPointLog.setOnClickListener(view -> {
            if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                startActivity(new Intent(getActivity(), PointLogActivity.class));
            }
        });
    }

    /**
     * 设置订单按钮事件
     */
    private void setOrderButtonEvent(LinearLayout btn, final String stateType) {
        btn.setOnClickListener(view -> showOrderList(stateType));
    }
    /**
     * 显示订单列表
     */
    private void showOrderList(String stateType) {
        int type;
        switch (stateType) {
            case "state_new":
                type = 1;
                break;
            case "state_send":
                type = 2;
                break;
            case "state_notakes":
                type = 3;
                break;
            case "state_noeval":
                type = 4;
                break;
            default:
                type = 0;
                break;
        }
        if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
            Intent it = new Intent();
            it.putExtra(ORDERNUMBER, type);
            it.putExtra(ORDERTYPE, false);
            it.setClass(getActivity(), OrderActivity.class);
            startActivity(it);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setLoginInfo();
    }

    @Override
    public void onStart() {
        super.onStart();
        registerBoradcastReceiver();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        getActivity().unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.LOGIN_SUCCESS_URL)) {
                loadMemberInfo();
            }
        }
    };
    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Constants.LOGIN_SUCCESS_URL);
        getActivity().registerReceiver(mBroadcastReceiver, myIntentFilter);  //注册广播
    }

    /**
     * 检测是否登录
     */
    public void setLoginInfo() {
        String loginKey = myApplication.getLoginKey();
        if (loginKey != null && !loginKey.equals("")) {
            llMemberInfo.setVisibility(View.VISIBLE);
            llLogin.setVisibility(View.GONE);
            ivFavGoods.setVisibility(View.GONE);
            ivFavStore.setVisibility(View.GONE);
            tvFavGoodsCount.setVisibility(View.VISIBLE);
            tvFavStoreCount.setVisibility(View.VISIBLE);
            loadMemberInfo();
        } else {
            llMemberInfo.setVisibility(View.GONE);
            llLogin.setVisibility(View.VISIBLE);
            ivFavGoods.setVisibility(View.VISIBLE);
            ivFavStore.setVisibility(View.VISIBLE);
            tvFavGoodsCount.setVisibility(View.GONE);
            tvFavStoreCount.setVisibility(View.GONE);
        }
    }

    String movie_msg = "";

    /**
     * 初始化加载我的信息
     */
    public void loadMemberInfo() {
        String url = Constants.URL_MYSTOIRE;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());

        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject obj = new JSONObject(json);
                        String objJson = obj.getString("member_info");
                        Mine bean = Mine.newInstanceList(objJson);
                        if (bean != null) {
                            tvMemberName.setText(bean.getMemberName() == null ? "" : bean.getMemberName());
                            imageLoader.displayImage(bean.getMemberAvatar(), ivMemberAvatar, options, animateFirstListener);
                            tvFavGoodsCount.setText(bean.getFavGoods() == null ? "0" : bean.getFavGoods());
                            tvFavStoreCount.setText(bean.getFavStore() == null ? "0" : bean.getFavStore());
                            tv_Member_v.setText(bean.getLevelName() == null ? "0" : bean.getLevelName());
                            if (bean.getOrderReturn().equals("0")) {
                                tuikuang.setVisibility(View.INVISIBLE);
                            } else {
                                tuikuang.setText(bean.getOrderReturn());
                                tuikuang.setVisibility(View.VISIBLE);
                            }
                            if (bean.getOrderNoeval().equals("0")) {
                                daipingjia.setVisibility(View.INVISIBLE);
                            } else {
                                daipingjia.setText(bean.getOrderNoeval());
                                daipingjia.setVisibility(View.VISIBLE);
                            }
                            if (bean.getOrderNotakes().equals("0")) {
                                daiziti.setVisibility(View.INVISIBLE);
                            } else {
                                daiziti.setText(bean.getOrderNotakes());
                                daiziti.setVisibility(View.VISIBLE);
                            }
                            if (bean.getOrderNoreceipt().equals("0")) {
                                daishouhuo.setVisibility(View.INVISIBLE);
                            } else {
                                daishouhuo.setText(bean.getOrderNoreceipt());
                                daishouhuo.setVisibility(View.VISIBLE);
                            }
                            if (bean.getOrderNopay().equals("0")) {
                                daifukuan.setVisibility(View.INVISIBLE);
                            } else {
                                daifukuan.setText(bean.getOrderNopay());
                                daifukuan.setVisibility(View.VISIBLE);
                            }
                            if (bean.getIs_distr() != null) {
                                if ("1".equals(bean.getIs_distr())) {
                                    ll_fenxiao.setVisibility(View.VISIBLE);
                                } else {
                                    ll_fenxiao.setVisibility(View.GONE);
                                }
                            } else {
                                ll_fenxiao.setVisibility(View.GONE);
                            }
                            if (bean.getIs_movie() != null) {
                                if ("1".equals(bean.getIs_movie())) {
                                    fenxiao_llOrderNoeval.setVisibility(View.VISIBLE);
                                } else {
                                    fenxiao_llOrderNoeval.setVisibility(View.GONE);
                                }
                            }
                            movie_msg = bean.getIs_movie_msg();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(getActivity(), json);
                }
            }
        });
    }

    public void ApplyVerifyMovie() {
        String url = Constants.URL_VERIFY_MOVIE;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, data -> {
            String json = data.getJson();
            if (data.getCode() == HttpStatus.SC_OK) {
                /*审核通过了*/
                if ("1".equals(json)) {
                    /*从没有申请过*/
                    T.showShort(getActivity(), "请申请直播");
                    startActivity(new Intent(getActivity(), ApplyLiveActivity.class));
                } else {
//  LiveCameraActivity.startActivity(getActivity(),"rtmp://video-center.alivecdn.com/shopnc/test1?vhost=live.shopnctest.com", AlivcMediaFormat.OUTPUT_RESOLUTION_360P,false,AlivcMediaFormat.CAMERA_FACING_FRONT);
                    startActivity(new Intent(getActivity(), BeginLiveActivity.class));
                }
            } else {
                /*失败有两种 正在进行中，没有通过*/
                if (data.getCode() == 400) {
                    try {
                        JSONObject objError = new JSONObject(json);
                        String error = objError.getString("error");
                        if (!objError.isNull("true_name")) {
                            String true_name = objError.getString("true_name");
                            String card_number = objError.getString("card_number");
                            String card_before_image = objError.getString("card_before_image");
                            String card_behind_image = objError.getString("card_behind_image");
                            String card_before_image_url = objError.getString("card_before_image_url");
                            String card_behind_image_url = objError.getString("card_behind_image_url");
                            String is_agree = objError.getString("is_agree");
                            String member_id = objError.getString("member_id");
                            String movie_id = objError.getString("movie_id");
                            Intent intent = new Intent(getActivity(), ApplyLiveActivity.class);
                            intent.putExtra("true_name", true_name);
                            intent.putExtra("card_number", card_number);
                            intent.putExtra("card_before_image", card_before_image);
                            intent.putExtra("card_behind_image", card_behind_image);
                            intent.putExtra("card_before_image_url", card_before_image_url);
                            intent.putExtra("card_behind_image_url", card_behind_image_url);
                            intent.putExtra("is_agree", is_agree);
                            intent.putExtra("member_id", member_id);
                            intent.putExtra("movie_id", movie_id);
                            startActivity(intent);
                        } else {
                            T.showShort(getActivity(), error);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(getActivity(), json);
                }
            }
        });
    }
}