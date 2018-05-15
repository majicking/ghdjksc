package com.xinyuangongxiang.shop.ui.fenxiao;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.RelativeLayout;

import com.orhanobut.logger.Logger;
import com.xinyuangongxiang.shop.common.StringUtils;

import com.xinyuangongxiang.shop.BaseActivity;
import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.bean.Mine;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.MyShopApplication;
import com.xinyuangongxiang.shop.common.ShopHelper;
import com.xinyuangongxiang.shop.common.T;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.ResponseData;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by snm on 2016/9/23.
 */
public class FenxiaoAllActivity extends BaseActivity {

    RelativeLayout rlPointLog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fenxiao_all);
        setCommonHeader("分销中心");
        rlPointLog = (RelativeLayout)findViewById(R.id.rlPointLog);
    }

    public void relatGoodsClick(View view){
        startActivity(new Intent(getApplicationContext(), FenxiaoGoodsActivity.class));
    }
    public void relatTijiaoClick(View view){
        startActivity(new Intent(getApplicationContext(), FenxiaoSettlementActivity.class));
    }
    public void relatOrderClick(View view){
        startActivity(new Intent(getApplicationContext(), FenxiaoOrderActivity.class));
    }
    public void relattxClick(View view){
       startActivity(new Intent(getApplicationContext(),FenxiaoTixianActivity.class));
    }
    public void relatLiveClick(View view){
        if(!StringUtils.isEmpty(movie_msg)){
            T.showShort(getApplicationContext(),movie_msg);
        }else {
            ApplyVerifyMovie();
        }
    }

    public void ApplyVerifyMovie() {
        String url = Constants.URL_VERIFY_MOVIE;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());

//        OkHttpUtil.postAsyn(getActivity(),url,new OkHttpUtil.ResultCallback<String>() {
//            @Override
//            public void onError(Request request, Exception e) {
//                Logger.d(e.toString());
//                T.showShort(getActivity(),e.getMessage());
//            }
//
//            @Override
//            public void onResponse(String response) {
//                Logger.d(response);
//                try {
//                    JSONObject objError = new JSONObject(response);
//                    String error = objError.getString("error");
//                    if (error != null) {
//                        T.showShort(getActivity(),error);
//                        return;
//                    }
//                }catch (Exception e){
//                    e.printStackTrace();
//                }
//            }
//        },params);

        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {

                String json = data.getJson();
//                Logger.d(data.toString());
                if (data.getCode() == HttpStatus.SC_OK) {
                    /*审核通过了*/
                    if("1".equals(json)){
                    /*从没有申请过*/
                        T.showShort(getApplicationContext(),"请申请直播");
                        startActivity(new Intent(getApplicationContext(), ApplyLiveActivity.class));
                    }else {
                        T.showShort(getApplicationContext(),json);
//                        LiveCameraActivity.startActivity(getApplicationContext(),"rtmp://video-center.alivecdn.com/shopnc/test1?vhost=live.shopnctest.com", AlivcMediaFormat.OUTPUT_RESOLUTION_360P,false,AlivcMediaFormat.CAMERA_FACING_FRONT);
                        startActivity(new Intent(getApplicationContext(), BeginLiveActivity.class));
                    }
                } else {
                    /*失败有两种 正在进行中，没有通过*/
                    if(data.getCode() == 400){
                        try {
                            JSONObject objError = new JSONObject(json);
                            String error = objError.getString("error");

                            if(!objError.isNull("true_name")) {
                                String true_name = objError.getString("true_name");
                                String card_number = objError.getString("card_number");
                                String card_before_image = objError.getString("card_before_image");
                                String card_behind_image = objError.getString("card_behind_image");
                                String card_before_image_url = objError.getString("card_before_image_url");
                                String card_behind_image_url = objError.getString("card_behind_image_url");
                                String is_agree = objError.getString("is_agree");
                                String member_id = objError.getString("member_id");
                                String movie_id = objError.getString("movie_id");

                                Intent intent = new Intent(getApplicationContext(), ApplyLiveActivity.class);
                                intent.putExtra("true_name",true_name);
                                intent.putExtra("card_number",card_number);
                                intent.putExtra("card_before_image",card_before_image);
                                intent.putExtra("card_behind_image",card_behind_image);
                                intent.putExtra("card_before_image_url",card_before_image_url);
                                intent.putExtra("card_behind_image_url",card_behind_image_url);
                                intent.putExtra("is_agree",is_agree);
                                intent.putExtra("member_id",member_id);
                                intent.putExtra("movie_id",movie_id);

                                startActivity(intent);
                            }else {
                                T.showShort(getApplicationContext(),error);
                            }
                        }catch (Exception e){
                            T.showShort(getApplicationContext(),"获取失败");
                            e.printStackTrace();
                        }
                    }else {
                        ShopHelper.showApiError(getApplicationContext(), json);
                    }
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadMemberInfo();
    }

    String movie_msg = "";

    /**
     * 初始化加载我的信息
     */
    public void loadMemberInfo() {
        String url = Constants.URL_MYSTOIRE;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());

        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {

                String json = data.getJson();
//                Logger.d(json);
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject obj = new JSONObject(json);
                        String objJson = obj.getString("member_info");
                        Mine bean = Mine.newInstanceList(objJson);

                        if (bean != null) {

                            if (bean.getIs_movie() != null) {
                                if ("1".equals(bean.getIs_movie())) {
                                    rlPointLog.setVisibility(View.VISIBLE);
                                } else {
                                    rlPointLog.setVisibility(View.GONE);
                                }

                            }
                            movie_msg = bean.getIs_movie_msg();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(getApplicationContext(), json);
                }
            }
        });
    }
}
