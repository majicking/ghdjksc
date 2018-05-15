package com.xinyuangongxiang.shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.bean.RpacketInfo;

import java.util.ArrayList;

/**
 * 收货地址列表适配器
 *
 * @author KingKong·HE
 * @Time 2014-1-6 下午12:06:09
 */
public class RpacketListSpinnerAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<RpacketInfo> rpacketLists;

    public RpacketListSpinnerAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return rpacketLists == null ? 0 : rpacketLists.size();
    }

    @Override
    public Object getItem(int position) {
        return rpacketLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public ArrayList<RpacketInfo> getRpacketLists() {
        return rpacketLists;
    }

    public void setRpacketLists(ArrayList<RpacketInfo> rpacketLists) {
        this.rpacketLists = rpacketLists;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.listview_rpacket_list_item, null);
            holder = new ViewHolder();
            holder.tvRpacket = (TextView) convertView.findViewById(R.id.tvRpacket);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RpacketInfo bean = rpacketLists.get(position);
        holder.tvRpacket.setText(bean.getRpacketDesc());

        return convertView;
    }

    class ViewHolder {
        TextView tvRpacket;
    }
}
