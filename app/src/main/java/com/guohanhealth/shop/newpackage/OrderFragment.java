package com.guohanhealth.shop.newpackage;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.adapter.OrderGroupListViewAdapter;
import com.guohanhealth.shop.adapter.VirtualOrderListViewAdapter;
import com.guohanhealth.shop.bean.OrderGroupList;
import com.guohanhealth.shop.bean.VirtualList;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.custom.XListView;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.ResponseData;
import com.guohanhealth.shop.ui.mine.OrderListActivity;
import com.guohanhealth.shop.xrefresh.utils.LogUtils;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link OrderFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class OrderFragment extends Fragment implements XListView.IXListViewListener {



    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER

    public OrderFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment OrderFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Fragment newInstance(String param1, String param2, String param3, String param4, boolean flag) {
        OrderFragment fragment = new OrderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        args.putString(ARG_PARAM3, param3);
        args.putString(ARG_PARAM4, param4);
        args.putBoolean(ARG_PARAM5, flag);
        fragment.setArguments(args);

        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
            mParam3 = getArguments().getString(ARG_PARAM3);
            mParam4 = getArguments().getString(ARG_PARAM4);
            flag = getArguments().getBoolean(ARG_PARAM5, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_order, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        initData();
        return view;
    }


    Unbinder unbinder;
    @BindView(R.id.xlistview)
    XListView xlistview;

    // TODO: Rename and change types of parameters
    private String mParam1;//标题
    private String mParam2;//标识
    private String mParam3;//搜索内容
    private String mParam4;//虚拟/实物标识url
    private boolean flag;//虚拟/实物标识flag
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private static final String ARG_PARAM3 = "param3";
    private static final String ARG_PARAM4 = "param4";
    private static final String ARG_PARAM5 = "param5";
    private OnFragmentInteractionListener mListener;
    private View view;
    private LinearLayout llListEmpty;
    private boolean isHasemore;
    Context context;
    private ArrayList<OrderGroupList> orderLists;
    private ArrayList<VirtualList> lists;

    private OrderGroupListViewAdapter adapter;
    private VirtualOrderListViewAdapter virtualadapter;
    int pageno = 1;//当前页码
    private Handler mXLHandler;

    private void initView() {
        orderLists = new ArrayList<>();
        lists = new ArrayList<>();
        xlistview.setXListViewListener(this);
        mXLHandler = new Handler();
        adapter = new OrderGroupListViewAdapter(getActivity());
        virtualadapter = new VirtualOrderListViewAdapter(getActivity());
        setListEmpty(R.drawable.nc_icon_order, "您还没有相关订单", "可以去看看哪些想要买的");
        if (flag) {
            xlistview.setAdapter(virtualadapter);
        } else {
            xlistview.setAdapter(adapter);
        }
    }


    /**
     * 空列表背景
     */
    protected void setListEmpty(int resId, String title, String subTitle) {
        llListEmpty = (LinearLayout) view.findViewById(R.id.llListEmpty);
        ImageView ivListEmpty = (ImageView) view.findViewById(R.id.ivListEmpty);
        ivListEmpty.setImageResource(resId);
        TextView tvListEmptyTitle = (TextView) view.findViewById(R.id.tvListEmptyTitle);
        TextView tvListEmptySubTitle = (TextView) view.findViewById(R.id.tvListEmptySubTitle);
        tvListEmptyTitle.setText(title);
        tvListEmptySubTitle.setText(subTitle);
    }

    private void initData() {

    }


    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(String title, String flag) {
        if (mListener != null) {
            mListener.onFragmentInteraction(title, flag);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }


    @Override
    public void onResume() {
        super.onResume();
        registerBoradcastReceiver();
        loadingListData();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        context.unregisterReceiver(mBroadcastReceiver);
    }

    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action.equals(Constants.REFRESHLAYOUT)) {
                pageno = 1;
                if (xlistview != null)
                    xlistview.setPullLoadEnable(true);
                loadingListData();//初始化加载我的信息
            }
        }
    };

    public void registerBoradcastReceiver() {
        IntentFilter myIntentFilter = new IntentFilter();
        myIntentFilter.addAction(Constants.REFRESHLAYOUT);
        context.registerReceiver(mBroadcastReceiver, myIntentFilter);  //注册广播
    }

    @Override
    public void onRefresh() {
        //下拉刷新
        mXLHandler.postDelayed(() -> {
            pageno = 1;
            try {
                xlistview.setPullLoadEnable(true);
                loadingListData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1000);
    }

    @Override
    public void onLoadMore() {
        //上拉加载
        mXLHandler.postDelayed(() -> {
            try {
//                if (!isHasemore){
//                    Toast.makeText(context,"没有数据啦",Toast.LENGTH_SHORT).show();
//                    return;
//                }

                pageno = pageno + 1;
                loadingListData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1000);
    }

    /**
     * 初始化加载数据
     */
    public void loadingListData() {
        LogUtils.i("当前请求数据参数  param1=" + mParam1 + "  param2=" + mParam2 + "  param3=" + mParam3 + "   param4=" + mParam4);


        String url = mParam4 + "&curpage=" + pageno + "&page=" + Constants.PAGESIZE;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("state_type", mParam2);
        if (mParam3 != null && !mParam3.equals("")) {
            params.put("order_key", mParam3);
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String updataTime = sdf.format(new Date(System.currentTimeMillis()));
        xlistview.setRefreshTime(updataTime);
        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                mParam3 = "";
                if (xlistview != null) {
                    xlistview.stopLoadMore();
                    xlistview.stopRefresh();
                }
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    if (!data.isHasMore()) {
                        isHasemore = false;
                        if (xlistview != null) {
                            xlistview.setPullLoadEnable(false);
                        }
                    } else {
                        isHasemore = true;
                        if (xlistview != null) {
                            xlistview.setPullLoadEnable(true);
                        }
                    }
                    if (pageno == 1) {
                        orderLists.clear();
                        lists.clear();
                        llListEmpty.setVisibility(View.GONE);
                    }

                    try {
                        if (flag) {
                            JSONObject obj = new JSONObject(json);
                            String order_group_list = obj.getString("order_list");
                            ArrayList<VirtualList> virtualLists = VirtualList.newInstanceList(order_group_list);
                            if (virtualLists.size() > 0) {
                                lists.addAll(virtualLists);
                                virtualadapter.setVirtualLists(lists);
                                virtualadapter.notifyDataSetChanged();
                            } else {
                                lists.clear();
                                virtualadapter.notifyDataSetChanged();
                                llListEmpty.setVisibility(View.VISIBLE);
                            }
                        } else {

                            JSONObject obj = new JSONObject(json);
                            String order_group_list = obj.getString("order_group_list");
                            ArrayList<OrderGroupList> groupList = OrderGroupList.newInstanceList(order_group_list);
                            if (groupList.size() > 0) {
                                orderLists.addAll(groupList);
                                adapter.setOrderLists(orderLists);
                                adapter.notifyDataSetChanged();
                            } else {
                                orderLists.clear();
                                adapter.notifyDataSetChanged();
                                llListEmpty.setVisibility(View.VISIBLE);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    ShopHelper.showApiError(context, json);
                }
            }
        });
    }


    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(String title, String flag);
    }
}
