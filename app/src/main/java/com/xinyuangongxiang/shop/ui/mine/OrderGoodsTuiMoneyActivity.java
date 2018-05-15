package com.xinyuangongxiang.shop.ui.mine;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import com.bumptech.glide.Glide;


import com.xinyuangongxiang.shop.BaseActivity;
import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.adapter.ExchangePhotoAdapter;
import com.xinyuangongxiang.shop.bean.GoodsListBean;
import com.xinyuangongxiang.shop.bean.ImageFileBean;
import com.xinyuangongxiang.shop.bean.ReasonBean;
import com.xinyuangongxiang.shop.bean.TuiOrderBean;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.FileUtils;
import com.xinyuangongxiang.shop.common.LogHelper;
import com.xinyuangongxiang.shop.common.MyExceptionHandler;
import com.xinyuangongxiang.shop.common.MyShopApplication;
import com.xinyuangongxiang.shop.common.ShopHelper;
import com.xinyuangongxiang.shop.common.T;
import com.xinyuangongxiang.shop.custom.NCDialog;
import com.xinyuangongxiang.shop.custom.PhotoBottomDialog;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.ResponseData;
import com.xinyuangongxiang.shop.ncinterface.INCOnDialogConfirm;
import com.xinyuangongxiang.shop.ui.type.GoodsDetailsActivity;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class OrderGoodsTuiMoneyActivity extends BaseActivity {
    private String orderId;
    private String orderGoodsId;
    private MyShopApplication myApplication;
    private LayoutInflater inflater;
    private EditText textNum;
    private TextView textOrderStoreName;
    private EditText editSeason;
    private LinearLayout lll;
    private LinearLayout addViewID;
    private Spinner spinner1;
    private String reason_id;
    private GridView gv_photo;
    private PhotoBottomDialog bottomDialog;
    private ExchangePhotoAdapter adapter;
    private List<ImageFileBean> images;
    private NCDialog ncDialog;
    public static final int COMMENT_SUCCESS = 122;
    public static final int SELELCT_FILE_TO_UPLOAD_ITEM = 108;
    private static final int FLAG_CHOOSE_PHONE = 6;
    private Double payPrice;
    private String refund_type="1";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_goods_tui_money);
        setCommonHeader("商品退款");
        inflater = LayoutInflater.from(OrderGoodsTuiMoneyActivity.this);
        orderId=getIntent().getStringExtra("order_id");
        orderGoodsId=getIntent().getStringExtra("order_goods_id");
        myApplication = (MyShopApplication) getApplicationContext();
        MyExceptionHandler.getInstance().setContext(this);
        initViews();
        loadDatas();
    }


    public void initViews(){
        lll = (LinearLayout) findViewById(R.id.lll);
        addViewID = (LinearLayout) lll.findViewById(R.id.addViewID);
        textNum=(EditText)findViewById(R.id.textNum);
        textOrderStoreName=(TextView)findViewById(R.id.textOrderStoreName);
        editSeason=(EditText)findViewById(R.id.editSeason);
        spinner1=(Spinner)findViewById(R.id.spinner1);
        gv_photo = (GridView)findViewById(R.id.gv_photo);
        adapter=new ExchangePhotoAdapter(OrderGoodsTuiMoneyActivity.this,images);
        gv_photo.setAdapter(adapter);
    }

    public void loadDatas(){
        String url= Constants.URL_ORDER_REFUND_SOME+ "&key=" + myApplication.getLoginKey() + "&order_id=" + orderId+"&order_goods_id="+orderGoodsId;
        Log.i("QIN",url.toString());
        RemoteDataHandler.asyncDataStringGet(url, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject obj = new JSONObject(json);

                        final ArrayList<ReasonBean> reasons = new ArrayList<ReasonBean>(com.alibaba.fastjson.JSONObject.parseArray(obj.getString("reason_list"), ReasonBean.class));

                        final ArrayList<String> list = new ArrayList();
                        for (ReasonBean r : reasons) {
                            list.add(r.getReason_info());
                        }
                        ArrayAdapter adapterSp = new ArrayAdapter(OrderGoodsTuiMoneyActivity.this, android.R.layout.simple_spinner_dropdown_item, list);
                        spinner1.setAdapter(adapterSp);
                        spinner1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                            @Override
                            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                String info = list.get(i);
                                for (ReasonBean r : reasons) {
                                    if (r.getReason_info().equals(info)) {
                                        reason_id = r.getReason_id();
                                        Log.i("QIN", reason_id);
                                    } else {
                                        continue;
                                    }
                                }

                            }

                            @Override
                            public void onNothingSelected(AdapterView<?> adapterView) {

                            }
                        });


                        TuiOrderBean order = com.alibaba.fastjson.JSONObject.parseObject(obj.getString("order"), TuiOrderBean.class);

                        final GoodsListBean goods = com.alibaba.fastjson.JSONObject.parseObject(obj.getString("goods"), GoodsListBean.class);


                        textOrderStoreName.setText(order.getStore_name());

                        //显示店铺商品列表
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
                        TextView textGoodsSPec = (TextView) orderGoodsListView.findViewById(R.id.textGoodsSPec);
                        LinearLayout llGift = (LinearLayout) orderGoodsListView.findViewById(R.id.llGift);
                        llGift.setVisibility(View.GONE);
                        textGoodsName.setText(goods.getGoods_name());
                        textGoodsPrice.setText("￥" + goods.getGoods_price());
                        textGoodsNUM.setText("×" + goods.getGoods_num());
                        Glide.with(OrderGoodsTuiMoneyActivity.this).load(goods.getGoods_img_360()).into(imageGoodsPic);
                        if (goods.getGoods_spec() == null) {
                            textGoodsSPec.setVisibility(View.GONE);
                        } else {
                            textGoodsSPec.setText(goods.getGoods_spec());
                        }
                        textGoodsName.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(OrderGoodsTuiMoneyActivity.this, GoodsDetailsActivity.class);
                                i.putExtra("goods_id", goods.getGoods_id());
                                OrderGoodsTuiMoneyActivity.this.startActivity(i);
                            }
                        });


                        payPrice = Double.valueOf(goods.getGoods_pay_price());
                        textNum.setText(goods.getGoods_pay_price());

                        gv_photo.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> adapterView, View view, final int i, long l) {
                                if (images == null || i == images.size()) {
                                    bottomDialog = new PhotoBottomDialog(OrderGoodsTuiMoneyActivity.this);
                                    bottomDialog.show();
                                } else {
                                    ncDialog = new NCDialog(OrderGoodsTuiMoneyActivity.this);
                                    ncDialog.setText1("确认删除该图片?");
                                    ncDialog.setOnDialogConfirm(new INCOnDialogConfirm() {
                                        @Override
                                        public void onDialogConfirm() {
                                            images.remove(i);
                                            adapter.notifyDataSetChanged();
                                        }
                                    });
                                    ncDialog.showPopupWindow();
                                }
                            }
                        });

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(OrderGoodsTuiMoneyActivity.this, json);
                }
            }
        });


    }


    /**
     * 上传图片
     * @param file
     */
    public void uploadImage(File file){
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("key", myApplication.getLoginKey());
        HashMap<String, File> fileMap = new HashMap<String, File>();
        fileMap.put("refund_pic", file);

        RemoteDataHandler.asyncMultipartPostString(Constants.URL_ORDER_EXCHANGE_PHOTO,params,fileMap,new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    ImageFileBean image= com.alibaba.fastjson.JSONObject.parseObject(json,ImageFileBean.class);
                    if (images != null) {
                        images.add(image);
                    } else {
                        images = new ArrayList<ImageFileBean>();
                        images.add(image);
                    }
                    adapter.setData(images);
                    adapter.notifyDataSetChanged();
                }else{
                    T.showShort(OrderGoodsTuiMoneyActivity.this, "文件上传失败");
                }
            }
        });
    }


    //选择照片后的回调方法
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case SELELCT_FILE_TO_UPLOAD_ITEM:
                if (resultCode == RESULT_OK) {
                    Uri uri = data.getData();
                    LogHelper.e("uri", uri.toString());

                    String path = FileUtils.getPath(this, uri);

                    File file = new File(path);

                    if (file.length() < 1024 * 1024) {
                        uploadImage(file);
                    } else {
                        T.showShort(OrderGoodsTuiMoneyActivity.this, "图片文件过大，请上传1M以下的图片");
                    }
                }
                break;
            case FLAG_CHOOSE_PHONE:
                if (resultCode == RESULT_OK) {

                    String imgpath = myApplication.getImgPath();
                    LogHelper.e("imgpath",imgpath);
                    File file = new File(imgpath);
                    if (file.length() < 1024 * 1024) {
                        uploadImage(file);
                    } else {
                        T.showShort(OrderGoodsTuiMoneyActivity.this, "图片文件过大，请上传1M以下的图片");
                    }

                }
        }
    }

    public void btnCommitExchange(View v){
        String url=Constants.URL_ORDER_EXCHANGE_SOME;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("order_id", orderId);
        params.put("order_goods_id",orderGoodsId);
        params.put("refund_amount",textNum.getText().toString());

        params.put("goods_num","");
        params.put("reason_id",reason_id);
        params.put("buyer_message",editSeason.getText().toString());
        params.put("refund_type",refund_type);
        try{
            if(Double.valueOf(textNum.getText().toString())>payPrice){
                T.showShort(OrderGoodsTuiMoneyActivity.this, "退款金额大于可退金额");
                return;
            }

        }catch (Exception e){
            T.showShort(OrderGoodsTuiMoneyActivity.this, "退款金额填写有误");
            return;
        }
        if(images!=null){
            for (int position=0;position<images.size();position++){
                ImageFileBean i=images.get(position);
                params.put("refund_pic["+position+"]",i.getFile_name());
            }
        }

        RemoteDataHandler.asyncLoginPostDataString(url,params,myApplication,new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (json.equals("1")) {
                    setResult(COMMENT_SUCCESS);
                    T.showShort(OrderGoodsTuiMoneyActivity.this, "退款提交成功，请等待审核");
                }else{
                    ShopHelper.showApiError(OrderGoodsTuiMoneyActivity.this, json);
                }
                finish();
            }
        });

    }



}
