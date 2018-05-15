package com.xinyuangongxiang.shop.ui.fenxiao;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.alibaba.view.BubblingView;
import com.bumptech.glide.Glide;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.adapter.fenxiao.ZhiBoGoodsMaiAdapter;
import com.xinyuangongxiang.shop.adapter.fenxiao.ZhiBoMaiAdapter;
import com.xinyuangongxiang.shop.adapter.fenxiao.ZhiBoTakeMessageAdapter;
import com.xinyuangongxiang.shop.bean.StoreInfo;
import com.xinyuangongxiang.shop.bean.TuijianGoods;
import com.xinyuangongxiang.shop.bean.fenxiao.GoodsList;
import com.xinyuangongxiang.shop.bean.fenxiao.MemberList;
import com.xinyuangongxiang.shop.bean.fenxiao.MsgList;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.DialogHelper;
import com.xinyuangongxiang.shop.common.JsonUtil;
import com.xinyuangongxiang.shop.common.MyShopApplication;
import com.xinyuangongxiang.shop.common.ScreenUtil;
import com.xinyuangongxiang.shop.common.ShopHelper;
import com.xinyuangongxiang.shop.common.StringUtils;
import com.xinyuangongxiang.shop.common.T;
import com.xinyuangongxiang.shop.custom.FenxiaoGoodsSpecPopupWindow;
import com.xinyuangongxiang.shop.custom.GlideCircleTransform;
import com.xinyuangongxiang.shop.custom.MyDecoration;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.ResponseData;
import com.xinyuangongxiang.shop.lib.tab.OnItemClickListener;
import com.xinyuangongxiang.shop.ncinterface.INCOnNumModify;
import com.xinyuangongxiang.shop.ncinterface.INCOnStringModify;
import com.xinyuangongxiang.shop.newpackage.ProgressDialog;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Timer;
import java.util.TimerTask;

import tcking.github.com.giraffeplayer.IjkVideoView;
import tv.danmaku.ijk.media.player.IMediaPlayer;
import tv.danmaku.ijk.media.player.IjkMediaPlayer;

/**
 * Created by snm on 2016/9/19.
 */
public class ZhiBoActivity extends Activity {
    String palyerurl = "";
    LinearLayout app_video_status, app_video_bottom_box, ll_look;
    RelativeLayout zhibao_box;
    private IjkVideoView videoView;

    private boolean isLive = false;//是否为直播
    private Query $;
    private int STATUS_ERROR = -1;
    private int STATUS_IDLE = 0;
    private int STATUS_LOADING = 1;
    private int STATUS_PLAYING = 2;
    private int STATUS_PAUSE = 3;
    private int STATUS_COMPLETED = 4;

    private static final int MESSAGE_SHOW_PROGRESS = 1;
    private static final int MESSAGE_FADE_OUT = 2;
    private static final int MESSAGE_SEEK_NEW_POSITION = 3;
    private static final int MESSAGE_HIDE_CENTER_BOX = 4;
    private static final int MESSAGE_RESTART_PLAY = 5;
    private int status = STATUS_IDLE;
    private long defaultRetryTime = 5000;
    private BubblingView bubbling_view;
    private ImageView zhibo_zam, zhibo_close, zhibo_share, zhibo_head;
    private TextView zhibo_title, zhibo_look_man, zhibo_goods, good_fenxiao_mun;
    private TextView zhibo_et_talk;
    ArrayList<GoodsList> goodsLists = new ArrayList<GoodsList>();
    private RecyclerView recyclerView, goods_recycler_view;
    private ListView recycler_take;
    private int[] images = {
            R.drawable.heart0,
            R.drawable.heart1,
            R.drawable.heart2,
            R.drawable.heart3,
            R.drawable.heart4,
            R.drawable.heart5,
            R.drawable.heart6,
            R.drawable.heart7,
            R.drawable.heart8
    };
    int i = 0;

    private ZhiBoMaiAdapter zhiBoMaiAdapter;
    private ZhiBoGoodsMaiAdapter zhiBoGoodsMaiAdapter;
    private ZhiBoTakeMessageAdapter zhiBoTakeMessageAdapter;
    private String live_id, live_url;
    private LinearLayout ll_toke_layout;
    private RelativeLayout rl_context;
    private Timer timer = null;
    //    private TimerTask task;
    private String headurl = null;

    Handler handlertst = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            // TODO Auto-generated method stub
            // 要做的事情
            getLiveMember();
            getget_chat();
            super.handleMessage(msg);
        }
    };

    String dis_memberid, live_stat_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_zhibo);

        live_id = getIntent().getStringExtra("live_id");
        live_url = getIntent().getStringExtra("live_url");
        Logger.d(live_url);
        palyerurl = live_url;
//        palyerurl = "http://live.shopnctest.com/shopnc/test1.m3u8";
        try {
            IjkMediaPlayer.loadLibrariesOnce(null);
            IjkMediaPlayer.native_profileBegin("libijkplayer.so");
        } catch (Throwable e) {
            Logger.e(e.getMessage());
        }
        $ = new Query(ZhiBoActivity.this, null);
        initView();
        setOnClick();
        initPopwindow();
    }

    private void initView() {
        /*视频错误信息*/
        app_video_status = (LinearLayout) findViewById(R.id.app_video_status);
        /*聊天*/
        ll_toke_layout = (LinearLayout) findViewById(R.id.ll_toke_layout);
        /*底部box*/
        app_video_bottom_box = (LinearLayout) findViewById(R.id.app_video_bottom_box);
        /*看多少人layout*/
        ll_look = (LinearLayout) findViewById(R.id.ll_look);
        app_video_status.setVisibility(View.GONE);
        bubbling_view = (BubblingView) findViewById(R.id.bubbling_view);
        int heightPixels = getResources().getDisplayMetrics().heightPixels;
        int widthPixels = getResources().getDisplayMetrics().widthPixels;
        $.id(tcking.github.com.giraffeplayer.R.id.app_video_box).height(Math.min(heightPixels, widthPixels), false);

        videoView = (IjkVideoView) findViewById(R.id.video_view);
        videoView.setOnCompletionListener(new IMediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(IMediaPlayer mp) {
//                statusChange(STATUS_COMPLETED);
//                oncomplete.run();
            }
        });
        videoView.setOnErrorListener(new IMediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(IMediaPlayer mp, int what, int extra) {
                statusChange(STATUS_ERROR);
//                onErrorListener.onError(what,extra);
                return true;
            }
        });
        videoView.setOnInfoListener(new IMediaPlayer.OnInfoListener() {
            @Override
            public boolean onInfo(IMediaPlayer mp, int what, int extra) {
                switch (what) {
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_START:
                        statusChange(STATUS_LOADING);
                        break;
                    case IMediaPlayer.MEDIA_INFO_BUFFERING_END:
                        statusChange(STATUS_PLAYING);
                        break;
                    case IMediaPlayer.MEDIA_INFO_NETWORK_BANDWIDTH:
                        //显示 下载速度
//                        Toaster.show("download rate:" + extra);
                        break;
                    case IMediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START:
                        statusChange(STATUS_PLAYING);
                        break;
                }
//                onInfoListener.onInfo(what,extra);
                return false;
            }
        });

        videoView.setVideoPath(palyerurl);
        videoView.start();

        zhibo_zam = (ImageView) findViewById(R.id.zhibo_zam);
        zhibo_close = (ImageView) findViewById(R.id.zhibo_close);
        zhibo_share = (ImageView) findViewById(R.id.zhibo_share);
        zhibo_head = (ImageView) findViewById(R.id.zhibo_head);

        zhibo_title = (TextView) findViewById(R.id.zhibo_title);
        zhibo_look_man = (TextView) findViewById(R.id.zhibo_look_man);
        zhibo_goods = (TextView) findViewById(R.id.zhibo_goods);
        good_fenxiao_mun = (TextView) findViewById(R.id.good_fenxiao_mun);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        goods_recycler_view = (RecyclerView) findViewById(R.id.goods_recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recyclerView.setLayoutManager(linearLayoutManager);
        zhiBoMaiAdapter = new ZhiBoMaiAdapter(getApplicationContext());

        recyclerView.setAdapter(zhiBoMaiAdapter);
        zhibo_et_talk = (TextView) findViewById(R.id.zhibo_et_talk);
        zhibao_box = (RelativeLayout) findViewById(R.id.zhibao_box);
        rl_context = (RelativeLayout) findViewById(R.id.rl_context);

        /*商品列表 */
        zhiBoGoodsMaiAdapter = new ZhiBoGoodsMaiAdapter(getApplicationContext(), true);

        LinearLayoutManager linearManager = new LinearLayoutManager(getApplicationContext());
        linearManager.setOrientation(LinearLayoutManager.VERTICAL);
        goods_recycler_view.setLayoutManager(linearManager);
        goods_recycler_view.addItemDecoration(new MyDecoration(this, MyDecoration.VERTICAL_LIST));
        goods_recycler_view.setAdapter(zhiBoGoodsMaiAdapter);
        zhiBoGoodsMaiAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(int position) {
                setFocus();
                String commonitid = goodsLists.get(position).getGoods_commonid();
                String goodsid = goodsLists.get(position).getGoods_id();
                Logger.d(commonitid);
//                loadingGoodsDetailsData(commonitid,dis_memberid);
                loadingGoodsDetailsData1(goodsid, commonitid);
            }

            @Override
            public void onLongClick(int position) {

            }
        });

        recycler_take = (ListView) findViewById(R.id.recycler_take);
//        LinearLayoutManager linearManager1 = new LinearLayoutManager(getApplicationContext());
//        linearManager1.setOrientation(LinearLayoutManager.VERTICAL);
//        recycler_take.setLayoutManager(linearManager1);
        zhiBoTakeMessageAdapter = new ZhiBoTakeMessageAdapter(getApplicationContext());

        recycler_take.setAdapter(zhiBoTakeMessageAdapter);
    }

    private void setOnClick() {
        zhibo_zam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bubbling_view.addBubblingItem(images[i++ % 5]);
                setFocus();
            }
        });
        zhibo_close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getlive_close();
            }
        });
        zhibo_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /**开启默认分享面板，分享列表**/
                ShareLibListener.mContent = ZhiBoActivity.this;
//                String url = "http://192.168.1.124:8020/test/radio.html?id="+live_id+"&url=" + live_url;
                String url = Constants.WAP_URL + "distri_live.html?id=" + live_id + "&url=" + live_url;
                String text = getResources().getString(R.string.app_name) + "正在直播地址为" + url;
                String title = getResources().getString(R.string.app_name) + zhibo_title.getText();
                ShareLibListener.goodsWapUrl = headurl;
                ShareLibListener.text = text;
                ShareLibListener.url = url;
                ShareLibListener.title = title;
                new ShareAction(ZhiBoActivity.this).setDisplayList(SHARE_MEDIA.SINA, SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.SMS)
                        .withText(text)
//                        .withTitle(title)
//                        .withTargetUrl(url)
                        .setShareboardclickCallback(ShareLibListener.shareBoardlistener)
                        .open();
                setFocus();
            }
        });
        zhibo_goods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFocus();
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) zhibao_box.getLayoutParams();
                params.height = 350;
                zhibao_box.setLayoutParams(params);
                zhibao_box.invalidate(); // 刷新界面
                app_video_bottom_box.setVisibility(View.GONE);
                ll_toke_layout.setVisibility(View.GONE);
                ll_look.setVisibility(View.GONE);

            }
        });
        app_video_bottom_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFocus();
            }
        });
        ll_look.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFocus();
            }
        });

//        点击软件盘里面的发送
//        zhibo_et_talk.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            public boolean onEditorAction(TextView v, int actionId,
//                                          KeyEvent event)  {
//                setFocus();
//
//                if (actionId == EditorInfo.IME_ACTION_SEND
//                        ||(event!=null&&event.getKeyCode()== KeyEvent.KEYCODE_ENTER)) {
//                    //do something;
//                    sendText();
//                    return true;
//                }
//                return false;
//            }
//        });
        zhibo_et_talk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopupWindow();
                InputTools.ShowKeyboard(et_talk);
                setFocus();
            }
        });

        rl_context.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFocus();
            }
        });

        zhibao_box.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBox();
            }
        });
        videoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBox();
            }
        });

        ll_toke_layout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setFocus();
            }
        });

        app_video_status.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showBox();
            }
        });
    }

    private void sendText() {
        final String searchStr = et_talk.getText().toString().trim();
        if (StringUtils.isEmpty(searchStr)) {
            T.showShort(getApplicationContext(), "请输入关键字");
        } else {
            getsendChat(searchStr);
        }
    }

    private void showBox() {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) zhibao_box.getLayoutParams();
        params.height = ScreenUtil.getScreenHeight(getApplicationContext()) - ScreenUtil.getScreenBarHeight(ZhiBoActivity.this);
        zhibao_box.setLayoutParams(params);
        zhibao_box.invalidate(); // 刷新界面
        app_video_bottom_box.setVisibility(View.VISIBLE);
        ll_toke_layout.setVisibility(View.VISIBLE);
        ll_look.setVisibility(View.VISIBLE);
        setFocus();
    }

    private void setFocus() {
        zhibao_box.clearFocus();//失去焦点
        et_talk.setFocusable(true);
        et_talk.setFocusableInTouchMode(true);
        et_talk.requestFocus();
        et_talk.findFocus();
    }

    private void loadData() {
//        Glide.with(getApplicationContext()).load("http://img.firefoxchina.cn/2016/09/4/201609200816220.jpg").transform(new GlideCircleTransform(getApplicationContext())).into(zhibo_head);
//        zhibo_title.setText("我们爱运动");
//        zhibo_look_man.setText("111人观看");

        getFristDate();
        getget_chat();
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();

            if (isShouldHideKeyboard(v, ev)) {

                InputTools.KeyBoard(et_talk, "close");
            }
            return super.dispatchTouchEvent(ev);
        }
        // 必不可少，否则所有的组件都不会有TouchEvent了
        if (getWindow().superDispatchTouchEvent(ev)) {
            return true;
        }
        return onTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (ZhiBoActivity.this.getCurrentFocus() != null) {
                if (ZhiBoActivity.this.getCurrentFocus().getWindowToken() != null) {
                    InputTools.KeyBoard(et_talk, "close");
                }
            }
        }
        return super.onTouchEvent(event);
    }

    /**
     * 根据EditText所在坐标和用户点击的坐标相对比，来判断是否隐藏键盘，因为当用户点击EditText时则不能隐藏
     *
     * @param v
     * @param event
     * @return
     */
    private boolean isShouldHideKeyboard(View v, MotionEvent event) {
        if (v != null && (v instanceof EditText)) {
            int[] l = {0, 0};
            v.getLocationInWindow(l);
            int left = l[0],
                    top = l[1],
                    bottom = top + v.getHeight(),
                    right = left + v.getWidth();
            if (event.getX() > left && event.getX() < right
                    && event.getY() > top && event.getY() < bottom) {
                // 点击EditText的事件，忽略它。
                return false;
            } else {
                return true;
            }
        }
        // 如果焦点不是EditText则忽略，这个发生在视图刚绘制完，第一个焦点不在EditText上，和用户用轨迹球选择其他的焦点
        return false;
    }

    /**
     * 获取InputMethodManager，隐藏软键盘
     *
     * @param token
     */
    private void hideKeyboard(IBinder token) {
        if (token != null) {
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(token, InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    class Query {
        private Activity activity = null;
        private View view;
        private View view1;

        public Query(Activity mActivity, View v) {
            if (v != null) {
                this.view1 = v;
            } else {
                this.activity = mActivity;
            }
        }

        public Query id(int id) {
            if (view1 != null) {
                view = view1.findViewById(id);
            } else {
                view = activity.findViewById(id);
            }

            return this;
        }

        public Query image(int resId) {
            if (view instanceof ImageView) {
                ((ImageView) view).setImageResource(resId);
            }
            return this;
        }

        public Query visible() {
            if (view != null) {
                view.setVisibility(View.VISIBLE);
            }
            return this;
        }

        public Query gone() {
            if (view != null) {
                view.setVisibility(View.GONE);
            }
            return this;
        }

        public Query invisible() {
            if (view != null) {
                view.setVisibility(View.INVISIBLE);
            }
            return this;
        }

        public Query clicked(View.OnClickListener handler) {
            if (view != null) {
                view.setOnClickListener(handler);
            }
            return this;
        }

        public Query text(CharSequence text) {
            if (view != null && view instanceof TextView) {
                ((TextView) view).setText(text);
            }
            return this;
        }

        public Query visibility(int visible) {
            if (view != null) {
                view.setVisibility(visible);
            }
            return this;
        }

        private void size(boolean width, int n, boolean dip) {
            if (view != null) {
                ViewGroup.LayoutParams lp = view.getLayoutParams();
                if (n > 0 && dip) {
                    n = dip2pixel(activity, n);
                }
                if (width) {
                    lp.width = n;
                } else {
                    lp.height = n;
                }
                view.setLayoutParams(lp);
            }
        }

        public void height(int height, boolean dip) {
            size(false, height, dip);
        }

        public int dip2pixel(Context context, float n) {
            int value = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, n, context.getResources().getDisplayMetrics());
            return value;
        }

        public float pixel2dip(Context context, float n) {
            Resources resources = context.getResources();
            DisplayMetrics metrics = resources.getDisplayMetrics();
            float dp = n / (metrics.densityDpi / 160f);
            return dp;

        }
    }

    private void statusChange(int newStatus) {
        status = newStatus;
        if (!isLive && newStatus == STATUS_COMPLETED) {
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            hideAll();
//            $.id(R.id.app_video_replay).visible();
        } else if (newStatus == STATUS_ERROR) {
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
            hideAll();
            if (isLive) {
                showStatus(getResources().getString(R.string.live_error));
                if (defaultRetryTime > 0) {
                    handler.sendEmptyMessageDelayed(MESSAGE_RESTART_PLAY, defaultRetryTime);
                }
            } else {
                showStatus(getResources().getString(R.string.live_error));
            }
        } else if (newStatus == STATUS_LOADING) {
            hideAll();
            $.id(R.id.app_video_loading).visible();
        } else if (newStatus == STATUS_PLAYING) {
            hideAll();
        }

    }

    private void showStatus(String statusText) {
        $.id(R.id.app_video_status).visible();
        $.id(R.id.app_video_status_text).text(statusText);
    }

    private void hideAll() {
        $.id(R.id.app_video_loading).gone();
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_FADE_OUT:
                    hide(false);
                    break;
                case MESSAGE_HIDE_CENTER_BOX:
                    break;
                case MESSAGE_SEEK_NEW_POSITION:

                    break;
                case MESSAGE_SHOW_PROGRESS:

                    break;
                case MESSAGE_RESTART_PLAY:
                    videoView.start();
                    break;
            }
        }
    };

    public void hide(boolean force) {
        if (force) {
            handler.removeMessages(MESSAGE_SHOW_PROGRESS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.removeAllViews();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (ShopHelper.isLogin(ZhiBoActivity.this, MyShopApplication.getInstance().getLoginKey())) {
            loadData();
        } else {
            T.showShort(getApplicationContext(), "请登录查看");
        }
        if (timer == null) {
            timer = new Timer();
        }
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                Message message = new Message();
                message.what = 1;
                handlertst.sendMessage(message);
            }
        }, 2000, 2000);
    }

    //    TODO
    @Override
    protected void onStop() {
        super.onStop();
        getlive_close();
        timer.cancel();
        timer.purge();
        timer = null;
    }

    /*分享回调*/

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /** 分享回调**/
        UMShareAPI.get(ZhiBoActivity.this).onActivityResult(requestCode, resultCode, data);

    }


    /*添加购物车pop*/

    //商品数量修改回调
    private INCOnNumModify incOnNumModify;
    private int goodsNum = 1; //商品数量
    private int goodsLimit = 0;
    private String ifcart = "0";//购物车购买标志 1购物车 0不是
    private String t_id, t_name; //记录商家ID 名称
    //商品规格修改回调
    private INCOnStringModify incOnStringModify;
    private FenxiaoGoodsSpecPopupWindow pwSpec;
    private String goods_id;
    Dialog progressDialog;

    /**
     * 初始化规格
     */
    private void initSpec(TuijianGoods goodsBean, String distri_id) {
        incOnNumModify = new INCOnNumModify() {
            @Override
            public void onModify(int num) {
//                goodsNum = num;
            }
        };

        incOnStringModify = new INCOnStringModify() {
            @Override
            public void onModify(String str) {
                goods_id = str;
//                loadingGoodsDetailsData1(goods_id);
                loadingGoodsDetailsData2(str);
            }


        };

        //限购数量
//        if (goodsBean.getUpper_limit() == null || goodsBean.getUpper_limit().equals("") || goodsBean.getUpper_limit().equals("0")) {
//            goodsLimit = Integer.parseInt((goodsBean.getGoods_storage() == null ? "0" : goodsBean.getGoods_storage()) == "" ? "0" : goodsBean.getGoods_storage());
//        }
        if (goodsBean.getGoods_info().getUpper_limit() == null || goodsBean.getGoods_info().getUpper_limit().equals("") || goodsBean.getGoods_info().getUpper_limit().equals("0")) {
            goodsLimit = Integer.parseInt((goodsBean.getGoods_info().getGoods_storage() == null ? "0" : goodsBean.getGoods_info().getGoods_storage()) == "" ? "0" : goodsBean.getGoods_info().getGoods_storage());
        }

        if (pwSpec == null) {
            pwSpec = new FenxiaoGoodsSpecPopupWindow(this, incOnNumModify, incOnStringModify);
        }
        ifcart = goodsBean.getGoods_info().getCart() + "";
        String goods_image = Arrays.asList(goodsBean.getGoods_image().split(",")).get(0);
//        Logger.d(goods_image);
        String store_info = goodsBean.getStore_info();
        com.xinyuangongxiang.shop.bean.StoreInfo storeInfo = StoreInfo.newInstanceList(store_info);
        t_id = storeInfo.getStoreId();
        t_name = storeInfo.getMemberName();
        pwSpec.setGoodsInfo(goodsBean.getGoods_info().getGoods_name(), goods_image, goodsBean.getGoods_info().getGoods_price(),
                goodsBean.getGoods_info().getGoods_storage(), goodsBean.getGoods_info().getGoods_id(), ifcart, goodsNum, goodsLimit,
                goodsBean.getGoods_info().getIs_fcode(), goodsBean.getGoods_info().getIs_virtual(), t_id, t_name);

        String spec_name = goodsBean.getGoods_info().getSpec_name();
        String spec_value = goodsBean.getGoods_info().getSpec_value();
        String goods_spec = goodsBean.getGoods_info().getGoods_spec();

        pwSpec.setSpecInfo(goodsBean.getSpec_list(), spec_name, spec_value, goods_spec);

        pwSpec.setFenxiao(distri_id);
    }

    private void loadingGoodsDetailsData(String goods_commonid, String dis_memberid) {
        progressDialog = ProgressDialog.showLoadingProgress(ZhiBoActivity.this, "正在加载中...");
        progressDialog.show();
        String url = Constants.URL_GET_SPEC;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("goods_commonid", goods_commonid);
        params.put("dis_memberid", dis_memberid);
        Logger.d(params.toString());
        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
//                 Logger.d(json);
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        String goods_id = JsonUtil.getString(json, "goods_id");
                        String distri_id = JsonUtil.getString(json, "distri_id");
                        loadingGoodsDetailsData1(goods_id, distri_id);
                    } catch (Exception e) {
                        ShopHelper.showApiError(getApplicationContext(), "加载失败");
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(getApplicationContext(), json);
                }
                ProgressDialog.dismissDialog(progressDialog);
            }
        });

    }

    @Override
    protected void onRestart() {
        super.onRestart();
        timer = null;
    }

    private void loadingGoodsDetailsData1(String goods_commonid, final String distri_id) {
        if (progressDialog != null) {
            progressDialog.show();
        } else {
            progressDialog = ProgressDialog.showLoadingProgress(ZhiBoActivity.this, "正在加载中...");
        }
        String url = Constants.URL_RECOMMEND_GOODS_DETAIL + "&goods_id=" + goods_commonid + "&key=" + MyShopApplication.getInstance().getLoginKey();

        Logger.d(url);

        RemoteDataHandler.asyncDataStringGet(url, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();

                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        String goods_detail = JsonUtil.getString(json, "goods_detail");
                        JSONObject obj = new JSONObject(goods_detail);

                        TuijianGoods goods = TuijianGoods.getInstance(obj);
                        initSpec(goods, distri_id);
                        if (!pwSpec.isShowing()) {
                            pwSpec.showPopupWindow();
                        }
                    } catch (Exception e) {
                        ShopHelper.showApiError(getApplicationContext(), "加载失败");
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(getApplicationContext(), json);
                }
               
                ProgressDialog.dismissDialog(progressDialog);
            }
        });

    }

    private void loadingGoodsDetailsData2(String goods_id) {
        if (progressDialog != null) {
            progressDialog.show();
        } else {
            progressDialog = ProgressDialog.showLoadingProgress(ZhiBoActivity.this, "正在加载中...");
        }
        String url = Constants.URL_GET_GOODS;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("goods_id", goods_id);
        params.put("dis_memberid", dis_memberid);
        Logger.d(params.toString());
        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {

            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();

                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        String distri_id = JsonUtil.getString(json, "distri_id");
                        String goods_id = JsonUtil.getString(json, "goods_id");
                        loadingGoodsDetailsData1(goods_id, distri_id);
                    } catch (Exception e) {
                        ProgressDialog.dismissDialog(progressDialog);
                        ShopHelper.showApiError(getApplicationContext(), "加载失败");
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(getApplicationContext(), json);
                }
                ProgressDialog.dismissDialog(progressDialog);
            }
        });

    }

    /*进入直播观看（*/
    public void getFristDate() {
        String url = Constants.URL_LIVE_INFO;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("live_id", live_id);
        Logger.d(params.toString());
        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
//                Logger.d(json);
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject jsonObj = new JSONObject(json);
                        String member_count = jsonObj.getString("member_count"); //观看人数
                        String goods_count = jsonObj.getString("goods_count");  //直播商品数
                        String live_member_info = jsonObj.getString("live_member_info");  //播主信息
                        String member_list = jsonObj.getString("member_list");  //观看会员信息列表
                        String goods_list = jsonObj.getString("goods_list");   //直播商品列表
                        live_stat_id = jsonObj.getString("live_stat_id");   //

                        ArrayList<MemberList> memberLists = JsonUtil.getBean(member_list, new TypeToken<ArrayList<MemberList>>() {
                        }.getType());
                        goodsLists = JsonUtil.getBean(goods_list, new TypeToken<ArrayList<GoodsList>>() {
                        }.getType());
                        if (memberLists != null) {
                            zhiBoMaiAdapter.setmList(memberLists);
                        }
//                        TODO
                        if (goodsLists != null) {
                            zhiBoGoodsMaiAdapter.setmList(goodsLists);
                        }

                        JSONObject live_member_infoObj = new JSONObject(live_member_info);
                        String movie_play_url = live_member_infoObj.getString("movie_play_url");   //直播地址
                        String movie_title = live_member_infoObj.getString("movie_title");   //标题
                        String member_avatar = live_member_infoObj.getString("member_avatar");   //播主头像
                        String movie_state = live_member_infoObj.getString("movie_state");   //播放状态
                        String movie_cover_img = live_member_infoObj.getString("movie_cover_img");   //播放状态

                        dis_memberid = live_member_infoObj.getString("member_id");
                        headurl = movie_cover_img;
                        Glide.with(getApplicationContext()).load(member_avatar).transform(new GlideCircleTransform(getApplicationContext())).into(zhibo_head);
                        zhibo_title.setText(movie_title);
                        zhibo_look_man.setText(member_count + "人观看");
                        zhibo_goods.setText("商品 " + goods_count);
                        good_fenxiao_mun.setText(goods_count);
//                        if("1".equals(movie_state)){
//
//                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(getApplicationContext(), json);
                }

            }
        });

    }

    /*获取直播观看用户列表*/
    public void getLiveMember() {
        String url = Constants.URL_LIVE_MEMBER;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("live_id", live_id);
        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
//                Logger.d(data.toString());
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject jsonObj = new JSONObject(json);
                        String member_list = jsonObj.getString("member_list");
                        String member_count = jsonObj.getString("member_count");
                        zhibo_look_man.setText(member_count + "人观看");
                        ArrayList<MemberList> memberLists = JsonUtil.getBean(member_list, new TypeToken<ArrayList<MemberList>>() {
                        }.getType());
                        if (memberLists != null) {
                            zhiBoMaiAdapter.setmList(memberLists);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(getApplicationContext(), json);
                }

            }
        });

    }

    /*获取聊天列表*/
    public void getget_chat() {
        String url = Constants.URL_GET_CHAT;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("live_id", live_id);
        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
//            Logger.d(data.toString());
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject jsonObj = new JSONObject(json);
                        String msg_list = jsonObj.getString("msg_list");
                        ArrayList<MsgList> msgLists = JsonUtil.getBean(msg_list, new TypeToken<ArrayList<MsgList>>() {
                        }.getType());
//                    ComparatorMsg comparatorMsg = new ComparatorMsg();
//                    Collections.sort(msgLists, comparatorMsg);
                        Collections.reverse(msgLists);
                        zhiBoTakeMessageAdapter.setmList(msgLists);
                        recycler_take.setSelection(msgLists.size());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(getApplicationContext(), json);
                }
            }
        });

    }

    /*发送聊天信息*/
    public void getsendChat(String mst_txt) {
        String url = Constants.URL_SEND_CHAT;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("live_id", live_id);
        params.put("msg_txt", mst_txt);
        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
//                Logger.d(data.toString());
                if (data.getCode() == HttpStatus.SC_OK) {
                    closeInput();
                    et_talk.setText("");
                    getget_chat();
                } else {
                    ShopHelper.showApiError(getApplicationContext(), json);
                }

            }
        });

    }

    /*退出直播观看*/
    public void getlive_close() {
        String url = Constants.URL_LIVE_CLOSE;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("live_id", live_id);
        params.put("live_stat_id", live_stat_id);
        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
//                Logger.d(data.toString());
                if (data.getCode() == HttpStatus.SC_OK) {
                    if ("1".equals(json)) {
                        finish();
                    } else {
                        T.showShort(getApplicationContext(), "退出失败");
                    }
                } else {
                    ShopHelper.showApiError(getApplicationContext(), json);
                }
            }
        });
    }

    private PopupWindow popupWindow;
    private View view;
    private MyEditText et_talk;
    private TextView tv_zhibo_send;

    public void initPopwindow() {
        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_live_camera_editview, null);
        popupWindow = DialogHelper.getAllPopupWindow(ZhiBoActivity.this, view);
        tv_zhibo_send = (TextView) view.findViewById(R.id.tv_zhibo_send);
        tv_zhibo_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendText();
            }
        });
        et_talk = (MyEditText) view.findViewById(R.id.zhibo_my_et_talk);
        et_talk.setBackListener(new MyEditText.BackListener() {
            @Override
            public void back(TextView textView) {
                if (popupWindow != null && popupWindow.isShowing()) {
                    closeInput();
                }
            }
        });
    }

    private void closeInput() {
        popupWindow.dismiss();
        InputTools.KeyBoard(et_talk, "close");
    }

    /**
     * 显示popupwindow
     */
    private void showPopupWindow() {
        popupWindow.showAtLocation(zhibo_et_talk, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);//这种方式无论有虚拟按键还是没有都可完全显示，因为它显示的在整个父布局中
        InputTools.KeyBoard(et_talk, "open");
        et_talk.setFocusable(true);
        et_talk.setFocusableInTouchMode(true);
        et_talk.requestFocus();
        et_talk.findFocus();


    }

}
