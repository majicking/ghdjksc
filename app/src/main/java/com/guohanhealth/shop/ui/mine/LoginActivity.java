package com.guohanhealth.shop.ui.mine;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.guohanhealth.shop.BaseActivity;
import com.guohanhealth.shop.R;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.MyExceptionHandler;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.common.T;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.newpackage.ProgressDialog;
import com.guohanhealth.shop.xrefresh.utils.LogUtils;
import com.umeng.socialize.UMAuthListener;
import com.umeng.socialize.UMShareAPI;
import com.umeng.socialize.UMShareConfig;
import com.umeng.socialize.bean.SHARE_MEDIA;

import org.apache.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.Subscriber;


/**
 * 登录页面
 *
 * @author wj
 */
public class LoginActivity extends BaseActivity implements View.OnClickListener {
    private MyShopApplication myApplication;

    private EditText etUsername, etPassword;
    private ImageButton btnAutoLogin;
    private Button btnLogin;
    private LinearLayout ThreebtnLogin;
    private ImageView ivThreeLogin;


    private ImageButton btnQQ, btnWeiXin, btnSina;

//    //weibo
//    private AuthInfo mAuthInfo;
//    private Oauth2AccessToken mAccessToken;
//    private SsoHandler mSsoHandler;
//
//    //qq
//    public static Tencent mTencent;


    //QQ
    private String token;
    private String openid;


    private UMShareAPI mShareAPI = null;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login_view);
        mShareAPI = UMShareAPI.get(this);
        myApplication = (MyShopApplication) getApplicationContext();
        MyExceptionHandler.getInstance().setContext(this);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setBtnLoginState();
            }
        };
        etUsername = (EditText) findViewById(R.id.etUsername);
        etUsername.addTextChangedListener(textWatcher);
        etPassword = (EditText) findViewById(R.id.etPassword);
        etPassword.addTextChangedListener(textWatcher);
        btnAutoLogin = (ImageButton) findViewById(R.id.btnAutoLogin);
        btnAutoLogin.setSelected(true);
        btnLogin = (Button) findViewById(R.id.btnLogin);
        btnLogin.setActivated(false);

        btnQQ = (ImageButton) findViewById(R.id.btnQQ);
        btnQQ.setOnClickListener(this);
        btnWeiXin = (ImageButton) findViewById(R.id.btnWeiXin);
        btnWeiXin.setOnClickListener(this);
        btnSina = (ImageButton) findViewById(R.id.btnSina);
        btnSina.setOnClickListener(this);

        ThreebtnLogin = (LinearLayout) findViewById(R.id.ThreebtnLogin);
        ivThreeLogin = (ImageView) findViewById(R.id.ivThreeLogin);
        if (Constants.APP_ID.equals("") || Constants.APP_SECRET.equals("") || Constants.WEIBO_APP_KEY.equals("") || Constants.WEIBO_APP_SECRET.equals("") || Constants.QQ_APP_ID.equals("") || Constants.QQ_APP_KEY.equals("")) {
            ThreebtnLogin.setVisibility(View.INVISIBLE);
            ivThreeLogin.setVisibility(View.INVISIBLE);
        }


    }

    //返回按钮
    public void btnBackClick(View v) {
        finish();
    }

    //注册按钮
    public void btnRegisterClick(View v) {
        startActivity(new Intent(LoginActivity.this, RegisteredActivity.class));
        finish();
    }

    //自动登录选择
    public void btnAutoLoginClick(View v) {
        if (btnAutoLogin.isSelected()) {
            btnAutoLogin.setSelected(false);
        } else {
            btnAutoLogin.setSelected(true);
        }

    }

    //登录
    public void btnLoginClick(View v) {
        if (btnLogin.isActivated()) {
            String username = etUsername.getText().toString();
            String password = etPassword.getText().toString();

            if (username == null || username.trim().equals("")) {
                Toast.makeText(LoginActivity.this, "用户名不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            if (password == null || password.trim().equals("")) {
                Toast.makeText(LoginActivity.this, "密码不能为空", Toast.LENGTH_SHORT).show();
                return;
            }

            login(username, password);
        }
    }

    //处理登录按钮状态
    private void setBtnLoginState() {
        if (etUsername.getText().toString().equals("") || etPassword.getText().toString().equals("")) {
            btnLogin.setActivated(false);
        } else {
            btnLogin.setActivated(true);
        }
    }

    //用户登录
    private void login(String username, String password) {
        String url = Constants.URL_LOGIN;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("username", username);
        params.put("password", password);
        params.put("client", "android");
        Dialog dialog = ProgressDialog.showLoadingProgress(mActivity, "登陆中...");
        dialog.show();
        RemoteDataHandler.asyncPostDataString(url, params, data -> {
            ProgressDialog.dismissDialog(dialog);
            String json = data.getJson();
            if (data.getCode() == HttpStatus.SC_OK) {
                ShopHelper.login(LoginActivity.this, myApplication, json);
                LoginActivity.this.finish();
            } else {
                ShopHelper.showApiError(LoginActivity.this, json);
            }
        });
    }

    /**
     * 找回密码按钮点击
     */
    public void btnFindPasswordClick(View v) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(Constants.WAP_FIND_PASSWORD));
            startActivity(intent);
        } catch (ActivityNotFoundException exception) {
            T.showShort(this, "链接失败");
        }
    }


    /**
     * QQ同步登录
     *
     * @param token
     */
    private void loginQq(String token, String openid, String nickname, String avatar) {
        String url = Constants.URL_CONNECT_QQ + "&token=" + token + "&open_id=" + openid + "&nickname=" + nickname + "&avatar=" + avatar + "&client=android";
        LogUtils.i("qq_login_url" + url);
        Dialog dialog = ProgressDialog.showLoadingProgress(mActivity, "QQ登陆中...");
        dialog.show();
        RemoteDataHandler.asyncDataStringGet(url, data -> {
//                Log.e("qq_login_response", data.toString());
            ProgressDialog.dismissDialog(dialog);
            String json = data.getJson();
            if (data.getCode() == HttpStatus.SC_OK) {
                ShopHelper.login(LoginActivity.this, myApplication, json);
                LoginActivity.this.finish();
            } else {
                ShopHelper.showApiError(LoginActivity.this, json);
            }
        });
    }


    /**
     * 微博同步登录
     *
     * @param accessToken
     * @param userId
     */
    private void loginWeibo(String accessToken, String userId) {
        String url = Constants.URL_CONNECT_WEIBO + "&accessToken=" + accessToken + "&userID=" + userId + "&client=android";
        Dialog dialog = ProgressDialog.showLoadingProgress(mActivity, "微博登陆中...");
        dialog.show();
        RemoteDataHandler.asyncDataStringGet(url, data -> {
            ProgressDialog.dismissDialog(dialog);
            String json = data.getJson();
//                LogHelper.e("json", json);
//                LogHelper.e("data", data.toString());
            if (data.getCode() == HttpStatus.SC_OK) {
                ShopHelper.login(LoginActivity.this, myApplication, json);
                LoginActivity.this.finish();
            } else {
                ShopHelper.showApiError(LoginActivity.this, json);
            }
        });
    }

    /**
     * 微信登录
     */
    private void loginWx(String access_token, String openid) {
        String url = Constants.URL_CONNECT_WX + "&access_token=" + access_token + "&openid=" + openid + "&client=android";
        Dialog dialog = ProgressDialog.showLoadingProgress(mActivity, "微信登陆中...");
        dialog.show();
        RemoteDataHandler.asyncDataStringGet(url, data -> {
            ProgressDialog.dismissDialog(dialog);
            String json = data.getJson();
            if (data.getCode() == HttpStatus.SC_OK) {
                ShopHelper.login(LoginActivity.this, myApplication, json);
                LoginActivity.this.finish();
            } else {
                ShopHelper.showApiError(LoginActivity.this, json);
            }
        });
    }

//
//    //授权
//    private UMAuthListener umAuthListener = new UMAuthListener() {
//
//        @Override
//        public void onStart(SHARE_MEDIA share_media) {
//
//        }
//
//        @Override
//        public void onComplete(SHARE_MEDIA platform, int action, Map<String, String> data) {
//            if (data != null) {
//                if (platform == SHARE_MEDIA.QQ) {
//                    token = data.get("access_token");
//                    openid = data.get("openid");
//                    UMShareAPI.get(LoginActivity.this).getPlatformInfo(LoginActivity.this, platform, userinfo);
//
//                } else if (platform == SHARE_MEDIA.WEIXIN) {
//                    String access_token = data.get("access_token");
//                    String openid = data.get("openid");
//                    loginWx(access_token, openid);
//
//                } else if (platform == SHARE_MEDIA.SINA) {
//                    String accessToken = data.get("access_token");
//                    String userId = data.get("uid");
//                    loginWeibo(accessToken, userId);
//                }
//
//            }
//
//        }
//
//        @Override
//        public void onError(SHARE_MEDIA platform, int action, Throwable t) {
//            Toast.makeText(getApplicationContext(), "授权失败", Toast.LENGTH_SHORT).show();
//        }
//
//        @Override
//        public void onCancel(SHARE_MEDIA platform, int action) {
//            Toast.makeText(getApplicationContext(), "取消授权", Toast.LENGTH_SHORT).show();
//        }
//    };


    //获取用户信息
    private UMAuthListener userinfo = new UMAuthListener() {


        @Override
        public void onStart(SHARE_MEDIA share_media) {

        }

        @Override
        public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {

        }

        @Override
        public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
            Toast.makeText(getApplicationContext(), "获取用户信息失败", Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onCancel(SHARE_MEDIA share_media, int i) {
            Toast.makeText(getApplicationContext(), "取消授权", Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        UMShareAPI.get(this).onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void onClick(View view) {
        SHARE_MEDIA platform = null;
        switch (view.getId()) {
            case R.id.btnQQ:

                platform = SHARE_MEDIA.QQ;
                break;
            case R.id.btnWeiXin:
                platform = SHARE_MEDIA.WEIXIN;
                break;
            case R.id.btnSina:
                platform = SHARE_MEDIA.SINA;
                break;
        }
        authorization(platform);
//        UMShareAPI.get(this).getPlatformInfo(this, platform,umAuthListener);
//                mShareAPI.doOauthVerify(LoginActivity.this, platform, umAuthListener);


    }

    //授权
    private void authorization(SHARE_MEDIA share_media) {
        if (!UMShareAPI.get(LoginActivity.this).isInstall(this, share_media)) {
            Toast.makeText(LoginActivity.this, "未安装客户端", Toast.LENGTH_SHORT).show();
            return;
        }

        Observable.create((Observable.OnSubscribe<Integer>) subscriber -> {
            subscriber.onNext(1);
            subscriber.onCompleted();
        }).subscribe(new Observer<Integer>() {
            @Override
            public void onNext(Integer integer) {
                UMShareAPI.get(LoginActivity.this).deleteOauth(LoginActivity.this, share_media, new UMAuthListener() {
                    @Override
                    public void onStart(SHARE_MEDIA share_media) {
                        LogUtils.i("onStart " + "删除授权开始");
                    }

                    @Override
                    public void onComplete(SHARE_MEDIA share_media, int i, Map<String, String> map) {
                        if (map != null && map.size() > 0) {
                            for (Map.Entry e : map.entrySet()) {
                                LogUtils.i("key " + e.getKey() + "VALUE=" + e.getValue());

                            }
                        }
                        LogUtils.i("onStart " + "删除授权完成" + i);
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                        LogUtils.i("onStart " + "删除授权错误");
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media, int i) {
                        LogUtils.i("onStart " + "删除授权取消");
                    }

                });
            }

            @Override
            public void onCompleted() {
                UMShareConfig config = new UMShareConfig();
                config.isNeedAuthOnGetUserInfo(true);
                UMShareAPI.get(LoginActivity.this).setShareConfig(config);
                UMShareAPI.get(LoginActivity.this).getPlatformInfo(LoginActivity.this, share_media, new UMAuthListener() {

                    @Override
                    public void onStart(SHARE_MEDIA share_media) {
                        LogUtils.i("onStart " + "授权开始");
//                UMShareAPI.get(LoginActivity.this).
                    }

                    @Override
                    public void onComplete(SHARE_MEDIA platform, int i, Map<String, String> map) {
                        if (map != null && map.size() > 0) {
                            for (Map.Entry e : map.entrySet()) {
                                LogUtils.i("key " + e.getKey() + "VALUE=" + e.getValue());
                            }
                        }

                        LogUtils.i("onComplete " + "授权完成");
                        if (map != null) {
                            //sdk是6.4.4的,但是获取值的时候用的是6.2以前的(access_token)才能获取到值,未知原因
                            uid = map.get("uid");
                            openid = map.get("openid");//微博没有
                            String unionid = map.get("unionid");//微博没有
                            token = map.get("access_token");
                            String refresh_token = map.get("refresh_token");//微信,qq,微博都没有获取到
                            String expires_in = map.get("expires_in");
                            String name = map.get("name");
                            String gender = map.get("gender");
                            String iconurl = map.get("iconurl");
                            if (platform == SHARE_MEDIA.QQ) {
                                String nickname = map.get("screen_name");
                                String avatar = map.get("profile_image_url");
                                loginQq(token, openid, nickname, avatar);
//                        UMShareAPI.get(LoginActivity.this).getPlatformInfo(LoginActivity.this, platform, userinfo);
                            } else if (platform == SHARE_MEDIA.WEIXIN) {
                                loginWx(token, openid);
                            } else if (platform == SHARE_MEDIA.SINA) {
                                loginWeibo(token, uid);
                            }

//                    Toast.makeText(getApplicationContext(), "name=" + name + ",gender=" + gender, Toast.LENGTH_SHORT).show();
                        }


                        //拿到信息去请求登录接口。。。
                    }

                    @Override
                    public void onError(SHARE_MEDIA share_media, int i, Throwable throwable) {
                        LogUtils.i("onError " + "授权失败" + i);
                        Toast.makeText(mActivity, "授权失败" + i, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onCancel(SHARE_MEDIA share_media, int i) {
                        LogUtils.i("onCancel " + "授权取消");
                        Toast.makeText(mActivity, "授权取消" + i, Toast.LENGTH_SHORT).show();
                    }
                });

            }

            @Override
            public void onError(Throwable e) {
            }
        });
    }


}
