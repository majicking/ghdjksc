package com.guohanhealth.shop.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.guohanhealth.shop.BaseActivity;
import com.guohanhealth.shop.R;
import com.guohanhealth.shop.adapter.RedpacketListViewAdapter;
import com.guohanhealth.shop.bean.RedpacketInfo;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.MyExceptionHandler;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.custom.XListView;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.ResponseData;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * 红包列表
 *
 * @author dqw
 * @date 2015/8/27
 */

public class RedpacketListActivity extends BaseActivity implements XListView.IXListViewListener {


    private MyShopApplication myApplication;
    private Handler mXLHandler;
    private RedpacketListViewAdapter adapter;
    private XListView listViewID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_redpacket_list);
        MyExceptionHandler.getInstance().setContext(this);
        setCommonHeader("");
        setTabButton();

        myApplication = (MyShopApplication) getApplicationContext();


        listViewID = (XListView) findViewById(R.id.listViewID);
        adapter = new RedpacketListViewAdapter(RedpacketListActivity.this);
        mXLHandler = new Handler();
        listViewID.setAdapter(adapter);
        listViewID.setXListViewListener(this);
        listViewID.setPullLoadEnable(false);
        loading();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        loadingListData();
    }

    /**
     * 设置头部切换按钮
     */

    private void setTabButton() {
        Button btnRedpacketList = (Button) findViewById(R.id.btnRedpacketList);
        Button btnRedpacketPasswordAdd = (Button) findViewById(R.id.btnRedpacketPasswordAdd);
        btnRedpacketList.setActivated(true);
        btnRedpacketPasswordAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(RedpacketListActivity.this, RedpacketPasswordAddActivity.class));
                finish();
            }
        });
    }

    public void loading(){
        loadingListData("");
    }

    /**
     * 加载列表数据
     */
    public void loadingListData(String usered) {
        String url = Constants.URL_MEMBER_REDPACKET_LIST + "&key=" + myApplication.getLoginKey() + "&curpage=1&page=1000";
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("rp_state", usered);//unused-未使用 used-已使用 expire-已过期

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String updataTime = sdf.format(new Date(System.currentTimeMillis()));
        listViewID.setRefreshTime(updataTime);

        RemoteDataHandler.asyncDataStringGet(url, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {

                listViewID.stopRefresh();

                if (data.getCode() == HttpStatus.SC_OK) {
                    String json = data.getJson();
                    try {
                        JSONObject obj = new JSONObject(json);
                        String objJson = obj.getString("redpacket_list");

                        ArrayList<RedpacketInfo> redpacketInfoArrayList= RedpacketInfo.newInstanceList(objJson);
                        int d = redpacketInfoArrayList == null ? 0 : redpacketInfoArrayList.size();
                        if(d > 0){
                            adapter.setList(redpacketInfoArrayList);
                            adapter.notifyDataSetChanged();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    Toast.makeText(RedpacketListActivity.this, getResources().getString(R.string.load_error), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        //下拉刷新
        mXLHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loading();
            }
        }, 1000);
    }

    @Override
    public void onLoadMore() {
        //上拉加载
    }

}
