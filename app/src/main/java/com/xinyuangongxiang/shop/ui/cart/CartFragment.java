package com.xinyuangongxiang.shop.ui.cart;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.xinyuangongxiang.shop.MainFragmentManager;
import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.bean.GoodsDetailStoreVoucherInfo;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.MyExceptionHandler;
import com.xinyuangongxiang.shop.common.MyShopApplication;
import com.xinyuangongxiang.shop.common.ShopHelper;
import com.xinyuangongxiang.shop.custom.NCDialog;
import com.xinyuangongxiang.shop.custom.NCStoreVoucherPopupWindow;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.ResponseData;
import com.xinyuangongxiang.shop.library.PullToRefreshBase;
import com.xinyuangongxiang.shop.library.PullToRefreshScrollView;
import com.xinyuangongxiang.shop.ncinterface.INCOnDialogConfirm;
import com.xinyuangongxiang.shop.ui.mine.LoginActivity;
import com.xinyuangongxiang.shop.ui.type.BuyStep1Activity;

import org.apache.http.HttpStatus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 购物车Fragment
 */
public class CartFragment extends Fragment {
    private MyShopApplication myApplication;

    private HashMap<String, CartGoodsItem> cartGoods = new HashMap<String, CartGoodsItem>();
    private ArrayList<ImageButton> btnStoreSelectList;
    private PullToRefreshScrollView mPullRefreshScrollView;
    private NCDialog delNCDialog;
    private LinearLayout llNoData;
    private LinearLayout llNoLogin;
    private LinearLayout llCartList;
    private ImageButton btnSelectAll;
    private TextView tvSum;
    private Button btnSubmit;
    private Button goShoppingID;
    private String loginKey;
    private Context context;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_cart, container, false);

        myApplication = (MyShopApplication) getActivity().getApplicationContext();
        MyExceptionHandler.getInstance().setContext(getActivity());

        btnStoreSelectList = new ArrayList<ImageButton>();
        mPullRefreshScrollView = (PullToRefreshScrollView) layout.findViewById(R.id.pull_refresh_scrollview);
        //下拉刷新监听
        mPullRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {
            @Override
            public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                loadCartData();
            }
        });
        goShoppingID = (Button) layout.findViewById(R.id.goShoppingID);
        goShoppingID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), MainFragmentManager.class);
                myApplication.sendBroadcast(new Intent(Constants.SHOW_HOME_URL));
                startActivity(intent);
            }
        });
        llNoData = (LinearLayout) layout.findViewById(R.id.llNoData);
        llNoLogin = (LinearLayout) layout.findViewById(R.id.llNoLogin);
        llCartList = (LinearLayout) layout.findViewById(R.id.llCartList);
        tvSum = (TextView) layout.findViewById(R.id.tvSum);
        btnSelectAll = (ImageButton) layout.findViewById(R.id.btnSelectAll);
        btnSelectAll.setSelected(true);
        btnSelectAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean selectState;
                if (btnSelectAll.isSelected()) {
                    btnSelectAll.setSelected(false);
                    selectState = false;
                } else {
                    btnSelectAll.setSelected(true);
                    selectState = true;
                }
                for (Map.Entry entry : cartGoods.entrySet()) {
                    CartGoodsItem item = (CartGoodsItem) entry.getValue();
                    ImageButton btn = item.getCheckBtn();
                    btn.setSelected(selectState);
                }
                for (ImageButton btn : btnStoreSelectList) {
                    btn.setSelected(selectState);
                }
                setCartTotal();
            }
        });

        //确认购买信息
        btnSubmit = (Button) layout.findViewById(R.id.btnSubmit);
        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String cartIdString = "";
                for (Map.Entry entry : cartGoods.entrySet()) {
                    String cartId = (String) entry.getKey();
                    CartGoodsItem item = (CartGoodsItem) entry.getValue();
                    ImageButton btn = item.getCheckBtn();
                    if (btn.isSelected()) {
                        cartGoods.remove(item);
                        cartIdString += cartId + "|" + item.getGoodsNum() + ",";
                    }
                }

                if (cartIdString.equals("")) {
                    ShopHelper.showMessage(getActivity(), "请选择想要购买的商品");
                } else {
                    Intent intent = new Intent(getActivity(), BuyStep1Activity.class);
                    intent.putExtra("ifcart", 1);
                    intent.putExtra("cart_id", cartIdString.substring(0, cartIdString.length() - 1));
                    startActivity(intent);
                }
            }
        });

        //登陆按钮
        Button btnLogin = (Button) layout.findViewById(R.id.btnLogin);
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), LoginActivity.class);
                startActivity(intent);
            }
        });

        loadCartData();

        return layout;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadCartData();
    }

    private void loadCartData() {
        //检查是否已经登录
        loginKey = myApplication.getLoginKey();
        if (loginKey == null || loginKey.equals("")) {
            llNoData.setVisibility(View.GONE);
            llNoLogin.setVisibility(View.VISIBLE);
            return;
        } else {
            llNoData.setVisibility(View.VISIBLE);
            llNoLogin.setVisibility(View.GONE);
        }

        String url = Constants.URL_CART_LIST;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());

        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                mPullRefreshScrollView.onRefreshComplete();//加载完成下拉控件取消显示
                //其他平台添加购物车时刷新通知显示
                try {
                    Intent intent = new Intent(Constants.SHOW_CART_NUM);
                    context.sendBroadcast(intent);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {

                    try {
                        llCartList.removeAllViews();
                        cartGoods.clear();
                        JSONObject obj = new JSONObject(json);
                        String cartListString = obj.getString("cart_list");
                        if (cartListString != null && !cartListString.equals("") && !cartListString.equals("[]")) {
                            JSONArray cartListObj = new JSONArray(cartListString);
                            for (int i = 0; i < cartListObj.length(); i++) {
                                JSONObject cartItemObj = (JSONObject) cartListObj.get(i);
                                View cartListItemView = getCartListItemView(cartItemObj);
                                llCartList.addView(cartListItemView);
                            }
                            llNoData.setVisibility(View.GONE);
                        } else {
                            llNoData.setVisibility(View.VISIBLE);
                        }

                        tvSum.setText(getString(R.string.text_rmb) + obj.getString("sum"));

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                } else {
                    if (loginKey == null || loginKey.equals("")) {
                        llNoData.setVisibility(View.GONE);
                        llNoLogin.setVisibility(View.VISIBLE);
                        return;
                    } else {
                        llNoData.setVisibility(View.VISIBLE);
                        llNoLogin.setVisibility(View.GONE);
                    }
                    ShopHelper.showApiError(context, json);
                }
            }
        });
    }

    private View getCartListItemView(JSONObject cartItemObj) {
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View cartListItemView = inflater.inflate(R.layout.cart_list_item, null);

        String storeName = cartItemObj.optString("store_name");
        TextView tvStoreName = (TextView) cartListItemView.findViewById(R.id.tvStoreName);
        tvStoreName.setText(storeName);
        final ArrayList<String> storeCardId = new ArrayList<String>();

        //代金券
        String voucherString = cartItemObj.optString("voucher");
        LinearLayout llVoucher = (LinearLayout) cartListItemView.findViewById(R.id.llVoucher);
        if (voucherString.equals("")) {
            llVoucher.setVisibility(View.GONE);
        } else {
            final NCStoreVoucherPopupWindow ncStoreVoucherPopupWindow = new NCStoreVoucherPopupWindow(getActivity());
            ArrayList<GoodsDetailStoreVoucherInfo> voucherList = GoodsDetailStoreVoucherInfo.newInstanceList(voucherString);
            ncStoreVoucherPopupWindow.setVoucherList(voucherList);
            ncStoreVoucherPopupWindow.setStoreName(storeName);
            llVoucher.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    ncStoreVoucherPopupWindow.showPopupWindow();
                }
            });
        }

        //免运费规则
        LinearLayout llFreight = (LinearLayout) cartListItemView.findViewById(R.id.llFreight);
        TextView tvFreight = (TextView) cartListItemView.findViewById(R.id.tvFreight);
        String freightString = cartItemObj.optString("free_freight");
        if (freightString.equals("")) {
            llFreight.setVisibility(View.GONE);
        } else {
            tvFreight.setText(freightString);
        }

        //满送
        LinearLayout llMansong = (LinearLayout) cartListItemView.findViewById(R.id.llMansong);
        String mansongString = cartItemObj.optString("mansong");
        if (mansongString.equals("") || mansongString.equals("[]")) {
            llMansong.setVisibility(View.GONE);
        } else {
            LinearLayout llMansongList = (LinearLayout) cartListItemView.findViewById(R.id.llMansongList);
            try {
                JSONArray mansongArray = new JSONArray(mansongString);
                for (int i = 0; i < mansongArray.length(); i++) {
                    JSONObject mansongObj = (JSONObject) mansongArray.get(i);

                    View mansongView = inflater.inflate(R.layout.cart_list_mansong_item, null);
                    TextView tvMansongDesc = (TextView) mansongView.findViewById(R.id.tvMansongDesc);
                    ImageView ivMansongImage = (ImageView) mansongView.findViewById(R.id.ivMansongImage);

                    tvMansongDesc.setText(mansongObj.optString("desc"));
                    ShopHelper.loadImage(ivMansongImage, mansongObj.optString("url"));

                    llMansongList.addView(mansongView);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        //店铺选择按钮
        final ImageButton btnSelectStore = (ImageButton) cartListItemView.findViewById(R.id.btnSelectStore);

        //商品
        LinearLayout llCartGoodsList = (LinearLayout) cartListItemView.findViewById(R.id.llCartGoodsList);
        String goodsListString = cartItemObj.optString("goods");
        try {
            JSONArray goodsArray = new JSONArray(goodsListString);
            int size = goodsArray.length();
            for (int i = 0; i < size; i++) {
                final JSONObject goodsObj = (JSONObject) goodsArray.get(i);

                final String cartId = goodsObj.getString("cart_id");
                storeCardId.add(cartId);
                final String goodsId = goodsObj.getString("goods_id");
                String goodsPrice = goodsObj.getString("goods_price");
                String goodsNum = goodsObj.getString("goods_num");
                String goodsTotal = goodsObj.getString("goods_total");
                String goodsName = goodsObj.optString("goods_name");
                String goodsSpec = goodsObj.optString("goods_spec");
                String goodsImage = goodsObj.optString("goods_image_url");

                View goodsView = inflater.inflate(R.layout.cart_list_goods_item, null);

                //选择按钮
                ImageButton btnSelect = (ImageButton) goodsView.findViewById(R.id.btnSelect);
                btnSelect.setSelected(true);
                btnSelect.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        CartGoodsItem item = cartGoods.get(cartId);
                        if (item.checkBtn.isSelected()) {
                            item.checkBtn.setSelected(false);
                        } else {
                            item.checkBtn.setSelected(true);
                        }

                        int length = storeCardId.size();
                        int num = 0;
                        for (String cartId : storeCardId) {
                            CartGoodsItem item1 = cartGoods.get(cartId);
                            if (item1.getCheckBtn().isSelected()) {
                                num++;
                            }
                        }
                        if (length == num) {
                            btnSelectStore.setSelected(true);
                        } else {
                            btnSelectStore.setSelected(false);
                        }

                        setCartTotal();
                    }
                });

                //商品信息
                ImageView ivGoodsImage = (ImageView) goodsView.findViewById(R.id.ivGoodsImage);
                ivGoodsImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShopHelper.showGoods(getActivity(), goodsId);
                    }
                });
                TextView tvGoodsName = (TextView) goodsView.findViewById(R.id.tvGoodsName);
                tvGoodsName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ShopHelper.showGoods(getActivity(), goodsId);
                    }
                });
                TextView tvGoodsSpec = (TextView) goodsView.findViewById(R.id.tvGoodsSpec);
                TextView tvGoodsPrice = (TextView) goodsView.findViewById(R.id.tvGoodsPrice);
                TextView tvLine = (TextView) goodsView.findViewById(R.id.tvLine);
                ShopHelper.loadImage(ivGoodsImage, goodsImage);
                tvGoodsName.setText(goodsName);
                tvGoodsSpec.setText(goodsSpec);
                tvGoodsPrice.setText(context.getResources().getString(R.string.text_rmb) + goodsPrice);
                if ((size - 1) <= i) {
                    tvLine.setVisibility(View.GONE);
                }

                //删除
                ImageButton btnDel = (ImageButton) goodsView.findViewById(R.id.btnDel);
                btnDel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        delCartGoods(cartId);
                    }
                });

                //数量
                final TextView tvGoodsNum = (TextView) goodsView.findViewById(R.id.tvAppCommonCount);
                Button btnMinus = (Button) goodsView.findViewById(R.id.btnAppCommonMinus);
                Button btnAdd = (Button) goodsView.findViewById(R.id.btnAppCommonAdd);
                tvGoodsNum.setText(goodsNum);
                btnMinus.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        changeCartGoodsQuantity(cartId, tvGoodsNum, "minus");
                    }
                });
                btnAdd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        changeCartGoodsQuantity(cartId, tvGoodsNum, "add");
                    }
                });

                llCartGoodsList.addView(goodsView);

                //赠品
                LinearLayout llGift = (LinearLayout) goodsView.findViewById(R.id.llGift);
                LinearLayout llGiftList = (LinearLayout) goodsView.findViewById(R.id.llGiftList);
                String giftListString = goodsObj.optString("gift_list");
                if (giftListString.equals("") || giftListString.equals("[]")) {
                    llGift.setVisibility(View.GONE);
                } else {
                    JSONArray giftArray = new JSONArray(giftListString);
                    for (int j = 0; j < giftArray.length(); j++) {
                        View giftView = inflater.inflate(R.layout.cart_list_gift_item, null);
                        TextView tvGiftInfo = (TextView) giftView.findViewById(R.id.tvGiftInfo);
                        JSONObject giftObj = (JSONObject) giftArray.get(j);
                        tvGiftInfo.setText(giftObj.optString("gift_goodsname") + "x" + giftObj.optString("gift_amount"));
                        llGiftList.addView(giftView);
                    }
                }

                //添加到商品状态集合
                cartGoods.put(cartId, new CartGoodsItem(goodsPrice, goodsNum, goodsTotal, btnSelect));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }


        btnStoreSelectList.add(btnSelectStore);
        btnSelectStore.setSelected(true);
        btnSelectStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                boolean selectState = false;
                if (btnSelectStore.isSelected()) {
                    btnSelectStore.setSelected(false);
                    selectState = false;
                } else {
                    btnSelectStore.setSelected(true);
                    selectState = true;
                }

                for (String cartId : storeCardId) {
                    CartGoodsItem item = cartGoods.get(cartId);
                    item.checkBtn.setSelected(selectState);
                }
                setCartTotal();
            }
        });

        return cartListItemView;
    }


    /**
     * 删除购物商品
     *
     * @param cartId
     */
    private void delCartGoods(final String cartId) {
        delNCDialog = new NCDialog(getActivity());
        delNCDialog.setText1("确认删除?");
        delNCDialog.setOnDialogConfirm(new INCOnDialogConfirm() {
            @Override
            public void onDialogConfirm() {
                String url = Constants.URL_CART_DETELE;
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("key", myApplication.getLoginKey());
                params.put("cart_id", cartId);

                RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new RemoteDataHandler.Callback() {
                    @Override
                    public void dataLoaded(ResponseData data) {
                        String json = data.getJson();
                        if (data.getCode() == HttpStatus.SC_OK) {
                            loadCartData();
                            Intent intent = new Intent(Constants.SHOW_CART_NUM);
                            context.sendBroadcast(intent);
                        } else {
                            ShopHelper.showApiError(getActivity(), json);
                        }
                    }
                });
            }
        });
        delNCDialog.showPopupWindow();
    }

    /**
     * 购物车商品数量调整
     */
    private void changeCartGoodsQuantity(final String cartId, final TextView tvNum, String type) {
        int newNum = Integer.valueOf(tvNum.getText().toString());
        if (type.equals("add")) {
            newNum = newNum + 1;
        } else {
            newNum = newNum - 1;
        }

        if (newNum <= 0) {
            return;
        }

        final String quantity = String.valueOf(newNum);

        String url = Constants.URL_CART_EDIT_QUANTITY;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("cart_id", cartId);
        params.put("quantity", quantity);

        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject obj = new JSONObject(json);
                        String goodsPrice = obj.optString("goods_price");
                        String goodsNum = obj.optString("quantity");
                        String goodsTotal = obj.optString("total_price");

                        CartGoodsItem item = cartGoods.get(cartId);
                        item.setGoodsNum(goodsNum);
                        item.setGoodsPrice(goodsPrice);
                        item.setGoodsTotal(goodsTotal);

                        tvNum.setText(quantity);

                        setCartTotal();

                        Intent intent = new Intent(Constants.SHOW_CART_NUM);
                        context.sendBroadcast(intent);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(getActivity(), json);
                }
            }
        });
    }

    /**
     * 计算购物车总价
     */
    private void setCartTotal() {
        double total = 0.0;
        for (Map.Entry entry : cartGoods.entrySet()) {
            CartGoodsItem item = (CartGoodsItem) entry.getValue();
            ImageButton btn = item.getCheckBtn();
            if (btn.isSelected()) {
                total += Double.valueOf(item.getGoodsTotal());
            }
        }
        tvSum.setText(context.getResources().getString(R.string.text_rmb) + ShopHelper.getPriceString(total));
    }

    class CartGoodsItem {
        private String goodsPrice;
        private String goodsNum;
        private String goodsTotal;
        private ImageButton checkBtn;

        public CartGoodsItem(String goodsPrice, String goodsNum, String goodsTotal, ImageButton checkBtn) {
            this.goodsPrice = goodsPrice;
            this.goodsNum = goodsNum;
            this.goodsTotal = goodsTotal;
            this.checkBtn = checkBtn;
        }

        public String getGoodsPrice() {
            return goodsPrice;
        }

        public void setGoodsPrice(String goodsPrice) {
            this.goodsPrice = goodsPrice;
        }

        public String getGoodsNum() {
            return goodsNum;
        }

        public void setGoodsNum(String goodsNum) {
            this.goodsNum = goodsNum;
        }

        public String getGoodsTotal() {
            return goodsTotal;
        }

        public void setGoodsTotal(String goodsTotal) {
            this.goodsTotal = goodsTotal;
        }

        public ImageButton getCheckBtn() {
            return checkBtn;
        }

        public void setCheckBtn(ImageButton checkBtn) {
            this.checkBtn = checkBtn;
        }
    }
}
