package com.xinyuangongxiang.shop.ui.cart;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.adapter.CartListViewAdapter;
import com.xinyuangongxiang.shop.bean.CartGood;
import com.xinyuangongxiang.shop.bean.CartList;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.MyExceptionHandler;
import com.xinyuangongxiang.shop.common.MyShopApplication;
import com.xinyuangongxiang.shop.custom.XListView;
import com.xinyuangongxiang.shop.custom.XListView.IXListViewListener;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.RemoteDataHandler.Callback;
import com.xinyuangongxiang.shop.http.ResponseData;
import com.xinyuangongxiang.shop.ui.mine.LoginActivity;
import com.xinyuangongxiang.shop.ui.type.BuyStep1Activity;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 购物车界面
 *
 * @author KingKong-HE
 * @Time 2015-1-12
 * @Email KingKong@QQ.COM
 */
public class CartFragmentOld extends Fragment implements IXListViewListener, OnClickListener {

    private MyShopApplication myApplication;

    public ArrayList<CartList> cartList;

    private CartListViewAdapter cartListViewAdapter;

    private XListView listViewID;

    private Handler mXLHandler;

    public CheckBox allCheackCartID;

    public boolean upflag = true; //记录选中后刷新状态

    public HashMap<String, CartGood> cartFlag = new HashMap<String, CartGood>();//标识购物车选择商品状态

    public double sumPrice = 0.00; //记录价格

    public TextView allPriceID;

    private LinearLayout cartYesDataID, cartNoDataID, cartNoLogin;

    private String ifcart = "1";//购物车购买标志 1购物车 0不是

    public int selectNum = 0;
    ;//选中个数

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View viewLayout = inflater.inflate(R.layout.main_cart_view, container, false);

        myApplication = (MyShopApplication) getActivity().getApplicationContext();
        MyExceptionHandler.getInstance().setContext(getActivity());
        initViewID(viewLayout);//初始化控件ID

        return viewLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadingCartListData();
    }

    /**
     * 初始化控件ID
     */
    public void initViewID(View view) {

        listViewID = (XListView) view.findViewById(R.id.listViewID);

        allCheackCartID = (CheckBox) view.findViewById(R.id.allCheackCartID);

        allPriceID = (TextView) view.findViewById(R.id.allPriceID);

        cartYesDataID = (LinearLayout) view.findViewById(R.id.cartYesDataID);

        cartNoDataID = (LinearLayout) view.findViewById(R.id.cartNoDataID);

        cartNoLogin = (LinearLayout) view.findViewById(R.id.cartNoLogin);

        cartListViewAdapter = new CartListViewAdapter(getActivity(), CartFragmentOld.this);

        Button settlementID = (Button) view.findViewById(R.id.settlementID);

        Button goShoppingID = (Button) view.findViewById(R.id.goShoppingID);

        Button btnLogin = (Button) view.findViewById(R.id.btnLogin);

        cartList = new ArrayList<CartList>();

        listViewID.setAdapter(cartListViewAdapter);

        listViewID.setPullLoadEnable(false);

        listViewID.setXListViewListener(this);

        settlementID.setOnClickListener(this);

        goShoppingID.setOnClickListener(this);

        btnLogin.setOnClickListener(this);

        mXLHandler = new Handler();

        loadingCartListData();

        allCheackCartID.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (upflag) {

                    sumPrice = 0.00;

                    for (int i = 0; i < cartList.size(); i++) {
                        CartList bean = cartList.get(i);

                        double goodsPrice = Double.parseDouble(bean.getGoods_price() == null ? "0.00" : bean.getGoods_price());

                        int goodsNum = Integer.parseInt(bean.getGoods_num() == null ? "0" : bean.getGoods_num());

                        double goodsAllPrice = goodsPrice * goodsNum;

                        String goodsID = bean.getCart_id();

                        if (isChecked) { //全选
                            sumPrice += goodsAllPrice;
                            selectNum = cartList.size();
                            cartFlag.put(bean.getCart_id(), new CartGood(goodsPrice, goodsAllPrice, goodsNum, goodsID, true));
                        } else {
                            selectNum = 0;
                            cartFlag.put(bean.getCart_id(), new CartGood(goodsPrice, goodsAllPrice, goodsNum, goodsID, false));
                        }
                    }
                    cartListViewAdapter.notifyDataSetChanged();
                }
            }
        });

    }

    /**
     * 初始化加载购物车数据
     */
    public void loadingCartListData() {
        //检查是否已经登录
        String loginKey = myApplication.getLoginKey();
        if (loginKey == null || loginKey.equals("")) {
            cartYesDataID.setVisibility(View.GONE);
            cartNoDataID.setVisibility(View.GONE);
            cartNoLogin.setVisibility(View.VISIBLE);
            return;
        } else {
            cartNoLogin.setVisibility(View.GONE);
        }

        String url = Constants.URL_CART_LIST;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        String updataTime = sdf.format(new Date(System.currentTimeMillis()));
        listViewID.setRefreshTime(updataTime);

        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {

                listViewID.stopRefresh();//加载完成下拉控件取消显示

                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {

                    try {
                        JSONObject obj = new JSONObject(json);
                        String objJson = obj.getString("cart_list");
                        if (objJson != null && !objJson.equals("") && !objJson.equals("[]")) {
                            cartYesDataID.setVisibility(View.VISIBLE);
                            cartNoDataID.setVisibility(View.GONE);
                        } else {
                            cartYesDataID.setVisibility(View.GONE);
                            cartNoDataID.setVisibility(View.VISIBLE);
                        }

                        cartList.clear();
//							upflag = true;
//							cartIDList.clear();
//							sumAdllPrice = 0.00;
//							sumPrice = 0.00;

                        cartList = CartList.newInstanceList(objJson);
//							for(int i = 0 ; i< cartList.size() ;i ++){
//								CartList bean = cartList.get(i);
//								cartListViewAdapter.cartFlag.put(bean.getCart_id()+"|"+bean.getGoods_num(), false);
//								sumAdllPrice += Double.parseDouble(bean.getGoods_price()  == null ? "0.00" : bean.getGoods_price()) * Integer.parseInt(bean.getGoods_num() == null ? "0" : bean.getGoods_num());
//							} 

//							allCheackCartID.setChecked(false);

                        cartListViewAdapter.setCartLists(cartList);
                        cartListViewAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    try {
                        JSONObject obj2 = new JSONObject(json);
                        String error = obj2.getString("error");
                        if (error != null) {
                            Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    cartYesDataID.setVisibility(View.GONE);
                    cartNoDataID.setVisibility(View.VISIBLE);
//					Toast.makeText(CartActivity.this, getString(R.string.datas_loading_fail_prompt), Toast.LENGTH_SHORT).show();;
                }
            }
        });
    }

    /**
     * 编辑购物车数量
     *
     * @param //key      用户标识
     * @param cart_id  购物车ID
     * @param quantity 商品数量
     */
    public void cartEditQuantity(final String cart_id, String quantity, final double oldPrice) {
        String url = Constants.URL_CART_EDIT_QUANTITY;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("cart_id", cart_id);
        params.put("quantity", quantity);
        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject obj = new JSONObject(json);
                        String quantity = obj.getString("quantity");
                        String goods_price = obj.getString("goods_price");
                        String total_price = obj.getString("total_price");

                        double dtotal_price = Double.parseDouble(total_price == null ? "0.00" : total_price);

                        CartGood cartGoodsBean = cartFlag.get(cart_id);
                        if (cartGoodsBean != null && cartGoodsBean.isFlag()) {
                            sumPrice = sumPrice - oldPrice + dtotal_price;
                        }

                        loadingCartListData();

                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }


                } else {
                    try {
                        JSONObject obj2 = new JSONObject(json);
                        String error = obj2.getString("error");
                        if (error != null) {
                            Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    /**
     * 删除购物车商品
     */
    public void cartDetele(final String cart_id) {
        String url = Constants.URL_CART_DETELE;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("cart_id", cart_id);
        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    if (json.equals("1")) {
                        sumPrice = 0.00;
                        cartFlag.remove(cart_id);

                        Iterator it = cartFlag.keySet().iterator();
                        while (it.hasNext()) {
                            String cartID = it.next().toString();
                            CartGood bean = cartFlag.get(cartID);

                            if (bean != null && bean.isFlag()) {
                                sumPrice += bean.getGoodsAllPrice();
                            }
                        }

                        if (selectNum < cartList.size()) {
                            upflag = false;
                            allCheackCartID.setChecked(false);
                            upflag = true;
                        } else if (selectNum == cartList.size()) {
                            upflag = true;
                            allCheackCartID.setChecked(true);
                        }

                        loadingCartListData();
                    }

                } else {
                    try {
                        JSONObject obj2 = new JSONObject(json);
                        String error = obj2.getString("error");
                        if (error != null) {
                            Toast.makeText(getActivity(), error, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    @Override
    public void onRefresh() {
        mXLHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                loadingCartListData();//初始化加载购物车数据
            }
        }, 1000);
    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.settlementID:

                int count = 0;
                String cart_id = "";
                Iterator it = cartFlag.keySet().iterator();
                while (it.hasNext()) {
                    String cartID = it.next().toString();
                    CartGood bean = cartFlag.get(cartID);
                    if (bean != null && bean.isFlag()) {
                        count++;
                        cart_id += "," + bean.getGoodsID() + "|" + bean.getGoodsNum();
                    }
                }

                cart_id = cart_id.replaceFirst(",", "");

                if (count <= 0) {
                    Toast.makeText(getActivity(), "您还没有选择购买的商品", Toast.LENGTH_SHORT).show();
                    return;
                }

                Intent intent = new Intent(getActivity(), BuyStep1Activity.class);
                intent.putExtra("ifcart", ifcart);
                intent.putExtra("cart_id", cart_id);
                startActivity(intent);

                break;

            case R.id.goShoppingID:
                getActivity().sendBroadcast(new Intent(Constants.SHOW_HOME_URL));
                break;
            case R.id.btnLogin:
                Intent intentLogin = new Intent(getActivity(), LoginActivity.class);
                startActivity(intentLogin);
            default:
                break;
        }
    }


}
