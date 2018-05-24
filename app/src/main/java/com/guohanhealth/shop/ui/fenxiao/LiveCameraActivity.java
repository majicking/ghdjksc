package com.guohanhealth.shop.ui.fenxiao;


//┏┓　　　┏┓
//┏┛┻━━━┛┻┓
//┃　　　　　　　┃ 　
//┃　　　━　　　┃
//┃　┳┛　┗┳　┃
//┃　　　　　　　┃
//┃　　　┻　　　┃
//┃　　　　　　　┃
//┗━┓　　　┏━┛
//  ┃　　　┃   神兽保佑　　　　　　　　
//  ┃　　　┃   代码无BUG！
//  ┃　　　┗━━━┓
//  ┃　　　　　　　┣┓
//  ┃　　　　　　　┏┛
//  ┗┓┓┏━┳┓┏┛
//    ┃┫┫　┃┫┫
//    ┗┻┛　┗┻┛

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.PermissionChecker;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.livecloud.live.AlivcMediaFormat;
import com.alibaba.livecloud.live.AlivcMediaRecorder;
import com.alibaba.livecloud.live.AlivcMediaRecorderFactory;
import com.alibaba.livecloud.live.AlivcRecordReporter;
import com.alibaba.livecloud.live.AlivcStatusCode;
import com.alibaba.livecloud.live.OnLiveRecordErrorListener;
import com.alibaba.livecloud.live.OnNetworkStatusListener;
import com.alibaba.livecloud.live.OnRecordStatusListener;
import com.bumptech.glide.Glide;
import com.duanqu.qupai.logger.DataStatistics;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;
import com.guohanhealth.shop.common.StringUtils;
import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareAPI;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.adapter.fenxiao.ZhiBoGoodsMaiAdapter;
import com.guohanhealth.shop.adapter.fenxiao.ZhiBoMaiAdapter;
import com.guohanhealth.shop.adapter.fenxiao.ZhiBoTakeMessageAdapter;
import com.guohanhealth.shop.bean.fenxiao.GoodsList;
import com.guohanhealth.shop.bean.fenxiao.MemberList;
import com.guohanhealth.shop.bean.fenxiao.MsgList;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.DialogHelper;
import com.guohanhealth.shop.common.JsonUtil;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ScreenUtil;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.common.T;
import com.guohanhealth.shop.custom.GlideCircleTransform;
import com.guohanhealth.shop.custom.MyDecoration;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.ResponseData;
import com.guohanhealth.shop.lib.popupwindow.BubbleRelativeLayout;
import com.guohanhealth.shop.lib.tab.OnItemClickListener;
import com.guohanhealth.shop.xrefresh.utils.LogUtils;

import org.apache.http.HttpStatus;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;


/**
 * author:snm
 * date:2016/6/27
 * description:LiveCameraActivity
 */
public class LiveCameraActivity extends Activity implements View.OnClickListener {

    private static final String TAG = "LiveCameraActivity";

    public final static String URL = "url";
    public final static String VIDEO_RESOLUTION = "video_resolution";
    public final static String SCREENORIENTATION = "screen_orientation";
    public final static String FRONT_CAMERA_FACING = "front_camera_face";
    private String member_id = "";
    private String member_name = "";
    private String video_id = "";

    private static final int PERMISSION_REQUEST_CODE = 1;
    private static final String[] permissionManifest = {
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
    };

    private SurfaceView _CameraSurface;
    private AlivcMediaRecorder mMediaRecorder;
    private AlivcRecordReporter mRecordReporter;

    private Surface mPreviewSurface;
    private Map<String, Object> mConfigure = new HashMap<>();
    private boolean isRecording = false;
    private int mPreviewWidth = 0;
    private int mPreviewHeight = 0;
    private DataStatistics mDataStatistics = new DataStatistics(1000);

    public static void startActivity(Context context, String rtmpUrl, int videoResolution, boolean screenOrientation1,
                                     int cameraFacingFront, String member_id, String member_name, String video_id) {
        Intent intent = new Intent(context, LiveCameraActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(URL, rtmpUrl);
        intent.putExtra(VIDEO_RESOLUTION, videoResolution);
        intent.putExtra(SCREENORIENTATION, screenOrientation1);
        intent.putExtra(FRONT_CAMERA_FACING, cameraFacingFront);
        intent.putExtra("member_id", member_id);
        intent.putExtra("member_name", member_name);
        intent.putExtra("video_id", video_id);
        context.startActivity(intent);
    }

    /*新添加*/
    private ImageView zhibo_zam, zhibo_close, zhibo_share, zhibo_head;
    private TextView zhibo_title, zhibo_look_man, zhibo_goods, good_fenxiao_mun;
    private TextView zhibo_et_talk;

    private Timer timer = new Timer();

    private String headurl, live_url;

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
    private RecyclerView recycler_view, goods_recycler_view;
    private ListView recycler_take;

    private ZhiBoMaiAdapter zhiBoMaiAdapter;
    private ZhiBoGoodsMaiAdapter zhiBoGoodsMaiAdapter;
    private ZhiBoTakeMessageAdapter zhiBoTakeMessageAdapter;
    private ArrayList<GoodsList> goodsLists = new ArrayList<GoodsList>();
    private BubbleRelativeLayout bubbleRelativeLayout;
    private RelativeLayout zhibao_box, rl_all_box;
    private LinearLayout app_video_bottom_box, ll_toke_layout, ll_look;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        RecordLoggerManager.createLoggerFile();
        setContentView(R.layout.activity_live_camera_test);
        if (Build.VERSION.SDK_INT >= 23) {
            permissionCheck();
        }

        getExtraData();

        initView();
        /*横和竖屏*/
        setRequestedOrientation(screenOrientation ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        //采集
        _CameraSurface = (SurfaceView) findViewById(R.id.camera_surface);
        _CameraSurface.getHolder().addCallback(_CameraSurfaceCallback);
        _CameraSurface.setOnTouchListener(mOnTouchListener);
        /* 设置高度*/
        ViewGroup.LayoutParams lp1 = _CameraSurface.getLayoutParams();
        lp1.height = ScreenUtil.getScreenHeight(getApplicationContext()) - ScreenUtil.getScreenBarHeight(LiveCameraActivity.this);
        _CameraSurface.setLayoutParams(lp1);

        //对焦，缩放
        mDetector = new GestureDetector(_CameraSurface.getContext(), mGestureDetector);
        mScaleDetector = new ScaleGestureDetector(_CameraSurface.getContext(), mScaleGestureListener);

        mMediaRecorder = AlivcMediaRecorderFactory.createMediaRecorder();
        mMediaRecorder.init(this);
        mDataStatistics.setReportListener(mReportListener);

        /**
         * this method only can be called after mMediaRecorder.init(),
         * otherwise,  will return null;
         */
        mRecordReporter = mMediaRecorder.getRecordReporter();

        mDataStatistics.start();
        mMediaRecorder.setOnRecordStatusListener(mRecordStatusListener);
        mMediaRecorder.setOnNetworkStatusListener(mOnNetworkStatusListener);
        mMediaRecorder.setOnRecordErrorListener(mOnErrorListener);

        mConfigure.put(AlivcMediaFormat.KEY_CAMERA_FACING, cameraFrontFacing);
        mConfigure.put(AlivcMediaFormat.KEY_MAX_ZOOM_LEVEL, 3);
        mConfigure.put(AlivcMediaFormat.KEY_OUTPUT_RESOLUTION, resolution);
        mConfigure.put(AlivcMediaFormat.KEY_MAX_VIDEO_BITRATE, 800000);
        mConfigure.put(AlivcMediaFormat.KEY_DISPLAY_ROTATION, screenOrientation ? AlivcMediaFormat.DISPLAY_ROTATION_90 : AlivcMediaFormat.DISPLAY_ROTATION_0);
        mConfigure.put(AlivcMediaFormat.KEY_EXPOSURE_COMPENSATION, 20);//曝光度

        initPopwindow();
    }

    private String pushUrl;
    private int resolution;
    private boolean screenOrientation;
    private int cameraFrontFacing;

    private void getExtraData() {
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            pushUrl = bundle.getString(URL);
            resolution = bundle.getInt(VIDEO_RESOLUTION);
            screenOrientation = bundle.getBoolean(SCREENORIENTATION);
            cameraFrontFacing = bundle.getInt(FRONT_CAMERA_FACING);
            member_id = bundle.getString("member_id");
            member_name = bundle.getString("member_name");
            video_id = bundle.getString("video_id");
        }
    }

    private void permissionCheck() {
        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (String permission : permissionManifest) {
            if (PermissionChecker.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                permissionCheck = PackageManager.PERMISSION_DENIED;
            }
        }
        if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissionManifest, PERMISSION_REQUEST_CODE);
        }
    }


    private void initView() {
        zhibo_head = (ImageView) findViewById(R.id.zhibo_head);
        zhibo_share = (ImageView) findViewById(R.id.zhibo_share);
        zhibo_close = (ImageView) findViewById(R.id.zhibo_close);
        zhibo_title = (TextView) findViewById(R.id.zhibo_title);
        zhibo_zam = (ImageView) findViewById(R.id.zhibo_zam);
        zhibo_look_man = (TextView) findViewById(R.id.zhibo_look_man);
        zhibo_goods = (TextView) findViewById(R.id.zhibo_goods);
        good_fenxiao_mun = (TextView) findViewById(R.id.good_fenxiao_mun);
        zhibo_et_talk = (TextView) findViewById(R.id.zhibo_et_talk);
        zhibo_et_talk.setOnClickListener(this);
        recycler_view = (RecyclerView) findViewById(R.id.recycler_view);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        recycler_view.setLayoutManager(linearLayoutManager);
        zhiBoMaiAdapter = new ZhiBoMaiAdapter(getApplicationContext());

        recycler_view.setAdapter(zhiBoMaiAdapter);

        recycler_take = (ListView) findViewById(R.id.recycler_take);

        zhiBoTakeMessageAdapter = new ZhiBoTakeMessageAdapter(getApplicationContext());

        recycler_take.setAdapter(zhiBoTakeMessageAdapter);

        goods_recycler_view = (RecyclerView) findViewById(R.id.goods_recycler_view);

        zhiBoGoodsMaiAdapter = new ZhiBoGoodsMaiAdapter(getApplicationContext(), false);

        LinearLayoutManager linearManager = new LinearLayoutManager(getApplicationContext());
        linearManager.setOrientation(LinearLayoutManager.VERTICAL);
        goods_recycler_view.setLayoutManager(linearManager);
        goods_recycler_view.addItemDecoration(new MyDecoration(this, MyDecoration.VERTICAL_LIST));
        goods_recycler_view.setAdapter(zhiBoGoodsMaiAdapter);
        zhiBoGoodsMaiAdapter.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onClick(int position) {

            }

            @Override
            public void onLongClick(int position) {

            }
        });

        zhibo_share.setOnClickListener(this);
        zhibo_close.setOnClickListener(this);
        zhibo_zam.setOnClickListener(this);
        zhibo_goods.setOnClickListener(this);


        bubbleRelativeLayout = (BubbleRelativeLayout) findViewById(R.id.brlBackground);
        zhibao_box = (RelativeLayout) findViewById(R.id.zhibao_box);
        rl_all_box = (RelativeLayout) findViewById(R.id.rl_all_box);
        zhibao_box.setOnClickListener(this);
        rl_all_box.setOnClickListener(this);

        bubbleView = LayoutInflater.from(this).inflate(R.layout.view_popup_window, null);
        bubbleRelativeLayout.addView(bubbleView);
        BubbleRelativeLayout.BubbleLegOrientation orientation = BubbleRelativeLayout.BubbleLegOrientation.BOTTOM;

        bubbleRelativeLayout.setBubbleParams(orientation, 1000); // 设置气泡布局方向及尖角偏移
        LinearLayout ll_takephone = (LinearLayout) bubbleView.findViewById(R.id.ll_takephone);
        LinearLayout ll_meiyan = (LinearLayout) bubbleView.findViewById(R.id.ll_meiyan);
        LinearLayout ll_shanguandeng = (LinearLayout) bubbleView.findViewById(R.id.ll_shanguandeng);

        iv_takephone = (ImageView) bubbleView.findViewById(R.id.iv_takephone);
        iv_meiyan = (ImageView) bubbleView.findViewById(R.id.iv_meiyan);
        iv_shanguandeng = (ImageView) bubbleView.findViewById(R.id.iv_shanguandeng);

        ll_takephone.setOnClickListener(this);
        ll_meiyan.setOnClickListener(this);
        ll_shanguandeng.setOnClickListener(this);

        app_video_bottom_box = (LinearLayout) findViewById(R.id.app_video_bottom_box);
        ll_toke_layout = (LinearLayout) findViewById(R.id.ll_toke_layout);
        ll_look = (LinearLayout) findViewById(R.id.ll_look);


        //        点击软件盘里面的发送
//        zhibo_et_talk.setOnEditorActionListener(new TextView.OnEditorActionListener() {
//            public boolean onEditorAction(TextView v, int actionId,
//                                          KeyEvent event)  {
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

    }

    private void sendText() {
        String searchStr = et_talk.getText().toString().trim();
        if (StringUtils.isEmpty(searchStr)) {
            T.showShort(getApplicationContext(), "请输入关键字");
        } else {
            getsendChat(searchStr);
        }
    }

    View bubbleView;
    ImageView iv_takephone, iv_meiyan, iv_shanguandeng;

    //TODO
    /*
     * 点击事件
     * */
    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.zhibo_share:
                if (bubbleRelativeLayout.getVisibility() == View.VISIBLE) {
                    bubbleRelativeLayout.setVisibility(View.GONE);
                }
                /**开启默认分享面板，分享列表**/
                ShareLibListener.mContent = LiveCameraActivity.this;
                String url = Constants.                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                              WAP_URL + "distri_live.html?id=" + video_id + "&url=" + live_url;
                String text = getResources().getString(R.string.app_name) + "正在直播地址为" + url;
                String title = getResources().getString(R.string.app_name) + "点击打开即可看【" + zhibo_title.getText() + "】";
                ShareLibListener.goodsWapUrl = headurl;
                ShareLibListener.text = text;
                ShareLibListener.url = url;
                ShareLibListener.title = title;
                new ShareAction(LiveCameraActivity.this)
//                        .setDisplayList(SHARE_MEDIA.SINA, SHARE_MEDIA.QQ, SHARE_MEDIA.QZONE, SHARE_MEDIA.WEIXIN, SHARE_MEDIA.WEIXIN_CIRCLE, SHARE_MEDIA.SMS)
                        .withText(text)
//                        .withTitle(title)
//                        .withTargetUrl(url)
                        .setShareboardclickCallback(ShareLibListener.shareBoardlistener)
                        .open();

                break;
            case R.id.zhibo_close:
                if (bubbleRelativeLayout.getVisibility() == View.VISIBLE) {
                    bubbleRelativeLayout.setVisibility(View.GONE);
                }
                movieLogout();
                mMediaRecorder.stopRecord();
                break;
            case R.id.zhibo_zam:

                if (bubbleRelativeLayout.getVisibility() == View.VISIBLE) {
                    bubbleRelativeLayout.setVisibility(View.GONE);
                } else {
                    bubbleRelativeLayout.setVisibility(View.VISIBLE);
                }

                break;
            case R.id.zhibo_goods:
                showAll();
                break;

            case R.id.zhibao_box:
                showAllview();
                zhibao_box.clearFocus();//失去焦点

                break;
            case R.id.ll_takephone:
                if (takephone) {
                    takephone = false;
                    iv_takephone.setImageDrawable(getResources().getDrawable(R.drawable.fanzhuang_no));
                } else {
                    takephone = true;
                    iv_takephone.setImageDrawable(getResources().getDrawable(R.drawable.fanzhuang_press));
                }
                int currFacing = mMediaRecorder.switchCamera();
                if (currFacing == AlivcMediaFormat.CAMERA_FACING_FRONT) {
                    mMediaRecorder.addFlag(AlivcMediaFormat.FLAG_BEAUTY_ON);
                }
                mConfigure.put(AlivcMediaFormat.KEY_CAMERA_FACING, currFacing);
                break;
            case R.id.ll_meiyan:
                if (is_meitu) {
                    is_meitu = false;
                    iv_meiyan.setImageDrawable(getResources().getDrawable(R.drawable.meiyan_no));
                    mMediaRecorder.removeFlag(AlivcMediaFormat.FLAG_BEAUTY_ON);
                } else {
                    is_meitu = true;
                    iv_meiyan.setImageDrawable(getResources().getDrawable(R.drawable.meiyan_press));
                    mMediaRecorder.addFlag(AlivcMediaFormat.FLAG_BEAUTY_ON);
                }
                break;
            case R.id.ll_shanguandeng:
                if (is_shangguandeng) {
                    is_shangguandeng = false;
                    iv_shanguandeng.setImageDrawable(getResources().getDrawable(R.drawable.shanguandeng_no));
                    mMediaRecorder.removeFlag(AlivcMediaFormat.FALG_FALSH_MODE_ON);
                } else {
                    is_shangguandeng = true;
                    iv_shanguandeng.setImageDrawable(getResources().getDrawable(R.drawable.shangguandeng_press));
                    mMediaRecorder.addFlag(AlivcMediaFormat.FALG_FALSH_MODE_ON);
                }
                break;

            case R.id.zhibo_et_talk:
                if (bubbleRelativeLayout.getVisibility() == View.VISIBLE) {
                    bubbleRelativeLayout.setVisibility(View.GONE);
                }
//                
                showPopupWindow();
                //TODO
                InputTools.ShowKeyboard(et_talk);
                break;
            case R.id.rl_all_box:
                showAllview();
                break;
            case R.id.tv_zhibo_send:
                sendText();
                popupWindow.dismiss();
                InputTools.HideKeyboard(et_talk);
                break;
        }
    }

    private void showAllview() {

        if (bubbleRelativeLayout.getVisibility() == View.VISIBLE) {
            bubbleRelativeLayout.setVisibility(View.GONE);
        }
        LinearLayout.LayoutParams params2 = (LinearLayout.LayoutParams) zhibao_box.getLayoutParams();
        params2.height = ScreenUtil.getScreenHeight(getApplicationContext()) - ScreenUtil.getScreenBarHeight(LiveCameraActivity.this);
        zhibao_box.setLayoutParams(params2);
        zhibao_box.invalidate(); // 刷新界面


        ViewGroup.LayoutParams lp2 = _CameraSurface.getLayoutParams();
        lp2.height = ScreenUtil.getScreenHeight(getApplicationContext()) - ScreenUtil.getScreenBarHeight(LiveCameraActivity.this);

        _CameraSurface.setLayoutParams(lp2);
        ;
        if (app_video_bottom_box.getVisibility() == View.GONE) {
            app_video_bottom_box.setVisibility(View.VISIBLE);
        }
        if (ll_toke_layout.getVisibility() == View.GONE) {
            ll_toke_layout.setVisibility(View.VISIBLE);
        }
        if (ll_look.getVisibility() == View.GONE) {
            ll_look.setVisibility(View.VISIBLE);
        }
    }

    private void showAll() {

        if (bubbleRelativeLayout.getVisibility() == View.VISIBLE) {
            bubbleRelativeLayout.setVisibility(View.GONE);
        }
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) zhibao_box.getLayoutParams();
        params.height = 450;
        zhibao_box.setLayoutParams(params);
        zhibao_box.invalidate(); // 刷新界面


        ViewGroup.LayoutParams lp = _CameraSurface.getLayoutParams();
        lp.height = 450;
        _CameraSurface.setLayoutParams(lp);
//                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

//                LinearLayout.LayoutParams params1 = (LinearLayout.LayoutParams) _CameraSurface.getLayoutParams();
//                params1.height = 350;
//                _CameraSurface.setLayoutParams(params1);
//                _CameraSurface.invalidate(); // 刷新界面

        app_video_bottom_box.setVisibility(View.GONE);
        ll_toke_layout.setVisibility(View.GONE);
        ll_look.setVisibility(View.GONE);
    }


    boolean takephone = false;
    boolean is_meitu = false;
    boolean is_shangguandeng = false;

    public void movieLogout() {
        String url = Constants.URL_MOVIE_LOGOUT + "&key=" + MyShopApplication.getInstance().getLoginKey() + "&video_id=" + video_id;
        RemoteDataHandler.asyncDataStringGet(url, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                Logger.d(data.getJson());
                if (data.getCode() == 200) {
                    if ("1".equals(data.getJson())) {
//                        T.showShort(getApplicationContext(),"退出直播成功");
                        onDestroy();
                    } else {
                        T.showShort(getApplicationContext(), "退出直播失败");
                    }
                } else {
                    ShopHelper.showApiError(getApplicationContext(), data.getJson());
                }
            }
        });
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mPreviewSurface != null) {
            mMediaRecorder.prepare(mConfigure, mPreviewSurface);
            LogUtils.i(" onResume==== isRecording =" + isRecording + "=====");
            if (isRecording) {
                mMediaRecorder.startRecord(pushUrl);
            }
        }
    }

    @Override
    protected void onPause() {
        if (isRecording) {
            mMediaRecorder.stopRecord();
        }
        /**
         * 如果要调用stopRecord和reset()方法，则stopRecord（）必须在reset之前调用，否则将会抛出IllegalStateException
         */
        mMediaRecorder.reset();
        super.onPause();
    }


    private GestureDetector mDetector;
    private ScaleGestureDetector mScaleDetector;
    private GestureDetector.OnGestureListener mGestureDetector = new GestureDetector.OnGestureListener() {
        @Override
        public boolean onDown(MotionEvent motionEvent) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onSingleTapUp(MotionEvent motionEvent) {
            if (mPreviewWidth > 0 && mPreviewHeight > 0) {
                float x = motionEvent.getX() / mPreviewWidth;
                float y = motionEvent.getY() / mPreviewHeight;
                mMediaRecorder.focusing(x, y);

            }
            return true;
        }

        @Override
        public boolean onScroll(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent motionEvent) {

        }

        @Override
        public boolean onFling(MotionEvent motionEvent, MotionEvent motionEvent1, float v, float v1) {
            return false;
        }
    };

    private View.OnTouchListener mOnTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mDetector.onTouchEvent(motionEvent);
            mScaleDetector.onTouchEvent(motionEvent);

            if (bubbleRelativeLayout.getVisibility() == View.VISIBLE) {
                bubbleRelativeLayout.setVisibility(View.GONE);
            }
            LinearLayout.LayoutParams params5 = (LinearLayout.LayoutParams) zhibao_box.getLayoutParams();
            params5.height = ScreenUtil.getScreenHeight(getApplicationContext()) - ScreenUtil.getScreenBarHeight(LiveCameraActivity.this);
            zhibao_box.setLayoutParams(params5);
            zhibao_box.invalidate(); // 刷新界面


            ViewGroup.LayoutParams lp1 = _CameraSurface.getLayoutParams();
            lp1.height = ScreenUtil.getScreenHeight(getApplicationContext()) - ScreenUtil.getScreenBarHeight(LiveCameraActivity.this);
            ;
            _CameraSurface.setLayoutParams(lp1);

            app_video_bottom_box.setVisibility(View.VISIBLE);
            ll_toke_layout.setVisibility(View.VISIBLE);
            ll_look.setVisibility(View.VISIBLE);

            return true;
        }
    };

    private ScaleGestureDetector.OnScaleGestureListener mScaleGestureListener = new ScaleGestureDetector.OnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector scaleGestureDetector) {
            mMediaRecorder.setZoom(scaleGestureDetector.getScaleFactor(), null);
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector scaleGestureDetector) {
            return true;
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector scaleGestureDetector) {
        }
    };

    private final SurfaceHolder.Callback _CameraSurfaceCallback = new SurfaceHolder.Callback() {
        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            holder.setKeepScreenOn(true);
            mPreviewSurface = holder.getSurface();

            mMediaRecorder.prepare(mConfigure, mPreviewSurface);
            if ((int) mConfigure.get(AlivcMediaFormat.KEY_CAMERA_FACING) == AlivcMediaFormat.CAMERA_FACING_FRONT) {
                mMediaRecorder.addFlag(AlivcMediaFormat.FLAG_BEAUTY_ON);
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
            mMediaRecorder.setPreviewSize(width, height);
            mPreviewWidth = width;
            mPreviewHeight = height;
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            mPreviewSurface = null;
            mMediaRecorder.stopRecord();
            mMediaRecorder.reset();
        }
    };


    private OnRecordStatusListener mRecordStatusListener = new OnRecordStatusListener() {
        @Override
        public void onDeviceAttach() {

        }

        @Override
        public void onDeviceAttachFailed(int facing) {

        }

        @Override
        public void onSessionAttach() {
            if (isRecording && !TextUtils.isEmpty(pushUrl)) {
                mMediaRecorder.startRecord(pushUrl);
            }
            mMediaRecorder.focusing(0.5f, 0.5f);
        }

        @Override
        public void onSessionDetach() {

        }

        @Override
        public void onDeviceDetach() {

        }

        @Override
        public void onIllegalOutputResolution() {
            Log.d(TAG, "selected illegal output resolution");
            T.showShort(LiveCameraActivity.this, "频率选择太大");
        }
    };


    private OnNetworkStatusListener mOnNetworkStatusListener = new OnNetworkStatusListener() {
        @Override
        public void onNetworkBusy() {
            Toast toast = Toast.makeText(LiveCameraActivity.this,
                    "当前网络状态极差，已无法正常流畅直播，确认要继续直播吗？", Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.TOP | Gravity.CENTER_HORIZONTAL, 0, 0);
            TextView text = (TextView) toast.getView().findViewById(android.R.id.message);
            text.setGravity(Gravity.CENTER);
            toast.show();
            Log.d("LiveRecord", "=== onNetworkBusy ===");
        }

        @Override
        public void onNetworkFree() {
            Log.d(TAG, "=== onNetworkFree ===");
        }

        @Override
        public void onConnectionStatusChange(int status) {
            switch (status) {
                case AlivcStatusCode.STATUS_CONNECTION_START:
                    T.showShort(LiveCameraActivity.this, "启动流连接!");
                    break;
                case AlivcStatusCode.STATUS_CONNECTION_ESTABLISHED:
                    T.showShort(LiveCameraActivity.this, "流连接建立成功!");
                    break;
                case AlivcStatusCode.STATUS_CONNECTION_CLOSED:
                    T.showShort(LiveCameraActivity.this, "流连接关闭!");
                    mMediaRecorder.stopRecord();
                    break;
            }
        }

        @Override
        public boolean onNetworkReconnect() {
            return true;
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        int toastTip = 0;
                        if (Manifest.permission.CAMERA.equals(permissions[i])) {
                            toastTip = R.string.no_camera_permission;
                        } else if (Manifest.permission.RECORD_AUDIO.equals(permissions[i])) {
                            toastTip = R.string.no_record_audio_permission;
                        }
                        if (toastTip != 0) {
                            Toast.makeText(this, toastTip, Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                break;
        }
    }

    private OnLiveRecordErrorListener mOnErrorListener = new OnLiveRecordErrorListener() {
        @Override
        public void onError(int errorCode) {
            switch (errorCode) {
                case AlivcStatusCode.ERROR_AUTH_FAILED:
                    Log.i("LiveRecord", "==== live auth failed, need new auth key ====");
                    T.showShort(LiveCameraActivity.this, "现场认证失败，需要新的认证密钥");
                    break;
                case AlivcStatusCode.ERROR_SERVER_CLOSED_CONNECTION:
                case AlivcStatusCode.ERORR_OUT_OF_MEMORY:
                case AlivcStatusCode.ERROR_CONNECTION_TIMEOUT:
                case AlivcStatusCode.ERROR_BROKEN_PIPE:
                case AlivcStatusCode.ERROR_ILLEGAL_ARGUMENT:
                case AlivcStatusCode.ERROR_IO:
                case AlivcStatusCode.ERROR_NETWORK_UNREACHABLE:
                case AlivcStatusCode.ERROR_OPERATION_NOT_PERMITTED:
                default:
                    Log.i("LiveRecord", "=== Live stream connection error-->" + errorCode + " ===");
                    T.showShort(LiveCameraActivity.this, "实时流连接错误-->" + errorCode);
                    break;
            }
        }
    };

    DataStatistics.ReportListener mReportListener = new DataStatistics.ReportListener() {
        @Override
        public void onInfoReport() {
            runOnUiThread(mLoggerReportRunnable);
        }
    };

    private Runnable mLoggerReportRunnable = new Runnable() {
        @Override
        public void run() {
            if (mRecordReporter != null) {
            }
        }
    };

    /*开启灯*/
    private final CompoundButton.OnCheckedChangeListener _SwitchFlashLightOnCheckedChange =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mMediaRecorder.addFlag(AlivcMediaFormat.FALG_FALSH_MODE_ON);
                    } else {
                        mMediaRecorder.removeFlag(AlivcMediaFormat.FALG_FALSH_MODE_ON);
                    }
                }
            };
    /*开启美图*/
    private final CompoundButton.OnCheckedChangeListener _SwitchBeautyOnCheckedChange =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        mMediaRecorder.addFlag(AlivcMediaFormat.FLAG_BEAUTY_ON);
                    } else {
                        mMediaRecorder.removeFlag(AlivcMediaFormat.FLAG_BEAUTY_ON);
                    }
                }
            };
    /*切换照相机*/
    private final CompoundButton.OnCheckedChangeListener _CameraOnCheckedChange =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    int currFacing = mMediaRecorder.switchCamera();
                    if (currFacing == AlivcMediaFormat.CAMERA_FACING_FRONT) {
                        mMediaRecorder.addFlag(AlivcMediaFormat.FLAG_BEAUTY_ON);
                    }
                    mConfigure.put(AlivcMediaFormat.KEY_CAMERA_FACING, currFacing);
                }
            };
    /*推流*/
    private final CompoundButton.OnCheckedChangeListener _PushOnCheckedChange =
            new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        try {
                            mMediaRecorder.startRecord(pushUrl);
                        } catch (Exception e) {
                        }
                        isRecording = true;
                    } else {
                        mMediaRecorder.stopRecord();
                    }
                }
            };

    @Override
    protected void onStop() {
        super.onStop();
        timer.cancel();
        timer.purge();
        timer = null;
//        movieLogout();

    }

    @Override
    protected void onStart() {
        super.onStart();
        try {
            mMediaRecorder.startRecord(pushUrl);
        } catch (Exception e) {
            e.printStackTrace();
        }
        isRecording = true;
        getFristDate();
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

    /*按下返回键触发的*/
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Log.d("DEBUG", "onBackPressed");
        movieLogout();
        mMediaRecorder.stopRecord();
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        RecordLoggerManager.closeLoggerFile();
        movieLogout();
        mMediaRecorder.stopRecord();
        mDataStatistics.stop();
        mMediaRecorder.release();
        finish();
    }


    /*分享回调*/
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        /** 分享回调**/
        UMShareAPI.get(LiveCameraActivity.this).onActivityResult(requestCode, resultCode, data);

    }

    /*进入直播观看（*/
    public void getFristDate() {
        String url = Constants.URL_LIVE_INFO;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("live_id", video_id);
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
                        String movie_cover_img = live_member_infoObj.getString("movie_cover_img");   //直播商品列表
                        headurl = movie_cover_img;
                        live_url = movie_play_url;
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
        params.put("live_id", video_id);
        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                Logger.d(data.toString());
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
        params.put("live_id", video_id);
        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                Logger.d(data.toString());
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
        params.put("live_id", video_id);
        params.put("msg_txt", mst_txt);
        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                Logger.d(data.toString());
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

    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            View v = getCurrentFocus();
            if (isShouldHideKeyboard(v, ev)) {
                hideKeyboard(v.getWindowToken());
            }
        }
        return super.dispatchTouchEvent(ev);
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
            InputTools.KeyBoard(et_talk, "close");
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // TODO Auto-generated method stub
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            if (LiveCameraActivity.this.getCurrentFocus() != null) {
                if (LiveCameraActivity.this.getCurrentFocus().getWindowToken() != null) {
                    InputTools.KeyBoard(et_talk, "close");
                }
            }
        }
        return super.onTouchEvent(event);
    }

    private PopupWindow popupWindow;
    private View view;
    private MyEditText et_talk;
    private TextView tv_zhibo_send;

    public void initPopwindow() {
        view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.activity_live_camera_editview, null);
        popupWindow = DialogHelper.getAllPopupWindow(LiveCameraActivity.this, view);
        tv_zhibo_send = (TextView) view.findViewById(R.id.tv_zhibo_send);
        tv_zhibo_send.setOnClickListener(this);
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

    /**
     * 显示popupwindow
     */
    private void showPopupWindow() {
//        if(!popupWindow.isShowing()){
//            popupWindow.showAsDropDown(zhibo_et_talk);
        popupWindow.showAtLocation(zhibo_et_talk, Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL, 0, 0);//这种方式无论有虚拟按键还是没有都可完全显示，因为它显示的在整个父布局中
        InputTools.KeyBoard(et_talk, "open");
        et_talk.setFocusable(true);
        et_talk.setFocusableInTouchMode(true);
        et_talk.requestFocus();
        et_talk.findFocus();
//        }else{
//            popupWindow.dismiss();
//        }
    }

    private void closeInput() {
        popupWindow.dismiss();
        InputTools.KeyBoard(et_talk, "close");
    }
}
