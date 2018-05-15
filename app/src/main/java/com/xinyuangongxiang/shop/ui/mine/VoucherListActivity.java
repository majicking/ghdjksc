package com.xinyuangongxiang.shop.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.xinyuangongxiang.shop.BaseActivity;
import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.adapter.VoucherListViewAdapter;
import com.xinyuangongxiang.shop.bean.VoucherList;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.MyExceptionHandler;
import com.xinyuangongxiang.shop.common.MyShopApplication;
import com.xinyuangongxiang.shop.custom.MyListEmpty;
import com.xinyuangongxiang.shop.custom.XListView;
import com.xinyuangongxiang.shop.custom.XListView.IXListViewListener;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.RemoteDataHandler.Callback;
import com.xinyuangongxiang.shop.http.ResponseData;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * 我的代金券列表
 *
 * @author KingKong-HE
 * @Time 2015-1-26
 * @Email KingKong@QQ.COM
 */
public class VoucherListActivity extends BaseActivity implements IXListViewListener {

    private MyShopApplication myApplication;
    private Handler mXLHandler;
    private VoucherListViewAdapter adapter;
    private XListView listViewID;
    private MyListEmpty myListEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voucher_list);
        MyExceptionHandler.getInstance().setContext(this);
        setCommonHeader("");
        setTabButton();

        myApplication = (MyShopApplication) getApplicationContext();

        myListEmpty = (MyListEmpty) findViewById(R.id.myListEmpty);
        myListEmpty.setListEmpty(R.drawable.nc_icon_predeposit_white, "您尚未代金券使用信息", "使用代金券让你享受更多优惠");

        listViewID = (XListView) findViewById(R.id.listViewID);
        adapter = new VoucherListViewAdapter(VoucherListActivity.this);
        mXLHandler = new Handler();
        listViewID.setAdapter(adapter);
        listViewID.setXListViewListener(this);
        listViewID.setPullLoadEnable(false);
        loading();
    }

    @Override
    protected void onResume() {
        super.onResume();
//        loading();
    }

    /**
     * 设置头部切换按钮
     */
    private void setTabButton() {
        Button btnVoucherList = (Button) findViewById(R.id.btnVoucherList);
        Button btnVoucherPasswordAdd = (Button) findViewById(R.id.btnVoucherPasswordAdd);
        btnVoucherList.setActivated(true);
        btnVoucherPasswordAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(VoucherListActivity.this, VoucherPasswordAddActivity.class));
                finish();
            }
        });
    }

    public void loading(){
        loadingListData("");
    }
    ArrayList<VoucherList> Listall = new ArrayList<VoucherList>();

    /**
     * 加载列表数据
     */
    public void loadingListData(String state) {
        String url = Constants.URL_MEMBER_VOUCHER_LIST + "&key=" + myApplication.getLoginKey() + "&curpage=1&page=" + Constants.PAGESIZE;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("voucher_state", state);//(1-未使用 2-已使用 3-已过期)

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String updataTime = sdf.format(new Date(System.currentTimeMillis()));
        listViewID.setRefreshTime(updataTime);

        RemoteDataHandler.asyncDataStringGet(url, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {

                listViewID.stopRefresh();

                if (data.getCode() == HttpStatus.SC_OK) {
                    String json = data.getJson();
                    try {
                        JSONObject obj = new JSONObject(json);
                        String objJson = obj.getString("voucher_list");
                        ArrayList<VoucherList> voucherList = VoucherList.newInstanceList(objJson);
                        int d = voucherList == null ? 0 : voucherList.size();
                        if(d > 0){
                            myListEmpty.setVisibility(View.GONE);
                            adapter.setVoucherLists(voucherList);
                            adapter.notifyDataSetChanged();
                        }else{
                            myListEmpty.setVisibility(View.VISIBLE);
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    myListEmpty.setVisibility(View.VISIBLE);
                    Toast.makeText(VoucherListActivity.this, getResources().getString(R.string.load_error), Toast.LENGTH_SHORT).show();
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
