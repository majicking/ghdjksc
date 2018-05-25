package com.guohanhealth.shop.ui.mine;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.TextView;

import com.guohanhealth.shop.BaseActivity;
import com.guohanhealth.shop.R;
import com.guohanhealth.shop.adapter.PointLogListViewAdapter;
import com.guohanhealth.shop.bean.PointLogInfo;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.MyExceptionHandler;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.ResponseData;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

public class PointLogActivity extends BaseActivity {
    private MyShopApplication myApplication;
    private TextView tvPoint;
    private ListView lvPointLog;
    private ArrayList<PointLogInfo> pointLogInfoArrayList;
    private PointLogListViewAdapter pointLogListViewAdapter;

    int currentPage = 1;
    boolean isHasMore = true;
    boolean isLastRow = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_point_log);
        MyExceptionHandler.getInstance().setContext(this);
        setCommonHeader("积分明细");
        myApplication = (MyShopApplication) getApplicationContext();

        tvPoint = (TextView) findViewById(R.id.tvPoint);

        lvPointLog = (ListView) findViewById(R.id.lvPointLog);
        lvPointLog.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (isHasMore && isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    isLastRow = false;
                    currentPage += 1;
                    loadPointLog();
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
                    isLastRow = true;
                }
            }
        });
        pointLogInfoArrayList = new ArrayList<PointLogInfo>();
        pointLogListViewAdapter = new PointLogListViewAdapter(PointLogActivity.this);
        lvPointLog.setAdapter(pointLogListViewAdapter);

        loadPoint();
        loadPointLog();
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPoint();
        loadPointLog();
    }

    /**
     * 读取预存款
     */
    private void loadPoint() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        RemoteDataHandler.asyncLoginPostDataString(Constants.URL_MEMBER_MY_ASSET + "&fields=point", params, myApplication, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject obj = new JSONObject(json);
                        tvPoint.setText(obj.optString("point"));
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(PointLogActivity.this, json);
                }
            }
        });
    }

    /**
     * 读取预存款
     */
    private void loadPointLog() {
        String url = Constants.URL_MEMBER_POINT_LOG + "&curpage=" + currentPage + "&page=" + Constants.PAGESIZE;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    if (!data.isHasMore()) {
                        isHasMore = false;
                    } else {
                        isHasMore = true;
                    }

                    if (currentPage == 1) {
                        pointLogInfoArrayList.clear();
                    }

                    try {
                        JSONObject obj = new JSONObject(json);
                        String pointJson = obj.getString("log_list");
                        ArrayList<PointLogInfo> list = PointLogInfo.newInstanceList(pointJson);
                        if (list.size() > 0) {
                            pointLogInfoArrayList.addAll(list);
                            pointLogListViewAdapter.setList(pointLogInfoArrayList);
                            pointLogListViewAdapter.notifyDataSetChanged();
                        } else {
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(PointLogActivity.this, json);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_point_log, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
