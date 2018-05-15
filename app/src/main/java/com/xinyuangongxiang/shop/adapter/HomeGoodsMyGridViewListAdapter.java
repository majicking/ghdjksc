package com.xinyuangongxiang.shop.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.bean.HomeGoodsList;
import com.xinyuangongxiang.shop.common.AnimateFirstDisplayListener;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.SystemHelper;
import com.xinyuangongxiang.shop.ui.home.SubjectWebActivity;
import com.xinyuangongxiang.shop.ui.type.GoodsDetailsActivity;
import com.xinyuangongxiang.shop.ui.type.GoodsListFragmentManager;

import java.util.ArrayList;

/**
 * 首页GoodsGridView适配器
 * @author KingKong-HE
 * @Time 2015年1月4日
 * @Email KingKong@QQ.COM
 */
public class HomeGoodsMyGridViewListAdapter extends BaseAdapter{
	private Context context;
	private LayoutInflater inflater;
	private ArrayList<HomeGoodsList> goodsDatas;
	protected ImageLoader imageLoader = ImageLoader.getInstance();
	private DisplayImageOptions options = SystemHelper.getDisplayImageOptions();
	private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();
	
	public HomeGoodsMyGridViewListAdapter(Context context) {
		this.context = context;
		this.inflater = LayoutInflater.from(context);
	}
	@Override
	public int getCount() {
		return goodsDatas == null ? 0 : goodsDatas.size();
	}

	@Override
	public Object getItem(int position) {
		return goodsDatas.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}
	
	public ArrayList<HomeGoodsList> getHomeGoodsList() {
		return goodsDatas;
	}
	public void setHomeGoodsList(ArrayList<HomeGoodsList> goodsDatas) {
		this.goodsDatas = goodsDatas;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		if (null == convertView) {
			convertView = inflater.inflate(R.layout.tab_home_item_goods_gridview_item, null);
			holder = new ViewHolder();
			holder.ImageViewImagePic01 = (ImageView) convertView.findViewById(R.id.ImageViewImagePic01);
			holder.TextViewTitle = (TextView) convertView.findViewById(R.id.TextViewTitle);
			holder.TextViewPrice = (TextView) convertView.findViewById(R.id.TextViewPrice);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}
		
		HomeGoodsList bean = goodsDatas.get(position);
		
		holder.TextViewTitle.setText(bean.getGoods_name());
		holder.TextViewPrice.setText("￥"+bean.getGoods_promotion_price());
		
		imageLoader.displayImage(bean.getGoods_image(), holder.ImageViewImagePic01, options, animateFirstListener);
		OnImageViewClick(holder.ImageViewImagePic01, "goods",bean.getGoods_id());

		return convertView;
	}
	
	class ViewHolder {
		ImageView ImageViewImagePic01;
		TextView TextViewTitle;
		TextView TextViewPrice;
	}
	public void OnImageViewClick(View view,final String type,final String data){
		view.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
			if(type.equals("keyword")){//搜索关键字
					Intent intent = new Intent(context,GoodsListFragmentManager.class);
					intent.putExtra("keyword", data);
					intent.putExtra("gc_name", data);
					context.startActivity(intent);
				}else if(type.equals("special")){//专题编号
					Intent intent = new Intent(context,SubjectWebActivity.class);
					intent.putExtra("data", Constants.URL_SPECIAL+"&special_id="+data+"&type=html");
					context.startActivity(intent);
				}else if(type.equals("goods")){//商品编号
					Intent intent =new Intent(context,GoodsDetailsActivity.class);
					intent.putExtra("goods_id", data);
					context.startActivity(intent);
				}else if(type.equals("url")){//地址
					Intent intent = new Intent(context,SubjectWebActivity.class);
					intent.putExtra("data", data);
					context.startActivity(intent);
				}
			}
		});
	}
}
