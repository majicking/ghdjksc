package com.guohanhealth.shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.bean.CateListBean;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by snm on 2016/9/27.
 */
public class BeginLiveAdapter extends BaseAdapter {
    private LayoutInflater inflater;
    private Context mContext;
    List<CateListBean> cateList = new ArrayList<CateListBean>();
    public BeginLiveAdapter(Context context){
        this.mContext = context;
        this.inflater = LayoutInflater.from(context);
    }

    public void setCateList(List<CateListBean> cateList) {
        this.cateList = cateList;
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return cateList == null?0:cateList.size();
    }

    @Override
    public Object getItem(int position) {
        return cateList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        ViewHolder holder;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.item_uploadphoto_select_name, null);
            holder = new ViewHolder();
            holder.tvTitle = (TextView) convertView.findViewById(R.id.tvTitle);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        CateListBean contractInfo = cateList.get(position);
        holder.tvTitle.setText(contractInfo.getCate_name());

        return convertView;
    }

    class ViewHolder {
        TextView tvTitle;
    }
}
