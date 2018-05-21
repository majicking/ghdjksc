package com.guohanhealth.shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.bean.PredepositLogInfo;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 预存款日志适配器
 *
 * dqw
 * 2015/8/25
 */
public class PredepositLogListViewAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<PredepositLogInfo> list;

    public PredepositLogListViewAdapter(Context context) {
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

    public void setList(ArrayList<PredepositLogInfo> list) {
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.listview_predeposit_log_item, parent, false);
            holder = new ViewHolder();
            holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
            holder.tvSn = (TextView) convertView.findViewById(R.id.tvSn);
            holder.tvAvAmount = (TextView) convertView.findViewById(R.id.tvAvAmount);
            holder.tvAddTimeText = (TextView) convertView.findViewById(R.id.tvAddTimeText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PredepositLogInfo info = list.get(position);
        String[] desc = info.getDesc().split(":");
        if (info.getDesc()!=null&&desc!=null&&desc.length>0) {
            holder.tvDesc.setText(desc[0]);
            holder.tvSn.setText(desc[1]);
        }
        float avAmount = Float.valueOf(info.getAvAmount());
        if(avAmount > 0) {
            holder.tvAvAmount.setText("+" + info.getAvAmount());
            holder.tvAvAmount.setTextColor(context.getResources().getColor(R.color.nc_red));
        } else {
            holder.tvAvAmount.setText(info.getAvAmount());
            holder.tvAvAmount.setTextColor(context.getResources().getColor(R.color.nc_green));
        }
        holder.tvAddTimeText.setText(info.getAddTimeText());

        return convertView;
    }

    class ViewHolder {
        TextView tvDesc;
        TextView tvSn;
        TextView tvAvAmount;
        TextView tvAddTimeText;
    }
}
