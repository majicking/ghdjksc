package com.guohanhealth.shop.ui.fenxiao;

import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.alibaba.livecloud.live.AlivcMediaFormat;
import com.google.gson.reflect.TypeToken;
import com.orhanobut.logger.Logger;
import com.guohanhealth.shop.common.StringUtils;

import com.guohanhealth.shop.BaseActivity;
import com.guohanhealth.shop.R;
import com.guohanhealth.shop.adapter.BeginLiveAdapter;
import com.guohanhealth.shop.bean.CateListBean;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.DialogHelper;
import com.guohanhealth.shop.common.JsonUtil;
import com.guohanhealth.shop.common.LoadImage;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.common.T;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.ResponseData;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * Created by snm on 2016/9/27.
 */
public class BeginLiveActivity extends BaseActivity {

    private LinearLayout ll_take_classification;
    private TextView tv_take_classification;
    private ImageView iv_left_add_img;
    private TextView tv_box_applay;
    private String file_name,cateid;
    private EditText et_title;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_begin_live);
        setCommonHeader("设置录制信息");
        ll_take_classification = (LinearLayout)findViewById(R.id.ll_take_classification);
        tv_take_classification = (TextView)findViewById(R.id.tv_take_classification);
        ll_take_classification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPopWindow();
            }
        });
        dialogFile();
        iv_left_add_img = (ImageView) findViewById(R.id.iv_left_add_img);
        iv_left_add_img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogHelper.setAlpha(BeginLiveActivity.this,0.6f);
                dialogFile();
            }
        });
        et_title = (EditText) findViewById(R.id.et_title);
        tv_box_applay = (TextView)findViewById(R.id.tv_box_applay);
        tv_box_applay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = et_title.getText().toString();
                if(checkapply(title)){
                    ApplyBeginSend(title);
                }
            }
        });
        LoadTabDate();
    }


    public void ApplyBeginSend(String title){

//        LiveCameraActivity.startActivity(getApplicationContext(),"rtmp://video-center.alivecdn.com/shopnc/test1?vhost=live.shopnctest.com", AlivcMediaFormat.OUTPUT_RESOLUTION_360P,false,
//                AlivcMediaFormat.CAMERA_FACING_FRONT,"191","smm113522","44");

        String url = Constants.URL_MOVIE_SEND;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        params.put("movie_title", title);
        params.put("cate_id", cateid);
        params.put("movie_cover_img", file_name);
        RemoteDataHandler.asyncLoginPostDataString(url, params, MyShopApplication.getInstance(), new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
//                Logger.d(json);
                if (data.getCode() == HttpStatus.SC_OK) {
                    try{
                        JSONObject jsonObj = new JSONObject(json);
                        String movie_url = jsonObj.getString("movie_url");
                        String movie_img = jsonObj.getString("member_avatar_url");
                        String member_id = jsonObj.getString("member_id");
                        String member_name = jsonObj.getString("member_name");
                        String video_id = jsonObj.getString("video_id");
//                        LiveCameraActivity.startActivity(getApplicationContext(),"rtmp://video-center.alivecdn.com/shopnc/test1?vhost=live.shopnctest.com", AlivcMediaFormat.OUTPUT_RESOLUTION_360P,false, AlivcMediaFormat.CAMERA_FACING_FRONT);
                        LiveCameraActivity.startActivity(getApplicationContext(),movie_url, AlivcMediaFormat.OUTPUT_RESOLUTION_240P,false,
                                AlivcMediaFormat.CAMERA_FACING_FRONT,member_id,member_name,video_id);

//                        PushActivity.startActivity(getApplicationContext(),movie_url, AlivcMediaFormat.OUTPUT_RESOLUTION_360P,false,
//                                AlivcMediaFormat.CAMERA_FACING_FRONT,member_id,member_name,video_id);
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(getApplicationContext(), json);
                }

            }
        });
    }

    public boolean checkapply(String title){
        if(StringUtils.isEmpty(title)){
            T.showShort(getApplicationContext(),"直播标题不能为空");
            return false;
        }
        if(StringUtils.isEmpty(cateid)){
            T.showShort(getApplicationContext(),"请选择分类");
            return false;
        }

        if(StringUtils.isEmpty(file_name)){
            T.showShort(getApplicationContext(),"封面图片不能为空");
            return false;
        }
        return true;
    }
    List<CateListBean> cateList = new ArrayList<CateListBean>();
    BeginLiveAdapter beginLiveAdapter;
    /*分类的数据*/
    private void LoadTabDate(){
        String url = Constants.URL_CATE_LIST ;

        RemoteDataHandler.asyncDataStringGet(url, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                if(data.getCode() == 200){
                    String json = data.getJson();
                    try {
                        String cate_lists = JsonUtil.getString(data.getJson(),"cate_list");

                        List<CateListBean> cateListBeen = JsonUtil.getBean(cate_lists,new TypeToken<ArrayList<CateListBean>>(){}.getType());
                        if(!cateListBeen.isEmpty()){
                            cateList.addAll(cateListBeen);
                            beginLiveAdapter = new BeginLiveAdapter(getApplicationContext());
                            beginLiveAdapter.setCateList(cateList);
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                    Logger.d(json);
                }else {
                    ShopHelper.showApiError(getApplicationContext(),data.getJson());
                }
            }
        });

    }

    /*分类的pop*/
    private PopupWindow popupWindow;
    private View viewPopScreen;
    private void showPopWindow(){
        if(popupWindow == null){
            viewPopScreen = LayoutInflater.from(BeginLiveActivity.this).inflate(R.layout.pop_beginlive, null);
            popupWindow = new PopupWindow(viewPopScreen, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, true);
            popupWindow.setTouchable(true);
            popupWindow.setOutsideTouchable(true);
            popupWindow.setBackgroundDrawable(new BitmapDrawable(BeginLiveActivity.this.getResources(), (Bitmap) null));
            popupWindow.update();

            //初始化popwindow的控件
            ListView lvcate = (ListView) viewPopScreen.findViewById(R.id.lvcate);
            TextView btnConfirm = (TextView) viewPopScreen.findViewById(R.id.btnConfirm);
            FrameLayout flBack = (FrameLayout) viewPopScreen.findViewById(R.id.flBack);

            flBack.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupWindow.dismiss();
                }
            });
            btnConfirm.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    popupWindow.dismiss();
                }
            });
            lvcate.setAdapter(beginLiveAdapter);
            lvcate.setOnItemClickListener(new AdapterView.OnItemClickListener() {//响应listview中的item的点击事件

                @Override
                public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
                                        long arg3) {
                    // TODO Auto-generated method stub

                    cateid = cateList.get(arg2).getCate_id();
                    tv_take_classification.setText(cateList.get(arg2).getCate_name());
//                    T.showShort(getApplicationContext(),cateList.get(arg2).getCate_name());
                    popupWindow.dismiss();
                }
            });
        }
        //设置出现位置
        popupWindow.showAtLocation(viewPopScreen, Gravity.CENTER, 0, 0);
    }

    private PopupWindow FilePopWindow;
    private View FileView;
    //  弹出选择 文件dialog
    public void dialogFile() {
        if (FilePopWindow == null) {
            FileView = this.getLayoutInflater().inflate(R.layout.comment_photo_dialog, null);
            FilePopWindow = getFilePopWindow(FileView);
        } else {
            if (!FilePopWindow.isShowing()) {
                FilePopWindow.showAtLocation(FileView, Gravity.RIGHT | Gravity.BOTTOM, 0, 0);
            }
        }
    }
    /*文件pop*/
    private PopupWindow getFilePopWindow(View view) {
        final PopupWindow popupWindow = DialogHelper.getPopupWindow(BeginLiveActivity.this,view);

        Button btn_photo = (Button) view.findViewById(R.id.btn_photo);
        Button btn_photobyalbum = (Button) view.findViewById(R.id.btn_photobyalbum);

        Button btn_cancle = (Button) view.findViewById(R.id.btn_cancle);
        btn_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_left_add_img.setClickable(false);
                Intent it = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(it, 1);
                popDismiss();
            }
        });
        btn_photobyalbum.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iv_left_add_img.setClickable(false);
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                startActivityForResult(intent, 108);
                popDismiss();
            }
        });
        btn_cancle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FilePopWindow.dismiss();
            }
        });
        return popupWindow;
    }

    private void popDismiss() {
        DialogHelper.setAlpha(BeginLiveActivity.this,1.0f);
        if(null != FilePopWindow) {
            FilePopWindow.dismiss();
        }
    }
    /**
     * 上传图片
     * @param file
     */
    public void uploadImage(final String path,File file) {
        HashMap<String,String> params = new HashMap<String,String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        HashMap<String, File> fileMap = new HashMap<String, File>();
        fileMap.put("file", file);

        RemoteDataHandler.asyncMultipartPostString(Constants.URL_FILE_UPLOAD, params, fileMap, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
//                Logger.d(json);
                if (data.getCode() == HttpStatus.SC_OK)  {
                    try {
                        popDismiss();
                        T.showShort(getApplicationContext(),"上传成功");
                        JSONObject obj = new JSONObject(json);
//                        int file_id = obj.optInt("file_id");
                        file_name = obj.optString("file_name");
//                        String origin_file_name = obj.optString("origin_file_name");
                        String file_url = obj.optString("file_url");
//                        ImageFile imageFile = new ImageFile();
//                        imageFile.setFile_id(file_id);
//                        imageFile.setFile_name(file_name);
//                        imageFile.setFile_url(file_url);
//                        imageFile.setOrigin_file_name(origin_file_name);
                        iv_left_add_img.setClickable(true);
                        LoadImage.loadImg(getApplicationContext(),iv_left_add_img,file_url);
                    } catch (JSONException e) {
                        iv_left_add_img.setClickable(true);
                        e.printStackTrace();
                    }
                } else {
                    iv_left_add_img.setClickable(true);
                    T.showShort(getApplicationContext(), "文件上传失败");
                }


            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 108:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        Uri uri = data.getData();
//                        Logger.e(uri.toString());
                        String path = getRealPathFromURI(uri);
                        try {
                            getPathUpload(path);
                        } catch (Exception e) {
                            iv_left_add_img.setClickable(true);
                            e.printStackTrace();
                        }
                    }
                }
                break;
            case 1:

                Bundle extras = data.getExtras();
                Bitmap b = (Bitmap) extras.get("data");
                String name = new SimpleDateFormat("yyyyMMddhhmmss").format(new Date());
                String fileNmae = Environment.getExternalStorageDirectory().toString()+File.separator+"dong/image/"+name+".jpg";
                Uri uri = MediaStore.Audio.Media.getContentUriForPath(fileNmae);

                File myCaptureFile = new File(fileNmae);
                String mimeType = "image/*";
                ContentValues values = new ContentValues(); values.put(MediaStore.MediaColumns.DATA, myCaptureFile.getAbsolutePath());
                values.put(MediaStore.MediaColumns.TITLE, myCaptureFile.getName());
                values.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                values.put(MediaStore.MediaColumns.SIZE, myCaptureFile.length());

                this.getContentResolver().insert(uri,values);
                try {
                    if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                        if(!myCaptureFile.getParentFile().exists()){
                            myCaptureFile.getParentFile().mkdirs();
                        }
                        BufferedOutputStream bos;
                        bos = new BufferedOutputStream(new FileOutputStream(myCaptureFile));
                        b.compress(Bitmap.CompressFormat.JPEG, 80, bos);
                        getPathUpload(fileNmae);
                        bos.flush();
                        bos.close();
                    }else{
                        iv_left_add_img.setClickable(true);
                        Toast toast= Toast.makeText(getApplicationContext(), "保存失败，SD卡无效", Toast.LENGTH_SHORT);
                        toast.setGravity(Gravity.CENTER, 0, 0);
                        toast.show();
                    }
                } catch (FileNotFoundException e) {
                    iv_left_add_img.setClickable(true);
                    e.printStackTrace();
                } catch (IOException e) {
                    iv_left_add_img.setClickable(true);
                    e.printStackTrace();
                }

                super.onActivityResult(requestCode, resultCode, data);
        }
    }
    public String getRealPathFromURI(Uri contentUri) {
        String[] proj = { MediaStore.Images.Media.DATA };
        CursorLoader loader = new CursorLoader(getApplicationContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }
    private void getPathUpload(String path) {
        if(path!=null){
            File file = new File(path);
            if(file.exists()){
                if (file.length() < 1024 * 1024 * 5) {
//                    Logger.e(file.getAbsolutePath());
                    uploadImage(path,file);
                } else {
                    iv_left_add_img.setClickable(true);
                    T.showShort(getApplicationContext(), "图片文件过大，请上传3M以下的图片");
                }
            } else {
                iv_left_add_img.setClickable(true);
                T.showShort(getApplicationContext(), "文件不存在");
            }

        }else {
            iv_left_add_img.setClickable(true);
            T.showShort(getApplicationContext(), "地址不存在");
        }
    }

}
