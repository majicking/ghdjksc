package com.guohanhealth.shop.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.bean.NewsListBean;
import com.guohanhealth.shop.common.LoadImage;
import com.guohanhealth.shop.lib.tab.OnItemClickListener;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by snm on 2016/8/31.
 */
public class DingboWenziAdapter extends RecyclerView.Adapter<DingboWenziAdapter.MyViewHolder> {

    private OnItemClickListener onItemClickListener;
    private Context mContext;
    private LayoutInflater inflater;
    List<NewsListBean.RecommendGoodsBean> mList = new ArrayList<NewsListBean.RecommendGoodsBean>();

    public DingboWenziAdapter(Context context){
        this.mContext = context;
        inflater = LayoutInflater.from(mContext);
    }


    public void setmList(List<NewsListBean.RecommendGoodsBean> list) {
        mList.clear();
        this.mList = list;
        notifyDataSetChanged();
    }
    public void addDatas(List<NewsListBean.RecommendGoodsBean> mList) {
        mList.addAll(mList);
        notifyDataSetChanged();
    }


    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout =inflater.inflate(R.layout.fragment_fenxiao_listview_item_dianbo_wenzi_item, parent, false);
        return new MyViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {

        holder.good_name.setText(mList.get(pos).getGoods_name());
        holder.good_price.setText("￥" + mList.get(pos).getGoods_price());
        LoadImage.loadImg(mContext,holder.good_img,mList.get(pos).getGoods_image());
        if(onItemClickListener != null){
            holder.good_img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    onItemClickListener.onClick(pos);
                }
            });
        }

    }

    @Override
    public int getItemCount() {
        return mList == null ?0 : mList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        TextView good_name,good_price;
        ImageView good_img;
        public MyViewHolder(View view) {
            super(view);
            good_price = (TextView)view.findViewById(R.id.good_price);
            good_name = (TextView)view.findViewById(R.id.good_name);
            good_img = (ImageView)view.findViewById(R.id.good_img);
        }

    }
}
