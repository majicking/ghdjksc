package com.xinyuangongxiang.shop.common;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.bean.GpsInfo;
import com.xinyuangongxiang.shop.bean.Login;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.ResponseData;
import com.xinyuangongxiang.shop.ui.mine.IMSendMsgActivity;
import com.xinyuangongxiang.shop.ui.mine.LoginActivity;
import com.xinyuangongxiang.shop.ui.type.GoodsDetailsActivity;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.DecimalFormat;
import java.util.HashMap;

/**
 * 商城通用类
 *
 * @author dqw
 * @Time 2015/7/15
 */
public class ShopHelper {

    private static ImageLoader imageLoader = ImageLoader.getInstance();
    private static DisplayImageOptions options = SystemHelper.getDisplayImageOptions();
    private static ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    private ShopHelper() {
    }

    /**
     * 登录
     */
    public static void login(Context context, MyShopApplication myApplication, String loginJson) {
        Login login = Login.newInstanceList(loginJson);
        myApplication.setLoginKey(login.getKey());
        myApplication.setUserName(login.getUsername());
        myApplication.setMemberID(login.getUserid());

        myApplication.loadingUserInfo(login.getKey(), login.getUserid());

        myApplication.getmSocket().connect();
        myApplication.UpDateUser();

        Intent mIntent = new Intent(Constants.LOGIN_SUCCESS_URL);
        context.sendBroadcast(mIntent);

        Intent intent=new Intent(Constants.SHOW_CART_NUM);
        context.sendBroadcast(intent);
    }

    /**
     * 检查是否已登录，如果未登录直接弹出登录页面
     */
    public static Boolean isLogin(Context context, String key) {
        if (StringUtils.isEmpty(key)) {
            Intent intent = new Intent(context, LoginActivity.class);
            context.startActivity(intent);
            return false;
        } else {
            return true;
        }
    }

    /**
     * 显示消息
     */
    public static void showMessage(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * 处理并显示接口错误信息
     */
    public static void showApiError(Context context, String json) {
        if(json != null){
            try {
                JSONObject objError = new JSONObject(json);
                String error = objError.getString("error");
                if (error != null) {
                    T.showShort(context,error);
                    return;
                }
            } catch (Exception e) {
                T.showShort(context,context.getResources().getString(R.string.load_error));
                e.printStackTrace();
            }
        }else{
            T.showShort(context,"网络异常");
        }
    }

    /**
     * 加载图片
     */
    public static void loadImage(ImageView iv, String imageUrl) {
        imageLoader.displayImage(imageUrl, iv, options, animateFirstListener);
    }

    /**
     * 短信验证码倒计时
     */
    public static void btnSmsCaptchaCountDown(final Context context, final Button btn, int smsTime) {
        btn.setTextColor(context.getResources().getColor(R.color.nc_text));
        btn.setActivated(false);

        new CountDownTimer(smsTime * 1000, 1000) {
            public void onTick(long millisUntilFinished) {
                btn.setText("重新获取验证码(" + millisUntilFinished / 1000 + ")");
            }

            public void onFinish() {
                btn.setText("重新获取验证码");
                btn.setTextColor(context.getResources().getColor(R.color.nc_blue));
                btn.setActivated(true);
            }
        }.start();
    }

    /**
     * 显示IM窗口
     */
    public static void showIm(Context context, final MyShopApplication myApplication, String memberId, String memberName) {
        if (myApplication.getMemberID().equals(memberId)) {
            Toast.makeText(context, "对不起，您不可以和自己聊天", Toast.LENGTH_SHORT).show();
            return;
        }
        if (myApplication.isIMConnect()) {
            Intent intent = new Intent(context, IMSendMsgActivity.class);
            intent.putExtra("t_id", memberId);
            intent.putExtra("t_name", memberName);
            context.startActivity(intent);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("IM离线通知").setMessage("您的IM账号已经离线，点击确定重新登录");
            builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    myApplication.getmSocket().connect();
                }
            }).create().show();
        }
    }

    /**
     * 添加购物车
     */
    public static void addCart(final Context context,final MyShopApplication myApplication, String goodsId, int goodsNum) {
        String url = Constants.URL_ADD_CART;

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("goods_id", goodsId);
        params.put("key", myApplication.getLoginKey());
        params.put("quantity", goodsNum + "");

        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    Toast.makeText(context, "添加购物车成功", Toast.LENGTH_SHORT).show();
                    getNum(context,myApplication);
                } else {
                    ShopHelper.showApiError(context, json);
                }
            }
        });


    }

    public static void  getNum(final Context context,MyShopApplication myApplication){
        String url=Constants.URL_GET_CART_NUM;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        RemoteDataHandler.asyncPostDataString(url,params,new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json=data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try{
                        JSONObject obj=new JSONObject(json);
                        String num=obj.getString("cart_count");
                        Intent intent=new Intent(Constants.SHOW_CART_NUM);
                        context.sendBroadcast(intent);
                    }catch (JSONException e){
                        Toast.makeText(context,"获取购物车数量失败",Toast.LENGTH_SHORT).show();
                    }

                }
            }
        });
    }

    /**
     * 拨号
     */
    public static void call(Context context, String phone) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_DIAL);
        intent.setData(Uri.parse("tel://" + phone));
        context.startActivity(intent);
    }

    /**
     * 获取GPS信息
     */
    public static GpsInfo getGpsInfo(Context context) {
        double latitude = 0.0;
        double longitude = 0.0;
        GpsInfo gpsInfo = new GpsInfo();

        try {
            LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (location == null) {
                LocationListener locationListener = new LocationListener() {

                    // Provider的状态在可用、暂时不可用和无服务三个状态直接切换时触发此函数
                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    // Provider被enable时触发此函数，比如GPS被打开
                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    // Provider被disable时触发此函数，比如GPS被关闭
                    @Override
                    public void onProviderDisabled(String provider) {

                    }

                    //当坐标改变时触发此函数，如果Provider传进相同的坐标，它就不会被触发
                    @Override
                    public void onLocationChanged(Location location) {
                        if (location != null) {
                            Log.d("dqw", "Location changed : Lat: "
                                    + location.getLatitude() + " Lng: "
                                    + location.getLongitude());
                        }
                    }
                };
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 0, locationListener);
                location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (location != null) {
                latitude = location.getLatitude();
                longitude = location.getLongitude();
            }
        } catch (Exception e) {
            Log.d("dqw", "获取定位信息失败");
        } finally {
            gpsInfo.setLat(latitude);
            gpsInfo.setLng(longitude);
            return gpsInfo;
        }
    }

    /**
     * 显示商品
     *
     * @param goodsId
     */
    public static void showGoods(Context context, String goodsId) {
        Intent intent = new Intent(context, GoodsDetailsActivity.class);
        intent.putExtra("goods_id", goodsId);
        context.startActivity(intent);
    }

    /**
     * 获取价格字符串
     */
    public static String getPriceString(double price) {
        DecimalFormat df = new DecimalFormat("0.00");
        return df.format(price).toString();
    }

}


