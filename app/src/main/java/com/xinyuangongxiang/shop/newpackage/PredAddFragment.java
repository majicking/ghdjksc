package com.xinyuangongxiang.shop.newpackage;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.alipay.sdk.pay.PayResult;
import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.JSONParser;
import com.xinyuangongxiang.shop.common.MyShopApplication;
import com.xinyuangongxiang.shop.common.ShopHelper;
import com.xinyuangongxiang.shop.common.T;
import com.xinyuangongxiang.shop.common.Utils;
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
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class PredAddFragment extends Fragment {
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    @BindView(R.id.rechargenumber)
    EditText rechargenumber;
    @BindView(R.id.recharge)
    Button recharge;
    Unbinder unbinder;
    private String mParam1;
    private String mParam2;
    private View view;
    private PopupWindow payPopupWindow;

    public PredAddFragment() {
    }

    public static PredAddFragment newInstance(String param1, String param2) {
        PredAddFragment fragment = new PredAddFragment();
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
        view = inflater.inflate(R.layout.fragment_pred_add, container, false);
        unbinder = ButterKnife.bind(this, view);
        rechargenumber.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (rechargenumber.getText().toString().length() == 1 && rechargenumber.getText().toString().equals(".")) {
                    rechargenumber.setText("");
                }
                if (s.toString().indexOf(".") >= 0) {
                    if (rechargenumber.getText().toString().indexOf(".", rechargenumber.getText().toString().indexOf(".") + 1) > 0) {
                        rechargenumber.setText(rechargenumber.getText().toString().substring(0, rechargenumber.getText().toString().length() - 1));
                        rechargenumber.setSelection(rechargenumber.getText().toString().length());
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        return view;
    }

    String rechargetext;

    @OnClick(R.id.recharge)
    public void rechargeApply() {
        rechargetext = rechargenumber.getText().toString().trim();
        if (TextUtils.isEmpty(rechargetext)) {
            Toast.makeText(context, "请输充值金额", Toast.LENGTH_SHORT).show();
            return;
        }
        String url = Constants.RECHARGE;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("pdr_amount", rechargetext);
        Dialog dialog = ProgressDialog.showLoadingProgress(context, "正在生成订单...");
        dialog.show();
        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), data -> {
            ProgressDialog.dismissDialog(dialog);
            String result = data.getJson();
            if (data.getCode() == HttpStatus.SC_OK) {
                if (!TextUtils.isEmpty(result)) {
                    String pay_sn = JSONParser.getStringFromJsonString("pay_sn", result);
                    if (!TextUtils.isEmpty(pay_sn)) {
                        Dialog dialog1 = ProgressDialog.showLoadingProgress(context, "正在处理订单...");
                        dialog1.show();
                        Utils.loadingPaymentListData(o -> {
                                    ProgressDialog.dismissDialog(dialog1);
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
                                            OrderPay(size, arr, pay_sn);
                                        } catch (JSONException e1) {
                                            e1.printStackTrace();
                                        }

                                    } else {
                                        ShopHelper.showApiError(context, json);
                                    }
                                }
                        );

                    }
                }
            } else {
                ShopHelper.showApiError(context, result);
            }
        });
    }


    public void onButtonPressed(String title, int flag) {
        if (mListener != null) {
            mListener.onFragmentInteraction(title, flag);
        }
    }

    Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (payPopupWindow != null && payPopupWindow.isShowing()) {
                payPopupWindow.dismiss();
            }
            if (msg.what == 1) {
                if (msg.obj != null) {
                    Toast.makeText(getActivity(), "支付成功", Toast.LENGTH_SHORT).show();
                    LogUtils.i((String) msg.obj);
                    onButtonPressed("success", 2);
                }
            } else if (msg.what == 2) {
                Toast.makeText(getActivity(), "支付异常,订单生成成功", Toast.LENGTH_SHORT).show();
                LogUtils.i((String) msg.obj);
                onButtonPressed("error", 2);
            } else if (msg.what == 3) {
                Toast.makeText(getContext(), "支付结果确认中", Toast.LENGTH_SHORT).show();
                onButtonPressed("error", 2);
            } else {
                Toast.makeText(getContext(), "支付失败", Toast.LENGTH_SHORT).show();
                onButtonPressed("error", 2);
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
            onButtonPressed("error",2);
        }, listener);
        if (payPopupWindow == null) {
            payPopupWindow = Utils.initPopupWindow(context, view, listener);
        }

        if (payPopupWindow != null && !payPopupWindow.isShowing()) {
            payPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            Utils.backgroundAlpha(context, 0.5f);
        }


    }


    Context context;

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

    private OnFragmentInteractionListener mListener;

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbinder.unbind();
    }
}
