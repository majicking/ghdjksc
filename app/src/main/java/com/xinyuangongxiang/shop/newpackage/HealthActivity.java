package com.xinyuangongxiang.shop.newpackage;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.JSONParser;
import com.xinyuangongxiang.shop.common.MyShopApplication;
import com.xinyuangongxiang.shop.common.ShopHelper;
import com.xinyuangongxiang.shop.common.T;
import com.xinyuangongxiang.shop.custom.XListView;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;

import org.apache.http.HttpStatus;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class HealthActivity extends Activity implements XListView.IXListViewListener {


    @BindView(R.id.btnBack)
    ImageButton btnBack;
    @BindView(R.id.tvCommonTitle)
    TextView tvCommonTitle;
    @BindView(R.id.tvCommonTitleBorder)
    TextView tvCommonTitleBorder;
    @BindView(R.id.xlist)
    XListView xlist;
    @BindView(R.id.healthnumber)
    TextView healthnumber;
    private CommonAdapter<HealthBean> adapter;
    List<HealthBean> datalist = null;
    Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_health);
        ButterKnife.bind(this);
        initData();
    }

    private void initData() {
        btnBack.setOnClickListener(v -> finish());
        tvCommonTitle.setText("健康豆");
        handler = new Handler();
        datalist = new ArrayList<>();
        adapter = new CommonAdapter<HealthBean>(this, datalist, R.layout.item_health_layout) {
            @Override
            public void convert(ViewHolder viewHolder, HealthBean item, int position, View convertView, ViewGroup parentViewGroup) {
                TextView tvDesc = viewHolder.getView(R.id.tvDesc);
                TextView tvSn = viewHolder.getView(R.id.tvSn);
                TextView tvAvAmount = viewHolder.getView(R.id.tvAvAmount);
                TextView tvAddTimeText = viewHolder.getView(R.id.tvAddTimeText);
                tvDesc.setText("订单号");
                tvSn.setText(item.order_sn);
                tvAvAmount.setText("-" + item.value);
                tvAddTimeText.setText(item.created_time);
            }
        };
        xlist.setAdapter(adapter);
        xlist.setXListViewListener(this);
    }

    //获取健康豆
    public void getHealthNumber() {
        RemoteDataHandler.asyncDataStringGet(Constants.HEALTHNUMBER + MyShopApplication.getInstance().getLoginKey(), data -> {
            String json = data.getJson();
            if (data.getCode() == HttpStatus.SC_OK) {
                if (!TextUtils.isEmpty(json)) {
                    String num = JSONParser.getStringFromJsonString("Value", json);
                    healthnumber.setText(num + " 个");
                }
            } else {
                ShopHelper.showApiError(HealthActivity.this, json);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        getData();
        getHealthNumber();
    }

    //当前页码
    int pagenum = 0;

    public void getData() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String updataTime = sdf.format(new Date(System.currentTimeMillis()));
        xlist.setRefreshTime(updataTime);
        RemoteDataHandler.asyncDataStringGet(
                Constants.HEALTHLIST + pagenum + "&key=" + MyShopApplication.getInstance().getLoginKey(),
                data -> {
                    String json = data.getJson();
                    if (xlist != null) {
                        xlist.stopLoadMore();
                        xlist.stopRefresh();
                    }
                    if (pagenum == 0) {
                        datalist.clear();
                    }
                    if (data.getCode() == HttpStatus.SC_OK) {

                        if (!json.equals("false")) {
//
//                            if (JSONParser.getStringFromJsonString("pages", json).equals("0")) {
//
//                            } else {
//                                xlist.setPullLoadEnable(true);
//                            }
                            List<HealthBean> list = JSONParser.JSON2Array(JSONParser.getStringFromJsonString("data", json), HealthBean.class);
                            if (list != null && list.size() > 0) {
                                datalist.addAll(list);
                                adapter.updataAdapter(datalist);
                            }
                        } else {
                            if (xlist != null)
                                xlist.setPullLoadEnable(false);
                            T.showShort(HealthActivity.this, "没有记录啦");
                        }
                    } else {
                        ShopHelper.showApiError(HealthActivity.this, json);
                    }


                }
        );
    }

    @Override
    public void onRefresh() {

        handler.postDelayed(() -> {
                    pagenum = 0;
                    try {
                        xlist.setPullLoadEnable(true);
                        getData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                , 1000);
    }

    @Override
    public void onLoadMore() {
        pagenum += 1;
        handler.postDelayed(() ->
                {
                    try {

                        pagenum = pagenum + 1;
                        getData();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                , 1000);
    }
}
