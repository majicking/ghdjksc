package com.xinyuangongxiang.shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.bean.AddressList;
import com.xinyuangongxiang.shop.ncinterface.INCOnDel;
import com.xinyuangongxiang.shop.ncinterface.INCOnEdit;

import java.util.ArrayList;

/**
 * 收货地址列表适配器
 *
 * @author KingKong·HE
 * @Time 2014-1-6 下午12:06:09
 */
public class AddressListViewAdapter extends BaseAdapter {
    private Context context;
    private LayoutInflater inflater;
    private ArrayList<AddressList> addressLists;
    private INCOnDel incOnDel;
    private INCOnEdit incOnEdit;

    public AddressListViewAdapter(Context context, INCOnDel incOnDel, INCOnEdit incOnEdit) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
        this.incOnDel = incOnDel;
        this.incOnEdit = incOnEdit;
    }

    @Override
    public int getCount() {
        return addressLists == null ? 0 : addressLists.size();
    }

    @Override
    public Object getItem(int position) {
        return addressLists.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public ArrayList<AddressList> getAddressLists() {
        return addressLists;
    }

    public void setAddressLists(ArrayList<AddressList> addressLists) {
        this.addressLists = addressLists;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.listview_address_item, null);
            holder = new ViewHolder();
            holder.textAddressName = (TextView) convertView.findViewById(R.id.textAddressName);
            holder.textAddressPhone = (TextView) convertView.findViewById(R.id.textAddressPhone);
            holder.textAddressAddress = (TextView) convertView.findViewById(R.id.textAddressAddress);
            holder.btnEdit = (TextView) convertView.findViewById(R.id.btnEdit);
            holder.btnDel = (TextView) convertView.findViewById(R.id.btnDel);
            holder.btnDefaultAddress = (ImageView)convertView.findViewById(R.id.btnDefaultAddress);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        AddressList bean = addressLists.get(position);
        final String addressId = bean.getAddress_id();
        holder.textAddressName.setText(bean.getTrue_name() == null ? "" : bean.getTrue_name());
        holder.textAddressPhone.setText(bean.getMob_phone() == null ? "" : bean.getMob_phone());
        holder.textAddressAddress.setText(bean.getArea_info() + "\t" + bean.getAddress());
        if(bean.getIs_default().equals("1")) {
            holder.btnDefaultAddress.setSelected(true);
        } else {
            holder.btnDefaultAddress.setSelected(false);
        }
        holder.btnEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incOnEdit.onEdit(addressId);
            }
        });
        holder.btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                incOnDel.onDel(addressId);
            }
        });

        return convertView;
    }

    class ViewHolder {
        TextView textAddressName;
        TextView textAddressPhone;
        TextView textAddressAddress;
        ImageView btnDefaultAddress;
        TextView btnEdit;
        TextView btnDel;
    }
}
