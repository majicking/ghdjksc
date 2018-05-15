package com.xinyuangongxiang.shop.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.bean.ContractDetail;
import com.xinyuangongxiang.shop.common.AnimateFirstDisplayListener;
import com.xinyuangongxiang.shop.common.SystemHelper;

import java.util.ArrayList;

/**
 * 消保详情适配器
 */
public class ContractDetailGridViewAdapter extends BaseAdapter {

    private Context context;
    private LayoutInflater inflater;
    private ArrayList<ContractDetail> contractList;

    protected ImageLoader imageLoader = ImageLoader.getInstance();
    private DisplayImageOptions options = SystemHelper.getDisplayImageOptions();
    private ImageLoadingListener animateFirstListener = new AnimateFirstDisplayListener();

    public ContractDetailGridViewAdapter(Context context) {
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    @Override
    public int getCount() {
        return contractList == null ? 0 : contractList.size();
    }

    @Override
    public Object getItem(int position) {
        return contractList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public void setContractList(ArrayList<ContractDetail> contractList) {
        this.contractList = contractList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (null == convertView) {
            convertView = inflater.inflate(R.layout.gridview_contract_detail_item, null);
            holder = new ViewHolder();
            holder.ivContractIcon = (ImageView) convertView.findViewById(R.id.ivContractIcon);
            holder.tvContractName = (TextView) convertView.findViewById(R.id.tvContractName);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        ContractDetail contractInfo = contractList.get(position);
        imageLoader.displayImage(contractInfo.getCtiIcon(), holder.ivContractIcon, options, animateFirstListener);
        holder.tvContractName.setText(contractInfo.getCtiName());

        return convertView;
    }

    class ViewHolder {
        ImageView ivContractIcon;
        TextView tvContractName;
    }
}
