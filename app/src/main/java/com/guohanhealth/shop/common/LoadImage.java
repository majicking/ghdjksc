package com.guohanhealth.shop.common;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;

import com.guohanhealth.shop.R;

/**
 * 加载网络图片
 *
 * @author huting
 * @date 2016/1/7
 */
public class LoadImage {
    public static void loadImg(Context context,ImageView imageView,String imgUrl){
        Glide.with(context)
                .load(imgUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
//                .placeholder(R.drawable.ic_launcher)  //设置占位图
                .error(R.drawable.ic_launcher)      //加载错误图
                .centerCrop()
                .into(imageView);
    }
}
