package com.guohanhealth.shop.ui.store;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.adapter.StoreGoodsMyGridViewListAdapter;
import com.guohanhealth.shop.bean.GoodsList;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.MyExceptionHandler;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.custom.MyGridView;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.ResponseData;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 店铺上新
 *
 * Created by huting on 2015/12/29.
 */
public class StoreNewGoodsFragment extends Fragment {

    private MyShopApplication myApplication;
    private static final String ARG_STORE_ID = "store_id";
    private String store_id;

    private LinearLayout llNoData;//没有数据

    private MyGridView sotreGoodsGridViewID;//店铺商品展示
    private StoreGoodsMyGridViewListAdapter goodsListViewAdapter;

    public StoreNewGoodsFragment() {}
    public static StoreNewGoodsFragment newInstance(String store_id) {
        StoreNewGoodsFragment fragment = new StoreNewGoodsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_STORE_ID, store_id);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            store_id = getArguments().getString(ARG_STORE_ID);
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_store_new, container, false);
        MyExceptionHandler.getInstance().setContext(getActivity());
        myApplication = (MyShopApplication) getActivity().getApplicationContext();

        initView(layout);

        return layout;
    }

    /**
     * 初始化控件
     * @param layout
     */
    private void initView(View layout){
        llNoData = (LinearLayout) layout.findViewById(R.id.llNoData);

        sotreGoodsGridViewID = (MyGridView)layout.findViewById(R.id.sotreGoodsGridViewID);
        goodsListViewAdapter = new StoreGoodsMyGridViewListAdapter(getActivity());
        sotreGoodsGridViewID.setAdapter(goodsListViewAdapter);
        sotreGoodsGridViewID.setFocusable(false);

        initData();
    }

    /**
     * 初始化界面数据
     */
    private void initData(){
        String url = Constants.URL_STORE_GOODS_NEW;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("store_id", store_id);

        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject obj = new JSONObject(json);
                        String array = obj.getString("goods_list");
                        if (array != "" && !array.equals("array") && array != null && !array.equals("[]")) {
                            ArrayList<GoodsList> list = GoodsList.newInstanceList(array);
                            goodsListViewAdapter.setGoodsList(list);
                            goodsListViewAdapter.notifyDataSetChanged();
                            llNoData.setVisibility(View.GONE);
                        } else {
                            llNoData.setVisibility(View.VISIBLE);
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

    public interface OnFragmentInteractionListener {
        public void onFragmentInteraction(Uri uri);
    }
}
