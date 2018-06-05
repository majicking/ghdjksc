package com.guohanhealth.shop.adapter;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.TextView;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.bean.GoodsList;
import com.guohanhealth.shop.common.AnimateFirstDisplayListener;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.LoadImage;
import com.guohanhealth.shop.common.LogHelper;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.common.StringUtils;
import com.guohanhealth.shop.common.SystemHelper;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.ResponseData;
import com.guohanhealth.shop.ui.store.newStoreInFoActivity;
import com.guohanhealth.shop.ui.type.GoodsDetailsActivity;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 商品列表适配器
 *
 * @author KingKong·HE
 * @Time 2014-1-6 下午12:06:09
 * @E-mail hjgang@bizpoer.com
 */
public class GoodsListViewAdapter extends BaseAdapter {
    private Context context;

    private LayoutInflater inflater;

    private String listType;

    private ArrayList<GoodsList> goodsLists;

    protected ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions options = SystemHelper.getDisplayImageOptions();
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    public GoodsListViewAdapter(Context context, String listType) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.listType = listType;
    }

    @Override
    public int getCount() {
        if (goodsLists == null) {
//            LogHelper.d("huting--count:", "0");
        } else {
//            LogHelper.d("huting--count:", String.valueOf(goodsLists.size()));
        }
        return goodsLists == null ? 0 : goodsLists.size();
    }

    @Override
    public Object getItem(int position) {
        return goodsLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    public ArrayList<GoodsList> getGoodsLists() {
        return goodsLists;
    }

    public void setGoodsLists(ArrayList<GoodsList> goodsLists) {
        this.goodsLists = goodsLists;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        GoodsList bean = goodsLists.get(position);
        final String goodsId = bean.getGoods_id();
        final String storeId = bean.getStore_id();

        if (null == convertView) {
            if (listType.equals("grid")) {
                convertView = inflater.inflate(R.layout.gridview_goods_item, null);
//                LogHelper.d("huting+++",convertView.toString());
            } else {
                convertView = inflater.inflate(R.layout.listivew_goods_item, null);
//                LogHelper.d("huting====",convertView.toString());
            }

            holder = new ViewHolder();
            holder.imageGoodsPic = (ImageView) convertView.findViewById(R.id.imageGoodsPic);
            holder.llGoodsItem = (LinearLayout) convertView.findViewById(R.id.llGoodsItem);
            holder.textGoodsName = (TextView) convertView.findViewById(R.id.textGoodsName);
            holder.textGoodsJingle = (TextView) convertView.findViewById(R.id.textGoodsJingle);
            holder.textGoodsPrice = (TextView) convertView.findViewById(R.id.textGoodsPrice);
            holder.textGoodsType = (TextView) convertView.findViewById(R.id.textGoodsType);
            holder.textZengPin = (TextView) convertView.findViewById(R.id.textZengPin);
            holder.tvGoodsSalenum = (TextView) convertView.findViewById(R.id.tvGoodsSalenum);
            holder.tvOwnShop = (TextView) convertView.findViewById(R.id.tvOwnShop);
            holder.btnStoreName = (Button) convertView.findViewById(R.id.btnStoreName);
            holder.llStoreInfo = (LinearLayout) convertView.findViewById(R.id.llStoreInfo);
            holder.tvStoreName = (TextView) convertView.findViewById(R.id.tvStoreName);
            holder.llStoreEval = (LinearLayout) convertView.findViewById(R.id.llStoreEval);
            holder.tvStoreDescPoint = (TextView) convertView.findViewById(R.id.tvStoreDescPoint);
            holder.tvStoreDescText = (TextView) convertView.findViewById(R.id.tvStoreDescText);
            holder.tvStoreServicePoint = (TextView) convertView.findViewById(R.id.tvStoreServicePoint);
            holder.tvStoreServiceText = (TextView) convertView.findViewById(R.id.tvStoreServiceText);
            holder.tvStoreDeliveryPoint = (TextView) convertView.findViewById(R.id.tvStoreDeliveryPoint);
            holder.tvStoreDeliveryText = (TextView) convertView.findViewById(R.id.tvStoreDeliveryText);
            holder.ratingBar = (RatingBar) convertView.findViewById(R.id.ratingbar);
            holder.ratinglayout = (LinearLayout) convertView.findViewById(R.id.ratinglayout);
            holder.imgrating = (ImageView) convertView.findViewById(R.id.imgrating);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.llStoreInfo.setVisibility(View.GONE);
        LogHelper.e("position", String.valueOf(position));
//        imageLoader.displayImage(bean.getGoods_image_url(), holder.imageGoodsPic, options, animateFirstListener);
        LoadImage.loadImg(context, holder.imageGoodsPic, bean.getGoods_image_url());

        holder.textGoodsName.setText((StringUtils.isEmpty(bean.getGoods_name()) ? "" : bean.getGoods_name()));
        holder.textGoodsJingle.setText(StringUtils.isEmpty(bean.getGoods_jingle()) ? "" : bean.getGoods_jingle());
        holder.textGoodsPrice.setText("￥" + (StringUtils.isEmpty(bean.getGoods_price()) ? "0.00" : bean.getGoods_price()));
        holder.tvGoodsSalenum.setText("销量:" + (StringUtils.isEmpty(bean.getGoods_salenum()) ? "0" : bean.getGoods_salenum()));

        if (Boolean.valueOf(bean.getSole_flag())) {
            holder.textGoodsType.setText("");
            holder.textGoodsType.setVisibility(View.VISIBLE);
            holder.textGoodsType.setBackgroundResource(R.drawable.nc_icon_mobile_price);
        } else if (Boolean.valueOf(bean.getGroup_flag())) {
            holder.textGoodsType.setText(context.getString(R.string.text_groupbuy));
            holder.textGoodsType.setVisibility(View.VISIBLE);
            holder.textGoodsType.setBackgroundResource(R.color.text_tuangou);
        } else if (Boolean.valueOf(bean.getXianshi_flag())) {
            holder.textGoodsType.setText(context.getString(R.string.text_xianshi));
            holder.textGoodsType.setVisibility(View.VISIBLE);
            holder.textGoodsType.setBackgroundResource(R.color.text_xianshi);
        } else if (bean.getIs_appoint().equals("1")) {
            holder.textGoodsType.setText(context.getString(R.string.text_appoint));
            holder.textGoodsType.setVisibility(View.VISIBLE);
            holder.textGoodsType.setBackgroundResource(R.color.text_yuyue);
        } else if (bean.getIs_fcode().equals("1")) {
            holder.textGoodsType.setText(context.getString(R.string.text_fcode));
            holder.textGoodsType.setVisibility(View.VISIBLE);
            holder.textGoodsType.setBackgroundResource(R.color.text_Fcode);
        } else if (bean.getIs_presell().equals("1")) {
            holder.textGoodsType.setText(context.getString(R.string.text_presell));
            holder.textGoodsType.setVisibility(View.VISIBLE);
            holder.textGoodsType.setBackgroundResource(R.color.text_yushou);
        } else if (bean.getIs_virtual().equals("1")) {
            holder.textGoodsType.setText(context.getString(R.string.text_virtual));
            holder.textGoodsType.setVisibility(View.VISIBLE);
            holder.textGoodsType.setBackgroundResource(R.color.text_xuni);
        } else {
            holder.textGoodsType.setVisibility(View.GONE);
        }
        if (bean.getHave_gift().equals("1")) {
            holder.textZengPin.setVisibility(View.VISIBLE);
        } else {
            holder.textZengPin.setVisibility(View.GONE);
        }

        if (!TextUtils.isEmpty(bean.getGoods_grade()) && !bean.getGoods_grade().equals("0")) {
            try {
                LoadImage.loadImg(context, holder.imgrating, bean.getGoods_grade());
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            holder.imgrating.setVisibility(View.GONE);
        }

        //店铺信息控制
        if (bean.getIs_own_shop().equals("1")) {
            holder.tvOwnShop.setVisibility(View.VISIBLE);
            holder.btnStoreName.setVisibility(View.GONE);
        } else {
            holder.tvOwnShop.setVisibility(View.GONE);
            holder.btnStoreName.setVisibility(View.VISIBLE);
            holder.btnStoreName.setText(bean.getStore_name());
            holder.btnStoreName.setTag(position);
            holder.btnStoreName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    holder.llStoreInfo.setVisibility(View.VISIBLE);
                    RemoteDataHandler.asyncDataStringGet(Constants.URL_STORE_CREDIT + "&store_id=" + storeId, new RemoteDataHandler.Callback() {
                        @Override
                        public void dataLoaded(ResponseData data) {
//                                    LogHelper.e("wj","点击了" + position);
                            String json = data.getJson();

                            if (data.getCode() == HttpStatus.SC_OK) {
                                try {
                                    JSONObject jsonObj = new JSONObject(json);
                                    String objString = jsonObj.getString("store_credit");
                                    JSONObject obj = new JSONObject(objString);
                                    String desc = obj.getString("store_desccredit");
                                    JSONObject objDesc = new JSONObject(desc);
                                    setStoreCredit(objDesc.getString("credit"), objDesc.getString("text"), holder.tvStoreDescPoint, holder.tvStoreDescText);
                                    String service = obj.getString("store_servicecredit");
                                    JSONObject objService = new JSONObject(service);
                                    setStoreCredit(objService.getString("credit"), objService.getString("text"), holder.tvStoreServicePoint, holder.tvStoreServiceText);
                                    String delivery = obj.getString("store_deliverycredit");
                                    JSONObject objDelivery = new JSONObject(delivery);
                                    setStoreCredit(objDelivery.getString("credit"), objDelivery.getString("text"), holder.tvStoreDeliveryPoint, holder.tvStoreDeliveryText);
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                    holder.llStoreInfo.setVisibility(View.GONE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            } else {
                                ShopHelper.showApiError(context, json);
                                holder.llStoreInfo.setVisibility(View.GONE);
                            }
                        }
                    });
                }
            });
        }

        //跳转到店铺
        holder.tvStoreName.setText(bean.getStore_name());
        holder.tvStoreName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, newStoreInFoActivity.class);
                intent.putExtra("store_id", storeId);
                context.startActivity(intent);
            }
        });

        //关闭店铺信息
        holder.llStoreEval.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.llStoreInfo.setVisibility(View.GONE);
            }
        });

        //点击商品显示商品详细
        holder.llGoodsItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GoodsDetailsActivity.class);
                intent.putExtra("goods_id", goodsId);
                context.startActivity(intent);
            }
        });

        //点击商品图片商品详细
        holder.imageGoodsPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, GoodsDetailsActivity.class);
                intent.putExtra("goods_id", goodsId);
                context.startActivity(intent);
            }
        });

        return convertView;
    }

    private void setStoreCredit(String credit, String type, TextView tvPoint, TextView tvText) {
        tvPoint.setText(credit);
        if (type.equals("low")) {
            tvPoint.setTextColor(context.getResources().getColor(R.color.nc_green));
            tvText.setText("低");
            tvText.setBackgroundColor(context.getResources().getColor(R.color.nc_green));
        }
        if (type.equals("equal")) {
            tvPoint.setTextColor(context.getResources().getColor(R.color.nc_red));
            tvText.setText("平");
            tvText.setBackgroundColor(context.getResources().getColor(R.color.nc_red));
        }
        if (type.equals("high")) {
            tvPoint.setTextColor(context.getResources().getColor(R.color.nc_red));
            tvText.setText("高");
            tvText.setBackgroundColor(context.getResources().getColor(R.color.nc_red));
        }
    }

    class ViewHolder {
        ImageView imageGoodsPic;
        TextView textGoodsName;
        TextView textGoodsJingle;
        TextView textGoodsPrice;
        TextView textGoodsType;
        TextView textZengPin;
        TextView tvGoodsSalenum;
        TextView tvOwnShop;
        Button btnStoreName;
        LinearLayout llGoodsItem;
        LinearLayout llStoreInfo;
        TextView tvStoreName;
        LinearLayout llStoreEval;
        TextView tvStoreDescPoint;
        TextView tvStoreDescText;
        TextView tvStoreServicePoint;
        TextView tvStoreServiceText;
        TextView tvStoreDeliveryPoint;
        TextView tvStoreDeliveryText;
        LinearLayout ratinglayout;
        RatingBar ratingBar;
        ImageView imgrating;
    }
}
