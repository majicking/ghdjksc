package com.xinyuangongxiang.shop.ui.type;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.adapter.GoodsListViewAdapter;
import com.xinyuangongxiang.shop.bean.GoodsList;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.MyExceptionHandler;
import com.xinyuangongxiang.shop.common.ShopHelper;
import com.xinyuangongxiang.shop.custom.MyGridView;
import com.xinyuangongxiang.shop.custom.MyListEmpty;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.RemoteDataHandler.Callback;
import com.xinyuangongxiang.shop.http.ResponseData;

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
public class GoodsGridFragment extends Fragment {

    public int pageno = 1;
    private boolean loadMore = true;
    public String url;

    private GoodsListViewAdapter goodsListViewAdapter;
    private ScrollView svGoodsGrid;
    private LinearLayout llGoodsGrid;
    private TextView tvLoadMore;
    private MyGridView gvGoodsGrid;
    private ArrayList<GoodsList> goodsLists;
    private MyListEmpty myListEmpty;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewLayout = inflater.inflate(R.layout.goods_fragment_grid, container, false);

        MyExceptionHandler.getInstance().setContext(getActivity());
        svGoodsGrid = (ScrollView) viewLayout.findViewById(R.id.svGoodsGrid);
        llGoodsGrid = (LinearLayout) viewLayout.findViewById(R.id.llGoodsGrid);
        gvGoodsGrid = (MyGridView) viewLayout.findViewById(R.id.gvGoodsGrid);
        tvLoadMore = (TextView) viewLayout.findViewById(R.id.tvLoadMore);
        goodsListViewAdapter = new GoodsListViewAdapter(getActivity(), "grid");
        goodsLists = new ArrayList<GoodsList>();
        gvGoodsGrid.setAdapter(goodsListViewAdapter);
        loadingGoodsListData();
        myListEmpty = (MyListEmpty) viewLayout.findViewById(R.id.myListEmpty);
        myListEmpty.setListEmpty(R.drawable.nc_icon_order, "没有找到符合条件的商品", "更换筛选条件找到你想要的商品");

        svGoodsGrid.setOnTouchListener(new View.OnTouchListener() {
            private int lastY = 0;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    lastY = svGoodsGrid.getScrollY();
                    if (lastY == (llGoodsGrid.getHeight() - svGoodsGrid.getHeight())) {
                        if (loadMore) {
                            tvLoadMore.setVisibility(View.VISIBLE);
                            pageno = pageno + 1;
                            loadingGoodsListData();
                        }
                    }
                }
                return false;
            }
        });

        return viewLayout;
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
                    if (!TextUtils.isEmpty(json)) {
                        tvLoadMore.setVisibility(View.GONE);
                        if (data.isHasMore()) {
                            loadMore = true;
                        } else {
                            loadMore = false;
                        }
                        if (pageno == 1) {
                            goodsLists.clear();
                        }

                        if (myListEmpty != null)
                            myListEmpty.setVisibility(View.GONE);
                        try {
                            JSONObject obj = new JSONObject(json);
                            String array = obj.getString("goods_list");
                            if (array != "" && !array.equals("array") && array != null && !array.equals("[]")) {
                                ArrayList<GoodsList> list = GoodsList.newInstanceList(array);
                                goodsLists.addAll(list);
                                goodsListViewAdapter.setGoodsLists(goodsLists);
                                goodsListViewAdapter.notifyDataSetChanged();
                                svGoodsGrid.scrollTo(0, svGoodsGrid.getScrollY() + 100);
                            } else {
                                if (pageno==1)
                                myListEmpty.setVisibility(View.VISIBLE);
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    } else {
                        myListEmpty.setVisibility(View.VISIBLE);
                        ShopHelper.showApiError(context, json);
                    }
                } else {
                    ShopHelper.showApiError(context, json);
                }
            }
        });
    }

    Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
