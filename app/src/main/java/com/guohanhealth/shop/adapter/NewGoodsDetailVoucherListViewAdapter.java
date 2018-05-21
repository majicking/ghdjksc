package com.guohanhealth.shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.bean.StoreVoucher;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.ResponseData;

import org.apache.http.HttpStatus;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 商品详细代金券列表适配器
 *
 * @author dqw
 * @date 2015/9/7
 */
public class NewGoodsDetailVoucherListViewAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<StoreVoucher> voucherLists;
    private MyShopApplication myApplication;

    public NewGoodsDetailVoucherListViewAdapter(Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        myApplication = (MyShopApplication) context.getApplicationContext();
    }

    @Override
    public int getCount() {
        return voucherLists == null ? 0 : voucherLists.size();
    }

    @Override
    public Object getItem(int position) {
        return voucherLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public ArrayList<StoreVoucher> getVoucherLists() {
        return voucherLists;
    }

    public void setVoucherLists(ArrayList<StoreVoucher> voucherLists) {
        this.voucherLists = voucherLists;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.listview_goods_detail_voucher_list_item, parent, false);
            holder = new ViewHolder();
            holder.tvPrice = (TextView) convertView.findViewById(R.id.tvPrice);
            holder.tvLimit = (TextView) convertView.findViewById(R.id.tvLimit);
            holder.tvEndDate = (TextView) convertView.findViewById(R.id.tvEndDate);
            holder.btnGetVoucher = (Button) convertView.findViewById(R.id.btnGetVoucher);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        StoreVoucher bean = voucherLists.get(position);
        final String voucherId = bean.getVoucher_t_id();
        holder.tvPrice.setText(bean.getVoucher_t_price());
        holder.tvLimit.setText("需消费" + bean.getVoucher_t_limit() + "使用");
        holder.tvEndDate.setText("至" + bean.getVoucher_t_end_date_text() + "前使用");

        holder.btnGetVoucher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HashMap<String, String> params = new HashMap<String, String>();
                params.put("key", myApplication.getLoginKey());
                params.put("tid", voucherId);
                RemoteDataHandler.asyncLoginPostDataString(Constants.URL_MEMBER_VOUCHER_FREE_ADD, params, myApplication, new RemoteDataHandler.Callback() {
                    @Override
                    public void dataLoaded(ResponseData data) {
                        String json = data.getJson();
                        if (data.getCode() == HttpStatus.SC_OK) {
                            ShopHelper.showMessage(context, "领取成功");
                        } else {
                            ShopHelper.showApiError(context, json);
                        }
                    }
                });
            }
        });

        return convertView;
    }

    class ViewHolder {
        TextView tvPrice, tvLimit, tvEndDate;
        Button btnGetVoucher;
    }
}
