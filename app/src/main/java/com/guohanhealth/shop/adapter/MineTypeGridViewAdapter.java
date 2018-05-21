package com.guohanhealth.shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.bean.MineType;

import java.util.ArrayList;

public class MineTypeGridViewAdapter extends BaseAdapter {
	private ArrayList<MineType> mineTypes;

	private LayoutInflater mLayoutInflater;
	
	private Context context;

	public MineTypeGridViewAdapter(Context context) {
		mLayoutInflater = LayoutInflater.from(context);
		this.context = context;
	}

	@Override
	public int getCount() {
		return mineTypes == null ? 0 : mineTypes.size();
	}

	@Override
	public MineType getItem(int position) {
		return mineTypes.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public ArrayList<MineType> getMineTypes() {
		return mineTypes;
	}

	public void setMineTypes(ArrayList<MineType> mineTypes) {
		this.mineTypes = mineTypes;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MyGridViewHolder viewHolder;
		if (convertView == null) {
			viewHolder = new MyGridViewHolder();
			convertView = mLayoutInflater.inflate(R.layout.listview_mine_type_item, null);
			viewHolder.imagePicID = (ImageView) convertView.findViewById(R.id.imagePicID);
			viewHolder.nameID = (TextView) convertView.findViewById(R.id.nameID);
			viewHolder.disID = (TextView) convertView.findViewById(R.id.disID);
			convertView.setTag(viewHolder);
		} else {
			viewHolder = (MyGridViewHolder) convertView.getTag();
		}
		
		MineType bean = mineTypes.get(position);
		
		viewHolder.imagePicID.setBackgroundResource(bean.getImageURL());
		viewHolder.nameID.setText(context.getString(bean.getName()));
		viewHolder.disID.setText(bean.getDescription());
		
		return convertView;
	}

	private static class MyGridViewHolder {
		ImageView imagePicID;
		TextView nameID,disID;
	}
}
