package com.xinyuangongxiang.shop.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.bean.goodsCommonList;
import com.xinyuangongxiang.shop.ui.type.GoodsDetailsActivity;

import java.util.ArrayList;

/**
 * Created by snm on 2016/9/1.
 */
public class ZhixunContentAdapter extends BaseAdapter{

    private Context mContext;
    private ArrayList<goodsCommonList> mList;
    public ZhixunContentAdapter(Context context,ArrayList<goodsCommonList> commonList){
        this.mContext = context;
        this.mList = commonList;
    }

    @Override
    public int getCount() {
        return mList == null ? 0 : mList.size();
    }

    @Override
    public Object getItem(int position) {
        return mList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        MyViewHolder holder;
        if(convertView == null) {
            convertView = LayoutInflater.from(mContext). inflate(R.layout.activity_zhixuncontent_goods_item, parent, false);
            holder = new MyViewHolder();

            holder.goods_item_image = (ImageView)convertView.findViewById(R.id.goods_item_image);
            holder.tv_goodsname = (TextView) convertView.findViewById(R.id.tv_goodsname);
            holder.tv_goodsprice = (TextView) convertView.findViewById(R.id.tv_goodsprice);

            convertView.setTag(holder);
        } else {
            holder = (MyViewHolder) convertView.getTag();
        }
        Glide.with(mContext).load(mList.get(position).getGoods_image_url()).into(holder.goods_item_image);
        holder.tv_goodsname.setText(mList.get(position).getGoods_name());
        holder.tv_goodsprice.setText("￥" + mList.get(position).getGoods_price());

        holder.goods_item_image.setOnClickListener(new setOnclick(mList.get(position).getGoods_id()));
        holder.tv_goodsname.setOnClickListener(new setOnclick(mList.get(position).getGoods_id()));
        holder.tv_goodsprice.setOnClickListener(new setOnclick(mList.get(position).getGoods_id()));

        return convertView;
    }

    public class MyViewHolder{

        ImageView goods_item_image;
        TextView tv_goodsname,tv_goodsprice;

    }

    public class setOnclick implements View.OnClickListener{

        private String goods_commonid;
        public setOnclick(String commonid){
            this.goods_commonid = commonid;
        }

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(mContext, GoodsDetailsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra("goods_id", goods_commonid);
            mContext.startActivity(intent);
        }
    }

}
