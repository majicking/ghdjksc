package com.xinyuangongxiang.shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.bean.PdcashInfo;

import java.util.ArrayList;

/**
 * 预存款提现适配器
 * <p/>
 * dqw
 * 2015/8/25
 */
public class PdcashListViewAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<PdcashInfo> list;

    public PdcashListViewAdapter(Context context) {
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

    public void setList(ArrayList<PdcashInfo> list) {
        this.list = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.listview_pdcash_item, parent, false);
            holder = new ViewHolder();
            holder.tvDesc = (TextView) convertView.findViewById(R.id.tvDesc);
            holder.status = (TextView) convertView.findViewById(R.id.status);
            holder.tvSn = (TextView) convertView.findViewById(R.id.tvSn);
            holder.tvAmount = (TextView) convertView.findViewById(R.id.tvAmount);
            holder.tvAddTimeText = (TextView) convertView.findViewById(R.id.tvAddTimeText);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        PdcashInfo info = list.get(position);
        holder.tvDesc.setText("提现审核" + "：");
        holder.status.setText(info.getPaymentStateText());
        if (info.getPaymentState().equals("1")){
            holder.status.setTextColor(context.getResources().getColor(R.color.nc_red));
        }else{
            holder.status.setTextColor(context.getResources().getColor(R.color.nc_green));

        }
        holder.tvSn.setText("提现单号：" + info.getSn());
        holder.tvAmount.setText(info.getAmount());
        holder.tvAddTimeText.setText(info.getAddTimeText());

        return convertView;
    }

    class ViewHolder {
        TextView tvDesc;
        TextView status;
        TextView tvSn;
        TextView tvAmount;
        TextView tvAddTimeText;
    }
}
