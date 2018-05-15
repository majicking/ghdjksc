package com.xinyuangongxiang.shop.adapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alipay.sdk.pay.PayResult;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;
import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.bean.OrderGoodsList;
import com.xinyuangongxiang.shop.bean.OrderGroupList;
import com.xinyuangongxiang.shop.bean.OrderList;
import com.xinyuangongxiang.shop.common.AnimateFirstDisplayListener;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.MyShopApplication;
import com.xinyuangongxiang.shop.common.SystemHelper;
import com.xinyuangongxiang.shop.common.T;
import com.xinyuangongxiang.shop.common.Utils;
import com.xinyuangongxiang.shop.http.HttpHelper;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.ResponseData;
import com.xinyuangongxiang.shop.ncinterface.DataCallback;
import com.xinyuangongxiang.shop.newpackage.ProgressDialog;
import com.xinyuangongxiang.shop.ui.mine.OrderDeliverDetailsActivity;
import com.xinyuangongxiang.shop.ui.mine.OrderDetailsActivity;
import com.xinyuangongxiang.shop.ui.mine.OrderExchangeActivity;
import com.xinyuangongxiang.shop.ui.type.EvaluateActivity;
import com.xinyuangongxiang.shop.ui.type.EvaluateAddActivity;
import com.xinyuangongxiang.shop.xrefresh.utils.LogUtils;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * 我的订单列表适配器
 *
 * @author KingKong·HE
 * @Time 2014-1-6 下午12:06:09
 * @E-mail hjgang@bizpoer.com
 */
public class OrderGroupListViewAdapter extends BaseAdapter {
    private Activity context;

    private LayoutInflater inflater;

    private ArrayList<OrderGroupList> orderLists;

    private MyShopApplication myApplication;

    protected ImageLoader imageLoader = ImageLoader.getInstance();

    private DisplayImageOptions options = SystemHelper.getDisplayImageOptions();

    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

//    private AlertDialog menuDialog;// menu菜单Dialog

//    private GridView menuGrid;

    //    private View menuView;
    PopupWindow pop;
    private OrderGroupList groupList2FU;
    private String pay_sn;

    public OrderGroupListViewAdapter(Activity context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        myApplication = (MyShopApplication) context.getApplicationContext();
        // 创建AlertDialog
//        menuView = View.inflate(context, R.layout.gridview_menu, null);
//        menuDialog = new AlertDialog.Builder(context).create();
//        menuDialog.setView(menuView);
//        menuGrid = (GridView) menuView.findViewById(R.id.gridview);
    }

    @Override
    public int getCount() {
        return orderLists == null ? 0 : orderLists.size();
    }

    @Override
    public Object getItem(int position) {
        return orderLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public ArrayList<OrderGroupList> getOrderLists() {
        return orderLists;
    }

    public void setOrderLists(ArrayList<OrderGroupList> orderLists) {
        this.orderLists = orderLists;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.listivew_order_item, null);
            holder = new ViewHolder();
            holder.linearLayoutFLag = (LinearLayout) convertView.findViewById(R.id.linearLayoutFLag);
            holder.buttonFuKuan = (Button) convertView.findViewById(R.id.buttonFuKuan);
            holder.addViewID = (LinearLayout) convertView.findViewById(R.id.addViewID);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final OrderGroupList bean = orderLists.get(position);

        if (!bean.getPay_amount().equals("")
                && !bean.getPay_amount().equals("null")
                && !bean.getPay_amount().equals("0")
                && bean.getPay_amount() != null) {
            holder.linearLayoutFLag.setVisibility(View.VISIBLE);
        } else {
            holder.linearLayoutFLag.setVisibility(View.GONE);
        }

        if (!bean.getPay_amount().equals("0") && !bean.getPay_amount().equals("null") && bean.getPay_amount() != null) {
            String price = new DecimalFormat("#0.00").format(Double.parseDouble((bean.getPay_amount() == null ? "0.00" : bean.getPay_amount()) == "" ? "0.00" : bean.getPay_amount()));
            holder.buttonFuKuan.setText("订单支付(￥ " + price + ")");
        }

        holder.buttonFuKuan.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // Intent intent = new Intent(context,PayMentWebAcivity.class);
                // intent.putExtra("pay_sn", bean.getPay_sn());
                // context.startActivity(intent);;
                groupList2FU = orderLists.get(position);
//                menuDialog.show();
//                loadingPaymentListData();
                Utils.loadingPaymentListData(
                        new DataCallback() {
                            @Override
                            public void data(Object o) {
                                ResponseData data = (ResponseData) o;
                                String json = data.getJson();
                                if (data.getCode() == HttpStatus.SC_OK) {
                                    try {
                                        JSONObject jsonObject = new JSONObject(json);
                                        String JosnObj = jsonObject
                                                .getString("payment_list");
                                        JSONArray arr = new JSONArray(JosnObj);
                                        Log.d("huting====pay", arr.toString());

                                        int size = null == arr ? 0 : arr.length();
                                        if (size < 1) {
                                            T.showShort(context, "没有支付方式，请后台配置");
                                            return;
                                        }
                                        LogUtils.i("订单号--->" + groupList2FU.getPay_sn());
                                        OrderPay(size, arr, groupList2FU.getPay_sn());

                                    } catch (JSONException e1) {
                                        e1.printStackTrace();
                                    }

                                } else {
                                    try {
                                        JSONObject obj2 = new JSONObject(json);
                                        String error = obj2.getString("error");
                                        if (error != null) {
                                            T.showShort(context, error);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                            }
                        });

            }
        });

        ArrayList<OrderList> orderLists = OrderList.newInstanceList(bean.getOrder_list());

        holder.addViewID.removeAllViews();

        for (int i = 0; i < orderLists.size(); i++) {
            OrderList orderList = orderLists.get(i);
            View orderListView = inflater.inflate(R.layout.listivew_order2_item, null);

            initUIOrderList(orderListView, orderList);

            holder.addViewID.addView(orderListView);
        }

        return convertView;
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
                    Toast.makeText(context, "支付成功", Toast.LENGTH_SHORT).show();
                    LogUtils.i((String) msg.obj);
                    Intent mIntent = new Intent(
                            Constants.REFRESHLAYOUT);
                    context.sendBroadcast(mIntent);
                }
            } else if (msg.what == 2) {
                Toast.makeText(context, "支付成功", Toast.LENGTH_SHORT).show();
                LogUtils.i((String) msg.obj);
                Intent mIntent = new Intent(
                        Constants.REFRESHLAYOUT);
                context.sendBroadcast(mIntent);
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

    DataCallback callback = new DataCallback() {
        @Override
        public void data(Object o) {
            switch (((Message) o).what) {
                case 1: {
                    PayResult payResult = new PayResult((String) ((Message) o).obj);

                    // 支付宝返回此次支付结果及加签，建议对支付宝签名信息拿签约时支付宝提供的公钥做验签
                    String resultInfo = payResult.getResult();

                    String resultStatus = payResult.getResultStatus();

                    // 判断resultStatus 为“9000”则代表支付成功，具体状态码代表含义可参考接口文档
                    if (TextUtils.equals(resultStatus, "9000")) {
                        Toast.makeText(context, "支付成功",
                                Toast.LENGTH_SHORT).show();

                        PayResult(resultInfo);

                    } else {
                        // 判断resultStatus 为非“9000”则代表可能支付失败
                        // “8000”代表支付结果因为支付渠道原因或者系统原因还在等待支付结果确认，最终交易是否成功以服务端异步通知为准（小概率状态）
                        if (TextUtils.equals(resultStatus, "8000")) {
                            Toast.makeText(context, "支付结果确认中",
                                    Toast.LENGTH_SHORT).show();
                        } else {
                            // 其他值就可以判断为支付失败，包括用户主动取消支付，或者系统返回的错误
                            Toast.makeText(context, "支付失败",
                                    Toast.LENGTH_SHORT).show();

                        }
                    }
                    break;
                }
                case 2: {
                    Toast.makeText(context, "检查结果为：" + ((Message) o).obj,
                            Toast.LENGTH_SHORT).show();
                    break;
                }
                default:
                    break;
            }
        }
    };

    PopupWindow payPopupWindow;
    PopupWindow.OnDismissListener listener = new PopupWindow.OnDismissListener() {
        @Override
        public void onDismiss() {
//            showToast("popdismiss");
            Utils.backgroundAlpha(context, 1f);
            if (payPopupWindow != null && payPopupWindow.isShowing()) {
                payPopupWindow.dismiss();
            }

        }
    };

    private void OrderPay(int size, JSONArray arr, final String pay_sn) {
        View view = Utils.initPayWindowView(context, size, arr, pay_sn, "1", callback, new OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.backgroundAlpha(context, 1f);
                if (payPopupWindow != null && payPopupWindow.isShowing()) {
                    payPopupWindow.dismiss();
                }
            }
        }, listener);
        if (payPopupWindow == null) {
            payPopupWindow = Utils.initPopupWindow(context, view, listener);
        }

        if (!payPopupWindow.isShowing()) {
            payPopupWindow.showAtLocation(view, Gravity.BOTTOM, 0, 0);
            Utils.backgroundAlpha(context, 0.5f);
        }


    }


    /**
     * 生成界面
     */
    public void initUIOrderList(View view, final OrderList orderList) {

        TextView textOrderStoreName = (TextView) view.findViewById(R.id.textOrderStoreName);
        TextView textOrderAllPrice = (TextView) view.findViewById(R.id.textOrderAllPrice);
        TextView textOrderShippingFee = (TextView) view.findViewById(R.id.textOrderShippingFee);
        final Button textOrderOperation = (Button) view.findViewById(R.id.textOrderOperation);
        final Button textOrderOperation1 = (Button) view.findViewById(R.id.textOrderOperation1);
        final Button buttonQueRen = (Button) view.findViewById(R.id.buttonQueRen);
        TextView textOrderSuccess = (TextView) view.findViewById(R.id.textOrderSuccess);
        LinearLayout addViewID = (LinearLayout) view.findViewById(R.id.addViewID);
        TextView textOrderGoodsNum = (TextView) view.findViewById(R.id.textOrderGoodsNum);
        TextView textOrderDel = (TextView) view.findViewById(R.id.textOrderDel);
        TextView textTui = (TextView) view.findViewById(R.id.textTui);

        textOrderStoreName.setText(orderList.getStore_name());
        textOrderAllPrice.setText("￥" + orderList.getOrder_amount());
        textOrderShippingFee.setText("(含运费￥" + orderList.getShipping_fee() + ")");
        ArrayList<OrderGoodsList> goodsDatas = OrderGoodsList.newInstanceList(orderList.getExtend_order_goods());

        textOrderGoodsNum.setText("共" + goodsDatas.size() + "件商品，合计");

        if (orderList.getIf_again().equals("true")) {
            textOrderOperation1.setVisibility(View.VISIBLE);
            textOrderOperation1.setText("再次确认");
        }
        if (orderList.getIf_cancel().equals("true")) {
            textOrderOperation.setVisibility(View.VISIBLE);
            textOrderOperation.setText("取消订单");
        }
        if (orderList.getIf_receive().equals("true")) {
            buttonQueRen.setVisibility(View.VISIBLE);
            buttonQueRen.setText("确认收货");
        }
        if (orderList.getIf_lock().equals("true")) {
            textTui.setVisibility(View.VISIBLE);
        }
        if (orderList.getIf_evaluation().equals("true")) {
            buttonQueRen.setVisibility(View.VISIBLE);
            buttonQueRen.setText("订单评价");
        }
        if (orderList.getIf_evaluation_again().equals("true")) {
            buttonQueRen.setVisibility(View.VISIBLE);
            buttonQueRen.setText("追加评价");
        }
        if (orderList.getIf_refund_cancel().equals("true")) {
            textOrderOperation.setVisibility(View.VISIBLE);
            textOrderOperation.setText("退款");
        }
        if (orderList.getIf_deliver().equals("true")) {
            textOrderOperation.setVisibility(View.VISIBLE);
            textOrderOperation.setText("查看物流");
        }


//        if (orderList.getIf_deliver().equals("true")) {
//            textOrderOperation2.setText(Html.fromHtml("<a href='#'>查看物流</a>"));
//            textOrderOperation2.setVisibility(View.VISIBLE);
//        } else {
//            textOrderOperation2.setVisibility(View.GONE);
//        }

        if (orderList.getState_desc() != null
                && !orderList.getState_desc().equals("")) {
            textOrderSuccess.setVisibility(View.VISIBLE);
            textOrderSuccess.setText(orderList.getState_desc());
            if (orderList.getState_desc().equals("已取消")) {
                textOrderDel.setVisibility(View.VISIBLE);
                textOrderDel.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        loadingSaveOrderData(Constants.URL_ORDER_DEL, orderList.getOrder_id(), textOrderOperation);
                    }
                });
            }
        } else {
            textOrderSuccess.setVisibility(View.GONE);
        }
//029755493
        buttonQueRen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                String s = buttonQueRen.getText().toString();
                if (s.equals("确认收货")) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(context);
                    builder.setTitle("操作提示")
                            .setMessage("是否确认操作")
                            .setNegativeButton("取消", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                }
                            })
                            .setPositiveButton("确认",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int whichButton) {
                                            loadingSaveOrderData(Constants.URL_ORDER_RECEIVE, orderList.getOrder_id(), textOrderOperation);
                                        }
                                    }).create().show();
                } else if (s.equals("订单评价")) {
                    Intent i = new Intent(context, EvaluateActivity.class);
                    i.putExtra("order_id", orderList.getOrder_id());
                    context.startActivity(i);
                } else if (s.equals("追加评价")) {
                    Intent i = new Intent(context, EvaluateAddActivity.class);
                    i.putExtra("order_id", orderList.getOrder_id());
                    context.startActivity(i);
                }
            }
        });
        textOrderOperation1.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("操作提示")
                    .setMessage("是否再次确认")
                    .setNegativeButton("取消", (dialog, whichButton) -> {
                    })
                    .setPositiveButton("确认",
                            (dialog, whichButton) ->
                                    OkAgain(Constants.SUREORDERAGAIN, orderList.getOrder_id())).create().show();
        });
        textOrderOperation.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                final String key = textOrderOperation.getText().toString();
                if (key.equals("查看物流")) {
                    Intent intent = new Intent(context, OrderDeliverDetailsActivity.class);
                    intent.putExtra("order_id", orderList.getOrder_id());
                    context.startActivity(intent);
                    return;
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(context);
                builder.setTitle("操作提示")
                        .setMessage("是否确认操作")
                        .setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int whichButton) {
                                    }
                                })
                        .setPositiveButton("确认",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(
                                            DialogInterface dialog,
                                            int whichButton) {
                                        if (key.equals("取消订单")) {
                                            loadingSaveOrderData(Constants.URL_ORDER_CANCEL, orderList.getOrder_id(), textOrderOperation);
                                        }
                                        if (key.equals("退款")) {
                                            Intent intent = new Intent(context, OrderExchangeActivity.class);
                                            intent.putExtra("order_id", orderList.getOrder_id());
                                            context.startActivity(intent);
                                        }
                                    }
                                }).create().show();

            }
        });
//        textOrderOperation2.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(context, OrderDeliverDetailsActivity.class);
//                intent.putExtra("order_id", orderList.getOrder_id());
//                context.startActivity(intent);
//            }
//        });

        LinearLayout llGift = null;
        LinearLayout llGiftList = null;
        TextView imgZeng = null;
        for (int j = 0; j < goodsDatas.size(); j++) {
            final OrderGoodsList ordergoodsList = goodsDatas.get(j);
            View orderGoodsListView = inflater.inflate(
                    R.layout.listivew_order_goods_item, null);
            addViewID.addView(orderGoodsListView);

            ImageView imageGoodsPic = (ImageView) orderGoodsListView
                    .findViewById(R.id.imageGoodsPic);
            TextView textGoodsName = (TextView) orderGoodsListView
                    .findViewById(R.id.textGoodsName);
            TextView textGoodsPrice = (TextView) orderGoodsListView
                    .findViewById(R.id.textGoodsPrice);
            TextView textGoodsNUM = (TextView) orderGoodsListView
                    .findViewById(R.id.textGoodsNUM);
            imgZeng = (TextView) orderGoodsListView.findViewById(R.id.imgZeng);
            ;
            TextView textGoodsSPec = (TextView) orderGoodsListView.findViewById(R.id.textGoodsSPec);
            llGift = (LinearLayout) orderGoodsListView.findViewById(R.id.llGift);
            llGiftList = (LinearLayout) orderGoodsListView.findViewById(R.id.llGiftList);

            textGoodsName.setText(ordergoodsList.getGoods_name());
            textGoodsPrice.setText("￥" + ordergoodsList.getGoods_price());
            textGoodsNUM.setText("×" + ordergoodsList.getGoods_num());
            textGoodsName.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(context, OrderDetailsActivity.class);
                    i.putExtra("order_id", orderList.getOrder_id());
                    context.startActivity(i);
                }
            });
            if (ordergoodsList.getGoods_spec().equals("null") || ordergoodsList.getGoods_spec().equals("")) {
                textGoodsSPec.setVisibility(View.GONE);
            } else {
                textGoodsSPec.setText(ordergoodsList.getGoods_spec());
            }


            imageLoader.displayImage(ordergoodsList.getGoods_image_url(),
                    imageGoodsPic, options, animateFirstListener);

        }


        //赠品
        String giftListString = orderList.getZengpin_list();
        if (giftListString.equals("") || giftListString.equals("[]")) {
            llGift.setVisibility(View.GONE);
        } else {
            try {
                imgZeng.setVisibility(View.VISIBLE);
                JSONArray giftArray = new JSONArray(giftListString);
                for (int j = 0; j < giftArray.length(); j++) {
                    View giftView = inflater.inflate(R.layout.cart_list_gift_item, null);
                    TextView tvGiftInfo = (TextView) giftView.findViewById(R.id.tvGiftInfo);
                    JSONObject giftObj = (JSONObject) giftArray.get(j);
                    tvGiftInfo.setText(giftObj.optString("goods_name") + "x" + giftObj.optString("goods_num"));
                    llGiftList.addView(giftView);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // goodsListView.setOnItemClickListener(new OnItemClickListener() {
        // @Override
        // public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
        // long arg3) {
        // OrderGoodsList bean =(OrderGoodsList)
        // goodsListView.getItemAtPosition(arg2);
        // if(bean != null){
        // Intent intent =new Intent(context,GoodsDetailsActivity.class);
        // intent.putExtra("goods_id", bean.getGoods_id());
        // context.startActivity(intent);
        // }
        // }
        // });
    }

//    /**
//     * 获取可用支付方式
//     */
//    public void loadingPaymentListData() {
//        HashMap<String, String> params = new HashMap<String, String>();
//        params.put("key", myApplication.getLoginKey());
//        RemoteDataHandler.asyncLoginPostDataString(
//                Constants.URL_ORDER_PAYMENT_LIST, params, myApplication,
//                new Callback() {
//                    @Override
//                    public void dataLoaded(ResponseData data) {
//                        String json = data.getJson();
//                        if (data.getCode() == HttpStatus.SC_OK) {
//                            try {
//                                JSONObject jsonObject = new JSONObject(json);
//                                String JosnObj = jsonObject
//                                        .getString("payment_list");
//                                JSONArray arr = new JSONArray(JosnObj);
//                                Log.d("huting====pay", arr.toString());
//
//                                int size = null == arr ? 0 : arr.length();
//                                if (size < 1) {
//                                    T.showShort(context, "没有支付方式，请后台配置");
//                                    return;
//                                }
//                                View view = initPayWindowView(context, size, arr);
//                                if (pop == null) {
//                                    pop = initPopupWindow(view);
//                                }
//                                if (!pop.isShowing()) {
//                                    pop.showAtLocation(view, Gravity.BOTTOM, 0, 0);
//                                    backgroundAlpha(0.5f);
//                                }
////                                ArrayList<HashMap<String, Object>> hashMaps = new ArrayList<HashMap<String, Object>>();
////                                for (int i = 0; i < size; i++) {
////                                    String Values = arr.getString(i);
////                                    HashMap<String, Object> map = new HashMap<String, Object>();
////                                    if (Values.equals("wxpay")) {
////                                        map.put("itemImage",
////                                                R.drawable.sns_weixin_icon);
////                                        map.put("itemText", "微信支付");
////                                    } else if (Values.equals("alipay")) {
////                                        map.put("itemImage",
////                                                R.drawable.zhifubao_appicon);
////                                        map.put("itemText", "支付宝");
////                                    } else if (Values.equals("alipay_native")) {//TODO Modify 支付宝原生支付
////                                        map.put("itemImage",
////                                                R.drawable.pay);
////                                        map.put("itemText", "原生支付");
////                                    }
////                                    if(!map.isEmpty()){
////                                        hashMaps.add(map);
////                                    }
////
////                                }
////                                SimpleAdapter simperAdapter = new SimpleAdapter(
////                                        context,
////                                        hashMaps,
////                                        R.layout.item_menu,
////                                        new String[]{"itemImage", "itemText"},
////                                        new int[]{R.id.item_image,
////                                                R.id.item_text});
////                                menuGrid.setAdapter(simperAdapter);
//                            } catch (JSONException e1) {
//                                e1.printStackTrace();
//                            }
//
//                        } else {
//                            try {
//                                JSONObject obj2 = new JSONObject(json);
//                                String error = obj2.getString("error");
//                                if (error != null) {
//                                    Toast.makeText(context, error,
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                });
//    }
//
//    /**
//     * 获取微信参数
//     *
//     * @param pay_sn 支付编号
//     */
//    public void loadingWXPaymentData(String pay_sn) {
//        HashMap<String, String> params = new HashMap<String, String>();
//        params.put("key", myApplication.getLoginKey());
//        params.put("pay_sn", pay_sn);
//        Log.d("dqw", Constants.URL_MEMBER_WX_PAYMENT);
//        Log.d("dqw", myApplication.getLoginKey());
//        Log.d("dqw", pay_sn);
//
//        RemoteDataHandler.asyncLoginPostDataString(
//                Constants.URL_MEMBER_WX_PAYMENT, params, myApplication,
//                new Callback() {
//                    @Override
//                    public void dataLoaded(ResponseData data) {
//                        String json = data.getJson();
//                        if (data.getCode() == HttpStatus.SC_OK) {
//                            try {
//                                JSONObject jsonObject = new JSONObject(json);
//                                String appid = jsonObject.getString("appid");// 微信开放平台appid
//                                String noncestr = jsonObject
//                                        .getString("noncestr");// 随机字符串
//                                String packageStr = jsonObject
//                                        .getString("package");// 支付内容
//                                String partnerid = jsonObject
//                                        .getString("partnerid");// 财付通id
//                                String prepayid = jsonObject
//                                        .getString("prepayid");// 微信预支付编号
//                                String sign = jsonObject.getString("sign");// 签名
//                                String timestamp = jsonObject
//                                        .getString("timestamp");// 时间戳
//
//                                IWXAPI api = WXAPIFactory.createWXAPI(context, appid);
//
//                                PayReq req = new PayReq();
//                                req.appId = appid;
//                                req.partnerId = partnerid;
//                                req.prepayId = prepayid;
//                                req.nonceStr = noncestr;
//                                req.timeStamp = timestamp;
//                                req.packageValue = packageStr;
//                                req.sign = sign;
//                                req.extData = "app data"; // optional
//
//                                Log.d("huting----------", req.toString());
//
//                                Toast.makeText(context, "正常调起支付",
//                                        Toast.LENGTH_SHORT).show();
//                                // 在支付之前，如果应用没有注册到微信，应该先调用IWXMsg.registerApp将应用注册到微信
//                                api.sendReq(req);
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//
//                        } else {
//                            T.showShort(context, "微信资质审核中...接口返回数据->" + data.toString());
//                            try {
//                                JSONObject obj2 = new JSONObject(json);
//                                String error = obj2.getString("error");
//                                if (error != null) {
//                                    Toast.makeText(context, error,
//                                            Toast.LENGTH_SHORT).show();
//                                }
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                });
//    }
//
//
//    /**
//     * 获取支付宝原生支付的参数
//     */
//    public void loadingAlipayNativePaymentData(String pay_sn) {
//        this.pay_sn=pay_sn;
//        HashMap<String, String> params = new HashMap<String, String>();
//        params.put("key", myApplication.getLoginKey());
//        params.put("pay_sn", pay_sn);
//
//        Log.d("huting", Constants.URL_ALIPAY_NATIVE_GOODS);
//        Log.d("huting", myApplication.getLoginKey());
//        Log.d("huting", pay_sn);
//
//        RemoteDataHandler.asyncLoginPostDataString(
//                Constants.URL_ALIPAY_NATIVE_GOODS, params, myApplication,
//                new Callback() {
//                    @Override
//                    public void dataLoaded(ResponseData data) {
//                        String json = data.getJson();
////                        T.showShort(context, "接口返回数据->"+data.toString());
//                        if (data.getCode() == HttpStatus.SC_OK) {
//                            try {
//                                JSONObject jsonObject = new JSONObject(json);
//                                String signStr = jsonObject.optString("signStr");
//
//                                Log.d("huting-----nativePay", signStr);
//                                PayDemoActivity payDemoActivity = new PayDemoActivity(context, signStr);
//                                payDemoActivity.doPay();
//
//
//                            } catch (JSONException e) {
//                                e.printStackTrace();
//                            }
//
//                        } else {
//
//                            T.showShort(context, "支付宝资质审核中..." + "接口返回值->" + data.toString());
////                            try {
////                                JSONObject obj2 = new JSONObject(json);
////                                String error = obj2.getString("error");
////                                if (error != null) {
////                                    Toast.makeText(context, error,
////                                            Toast.LENGTH_SHORT).show();
////                                }
////                            } catch (JSONException e) {
////                                e.printStackTrace();
////                            }
//                        }
//                    }
//                });
//    }


    /**
     * 确认收货、取消订单
     *
     * @param url
     * @param //orderID 订单ID
     */
    public void loadingSaveOrderData(String url, String order_id, View textOrderOperation) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("order_id", order_id);
        Dialog dialog = ProgressDialog.showLoadingProgress(context, "处理中...");
        dialog.show();
        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication,
                data -> {
                    ProgressDialog.dismissDialog(dialog);
                    String json = data.getJson();
                    if (data.getCode() == HttpStatus.SC_OK) {
                        if (json.equals("1")) {
                            // Toast.makeText(context, "",
                            // Toast.LENGTH_SHORT).show();;
                            // 刷新界面
                            textOrderOperation.setVisibility(View.GONE);
                            Intent mIntent = new Intent(
                                    Constants.REFRESHLAYOUT);
                            context.sendBroadcast(mIntent);
                        }

                    } else {
                        try {
                            JSONObject obj2 = new JSONObject(json);
                            String error = obj2.getString("error");
                            if (error != null) {
                                Toast.makeText(context, error,
                                        Toast.LENGTH_SHORT).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    /**
     * 再次确认
     *
     * @param url
     * @param //orderID 订单ID
     */
    public void OkAgain(String url, String order_id) {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("order_id", order_id);
        Handler handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    if (!TextUtils.isEmpty((String) msg.obj)) {
                        try {
                            JSONObject jsonObject = new JSONObject((String) msg.obj);
                            String code = jsonObject.getString("code");
                            String datas = jsonObject.getString("datas");
                            if (code.equals("200")) {
                                if (datas.equals("1")) {
                                    Intent mIntent = new Intent(
                                            Constants.REFRESHLAYOUT);
                                    context.sendBroadcast(mIntent);
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(context, "操作失败", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
                    }
                } else if (msg.what == 2) {
                    Toast.makeText(context, "请求失败", Toast.LENGTH_SHORT).show();
                }
            }
        };
        new Thread(() -> {
            Message message = new Message();
            try {
                String json = HttpHelper.post(url, params);

                message.obj = json;
                message.what = 1;

            } catch (Exception e) {
                e.printStackTrace();
                message.what = 2;
                LogUtils.i(e.toString());
            }
            handler.sendMessage(message);
        }).start();
    }

    class ViewHolder {
        LinearLayout linearLayoutFLag;
        Button buttonFuKuan;
        LinearLayout addViewID;
    }
//
//    /**
//     * 初始化popupwindow
//     */
//    public PopupWindow initPopupWindow(View mPopupWindowView) {
//        PopupWindow popupWindow;
//
//        popupWindow = new PopupWindow(mPopupWindowView, ActionBar.LayoutParams.MATCH_PARENT, ActionBar.LayoutParams.WRAP_CONTENT);
//        popupWindow.setTouchable(true);
//        popupWindow.setOutsideTouchable(true);
//        popupWindow.setBackgroundDrawable(new BitmapDrawable(context.getResources(), (Bitmap) null));
//        popupWindow.update();
//        popupWindow.setAnimationStyle(R.anim.popup_window_enter);
//        popupWindow.setAnimationStyle(R.anim.popup_window_exit);
//        popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
//            @Override
//            public void onDismiss() {
//                backgroundAlpha(1f);
//            }
//        });
//        return popupWindow;
//    }
//
//    /**
//     * 初始化 支付window
//     */
//    public View initPayWindowView(Context context, int size, JSONArray arr) {
//        View mPopupWindowView = LayoutInflater.from(context).inflate(R.layout.es_activity_social_share, null);
//        TextView pay_miss = (TextView) mPopupWindowView.findViewById(R.id.pay_miss);
//        pay_miss.setOnClickListener(new SetOnclickListener(R.id.pay_miss));
//        ShareButton social_share_sb_wechat = (ShareButton) mPopupWindowView.findViewById(R.id.social_sb_wechat);
//        social_share_sb_wechat.setOnClickListener(new SetOnclickListener(R.id.social_sb_wechat));
//        ShareButton social_share_sb_zhifubao = (ShareButton) mPopupWindowView.findViewById(R.id.social_sb_zhifubao);
//        social_share_sb_zhifubao.setOnClickListener(new SetOnclickListener(R.id.social_sb_zhifubao));
//        ShareButton social_sb_wzhifubao = (ShareButton) mPopupWindowView.findViewById(R.id.social_sb_wzhifubao);
//        social_sb_wzhifubao.setOnClickListener(new SetOnclickListener(R.id.social_sb_wzhifubao));
//        try {
//            for (int i = 0; i < size; i++) {
//                String Values = arr.getString(i);
//                HashMap<String, Object> map = new HashMap<String, Object>();
//                if (Values.equals("wxpay")) {
//                    social_share_sb_wechat.setVisibility(View.VISIBLE);
//                } else if (Values.equals("alipay")) {
//                    social_sb_wzhifubao.setVisibility(View.VISIBLE);
//                } else if (Values.equals("alipay_native")) {//TODO Modify 支付宝原生支付
//                    social_share_sb_zhifubao.setVisibility(View.VISIBLE);
//                }
//
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//        return mPopupWindowView;
//    }

//    public class SetOnclickListener implements View.OnClickListener {
//
//        public int id;
//
//        public SetOnclickListener(int clickId) {
//            this.id = clickId;
//        }
//
//        @Override
//        public void onClick(View view) {
//            pop.dismiss();
//            Intent intent;
//            switch (id) {
//                case R.id.social_sb_wechat:// "微信"
////                    loadingWXPaymentData(Qbean.getOrder_sn());
//                    loadingWXPaymentData(groupList2FU.getPay_sn());
//                    break;
//                case R.id.social_sb_zhifubao:// "获取支付宝原生支付的参数"
////                    loadingAlipayNativePaymentData(Qbean.getOrder_sn());
//                    loadingAlipayNativePaymentData(groupList2FU.getPay_sn());
//                    break;
//
//                case R.id.social_sb_wzhifubao:// "web支付宝
////                    intent = new Intent(context, PayMentWebActivity.class);
////                    intent.putExtra("order_sn", Qbean.getOrder_sn());
////                    context.startActivity(intent);
//                    intent = new Intent(context,
//                            PayMentWebActivity.class);
//                    intent.putExtra("pay_sn", groupList2FU.getPay_sn());
//                    context.startActivity(intent);
//                    break;
//            }
//        }
//    }
//
//    /**
//     * 设置添加屏幕的背景透明度
//     *
//     * @param bgAlpha
//     */
//    public void backgroundAlpha(float bgAlpha) {
//        WindowManager.LayoutParams lp = context.getWindow().getAttributes();
//        lp.alpha = bgAlpha; //0.0-1.0
//        context.getWindow().setAttributes(lp);
//    }
}
