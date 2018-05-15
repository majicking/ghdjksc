package com.xinyuangongxiang.shop.adapter.fenxiao;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bigkoo.convenientbanner.listener.OnItemClickListener;
import com.bumptech.glide.Glide;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.bean.fenxiao.MemberList;
import com.xinyuangongxiang.shop.custom.GlideCircleTransform;

import java.util.ArrayList;

/**
 * Created by snm on 2016/9/28.
 */
public class ZhiBoMaiAdapter extends RecyclerView.Adapter<ZhiBoMaiAdapter.MyViewHolder> {

    private OnItemClickListener onItemClickListener;
    private Context mContext;
    private LayoutInflater inflater;
    ArrayList<MemberList> mList = new ArrayList<MemberList>();

    public ZhiBoMaiAdapter(Context context) {
        this.mContext = context;
        inflater = LayoutInflater.from(mContext);
    }


    public void setmList(ArrayList<MemberList> list) {
        mList.clear();
        this.mList = list;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layout = inflater.inflate(R.layout.activity_zhibo_man_item, parent, false);
        return new MyViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
//            LoadImage.loadImg(mContext,holder.iv_man_head,"");
        MemberList memberList = mList.get(pos);
        Glide.with(mContext).load(memberList.getMember_avatar()).transform(new GlideCircleTransform(mContext)).into(holder.iv_man_head);
    }

    @Override
    public int getItemCount() {
        return mList == null ? 0 : mList.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView iv_man_head;

        public MyViewHolder(View view) {
            super(view);
            iv_man_head = (ImageView) view.findViewById(R.id.iv_man_head);
        }

    }
}