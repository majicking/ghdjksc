package com.xinyuangongxiang.shop.ui.type;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.adapter.GoodsListViewAdapter;
import com.xinyuangongxiang.shop.bean.GoodsList;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.MyExceptionHandler;
import com.xinyuangongxiang.shop.common.ShopHelper;
import com.xinyuangongxiang.shop.custom.MyListEmpty;
import com.xinyuangongxiang.shop.custom.XListView;
import com.xinyuangongxiang.shop.custom.XListView.IXListViewListener;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.RemoteDataHandler.Callback;
import com.xinyuangongxiang.shop.http.ResponseData;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 商品列表Fragment
 *
 * @author dqw
 * @Time 2015-7-10
 */
public class GoodsListFragment extends Fragment implements IXListViewListener {

    public String url;
    public int pageno = 1;

    private GoodsListViewAdapter goodsListViewAdapter;

    private Handler mXLHandler;

    private XListView listViewID;

    private ArrayList<GoodsList> goodsLists;

    private MyListEmpty myListEmpty;
    private RelativeLayout rl_main_list;
    private Button top_btn;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View viewLayout = inflater.inflate(R.layout.goods_fragment_list, container, false);
        MyExceptionHandler.getInstance().setContext(getActivity());
        initViewID(viewLayout);//注册控件ID

        return viewLayout;
    }

    /**
     * 初始化注册控件ID
     */
    public void initViewID(View view) {

        listViewID = (XListView) view.findViewById(R.id.listViewID);
        rl_main_list = (RelativeLayout) view.findViewById(R.id.rl_main_list);
        top_btn = (Button) view.findViewById(R.id.top_btn);
        goodsListViewAdapter = new GoodsListViewAdapter(getActivity(), "list");

        goodsLists = new ArrayList<GoodsList>();

        listViewID.setAdapter(goodsListViewAdapter);

        loadingGoodsListData();
        listViewID.setXListViewListener(this);
        listViewID.setPullRefreshEnable(false);//禁止下拉刷新
        mXLHandler = new Handler();
        listViewID.setOnScrollListener(new XListView.OnXScrollListener() {
            @Override
            public void onXScrolling(View view) {

            }

            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {

            }

            @Override
            public void onScroll(AbsListView absListView, int i, int i2, int i3) {
                int top = absListView.getTop();
                int scrollheight = -top + i * absListView.getHeight();
                if (scrollheight >= rl_main_list.getBottom()) {
                    top_btn.setVisibility(View.VISIBLE);
                } else {
                    top_btn.setVisibility(View.GONE);
                }
            }
        });

        myListEmpty = (MyListEmpty) view.findViewById(R.id.myListEmpty);
        myListEmpty.setListEmpty(R.drawable.nc_icon_order, "没有找到符合条件的商品", "更换筛选条件找到你想要的商品");

        top_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                top_btn.setVisibility(View.GONE);
                listViewID.setSelectionFromTop(0, 0);
            }
        });
    }

    @Override
    public void onRefresh() {
        //下拉刷新
    }

    @Override
    public void onLoadMore() {
        //上拉加载
        mXLHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                pageno = pageno + 1;
                loadingGoodsListData();
            }
        }, 1000);
    }

    /**
     * 初始化加载列表数据
     */
    public void loadingGoodsListData() {

        url = url + "&curpage=" + pageno + "&page=" + Constants.PAGESIZE;

        RemoteDataHandler.asyncDataStringGet(url, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {

                listViewID.stopLoadMore();
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    if (!TextUtils.isEmpty(json)) {
                        if (!data.isHasMore()) {
                            listViewID.setPullLoadEnable(false);
                        } else {
                            listViewID.setPullLoadEnable(true);
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
                            } else {
                                if (pageno == 1)
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

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }
}
