package com.guohanhealth.shop.ui.store;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.adapter.GoodsListViewAdapter;
import com.guohanhealth.shop.bean.GoodsList;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.MyExceptionHandler;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.custom.GridViewForScrollView;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.RemoteDataHandler.Callback;
import com.guohanhealth.shop.http.ResponseData;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 商品列表网格Fragment
 *
 * @author dqw
 * @Time 2015-7-10
 */
public class StoreGoodsGridFragment extends Fragment {

    public int pageno = 1;
    private boolean loadMore = true;
    public String url;

    private GoodsListViewAdapter goodsListViewAdapter;

    private GridViewForScrollView gvGoodsGrid;
    private ArrayList<GoodsList> goodsLists;
    private TextView tvNoResult;
    private TextView textMore;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewLayout = inflater.inflate(R.layout.store_goods_fragment_grid, container, false);
        MyExceptionHandler.getInstance().setContext(getActivity());
        initViewID(viewLayout);//注册控件ID

        return viewLayout;
    }

    /** 初始化注册控件ID */
    public void initViewID(View view){
        textMore = (TextView)view.findViewById(R.id.textMore);
        gvGoodsGrid = (GridViewForScrollView)view.findViewById(R.id.gvGoodsGrid);
        goodsListViewAdapter = new GoodsListViewAdapter(getActivity(),"grid");
        goodsLists = new ArrayList<GoodsList>();
        gvGoodsGrid.setAdapter(goodsListViewAdapter);

        loadingGoodsListData();

        tvNoResult = (TextView) view.findViewById(R.id.tvNoResult);
    }

    /**
     * 初始化加载列表数据
     */
    public void loadingGoodsListData() {

        url = url + "&curpage=" + pageno + "&page=" + Constants.PAGESIZE;
        RemoteDataHandler.asyncDataStringGet(url, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {

                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    if (!data.isHasMore()) {
                        textMore.setVisibility(View.GONE);
                    } else {
                        textMore.setVisibility(View.VISIBLE);
                        loadMoreData();
                    }
                    if (pageno == 1) {
                        goodsLists.clear();
                    }
                    try {
                        JSONObject obj = new JSONObject(json);
                        String array = obj.getString("goods_list");
                        if (array != "" && !array.equals("array") && array != null && !array.equals("[]")) {
                            ArrayList<GoodsList> list = GoodsList.newInstanceList(array);
                            goodsLists.addAll(list);
                            goodsListViewAdapter.setGoodsLists(goodsLists);
                            goodsListViewAdapter.notifyDataSetChanged();
                        } else {
                            if (pageno == 1) {
                                tvNoResult.setVisibility(View.VISIBLE);
                            }
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

    private void loadMoreData(){
        textMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pageno = pageno + 1;
                loadingGoodsListData();
            }
        });
    }
}
