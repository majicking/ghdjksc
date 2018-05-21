package com.guohanhealth.shop.ui.home;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.guohanhealth.shop.bean.Goods1Bean;
import com.guohanhealth.shop.bean.Goods2Bean;
import com.guohanhealth.shop.bean.Home5Bean;
import com.guohanhealth.shop.bean.HomeMenuBtn;
import com.guohanhealth.shop.common.JSONParser;
import com.guohanhealth.shop.newpackage.CommonAdapter;
import com.guohanhealth.shop.newpackage.OrderActivity;
import com.guohanhealth.shop.newpackage.ViewHolder;
import com.guohanhealth.shop.ui.mine.MyAssetActivity;
import com.guohanhealth.shop.ui.type.GoodsBrowseActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import com.guohanhealth.shop.MainFragmentManager;
import com.guohanhealth.shop.R;
import com.guohanhealth.shop.adapter.HomeActivityMyGridViewListAdapter;
import com.guohanhealth.shop.bean.AdvertList;
import com.guohanhealth.shop.bean.Home1Data;
import com.guohanhealth.shop.bean.Home2Data;
import com.guohanhealth.shop.bean.Home3Data;
import com.guohanhealth.shop.bean.HomeGoodsList;
import com.guohanhealth.shop.bean.HomeVideo;
import com.guohanhealth.shop.common.AnimateFirstDisplayListener;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.JsonUtil;
import com.guohanhealth.shop.common.LoadImage;
import com.guohanhealth.shop.common.MyExceptionHandler;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ScreenUtil;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.common.SystemHelper;
import com.guohanhealth.shop.custom.MyGridView;
import com.guohanhealth.shop.custom.ViewFlipperScrollView;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.RemoteDataHandler.Callback;
import com.guohanhealth.shop.http.ResponseData;
import com.guohanhealth.shop.scannercode.android.CaptureActivity;
import com.guohanhealth.shop.ui.fenxiao.FenXiaoAudeoListTabActivity;
import com.guohanhealth.shop.library.PullToRefreshBase;
import com.guohanhealth.shop.library.PullToRefreshBase.OnRefreshListener;
import com.guohanhealth.shop.library.PullToRefreshScrollView;
import com.guohanhealth.shop.ui.mine.IMNewListActivity;
import com.guohanhealth.shop.ui.mine.SigninActivity;
import com.guohanhealth.shop.ui.type.GoodsDetailsActivity;
import com.guohanhealth.shop.ui.type.GoodsListFragmentManager;
import com.guohanhealth.shop.xrefresh.utils.LogUtils;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static com.guohanhealth.shop.common.Constants.ORDERNUMBER;
import static com.guohanhealth.shop.common.Constants.ORDERTYPE;

/**
 * 首页
 *
 * @author dqw
 * @Time 2015-8-17
 */
public class HomeFragment extends Fragment implements OnGestureListener, OnTouchListener {
    private MyShopApplication myApplication;

    private Intent intent = null;

    private TextView tvSearch;
    private Button btnCamera;
    private LinearLayout llIm;
    private TextView tvSearchD;
    private Button btnCameraD;
    private LinearLayout llImD;

    private LinearLayout llHomeGoodsClassify;//商品分类
    private LinearLayout llHomeCart;//购物车
    private LinearLayout llHomeMine;//我的商城
    private LinearLayout llHomeSignin;//每日签到


    private PullToRefreshScrollView mPullRefreshScrollView;
    private ViewFlipper viewflipper;
    private LinearLayout dian;
    private boolean showNext = true;
    private int currentPage = 0;
    private final int SHOW_NEXT = 0011;
    private float downNub;//记录按下时的距离

    private LinearLayout HomeView, tab_home_item_video;

    private static final int FLING_MIN_DISTANCE = 50;
    private static final int FLING_MIN_VELOCITY = 0;

    private GestureDetector mGestureDetector;
    private ViewFlipperScrollView myScrollView;
    private ArrayList<ImageView> viewList = new ArrayList<ImageView>();
    private Animation left_in, left_out, right_in, right_out;

    protected ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions options = SystemHelper.getDisplayImageOptions();
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    private LinearLayout homeSearch;
    private LinearLayout search;

    private Button toTopBtn;// 返回顶部的按钮
    private int scrollY = 0;// 标记上次滑动位置
    private View contentView;
    private ScrollView scrollView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View viewLayout = inflater.inflate(R.layout.main_home_view, container, false);

        MyExceptionHandler.getInstance().setContext(getActivity());

        initViewID(viewLayout);//注册控件ID
        mGestureDetector = new GestureDetector(this);
        viewflipper.setOnTouchListener(this);
        myScrollView.setGestureDetector(mGestureDetector);

        return viewLayout;
    }

    /**
     * 初始化注册控件ID
     */
    public void initViewID(View view) {
        myApplication = (MyShopApplication) getActivity().getApplicationContext();

        //搜索
        tvSearch = (TextView) view.findViewById(R.id.tvSearch);
        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SearchActivity.class));
            }
        });
        tvSearchD = (TextView) view.findViewById(R.id.tvSearchD);
        tvSearchD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), SearchActivity.class));
            }
        });
        //摄像头
        btnCamera = (Button) view.findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CaptureActivity.class));
            }
        });
        btnCameraD = (Button) view.findViewById(R.id.btnCameraD);
        btnCameraD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), CaptureActivity.class));
            }
        });
        //IM
        llIm = (LinearLayout) view.findViewById(R.id.llIm);
        llIm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
//                    startActivity(new Intent(getActivity(), IMFriendsListActivity.class));
                    startActivity(new Intent(getActivity(), IMNewListActivity.class));
                }
            }
        });
        llImD = (LinearLayout) view.findViewById(R.id.llImD);
        llImD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
//                    startActivity(new Intent(getActivity(), IMFriendsListActivity.class));
                    startActivity(new Intent(getActivity(), IMNewListActivity.class));
                }
            }
        });

        MyGridView gridView = (MyGridView) view.findViewById(R.id.gridemenu);
        CommonAdapter<HomeMenuBtn> adapter = new CommonAdapter<HomeMenuBtn>(context, HomeMenuBtn.getHomeBtn(), R.layout.home_menu_item) {
            @Override
            public void convert(ViewHolder viewHolder, HomeMenuBtn item, int position, View convertView, ViewGroup parentViewGroup) {
                viewHolder.setText(R.id.title, item.title);
                viewHolder.setImageBitmap(R.id.menubtn, item.icon);
                View view1 = viewHolder.getView(R.id.backview);
                GradientDrawable mm = (GradientDrawable) view1.getBackground();
                mm.setColor(item.backgroundCoror);
            }
        };
        gridView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        gridView.setOnItemClickListener((v, arg1, position, arg2) -> {
            switch (position) {
                case 0:
                    intent = new Intent(getActivity(), MainFragmentManager.class);
                    myApplication.sendBroadcast(new Intent(Constants.SHOW_Classify_URL));
                    context.startActivity(intent);
                    break;
                case 1:
                    if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                        //startActivity(new Intent(getActivity(), OrderListActivity.class));
                        intent = new Intent(getActivity(), MainFragmentManager.class);
                        myApplication.sendBroadcast(new Intent(Constants.SHOW_CART_URL));
                        context.startActivity(intent);
                    }
                    break;
                case 2:
                   Toast.makeText(context,"功能即将开放",Toast.LENGTH_SHORT).show();
                    break;
                case 3:
                    if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                        context.startActivity(new Intent(getActivity(), SigninActivity.class));
                    }
                    break;
                case 4:
                    intent = new Intent(getActivity(), MainFragmentManager.class);
                    myApplication.sendBroadcast(new Intent(Constants.SHOW_Mine_URL));
                    context.startActivity(intent);
                    break;
                case 5:
                    if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                        Intent it = new Intent();
                        it.putExtra(ORDERNUMBER, "");
                        it.putExtra(ORDERTYPE, false);
                        it.setClass(getActivity(), OrderActivity.class);
                        context.startActivity(it);
                    }
                    break;
                case 6:
                    if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {

                        startActivity(new Intent(getActivity(), MyAssetActivity.class));
                    }
                    break;
                case 7:
                    if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
                        context.startActivity(new Intent(getActivity(), GoodsBrowseActivity.class));
                    }
                    break;
            }
        });
//        //商品分类
//        llHomeGoodsClassify = (LinearLayout) view.findViewById(R.id.llHomeFavGoods);
//        llHomeGoodsClassify.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                /*if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
//                    startActivity(new Intent(getActivity(), FavGoodsListActivity.class));
//                }*/
//
//                intent = new Intent(getActivity(), MainFragmentManager.class);
//                myApplication.sendBroadcast(new Intent(Constants.SHOW_Classify_URL));
//                startActivity(intent);
//            }
//        });
//        //购物车
//        llHomeCart = (LinearLayout) view.findViewById(R.id.llHomeMyOrder);
//        llHomeCart.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
//                    //startActivity(new Intent(getActivity(), OrderListActivity.class));
//                    intent = new Intent(getActivity(), MainFragmentManager.class);
//                    myApplication.sendBroadcast(new Intent(Constants.SHOW_CART_URL));
//                    startActivity(intent);
//                }
//            }
//        });
//        //我的商城
//        llHomeMine = (LinearLayout) view.findViewById(R.id.llHomeMyAsset);
//        llHomeMine.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                /*if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
//                    startActivity(new Intent(getActivity(), MyAssetActivity.class));
//                }*/
//                intent = new Intent(getActivity(), MainFragmentManager.class);
//                myApplication.sendBroadcast(new Intent(Constants.SHOW_Mine_URL));
//                startActivity(intent);
//            }
//        });
//        //每日签到
//        llHomeSignin = (LinearLayout) view.findViewById(R.id.llHomeSignin);
//        llHomeSignin.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
//                    startActivity(new Intent(getActivity(), SigninActivity.class));
//                }
//            }
//        });

        mPullRefreshScrollView = (PullToRefreshScrollView) view.findViewById(R.id.pull_refresh_scrollview);
        viewflipper = (ViewFlipper) view.findViewById(R.id.viewflipper);
        dian = (LinearLayout) view.findViewById(R.id.dian);
        myScrollView = (ViewFlipperScrollView) view.findViewById(R.id.viewFlipperScrollViewID);

        HomeView = (LinearLayout) view.findViewById(R.id.homeViewID);
        tab_home_item_video = (LinearLayout) view.findViewById(R.id.tab_home_item_video);

        left_in = AnimationUtils.loadAnimation(getActivity(), R.anim.push_left_in);
        left_out = AnimationUtils.loadAnimation(getActivity(), R.anim.push_left_out);
        right_in = AnimationUtils.loadAnimation(getActivity(), R.anim.push_right_in);
        right_out = AnimationUtils.loadAnimation(getActivity(), R.anim.push_right_out);

        homeSearch = (LinearLayout) view.findViewById(R.id.homeSearch);
        search = (LinearLayout) view.findViewById(R.id.search);

        homeSearch.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        //下拉刷新监听

        mPullRefreshScrollView.setOnRefreshListener(new OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                toTopBtn.setVisibility(View.GONE);
                search.setVisibility(View.GONE);
                homeSearch.setVisibility(View.VISIBLE);
                loadUIData();
            }
        });

        scrollView = mPullRefreshScrollView.getRefreshableView();
        if (contentView == null) {
            contentView = scrollView.getChildAt(0);
        }

        toTopBtn = (Button) view.findViewById(R.id.top_btn);
        toTopBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                scrollView.post(new Runnable() {
                    @Override
                    public void run() {
                        scrollView.fullScroll(ScrollView.FOCUS_UP);
                    }
                });
                toTopBtn.setVisibility(View.GONE);
                search.setVisibility(View.GONE);
                homeSearch.setVisibility(View.VISIBLE);
            }
        });

        scrollView.setOnTouchListener(new OnTouchListener() {
            private int lastY = 0;
            private int touchEventId = -9983761;
            Handler handler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    View scroller = (View) msg.obj;
                    if (msg.what == touchEventId) {
                        if (lastY == scroller.getScrollY()) {
                            handleStop(scroller);
                        } else {
                            handler.sendMessageDelayed(handler.obtainMessage(
                                    touchEventId, scroller), 5);
                            lastY = scroller.getScrollY();
                        }
                    }
                }
            };

            @Override
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == MotionEvent.ACTION_UP) {
                    handler.sendMessageDelayed(
                            handler.obtainMessage(touchEventId, view), 5);
                }
                return false;
            }

            private void handleStop(Object view) {
                ScrollView scroller = (ScrollView) view;
                scrollY = scroller.getScrollY();
                doOnBorderListener();
            }
        });

        loadUIData();

        //读取热门关键词
        getSearchHot();

        //读取搜素关键词列表
        getSearchKeyList();
    }

    private void doOnBorderListener() {
        LogUtils.i(ScreenUtil.getScreenViewBottomHeight(scrollView) + "  "
                + scrollView.getScrollY() + " " + ScreenUtil
                .getScreenHeight(getActivity()));

        // 底部判断
        if (contentView != null
                && contentView.getMeasuredHeight() <= scrollView.getScrollY()
                + scrollView.getHeight()) {
            toTopBtn.setVisibility(View.VISIBLE);
            search.setVisibility(View.VISIBLE);
            homeSearch.setVisibility(View.GONE);
        } else if (scrollView.getScrollY() == 0) {//顶部判断
            toTopBtn.setVisibility(View.GONE);
            homeSearch.setVisibility(View.VISIBLE);
            search.setVisibility(View.GONE);
        } else if (scrollView.getScrollY() > 13) {
            toTopBtn.setVisibility(View.VISIBLE);
            search.setVisibility(View.VISIBLE);
            homeSearch.setVisibility(View.GONE);
        }
    }

    /**
     * 初始化加载数据
     */
    public void loadUIData() {
        RemoteDataHandler.asyncDataStringGet(Constants.URL_HOME, data -> {
            mPullRefreshScrollView.onRefreshComplete();//加载完成下拉控件取消显示
            if (data.getCode() == HttpStatus.SC_OK) {
                HomeView.removeAllViews(); //删除homeview所有View
                tab_home_item_video.removeAllViews(); //删除homeview所有View
                try {
                    String json = data.getJson();
                    JSONArray arr = new JSONArray(json);
//                        Logger.d(json);
                    int size = null == arr ? 0 : arr.length();

                    for (int i = 0; i < size; i++) {

                        JSONObject obj = arr.getJSONObject(i);
                        JSONObject JsonObj = new JSONObject(obj.toString());
                        if (!JsonObj.isNull("home1")) {
                            showHome1(JsonObj);
                        } else if (!JsonObj.isNull("home2")) {
                            showHome2(JsonObj);
                        } else if (!JsonObj.isNull("home3")) {
                            showHome3(JsonObj);
                        } else if (!JsonObj.isNull("home4")) {
                            showHome4(JsonObj);
                        } else if (!JsonObj.isNull("home5")) {
                            showHome5(JsonObj);
                        } else if (!JsonObj.isNull("adv_list")) {//banner
                            showAdvList(JsonObj);
                        } else if (!JsonObj.isNull("video_list")) {     //视频接口
                            showVideoView(JsonObj);
                        } else if (!JsonObj.isNull("goods")) {//商品版块
                            showGoods(JsonObj);
                        } else if (!JsonObj.isNull("goods1")) {    //限时商品
                            showGoods1(JsonObj);
                        } else if (!JsonObj.isNull("goods2")) {     //抢购商品
                            showGoods2(JsonObj);
                        }

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } else {
                Toast.makeText(getActivity(), getActivity().getResources().getString(R.string.load_error), Toast.LENGTH_SHORT).show();
            }
        });
    }


    boolean endThread;

    /**
     * 显示商品块
     *
     * @param jsonObj
     * @throws JSONException
     */
    private void showGoods(JSONObject jsonObj) {
        try {
            String goodsJson = jsonObj.getString("goods");
            JSONObject itemObj = new JSONObject(goodsJson);
            String item = itemObj.getString("item");
            String title = itemObj.getString("title");

            if (!item.equals("[]")) {

                ArrayList<HomeGoodsList> goodsList = HomeGoodsList.newInstanceList(item);
                View goodsView = getActivity().getLayoutInflater().inflate(R.layout.tab_home_item_goods, null);
                TextView textView = (TextView) goodsView.findViewById(R.id.TextViewTitle);
                MyGridView gridview = (MyGridView) goodsView.findViewById(R.id.gridview);
                gridview.setFocusable(false);
                CommonAdapter<HomeGoodsList> adapter = new CommonAdapter<HomeGoodsList>(context, goodsList, R.layout.tab_home_item_goods_gridview_item) {
                    @Override
                    public void convert(ViewHolder viewHolder, HomeGoodsList item, int position, View convertView, ViewGroup parentViewGroup) {
                        viewHolder.setText(R.id.TextViewTitle, item.getGoods_name());
                        viewHolder.setImageBitmap(R.id.ImageViewImagePic01, item.getGoods_image());
                        viewHolder.setText(R.id.TextViewPrice, "￥" + item.getGoods_promotion_price());
                    }
                };
                gridview.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                gridview.setOnItemClickListener((parent, view, position, id) -> {
                    Intent intent = new Intent(context, GoodsDetailsActivity.class);
                    intent.putExtra("goods_id", goodsList.get(position).getGoods_id());
                    context.startActivity(intent);
                });


//                HomeGoodsMyGridViewListAdapter adapter = new HomeGoodsMyGridViewListAdapter(getActivity());
//                adapter.setHomeGoodsList(goodsList);
//                gridview.setAdapter(adapter);
//                adapter.notifyDataSetChanged();

                if (!title.equals("") && !title.equals("null") && title != null) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(title);
                } else {
                    textView.setVisibility(View.GONE);
                }

                HomeView.addView(goodsView);

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 限时商品
     */
    public void showGoods1(JSONObject jsonObject) {
        try {
            String json = jsonObject.getString("goods1");
            JSONObject itemObj = new JSONObject(json);
            String item = itemObj.getString("item");
            String title = itemObj.getString("title");
            if (!item.equals("[]") && !TextUtils.isEmpty(item)) {
                List<Goods1Bean> list = JSONParser.JSON2Array(item, Goods1Bean.class);
                View goodsView = getActivity().getLayoutInflater().inflate(R.layout.tab_home_item_goods, null);
                TextView textView = (TextView) goodsView.findViewById(R.id.TextViewTitle);
                MyGridView gridview = (MyGridView) goodsView.findViewById(R.id.gridview);
                gridview.setFocusable(false);
                if (!title.equals("") && !title.equals("null") && title != null) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(title);
                } else {
                    textView.setVisibility(View.GONE);
                }
                gridview.setNumColumns(1);
                CommonAdapter<Goods1Bean> adapter = new CommonAdapter<Goods1Bean>(context, list, R.layout.tab_home_item_goods1_adapter) {
                    @Override
                    public void convert(ViewHolder viewHolder, Goods1Bean item, int position, View convertView, ViewGroup parentViewGroup) {
                        viewHolder.setText(R.id.title, item.goods_name);
                        viewHolder.setImageBitmap(R.id.img, item.goods_image);
                        viewHolder.setText(R.id.newprize, "￥" + item.xianshi_price);
                        TextView oldprice = viewHolder.getView(R.id.oldprice);
                        oldprice.getPaint().setFlags(Paint.STRIKE_THRU_TEXT_FLAG);
                        oldprice.setText("" + item.goods_price);
                        viewHolder.setText(R.id.endtime, item.activtime);

                    }
                };
                gridview.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                Handler handler = new Handler() {
                    public void handleMessage(Message msg) {
                        switch (msg.what) {
                            case 1:
                                adapter.notifyDataSetChanged();
                                break;
                        }
                        super.handleMessage(msg);
                    }
                };
                new Thread(() -> {
                    while (!endThread) {
                        try {
                            Thread.sleep(1000);
                            for (int i = 0; i < list.size(); i++) {
                                //拿到每件商品的时间差，转化为具体的多少天多少小时多少分多少秒
                                //并保存在商品time这个属性内

                                long counttime = list.get(i).time;
                                long days = counttime / (60 * 60 * 24);
                                long hours = (counttime - days * (60 * 60 * 24)) / (60 * 60);
                                long minutes = (counttime - days * (60 * 60 * 24) - hours * (60 * 60)) / (60);
                                long second = (counttime - days * (60 * 60 * 24) - hours * (60 * 60) - minutes * (60));
                                //并保存在商品time这个属性内
                                String finaltime = days + "天" + hours + "时" + minutes + "分" + second + "秒";
                                list.get(i).setActivtime(finaltime);
                                //如果时间差大于1秒钟，将每件商品的时间差减去一秒钟，
                                // 并保存在每件商品的counttime属性内
                                if (counttime > 1) {
                                    list.get(i).setTime(counttime - 1);
                                }
                            }
                            Message message = new Message();
                            message.what = 1;
                            //发送信息给handler
                            handler.sendMessage(message);
                        } catch (Exception e) {

                        }
                    }
                }).start();
                gridview.setOnItemClickListener((parent, view, position, id) -> {
                    Intent intent = new Intent(context, GoodsDetailsActivity.class);
                    intent.putExtra("goods_id", list.get(position).goods_id);
                    context.startActivity(intent);
                });


                HomeView.addView(goodsView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 抢购商品
     */
    public void showGoods2(JSONObject jsonObject) {
        try {
            String json = jsonObject.getString("goods2");
            JSONObject itemObj = new JSONObject(json);
            String item = itemObj.getString("item");
            String title = itemObj.getString("title");
            if (!item.equals("[]") && !TextUtils.isEmpty(item)) {
                List<Goods2Bean> list = JSONParser.JSON2Array(item, Goods2Bean.class);
                View goodsView = getActivity().getLayoutInflater().inflate(R.layout.tab_home_item_goods, null);
                TextView textView = (TextView) goodsView.findViewById(R.id.TextViewTitle);
                MyGridView gridview = (MyGridView) goodsView.findViewById(R.id.gridview);
                gridview.setFocusable(false);
                CommonAdapter<Goods2Bean> adapter = new CommonAdapter<Goods2Bean>(context, list, R.layout.tab_home_item_goods2_adapter) {
                    @Override
                    public void convert(ViewHolder viewHolder, Goods2Bean item, int position, View convertView, ViewGroup parentViewGroup) {
                        viewHolder.setText(R.id.title, item.goods_name);
                        viewHolder.setImageBitmap(R.id.img, item.goods_image);
                        viewHolder.setText(R.id.money, "￥" + item.goods_promotion_price);
                    }
                };
                gridview.setAdapter(adapter);
                adapter.notifyDataSetChanged();
                gridview.setOnItemClickListener((parent, view, position, id) -> {
                    Intent intent = new Intent(context, GoodsDetailsActivity.class);
                    intent.putExtra("goods_id", list.get(position).goods_id);
                    context.startActivity(intent);
                });
                if (!title.equals("") && !title.equals("null") && title != null) {
                    textView.setVisibility(View.VISIBLE);
                    textView.setText(title);
                } else {
                    textView.setVisibility(View.GONE);
                }

                HomeView.addView(goodsView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 显示广告块
     *
     * @param jsonObj
     * @throws JSONException
     */
    private void showAdvList(JSONObject jsonObj) {
        try {
            String advertJson = jsonObj.getString("adv_list");
            JSONObject itemObj = new JSONObject(advertJson);
            String item = itemObj.getString("item");

            if (!item.equals("[]")) {

                ArrayList<AdvertList> advertList = AdvertList.newInstanceList(item);

                if (advertList.size() > 0 && advertList != null) {
                    initHeadImage(advertList);
                }

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示Home1
     *
     * @param jsonObj
     * @throws JSONException
     */
    private void showHome1(JSONObject jsonObj) {
        try {
            String home1Json = jsonObj.getString("home1");
            Home1Data bean = Home1Data.newInstanceList(home1Json);
            View home1View = getActivity().getLayoutInflater().inflate(R.layout.tab_home_item_home1, null);
            TextView textView = (TextView) home1View.findViewById(R.id.TextViewHome1Title01);
            ImageView imageView = (ImageView) home1View.findViewById(R.id.ImageViewHome1Imagepic01);

            if (!bean.getTitle().equals("") && !bean.getTitle().equals("null") && bean.getTitle() != null) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(bean.getTitle());
            } else {
                textView.setVisibility(View.GONE);
            }

            imageLoader.displayImage(bean.getImage(), imageView, options, animateFirstListener);
            OnImageViewClick(imageView, bean.getType(), bean.getData());
            HomeView.addView(home1View);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示Home2
     *
     * @param jsonObj
     * @throws JSONException
     */
    private void showHome2(JSONObject jsonObj) {
        try {
            String home2Json = jsonObj.getString("home2");
            Home2Data bean = Home2Data.newInstanceDetelis(home2Json);
            View home2View = getActivity().getLayoutInflater().inflate(R.layout.tab_home_item_home2_left, null);
            TextView textView = (TextView) home2View.findViewById(R.id.TextViewTitle);

            ImageView imageViewSquare = (ImageView) home2View.findViewById(R.id.ImageViewSquare);
            ImageView imageViewRectangle1 = (ImageView) home2View.findViewById(R.id.ImageViewRectangle1);
            ImageView imageViewRectangle2 = (ImageView) home2View.findViewById(R.id.ImageViewRectangle2);

            imageLoader.displayImage(bean.getSquare_image(), imageViewSquare, options, animateFirstListener);
            imageLoader.displayImage(bean.getRectangle1_image(), imageViewRectangle1, options, animateFirstListener);
            imageLoader.displayImage(bean.getRectangle2_image(), imageViewRectangle2, options, animateFirstListener);

            OnImageViewClick(imageViewSquare, bean.getSquare_type(), bean.getSquare_data());
            OnImageViewClick(imageViewRectangle1, bean.getRectangle1_type(), bean.getRectangle1_data());
            OnImageViewClick(imageViewRectangle2, bean.getRectangle2_type(), bean.getRectangle2_data());

            if (!bean.getTitle().equals("") && !bean.getTitle().equals("null") && bean.getTitle() != null) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(bean.getTitle());
            } else {
                textView.setVisibility(View.GONE);
            }

            HomeView.addView(home2View);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示Home3
     *
     * @param jsonObj
     * @throws JSONException
     */
    private void showHome3(JSONObject jsonObj) {
        try {
            String home3Json = jsonObj.getString("home3");
            Home3Data bean = Home3Data.newInstanceDetelis(home3Json);
            ArrayList<Home3Data> home3Datas = Home3Data.newInstanceList(bean.getItem());
            View home3View = getActivity().getLayoutInflater().inflate(R.layout.tab_home_item_home3, null);
            TextView textView = (TextView) home3View.findViewById(R.id.TextViewTitle);
            MyGridView gridview = (MyGridView) home3View.findViewById(R.id.gridview);
            gridview.setFocusable(false);
            HomeActivityMyGridViewListAdapter adapter = new HomeActivityMyGridViewListAdapter(getActivity());
            adapter.setHome3Datas(home3Datas);
            gridview.setAdapter(adapter);
            adapter.notifyDataSetChanged();

            if (!bean.getTitle().equals("") && !bean.getTitle().equals("null") && bean.getTitle() != null) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(bean.getTitle());
            } else {
                textView.setVisibility(View.GONE);
            }

            HomeView.addView(home3View);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 显示Home4
     *
     * @param jsonObj
     * @throws JSONException
     */
    private void showHome4(JSONObject jsonObj) {
        try {
            String home2Json = jsonObj.getString("home4");
            Home2Data bean = Home2Data.newInstanceDetelis(home2Json);
            View home4View = getActivity().getLayoutInflater().inflate(R.layout.tab_home_item_home2_rehit, null);
            TextView textView = (TextView) home4View.findViewById(R.id.TextViewTitle);

            ImageView imageViewSquare = (ImageView) home4View.findViewById(R.id.ImageViewSquare);
            ImageView imageViewRectangle1 = (ImageView) home4View.findViewById(R.id.ImageViewRectangle1);
            ImageView imageViewRectangle2 = (ImageView) home4View.findViewById(R.id.ImageViewRectangle2);

            imageLoader.displayImage(bean.getSquare_image(), imageViewSquare, options, animateFirstListener);
            imageLoader.displayImage(bean.getRectangle1_image(), imageViewRectangle1, options, animateFirstListener);
            imageLoader.displayImage(bean.getRectangle2_image(), imageViewRectangle2, options, animateFirstListener);

            OnImageViewClick(imageViewSquare, bean.getSquare_type(), bean.getSquare_data());
            OnImageViewClick(imageViewRectangle1, bean.getRectangle1_type(), bean.getRectangle1_data());
            OnImageViewClick(imageViewRectangle2, bean.getRectangle2_type(), bean.getRectangle2_data());

            if (!bean.getTitle().equals("") && !bean.getTitle().equals("null") && bean.getTitle() != null) {
                textView.setVisibility(View.VISIBLE);
                textView.setText(bean.getTitle());
            } else {
                textView.setVisibility(View.GONE);
            }

            HomeView.addView(home4View);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * 显示Home5  楼层布局
     *
     * @param jsonObj
     * @throws JSONException
     */
    private void showHome5(JSONObject jsonObj) {
        try {
            String home2Json = jsonObj.getString("home5");
            Home5Bean bean = JSONParser.JSON2Object(home2Json, Home5Bean.class);
            View homeView = getActivity().getLayoutInflater().inflate(R.layout.tab_home_item_home5, null);
            TextView title1 = (TextView) homeView.findViewById(R.id.title1);
            TextView title2 = (TextView) homeView.findViewById(R.id.title2);
            ImageView img1 = (ImageView) homeView.findViewById(R.id.img1);
            ImageView img2 = (ImageView) homeView.findViewById(R.id.img2);
            ImageView img3 = (ImageView) homeView.findViewById(R.id.img3);
            ImageView img4 = (ImageView) homeView.findViewById(R.id.img4);

            imageLoader.displayImage(bean.getSquare_image(), img1, options, animateFirstListener);
            imageLoader.displayImage(bean.getRectangle1_image(), img2, options, animateFirstListener);
            imageLoader.displayImage(bean.getRectangle2_image(), img3, options, animateFirstListener);
            imageLoader.displayImage(bean.getRectangle3_image(), img4, options, animateFirstListener);

            OnImageViewClick(img1, bean.getSquare_type(), bean.getSquare_data());
            OnImageViewClick(img2, bean.getRectangle1_type(), bean.getRectangle1_data());
            OnImageViewClick(img3, bean.getRectangle2_type(), bean.getRectangle2_data());
            OnImageViewClick(img4, bean.getRectangle3_type(), bean.getRectangle3_data());

            if (!bean.getTitle().equals("") && !bean.getTitle().equals("null") && bean.getTitle() != null) {
                title1.setVisibility(View.VISIBLE);
                title1.setText(bean.getTitle());
            } else {
                title1.setVisibility(View.GONE);
            }
            if (!bean.getStitle().equals("") && !bean.getStitle().equals("null") && bean.getStitle() != null) {
                title2.setVisibility(View.VISIBLE);
                title2.setText(bean.getTitle());
            } else {
                title2.setVisibility(View.GONE);
            }

            HomeView.addView(homeView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void initHeadImage(ArrayList<AdvertList> list) {

        mHandler.removeMessages(SHOW_NEXT);

        //清除已有视图防止重复
        viewflipper.removeAllViews();
        dian.removeAllViews();
        viewList.clear();

        for (int i = 0; i < list.size(); i++) {
            AdvertList bean = list.get(i);
            ImageView imageView = new ImageView(HomeFragment.this.getActivity());
            imageView.setScaleType(ScaleType.FIT_XY);
            imageView.setBackgroundResource(R.drawable.dic_av_item_pic_bg);
            imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            imageLoader.displayImage(bean.getImage(), imageView, options, animateFirstListener);
            viewflipper.addView(imageView);
            OnImageViewClick(imageView, bean.getType(), bean.getData());

//            ImageView dianimageView = new ImageView(HomeFragment.this.getActivity());
//            LayoutParams params = new LinearLayout.LayoutParams(
//                    LinearLayout.LayoutParams.MATCH_PARENT, 3, 1);
//            dianimageView.setLayoutParams(params);
////            imageView.setScaleType(ScaleType.FIT_XY);
//            dianimageView.setBackgroundResource(R.drawable.dian_select);
            ImageView localImageView = new ImageView(HomeFragment.this.getActivity());
            localImageView.setId(i);
            ImageView.ScaleType localScaleType = ImageView.ScaleType.FIT_XY;
            localImageView.setScaleType(localScaleType);
            LinearLayout.LayoutParams localLayoutParams = new LinearLayout.LayoutParams(
                    24, 24);
            localImageView.setLayoutParams(localLayoutParams);
            localImageView.setPadding(5, 5, 5, 5);
            localImageView.setImageResource(R.drawable.point_unfocused);

            dian.addView(localImageView);
            viewList.add(localImageView);
        }

        //mGestureDetector = new GestureDetector(this);
        viewflipper.setOnTouchListener(this);
        //myScrollView.setGestureDetector(mGestureDetector);

        if (viewList.size() > 1) {
            dian_select(currentPage);
            mHandler.sendEmptyMessageDelayed(SHOW_NEXT, 3800);
        }
    }

    public void OnImageViewClick(View view, final String type, final String data) {
        view.setOnTouchListener((v, event) -> {
            boolean flag = false;
            if (-1 != SystemHelper.getNetworkType(getActivity())) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //  触摸时按下
                    downNub = event.getX();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    // 触摸时移动
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //  触摸时抬起
                    if (downNub == event.getX()) {
                        if (type.equals("keyword")) {//搜索关键字
                            Intent intent = new Intent(getActivity(), GoodsListFragmentManager.class);
                            intent.putExtra("keyword", data);
                            intent.putExtra("gc_name", data);
                            startActivity(intent);
                        } else if (type.equals("special")) {//专题编号
                            Intent intent = new Intent(getActivity(), SubjectWebActivity.class);
                            intent.putExtra("data", Constants.URL_SPECIAL + "&special_id=" + data + "&type=html");
                            startActivity(intent);
                        } else if (type.equals("goods")) {//商品编号
                            Intent intent = new Intent(getActivity(), GoodsDetailsActivity.class);
                            intent.putExtra("goods_id", data);
                            startActivity(intent);
                        } else if (type.equals("url")) {//地址
                            Intent intent = new Intent(getActivity(), SubjectWebActivity.class);
                            intent.putExtra("data", data);
                            startActivity(intent);
                        }
                    }
                }
                flag = true;
            }
            return flag;
        });
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SHOW_NEXT:
                    if (showNext) {
                        // 从右向左滑动
                        showNextView();
                    } else {
                        // 从左向右滑动
                        showPreviousView();
                    }
                    mHandler.sendEmptyMessageDelayed(SHOW_NEXT, 3800);
                    break;
                default:
                    break;
            }
            super.handleMessage(msg);
        }

    };

    /**
     * 向左滑动
     */
    private void showNextView() {
        viewflipper.setInAnimation(left_in);
        viewflipper.setOutAnimation(left_out);
        viewflipper.showNext();
        currentPage++;
        if (currentPage == viewflipper.getChildCount()) {
            dian_unselect(currentPage - 1);
            currentPage = 0;
            dian_select(currentPage);
        } else {
            dian_select(currentPage);//第currentPage页
            dian_unselect(currentPage - 1);
        }
    }

    /**
     * 向右滑动
     */
    private void showPreviousView() {
        dian_select(currentPage);
        viewflipper.setInAnimation(right_in);
        viewflipper.setOutAnimation(right_out);
        viewflipper.showPrevious();
        currentPage--;
        if (currentPage == -1) {
            dian_unselect(currentPage + 1);
            currentPage = viewflipper.getChildCount() - 1;
            dian_select(currentPage);
        } else {
            dian_select(currentPage);
            dian_unselect(currentPage + 1);
        }
    }

    /**
     * 对应被选中的点的图片
     *
     * @param id
     */
    private void dian_select(int id) {
        ImageView img = viewList.get(id);
        img.setImageResource(R.drawable.point_focused);
//        img.setSelected(true);
    }

    /**
     * 对应未被选中的点的图片
     *
     * @param id
     */
    private void dian_unselect(int id) {
        ImageView img = viewList.get(id);
//        img.setSelected(false);
        img.setImageResource(R.drawable.point_unfocused);
    }

    /**
     * 获取搜索热词
     */
    private void getSearchHot() {
        RemoteDataHandler.asyncDataStringGet(Constants.URL_SEARCH_HOT, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                if (data.getCode() == HttpStatus.SC_OK) {
                    String json = data.getJson();
                    try {
                        JSONObject obj = new JSONObject(json);
                        String hotInfoString = obj.getString("hot_info");
                        String searchHotName = "";
                        if (!hotInfoString.equals("[]")) {
                            JSONObject hotInfoObj = new JSONObject(hotInfoString);
                            searchHotName = hotInfoObj.getString("name");
                            myApplication.setSearchHotName(searchHotName);
                            myApplication.setSearchHotValue(hotInfoObj.getString("value"));
                        }
                        if (searchHotName != null && !searchHotName.equals("")) {
                            tvSearch.setHint(searchHotName);
                        } else {
                            tvSearch.setHint(getActivity().getResources().getString(R.string.default_search_text));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 获取搜索关键词列表
     */
    private void getSearchKeyList() {
        RemoteDataHandler.asyncDataStringGet(Constants.URL_SEARCH_KEY_LIST, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                if (data.getCode() == HttpStatus.SC_OK) {
                    String json = data.getJson();
                    try {
                        ArrayList<String> searchKeyList = new ArrayList<String>();
                        JSONObject obj = new JSONObject(json);
                        String searchKeyListString = obj.getString("list");
                        if (!searchKeyListString.equals("[]")) {
                            JSONArray searchKeyListArray = new JSONArray(searchKeyListString);
                            int size = null == searchKeyListArray ? 0 : searchKeyListArray.length();
                            for (int i = 0; i < size; i++) {
                                String key = searchKeyListArray.getString(i);
                                searchKeyList.add(key);
                            }
                        }
                        myApplication.setSearchKeyList(searchKeyList);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public boolean onDown(MotionEvent arg0) {
        return false;
    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
                           float velocityY) {
        if (e1.getX() - e2.getX() > FLING_MIN_DISTANCE
                && Math.abs(velocityX) > FLING_MIN_VELOCITY) {//开始向左滑动了
            if (viewList.size() > 1) {
                showNextView();
                showNext = true;
            }

        } else if (e2.getX() - e1.getX() > FLING_MIN_DISTANCE
                && Math.abs(velocityX) > FLING_MIN_VELOCITY) {//开始向右滑动了
            if (viewList.size() > 1) {
                showPreviousView();
                showNext = true;
            }
        }/*else if (e1.getY() - e2.getY() > FLING_MIN_DISTANCE
                &&  Math.abs(velocityY) > FLING_MIN_VELOCITY){
            search.setVisibility(View.VISIBLE);
            homeSearch.setVisibility(View.GONE);

        }else if(e2.getY() - e1.getY() >= 300
                && Math.abs(velocityY) > FLING_MIN_VELOCITY){
            homeSearch.setVisibility(View.VISIBLE);
            search.setVisibility(View.GONE);
        }*/
        return false;
    }

    @Override
    public void onLongPress(MotionEvent arg0) {
    }

    @Override
    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2,
                            float arg3) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent arg0) {
    }

    @Override
    public boolean onSingleTapUp(MotionEvent arg0) {
        return false;
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        return mGestureDetector.onTouchEvent(event);
    }

    /*首页中直播咨询和点播入口*/
    public void showVideoView(JSONObject jsonObj) {
        try {
            String json = jsonObj.getString("video_list");
            HomeVideo homeVideo = JsonUtil.getBean(json, HomeVideo.class);
            if (homeVideo != null) {
                if ("1".equals(homeVideo.getVideo_isuse())) {
                    View VideoView = getActivity().getLayoutInflater().inflate(R.layout.tab_home_item_video, null);
                    TextView fenxiao_title = (TextView) VideoView.findViewById(R.id.fenxiao_title);
                    Button fenxiao_more = (Button) VideoView.findViewById(R.id.fenxiao_more);
                    ImageView fenxiao_logo = (ImageView) VideoView.findViewById(R.id.fenxiao_logo);
                    LinearLayout ll_item = (LinearLayout) VideoView.findViewById(R.id.ll_item);

                    LinearLayout ll_zhibao1 = (LinearLayout) VideoView.findViewById(R.id.ll_zhibao1);
                    TextView fengxiao_r_title_1 = (TextView) VideoView.findViewById(R.id.fengxiao_r_title_1);
                    TextView fengxiao_r_brief_1 = (TextView) VideoView.findViewById(R.id.fengxiao_r_brief_1);
                    ImageView fengxiao_r_img_1 = (ImageView) VideoView.findViewById(R.id.fengxiao_r_img_1);

                    LinearLayout ll_zhibao2 = (LinearLayout) VideoView.findViewById(R.id.ll_zhibao2);
                    TextView fengxiao_r_title_2 = (TextView) VideoView.findViewById(R.id.fengxiao_r_title_2);
                    TextView fengxiao_r_brief_2 = (TextView) VideoView.findViewById(R.id.fengxiao_r_brief_2);
                    ImageView fengxiao_r_img_2 = (ImageView) VideoView.findViewById(R.id.fengxiao_r_img_2);

                    LinearLayout ll_zhibao3 = (LinearLayout) VideoView.findViewById(R.id.ll_zhibao3);
                    TextView fengxiao_r_title_3 = (TextView) VideoView.findViewById(R.id.fengxiao_r_title_3);
                    TextView fengxiao_r_brief_3 = (TextView) VideoView.findViewById(R.id.fengxiao_r_brief_3);
                    ImageView fengxiao_r_img_3 = (ImageView) VideoView.findViewById(R.id.fengxiao_r_img_3);

                    fenxiao_title.setText(homeVideo.getVideo_modules_name());

                    fenxiao_title.setOnClickListener(new FenXiaoAudeoListTabOnclienr(""));
                    fenxiao_more.setOnClickListener(new FenXiaoAudeoListTabOnclienr(""));
                    fenxiao_logo.setOnClickListener(new FenXiaoAudeoListTabOnclienr(""));
                    String logo = homeVideo.getVideo_modules_logo();
                    if (!com.guohanhealth.shop.common.StringUtils.isEmpty(logo)) {
                        LoadImage.loadImg(getActivity(), fenxiao_logo, logo);
                    }


                    int size = homeVideo.getItem().size();
                    for (int i = 0; i < size; i++) {
                        if (i == 0) {
                            fengxiao_r_title_1.setText(homeVideo.getItem().get(i).getCate_name());
                            fengxiao_r_brief_1.setText(homeVideo.getItem().get(i).getCate_description());
//                            LoadImage.loadImg(getActivity(),fengxiao_r_img_1,homeVideo.getItem().get(i).getCate_image());
                            imageLoader.displayImage(homeVideo.getItem().get(i).getCate_image(), fengxiao_r_img_1, options, animateFirstListener);
                            ll_zhibao1.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
                            fengxiao_r_title_1.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
                            fengxiao_r_brief_1.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
                            fengxiao_r_img_1.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
                        }
                        if (i == 1) {
                            fengxiao_r_title_2.setText(homeVideo.getItem().get(i).getCate_name());
                            fengxiao_r_brief_2.setText(homeVideo.getItem().get(i).getCate_description());
//                            LoadImage.loadImg(getActivity(),fengxiao_r_img_2,homeVideo.getItem().get(i).getCate_image());
                            imageLoader.displayImage(homeVideo.getItem().get(i).getCate_image(), fengxiao_r_img_2, options, animateFirstListener);
                            ll_zhibao2.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
                            fengxiao_r_title_2.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
                            fengxiao_r_brief_2.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
                            fengxiao_r_img_2.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
                        }
                        if (i == 2) {
                            fengxiao_r_title_3.setText(homeVideo.getItem().get(i).getCate_name());
                            fengxiao_r_brief_3.setText(homeVideo.getItem().get(i).getCate_description());
//                            LoadImage.loadImg(getActivity(),fengxiao_r_img_3,homeVideo.getItem().get(i).getCate_image());
                            imageLoader.displayImage(homeVideo.getItem().get(i).getCate_image(), fengxiao_r_img_3, options, animateFirstListener);
                            ll_zhibao3.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
                            fengxiao_r_title_3.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
                            fengxiao_r_brief_3.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
                            fengxiao_r_img_3.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
                        }

//                        View VideoView_item = getActivity().getLayoutInflater().inflate(R.layout.tab_home_item_video_item, null);
//                        LinearLayout ll_zhibao0 = (LinearLayout)VideoView_item.findViewById(R.id.ll_zhibao0);
//                        TextView fengxiao_r_title_0 = (TextView)VideoView_item.findViewById(R.id.fengxiao_r_title_0);
//                        TextView fengxiao_r_brief_0 = (TextView)VideoView_item.findViewById(R.id.fengxiao_r_brief_0);
//                        ImageView fengxiao_r_img_0 = (ImageView)VideoView_item.findViewById(R.id.fengxiao_r_img_0);
//                        fengxiao_r_title_0.setText(homeVideo.getItem().get(i).getCate_name());
//                        fengxiao_r_brief_0.setText(homeVideo.getItem().get(i).getCate_description());
//                        LoadImage.loadImg(getActivity(),fengxiao_r_img_0,homeVideo.getItem().get(i).getCate_image());
//
//                        ll_zhibao0.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
//                        fengxiao_r_title_0.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
//                        fengxiao_r_brief_0.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
//                        fengxiao_r_img_0.setOnClickListener(new FenXiaoAudeoListTabOnclienr(homeVideo.getItem().get(i).getCate_id()));
//                        ll_item.addView(VideoView_item);
                    }
                    tab_home_item_video.addView(VideoView);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public class FenXiaoAudeoListTabOnclienr implements View.OnClickListener {

        private String mCate_id;

        public FenXiaoAudeoListTabOnclienr(String cate_id) {
            this.mCate_id = cate_id;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(getActivity(), FenXiaoAudeoListTabActivity.class);
            intent.putExtra("cate_id", mCate_id);
            startActivity(intent);
        }
    }

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
