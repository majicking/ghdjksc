package com.guohanhealth.shop.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.bean.RechargeCardLogInfo;
import com.umeng.debug.log.E;

import java.util.ArrayList;

/**
 * 预存款日志适配器
 * <p/>
 * dqw
 * 2015/8/25
 */
public class RechargeCardLogListViewAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<RechargeCardLogInfo> list;

    public RechargeCardLogListViewAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return list == null ? 0 : list.size();
    }

    @Override
    public Object getItem(int position) {
        return list.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setList(ArrayList<RechargeCardLogInfo> list) {
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.listview_recharge_card_log_item, parent, false);
            holder = new ViewHolder();
            holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
            holder.tvSn = (TextView) convertView.findViewById(R.id.tvSn);
            holder.tvAmount = (TextView) convertView.findViewById(R.id.tvAmount);
            holder.tvAddTimeText = (TextView) convertView.findViewById(R.id.tvAddTimeText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        RechargeCardLogInfo info = list.get(position);
        Log.d("dqw", info.toString());
        String[] desc = info.getDescription().split(" ");
        try {
            holder.tvDesc.setText(desc[0]);
            holder.tvSn.setText(desc[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (info.getType().equals("order_pay") || info.getType().equals("order_freeze")) {

            holder.tvAmount.setText(info.getAvailableAmount());
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.nc_green));
        } else {
            holder.tvAmount.setText("+" + info.getAvailableAmount());
            holder.tvAmount.setTextColor(context.getResources().getColor(R.color.nc_red));

        }
        holder.tvAddTimeText.setText(info.getAddTimeText());

        return convertView;
    }

    class ViewHolder {
        TextView tvDesc;
        TextView tvSn;
        TextView tvAmount;
        TextView tvAddTimeText;
    }
}
