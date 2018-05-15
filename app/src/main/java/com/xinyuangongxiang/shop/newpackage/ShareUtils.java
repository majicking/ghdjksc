package com.xinyuangongxiang.shop.newpackage;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.umeng.socialize.ShareAction;
import com.umeng.socialize.UMShareListener;
import com.umeng.socialize.bean.SHARE_MEDIA;
import com.umeng.socialize.media.UMImage;
import com.umeng.socialize.media.UMWeb;

/**
 * Author     wildma
 * DATE       2017/07/16
 * Des	      ${友盟分享工具类}
 */
public class ShareUtils {

    Context context;
    public ShareUtils(Context context) {
        this.context=context;
    }

    /**
     * 分享链接
     */
    public  void shareWeb(final Activity activity,
                          String WebUrl, /**链接地址*/
                          String title, /**标题*/
                          String description, /**描述*/
                          String imageUrl, /**网络图片地址*/
                          int imageID, /**本地图片地址*/
                          SHARE_MEDIA platform) {
        UMWeb web = new UMWeb(WebUrl);//连接地址
        web.setTitle(title);//标题
        web.setDescription(description);//描述
        if (TextUtils.isEmpty(imageUrl)) {
            web.setThumb(new UMImage(activity, imageID));  //本地缩略图
        } else {
            web.setThumb(new UMImage(activity, imageUrl));  //网络缩略图
        }
        new ShareAction(activity)
                .setPlatform(platform)
                .withMedia(web)
                .setCallback(umShareListener)
                .share();

    }
    //设置单个监听是否分享成功
    private UMShareListener umShareListener = new UMShareListener() {

        @Override
        public void onStart(SHARE_MEDIA share_media) {
        }

        @Override
        public void onResult(SHARE_MEDIA platform) {
            if (platform == SHARE_MEDIA.QQ) {
                Toast.makeText(context, "QQ分享成功啦", Toast.LENGTH_SHORT).show();
            } else if (platform == SHARE_MEDIA.SINA) {
                Toast.makeText(context, "新浪微博分享成功啦", Toast.LENGTH_SHORT).show();
            } else if (platform == SHARE_MEDIA.QZONE) {
                Toast.makeText(context, "QQ空间分享成功啦", Toast.LENGTH_SHORT).show();
            } else if (platform == SHARE_MEDIA.WEIXIN) {
                Toast.makeText(context, "微信分享成功啦", Toast.LENGTH_SHORT).show();
            } else if (platform == SHARE_MEDIA.WEIXIN_CIRCLE) {
                Toast.makeText(context, "微信朋友圈分享成功啦", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onError(SHARE_MEDIA platform, Throwable t) {
            if (platform == SHARE_MEDIA.QQ) {
                Toast.makeText(context, "QQ分享失败啦", Toast.LENGTH_SHORT).show();
            } else if (platform == SHARE_MEDIA.SINA) {
                Toast.makeText(context, "新浪微博分享失败啦", Toast.LENGTH_SHORT).show();
            } else if (platform == SHARE_MEDIA.QZONE) {
                Toast.makeText(context, "QQ空间分享失败啦", Toast.LENGTH_SHORT).show();
            } else if (platform == SHARE_MEDIA.WEIXIN) {
                Toast.makeText(context, "微信分享失败啦", Toast.LENGTH_SHORT).show();
            } else if (platform == SHARE_MEDIA.WEIXIN_CIRCLE) {
                Toast.makeText(context, "微信朋友圈分享失败啦", Toast.LENGTH_SHORT).show();
            }
        }

        @Override
        public void onCancel(SHARE_MEDIA platform) {
            if (platform == SHARE_MEDIA.QQ) {
                Toast.makeText(context, "QQ分享取消了", Toast.LENGTH_SHORT).show();
            } else if (platform == SHARE_MEDIA.SINA) {
                Toast.makeText(context, "新浪微博分享取消了", Toast.LENGTH_SHORT).show();
            } else if (platform == SHARE_MEDIA.QZONE) {
                Toast.makeText(context, "QQ空间分享取消了", Toast.LENGTH_SHORT).show();
            } else if (platform == SHARE_MEDIA.WEIXIN) {
                Toast.makeText(context, "微信分享取消了", Toast.LENGTH_SHORT).show();
            } else if (platform == SHARE_MEDIA.WEIXIN_CIRCLE) {
                Toast.makeText(context, "微信朋友圈分享取消了", Toast.LENGTH_SHORT).show();
            }
        }



    };



}
