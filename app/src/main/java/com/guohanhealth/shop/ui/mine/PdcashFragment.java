package com.guohanhealth.shop.ui.mine;


import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.adapter.PdcashListViewAdapter;
import com.guohanhealth.shop.bean.PdcashInfo;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.MyExceptionHandler;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.custom.MyListEmpty;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.ResponseData;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 余额提现列表Fragment
 *
 * @author dqw
 * @Time 2015-8-25
 */
public class PdcashFragment extends Fragment {
    MyShopApplication myApplication;
    ListView lvPdcash;
    ArrayList<PdcashInfo> pdcashInfoArrayList;
    PdcashListViewAdapter pdcashListViewAdapter;
    MyListEmpty myListEmpty;

    int currentPage = 1;
    boolean isHasMore = true;
    boolean isLastRow = false;

    public static PdcashFragment newInstance() {
        PdcashFragment fragment = new PdcashFragment();
        return fragment;
    }

    public PdcashFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        myApplication = (MyShopApplication) getActivity().getApplicationContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_pdcash, container, false);
        MyExceptionHandler.getInstance().setContext(getActivity());
        lvPdcash = (ListView) layout.findViewById(R.id.lvPdcash);
        lvPdcash.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (isHasMore && isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    isLastRow = false;
                    currentPage += 1;
                    loadPdcash();
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
                    isLastRow = true;
                }
            }
        });
        pdcashInfoArrayList = new ArrayList<PdcashInfo>();
        pdcashListViewAdapter = new PdcashListViewAdapter(getActivity());
        lvPdcash.setAdapter(pdcashListViewAdapter);
        myListEmpty = (MyListEmpty) layout.findViewById(R.id.myListEmpty);
        myListEmpty.setListEmpty(R.drawable.nc_icon_predeposit_white, "您尚未提现过预存款", "使用商城预存款结算更方便");
        loadPdcash();
        return layout;
    }

    /**
     * 读取充值明细
     */
    private void loadPdcash() {
        String url = Constants.URL_MEMBER_FUND_PDCASHLIST + "&curpage=" + currentPage + "&page=" + Constants.PAGESIZE;
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
                        pdcashInfoArrayList.clear();
                        myListEmpty.setVisibility(View.GONE);
                    }

                    try {
                        JSONObject obj = new JSONObject(json);
                        String pdcashJson = obj.getString("list");
                        ArrayList<PdcashInfo> list = PdcashInfo.newInstanceList(pdcashJson);
                        if (list.size() > 0) {
                            pdcashInfoArrayList.addAll(list);
                            pdcashListViewAdapter.setList(pdcashInfoArrayList);
                            pdcashListViewAdapter.notifyDataSetChanged();
                        } else {
                            myListEmpty.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(getActivity(), json);
                }
            }
        });
    }


}
