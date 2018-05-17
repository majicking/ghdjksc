package com.xinyuangongxiang.shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.bean.CityList;

import java.util.ArrayList;

/**
 * 地区Spinner适配器
 */
public class AreaSpinnerAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<CityList> areaList;

    public AreaSpinnerAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        areaList = new ArrayList<>();
    }

    @Override
    public int getCount() {
        return areaList == null ? 0 : areaList.size();
    }

    @Override
    public Object getItem(int position) {
        return areaList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setAreaList(ArrayList<CityList> areaList) {
        if (areaList != null && areaList.size() > 0)
            this.areaList = areaList;
        else
            this.areaList.clear();
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.spinner_area_item, null);
            holder = new ViewHolder();
            holder.tvAreaName = (TextView) convertView.findViewById(R.id.tvAreaName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        CityList areaInfo = areaList.get(position);
        holder.tvAreaName.setText(areaInfo.getArea_name());

        return convertView;
    }

    class ViewHolder {
        TextView tvAreaName;
    }
}
