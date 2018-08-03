package com.guohanhealth.shop.newpackage;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.guohanhealth.shop.R;
import com.guohanhealth.shop.adapter.HomeActivityMyGridViewListAdapter;
import com.guohanhealth.shop.bean.AdvertList;
import com.guohanhealth.shop.bean.Goods1Bean;
import com.guohanhealth.shop.bean.Goods2Bean;
import com.guohanhealth.shop.bean.Home1Data;
import com.guohanhealth.shop.bean.Home2Data;
import com.guohanhealth.shop.bean.Home3Data;
import com.guohanhealth.shop.bean.Home5Bean;
import com.guohanhealth.shop.bean.HomeGoodsList;
import com.guohanhealth.shop.common.AnimateFirstDisplayListener;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.JSONParser;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.common.StringUtil;
import com.guohanhealth.shop.common.SystemHelper;
import com.guohanhealth.shop.custom.MyGridView;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.library.PullToRefreshScrollView;
import com.guohanhealth.shop.ui.home.SubjectWebActivity;
import com.guohanhealth.shop.ui.type.GoodsDetailsActivity;
import com.guohanhealth.shop.ui.type.GoodsListFragmentManager;
import com.guohanhealth.shop.xrefresh.utils.LogUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.youth.banner.Banner;
import com.youth.banner.BannerConfig;
import com.youth.banner.Transformer;
import com.youth.banner.listener.OnBannerListener;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SpecialActivity extends Activity {

    @BindView(R.id.homeView)
    LinearLayout HomeView;
    String special_id;
    protected ImageLoader imageLoader = ImageLoader.getInstance();
    @BindView(R.id.back)
    ImageView back;
    @BindView(R.id.title)
    TextView title;
    @BindView(R.id.pull_refresh_scrollview)
    PullToRefreshScrollView pullRefreshScrollview;
    @BindView(R.id.banner)
    Banner banner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_special);
        ButterKnife.bind(this);
        special_id = getIntent().getStringExtra("special_id");
        back.setOnClickListener(v -> finish());
        title.setText("专题页面");
        pullRefreshScrollview.setOnRefreshListener(refreshView -> {
            getData();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
    }


    public void getData() {
        RemoteDataHandler.asyncDataStringGet(Constants.URL_SPECIAL + "&special_id=" + special_id, data -> {
            String json = data.getJson();
            pullRefreshScrollview.onRefreshComplete();//加载完成下拉控件取消显示
            if (data.getCode() == HttpStatus.SC_OK) {
                if (StringUtil.isNoEmpty(json)) {
                    String spectitle = JSONParser.getStringFromJsonString("special_desc", json);
                    title.setText(spectitle);
                }
                HomeView.removeAllViews(); //删除homeview所有View
//                tab_home_item_video.removeAllViews(); //删除homeview所有View
                String Object = JSONParser.getStringFromJsonString("list", json);
                try {
                    JSONArray arr = new JSONArray(Object);
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
//                        } else if (!JsonObj.isNull("video_list")) {     //视频接口
//                            showVideoView(JsonObj);
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
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                ShopHelper.showApiError(this, json);
            }

        });
    }

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
                View goodsView = LayoutInflater.from(SpecialActivity.this).inflate(R.layout.tab_home_item_goods, null);
                TextView textView = (TextView) goodsView.findViewById(R.id.TextViewTitle);
                MyGridView gridview = (MyGridView) goodsView.findViewById(R.id.gridview);
                View linev = goodsView.findViewById(R.id.linev);
                LinearLayout titleview = (LinearLayout) goodsView.findViewById(R.id.titleview);
                gridview.setFocusable(false);
                CommonAdapter<HomeGoodsList> adapter = new CommonAdapter<HomeGoodsList>(SpecialActivity.this, goodsList, R.layout.tab_home_item_goods_gridview_item) {
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
                    Intent intent = new Intent(SpecialActivity.this, GoodsDetailsActivity.class);
                    intent.putExtra("goods_id", goodsList.get(position).getGoods_id());
                    SpecialActivity.this.startActivity(intent);
                });
                if (!title.equals("") && !title.equals("null") && title != null) {
                    titleview.setVisibility(View.VISIBLE);
                    textView.setText(title);
                    linev.setBackgroundColor(Constants.BGCOLORS[new Random().nextInt(10)]);
                } else {
                    titleview.setVisibility(View.GONE);
                }
                HomeView.addView(goodsView);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    boolean endThread;

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
                View goodsView = LayoutInflater.from(SpecialActivity.this).inflate(R.layout.tab_home_item_goods, null);
                TextView textView = (TextView) goodsView.findViewById(R.id.TextViewTitle);
                MyGridView gridview = (MyGridView) goodsView.findViewById(R.id.gridview);
                View linev = goodsView.findViewById(R.id.linev);
                LinearLayout titleview = (LinearLayout) goodsView.findViewById(R.id.titleview);
                gridview.setFocusable(false);
                if (!title.equals("") && !title.equals("null") && title != null) {
                    linev.setBackgroundColor(Constants.BGCOLORS[new Random().nextInt(10)]);
                    titleview.setVisibility(View.VISIBLE);
                    textView.setText(title);
                } else {
                    titleview.setVisibility(View.GONE);
                }
                gridview.setNumColumns(1);
                CommonAdapter<Goods1Bean> adapter = new CommonAdapter<Goods1Bean>(SpecialActivity.this, list, R.layout.tab_home_item_goods1_adapter) {
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
                    Intent intent = new Intent(SpecialActivity.this, GoodsDetailsActivity.class);
                    intent.putExtra("goods_id", list.get(position).goods_id);
                    SpecialActivity.this.startActivity(intent);
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
                View goodsView = LayoutInflater.from(SpecialActivity.this).inflate(R.layout.tab_home_item_goods, null);
                TextView textView = (TextView) goodsView.findViewById(R.id.TextViewTitle);
                MyGridView gridview = (MyGridView) goodsView.findViewById(R.id.gridview);
                View linev = goodsView.findViewById(R.id.linev);
                LinearLayout titleview = (LinearLayout) goodsView.findViewById(R.id.titleview);
                gridview.setFocusable(false);
                CommonAdapter<Goods2Bean> adapter = new CommonAdapter<Goods2Bean>(SpecialActivity.this, list, R.layout.tab_home_item_goods2_adapter) {
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
                    Intent intent = new Intent(SpecialActivity.this, GoodsDetailsActivity.class);
                    intent.putExtra("goods_id", list.get(position).goods_id);
                    SpecialActivity.this.startActivity(intent);
                });
                if (!title.equals("") && !title.equals("null") && title != null) {
                    linev.setBackgroundColor(Constants.BGCOLORS[new Random().nextInt(10)]);
                    titleview.setVisibility(View.VISIBLE);
                    textView.setText(title);
                } else {
                    titleview.setVisibility(View.GONE);
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
                    banner.setVisibility(View.VISIBLE);
//                    initHeadImage(advertList);
                    //设置banner样式
                    banner.setBannerStyle(BannerConfig.CIRCLE_INDICATOR);
                    banner.setImages(advertList);
                    //设置图片加载器
                    banner.setImageLoader(new com.youth.banner.loader.ImageLoader() {
                        @Override
                        public void displayImage(Context context, Object path, ImageView imageView) {
                            LogUtils.i(path);
                            ImageLoader.getInstance().displayImage(((AdvertList) path).getImage(), imageView);
                        }
                    });
                    //设置banner动画效果
                    banner.setBannerAnimation(Transformer.DepthPage);
//                    //设置标题集合（当banner样式有显示title时）
//                    banner.setBannerTitles(titles);
                    //设置自动轮播，默认为true
                    banner.isAutoPlay(true);
                    //设置轮播时间
                    banner.setDelayTime(1500);
                    //设置指示器位置（当banner模式中有指示器时）
                    banner.setIndicatorGravity(BannerConfig.CENTER);
//                    //banner设置方法全部调用完毕时最后调用
                    banner.start();
                    banner.setOnBannerListener(position -> {
                        OnImageViewClick(banner, advertList.get(position).getType(), advertList.get(position).getData());
                    });
                } else {
                    banner.setVisibility(View.GONE);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private DisplayImageOptions options = SystemHelper.getDisplayImageOptions();
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

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
            View home1View = LayoutInflater.from(SpecialActivity.this).inflate(R.layout.tab_home_item_home1, null);
            TextView textView = (TextView) home1View.findViewById(R.id.TextViewHome1Title01);
            ImageView imageView = (ImageView) home1View.findViewById(R.id.ImageViewHome1Imagepic01);
            View linev = home1View.findViewById(R.id.linev);
            LinearLayout titleview = (LinearLayout) home1View.findViewById(R.id.titleview);
            if (!bean.getTitle().equals("") && !bean.getTitle().equals("null") && bean.getTitle() != null) {
                titleview.setVisibility(View.VISIBLE);
                textView.setText(bean.getTitle());
                linev.setBackgroundColor(Constants.BGCOLORS[new Random().nextInt(10)]);
            } else {
                titleview.setVisibility(View.GONE);
            }
            imageLoader.displayImage(bean.getImage(), imageView, options, animateFirstListener);
            OnImageViewClick(imageView, bean.getType(), bean.getData());
            HomeView.addView(home1View);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static Drawable tintDrawable(Drawable drawable, int colors) {
        final Drawable wrappedDrawable = DrawableCompat.wrap(drawable);
        DrawableCompat.setTintList(wrappedDrawable, ColorStateList.valueOf(colors));
        return wrappedDrawable;
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
            View home2View = LayoutInflater.from(SpecialActivity.this).inflate(R.layout.tab_home_item_home2_left, null);
            TextView textView = (TextView) home2View.findViewById(R.id.TextViewTitle);
            View linev = home2View.findViewById(R.id.linev);
            LinearLayout titleview = (LinearLayout) home2View.findViewById(R.id.titleview);
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
                titleview.setVisibility(View.VISIBLE);
                textView.setText(bean.getTitle());
                linev.setBackgroundColor(Constants.BGCOLORS[new Random().nextInt(10)]);
            } else {
                titleview.setVisibility(View.GONE);
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
            View home3View = LayoutInflater.from(SpecialActivity.this).inflate(R.layout.tab_home_item_home3, null);
            TextView textView = (TextView) home3View.findViewById(R.id.TextViewTitle);
            MyGridView gridview = (MyGridView) home3View.findViewById(R.id.gridview);
            View linev = home3View.findViewById(R.id.linev);
            LinearLayout titleview = (LinearLayout) home3View.findViewById(R.id.titleview);
            gridview.setFocusable(false);
            HomeActivityMyGridViewListAdapter adapter = new HomeActivityMyGridViewListAdapter(SpecialActivity.this);
            adapter.setHome3Datas(home3Datas);
            gridview.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            if (!bean.getTitle().equals("") && !bean.getTitle().equals("null") && bean.getTitle() != null) {
                linev.setBackgroundColor(Constants.BGCOLORS[new Random().nextInt(10)]);
                titleview.setVisibility(View.VISIBLE);
                textView.setText(bean.getTitle());
            } else {
                titleview.setVisibility(View.GONE);
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
            View home4View = LayoutInflater.from(SpecialActivity.this).inflate(R.layout.tab_home_item_home2_rehit, null);
            TextView textView = (TextView) home4View.findViewById(R.id.TextViewTitle);
            View linev = home4View.findViewById(R.id.linev);
            LinearLayout titleview = (LinearLayout) home4View.findViewById(R.id.titleview);
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
                linev.setBackgroundColor(Constants.BGCOLORS[new Random().nextInt(10)]);
                titleview.setVisibility(View.VISIBLE);
                textView.setText(bean.getTitle());
            } else {
                titleview.setVisibility(View.GONE);
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
            View homeView = LayoutInflater.from(SpecialActivity.this).inflate(R.layout.tab_home_item_home5, null);
            View titlelayout = homeView.findViewById(R.id.titlelayout);
            TextView title1 = (TextView) homeView.findViewById(R.id.title1);
            TextView title2 = (TextView) homeView.findViewById(R.id.title2);
            View linev = homeView.findViewById(R.id.linev);
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
            String titlemain = bean.getTitle();
            String titlemain1 = bean.getStitle();
            if (!TextUtils.isEmpty(titlemain) || TextUtils.isEmpty(titlemain1)) {
                titlelayout.setVisibility(View.VISIBLE);
                title1.setText(TextUtils.isEmpty(titlemain) ? "" : titlemain);
                title2.setText(TextUtils.isEmpty(titlemain1) ? "" : titlemain1);
                linev.setBackgroundColor(Constants.BGCOLORS[new Random().nextInt(10)]);
            } else {
                titlelayout.setVisibility(View.GONE);
            }

            HomeView.addView(homeView);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private float downNub;//记录按下时的距离

    public void OnImageViewClick(View view, final String type, final String data) {
        view.setOnTouchListener((v, event) -> {
            boolean flag = false;
            if (-1 != SystemHelper.getNetworkType(SpecialActivity.this)) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    //  触摸时按下
                    downNub = event.getX();
                } else if (event.getAction() == MotionEvent.ACTION_MOVE) {
                    // 触摸时移动
                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    //  触摸时抬起
                    if (downNub == event.getX()) {
                        if (type.equals("keyword")) {//搜索关键字
                            Intent intent = new Intent(SpecialActivity.this, GoodsListFragmentManager.class);
                            intent.putExtra("keyword", data);
                            intent.putExtra("gc_name", data);
                            startActivity(intent);
                        } else if (type.equals("special")) {//专题编号
//                            Intent intent = new Intent(SpecialActivity.this, SubjectWebActivity.class);

//                            intent.putExtra("data", Constants.URL_SPECIAL + "&special_id=" + data + "&type=html");
                            Intent intent = new Intent(SpecialActivity.this, SpecialActivity.class);
                            intent.putExtra("special_id", data);
                            startActivity(intent);
//                            startActivity(intent);
                        } else if (type.equals("goods")) {//商品编号
                            Intent intent = new Intent(SpecialActivity.this, GoodsDetailsActivity.class);
                            intent.putExtra("goods_id", data);
                            startActivity(intent);
                        } else if (type.equals("url")) {//地址
                            Intent intent = new Intent(SpecialActivity.this, SubjectWebActivity.class);
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

    //如果你需要考虑更好的体验，可以这么操作
    @Override
    protected void onStart() {
        super.onStart();
        //开始轮播
        banner.startAutoPlay();
    }

    @Override
    protected void onStop() {
        super.onStop();
        //结束轮播
        banner.stopAutoPlay();
    }

}
