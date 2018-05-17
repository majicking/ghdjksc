package com.xinyuangongxiang.shop.newpackage;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.alipay.sdk.pay.PayResult;
import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.adapter.PdcashListViewAdapter;
import com.xinyuangongxiang.shop.adapter.PdrechargeListViewAdapter;
import com.xinyuangongxiang.shop.adapter.PredepositLogListViewAdapter;
import com.xinyuangongxiang.shop.bean.PdcashInfo;
import com.xinyuangongxiang.shop.bean.PdrechargeInfo;
import com.xinyuangongxiang.shop.bean.PredepositLogInfo;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.MyShopApplication;
import com.xinyuangongxiang.shop.common.ShopHelper;
import com.xinyuangongxiang.shop.common.T;
import com.xinyuangongxiang.shop.common.Utils;
import com.xinyuangongxiang.shop.custom.MyListEmpty;
import com.xinyuangongxiang.shop.custom.XListView;
import com.xinyuangongxiang.shop.http.HttpHelper;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.ResponseData;
import com.xinyuangongxiang.shop.ncinterface.DataCallback;
import com.xinyuangongxiang.shop.xrefresh.utils.LogUtils;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;


public class PredOtherFragment extends Fragment implements XListView.IXListViewListener {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    @BindView(R.id.xlistview)
    XListView xlistview;
    Unbinder unbinder;
    @BindView(R.id.myListEmpty)
    MyListEmpty myListEmpty;
    private String mParam1;
    private String mParam2;
    private View view;
    private Handler mXLHandler;



    public PredOtherFragment() {
    }

    public static PredOtherFragment newInstance(String param1, String param2) {
        PredOtherFragment fragment = new PredOtherFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_pred_other, container, false);
        unbinder = ButterKnife.bind(this, view);
        initView();
        return view;
    }

    ArrayList<PredepositLogInfo> predepositLogInfoArrayList;
    PredepositLogListViewAdapter predepositLogListViewAdapter;

    ArrayList<PdrechargeInfo> pdrechargeInfoArrayList;
    PdrechargeListViewAdapter pdrechargeListViewAdapter;

    ArrayList<PdcashInfo> pdcashInfoArrayList;
    PdcashListViewAdapter pdcashListViewAdapter;


    int currentPage = 1;
    boolean isHasMore = true;

    private void initView() {
        mXLHandler = new Handler();
        xlistview.setXListViewListener(this);
        switch (mParam1) {
            case "1":
                myListEmpty.setListEmpty(R.drawable.nc_icon_predeposit_white, "您尚无预存款收支信息", "使用商城预存款结算更方便");
                predepositLogInfoArrayList = new ArrayList<>();
                predepositLogListViewAdapter = new PredepositLogListViewAdapter(getActivity());
                xlistview.setAdapter(predepositLogListViewAdapter);
                break;
            case "2":
                myListEmpty.setListEmpty(R.drawable.nc_icon_predeposit_white, "您尚未充值过预存款", "使用商城预存款结算更方便");
                pdrechargeInfoArrayList = new ArrayList<>();
                pdrechargeListViewAdapter = new PdrechargeListViewAdapter(getActivity());
                xlistview.setAdapter(pdrechargeListViewAdapter);
                xlistview.setOnItemClickListener((parent, view, position, id) -> {
                    if (pdrechargeInfoArrayList.get(position-1).getPaymentState().equals("0")){
                        Dialog dialog=ProgressDialog.showLoadingProgress(context,"请稍后...");
                        dialog.show();
                        Utils.loadingPaymentListData(o -> {
                                    ProgressDialog.dismissDialog(dialog);
                                    ResponseData data1 = (ResponseData) o;
                                    String json = data1.getJson();
                                    if (data1.getCode() == HttpStatus.SC_OK) {
                                        try {
                                            JSONObject jsonObject = new JSONObject(json);
                                            String JosnObj = jsonObject
                                                    .getString("payment_list");
                                            JSONArray arr = new JSONArray(JosnObj);
                                            LogUtils.i(arr.toString());
                                            int size = null == arr ? 0 : arr.length();
                                            if (size < 1) {
                                                T.showShort(context, "没有支付方式，请后台配置");
                                                return;
                                            }
                                            OrderPay(size, arr, pdrechargeInfoArrayList.get(position-1).getSn());
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }

                                    } else {
                                        ShopHelper.showApiError(context, json);
                                    }
                                }
                        );
                    }

                });
                break;
            case "3":
                myListEmpty.setListEmpty(R.drawable.nc_icon_predeposit_white, "您尚未提现过预存款", "使用商城预存款结算更方便");
                pdcashInfoArrayList = new ArrayList<>();
                pdcashListViewAdapter = new PdcashListViewAdapter(getActivity());
                xlistview.setAdapter(pdcashListViewAdapter);
                break;
        }

    }
    private PopupWindow payPopupWindow;
    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (payPopupWindow != null && payPopupWindow.isShowing()) {
                payPopupWindow.dismiss();
            }
            ((PredepositActivity)context).loadPredeposit();
            if (msg.what == 1) {
                if (msg.obj != null) {
                    Toast.makeText(getActivity(), "支付成功", Toast.LENGTH_SHORT).show();
                    LogUtils.i((String) msg.obj);
                    loadingListData();
                }
            } else if (msg.what == 2) {
                Toast.makeText(getActivity(), "支付异常,订单生成成功", Toast.LENGTH_SHORT).show();
                LogUtils.i((String) msg.obj);

            } else if (msg.what == 3) {
                Toast.makeText(getContext(), "支付结果确认中", Toast.LENGTH_SHORT).show();

            } else {
                Toast.makeText(getContext(), "支付失败", Toast.LENGTH_SHORT).show();

            }

        }
    };

    private void PayResult(String resultInfo) {
        final HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("payresult", resultInfo);
        params.put("pay_sn", pay_sn);
        new Thread(() -> {
            Message ms = new Message();
            String url = MyShopApplication.getInstance().getNotify_ur();
            try {
                String json = HttpHelper.post(url, params);
                ms.what = 1;
                ms.obj = json;
            } catch (IOException e) {
                e.printStackTrace();
                ms.what = 2;
            } finally {
                handler.sendMessage(ms);
            }

        }).start();
    }

    DataCallback callback = (Object o) -> {

        switch (((Message) o).what) {

            case 1:
                PayResult payResult = new PayResult((String) ((Message) o).obj);
                LogUtils.i("payResult------>" + payResult.toString());
                String resultInfo = payResult.getResult();
                String resultStatus = payResult.getResultStatus();
                if (TextUtils.equals(resultStatus, "9000")) {
                    PayResult(resultInfo);
                } else {
                    if (TextUtils.equals(resultStatus, "8000")) {
                        handler.sendEmptyMessage(3);
                    } else {
                        handler.sendEmptyMessage(4);
                    }
                }
                break;
            case 2:
                Toast.makeText(getActivity(), "检查结果为：" + ((Message) o).obj,
                        Toast.LENGTH_SHORT).show();
                handler.sendEmptyMessage(4);
                break;
            default:
                break;
        }
    };

    PopupWindow.OnDismissListener listener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
            Utils.backgroundAlpha(context, 1f);
            if (payPopupWindow != null && payPopupWindow.isShowing()) {
                payPopupWindow.dismiss();
            }
        }
    };

    String pay_sn;

    private void OrderPay(int size, JSONArray arr, final String pay_sn) {
        this.pay_sn = pay_sn;
        View view = Utils.initPayWindowView(context, size, arr, pay_sn, "3", callback, v -> {
            Utils.backgroundAlpha(context, 1f);
            if (payPopupWindow != null && payPopupWindow.isShowing()) {
                payPopupWindow.dismiss();
            }
        }, listener);
        if (payPopupWindow == null) {
            payPopupWindow = Utils.initPopupWindow(context, view, listener);
        }

        if (payPopupWindow != null && !payPopupWindow.isShowing()) {
            payPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            Utils.backgroundAlpha(context, 0.5f);
        }


    }


    @Override
    public void onResume() {
        super.onResume();
        loadingListData();
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.w("--------->", "执行setUserVisibleHint  isVisibleToUser=" + isVisibleToUser);
        if (isVisibleToUser) {
            LogUtils.i("-------------------当前 页面 " + mParam1 + "  isVisibleToUser=" + isVisibleToUser);
        }
    }

    /**
     * 获取数据
     */
    private void loadingListData() {
        ((PredepositActivity)context).loadPredeposit();
        String url = mParam2 + "&curpage=" + currentPage + "&page=" + Constants.PAGESIZE;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String updataTime = sdf.format(new Date(System.currentTimeMillis()));
        xlistview.setRefreshTime(updataTime);
        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), data -> {
            if (xlistview != null) {
                xlistview.stopLoadMore();
                xlistview.stopRefresh();
            }
            String json = data.getJson();
            if (data.getCode() == HttpStatus.SC_OK) {
                if (!data.isHasMore()) {
                    isHasMore = false;
                    if (xlistview != null) {
                        xlistview.setPullLoadEnable(false);
                    }
                } else {
                    isHasMore = true;
                    if (xlistview != null) {
                        xlistview.setPullLoadEnable(true);
                    }
                }
                if (currentPage == 1) {
                    switch (mParam1) {
                        case "1":
                            predepositLogInfoArrayList.clear();
                            break;
                        case "2":
                            pdrechargeInfoArrayList.clear();
                            break;
                        case "3":
                            pdcashInfoArrayList.clear();
                            break;
                    }

                    if (myListEmpty != null)
                        myListEmpty.setVisibility(View.GONE);
                }

                try {
                    JSONObject obj = new JSONObject(json);
                    String result = obj.getString("list");
                    switch (mParam1) {
                        case "1":
                            ArrayList<PredepositLogInfo> list = PredepositLogInfo.newInstanceList(result);
                            if (list.size() > 0) {
                                predepositLogInfoArrayList.addAll(list);
                                predepositLogListViewAdapter.setList(predepositLogInfoArrayList);
                                predepositLogListViewAdapter.notifyDataSetChanged();
                            } else {
                                predepositLogInfoArrayList.clear();
                                myListEmpty.setVisibility(View.VISIBLE);
                            }
                            predepositLogListViewAdapter.notifyDataSetChanged();
                            break;
                        case "2":
                            ArrayList<PdrechargeInfo> list1 = PdrechargeInfo.newInstanceList(result);
                            if (list1.size() > 0) {
                                pdrechargeInfoArrayList.addAll(list1);
                                pdrechargeListViewAdapter.setList(pdrechargeInfoArrayList);
                                pdrechargeListViewAdapter.notifyDataSetChanged();
                            } else {
                                pdrechargeInfoArrayList.clear();
                                myListEmpty.setVisibility(View.VISIBLE);
                            }
                            pdrechargeListViewAdapter.notifyDataSetChanged();
                            break;
                        case "3":
                            ArrayList<PdcashInfo> list2 = PdcashInfo.newInstanceList(result);
                            if (list2.size() > 0) {
                                pdcashInfoArrayList.addAll(list2);
                                pdcashListViewAdapter.setList(pdcashInfoArrayList);
                                pdcashListViewAdapter.notifyDataSetChanged();
                            } else {
                                pdcashInfoArrayList.clear();
                                myListEmpty.setVisibility(View.VISIBLE);
                            }
                            pdcashListViewAdapter.notifyDataSetChanged();
                            break;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                ShopHelper.showApiError(getActivity(), json);
            }
        });
    }

    private Context context;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
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

    @Override
    public void onRefresh() {
        //下拉刷新
        mXLHandler.postDelayed(() -> {
            currentPage = 1;
            isHasMore = true;
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
                currentPage = currentPage + 1;
                loadingListData();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1000);
    }
}
