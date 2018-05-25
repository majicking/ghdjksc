package com.guohanhealth.shop.ui.type;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.adapter.BrandGridViewAdapter;
import com.guohanhealth.shop.adapter.GoodsClassGridViewAdapter;
import com.guohanhealth.shop.bean.BrandInfo;
import com.guohanhealth.shop.bean.GoodsClassInfo;
import com.guohanhealth.shop.bean.OneType;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.MyExceptionHandler;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.RemoteDataHandler.Callback;
import com.guohanhealth.shop.http.ResponseData;
import com.guohanhealth.shop.scannercode.android.CaptureActivity;
import com.guohanhealth.shop.ui.home.SearchActivity;
import com.guohanhealth.shop.ui.mine.IMNewListActivity;
import com.guohanhealth.shop.xrefresh.utils.LogUtils;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 商品分类界面
 *
 * @author dqw
 * @Time 2015-7-2
 */
public class OneTypeFragment extends Fragment {
    private MyShopApplication myApplication;
    private ScrollView svGoodsClassRoot;
    private LinearLayout llGoodsClassRoot;
    private View currentGoodsClassView;
    protected ImageLoader imageLoader = ImageLoader.getInstance();
    private GridView gvBrand;
    private ScrollView svGoodsClass;
    private LinearLayout llGoodsClass;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View viewLayout = inflater.inflate(R.layout.main_one_type_view, container, false);

        myApplication = (MyShopApplication) getActivity().getApplicationContext();
        MyExceptionHandler.getInstance().setContext(getActivity());
        svGoodsClassRoot = (ScrollView) viewLayout.findViewById(R.id.svGoodsClassRoot);
        llGoodsClassRoot = (LinearLayout) viewLayout.findViewById(R.id.llGoodsClassRoot);
        gvBrand = (GridView) viewLayout.findViewById(R.id.gvBrand);
        svGoodsClass = (ScrollView) viewLayout.findViewById(R.id.svGoodsClass);
        llGoodsClass = (LinearLayout) viewLayout.findViewById(R.id.llGoodsClass);

        TextView tvSearch = (TextView) viewLayout.findViewById(R.id.tvSearch);

        //显示搜索热词
        String searchHotName = myApplication.getSearchHotName();
        if (searchHotName != null && !searchHotName.equals("")) {
            tvSearch.setHint(searchHotName);
        } else {
            tvSearch.setHint(getActivity().getResources().getString(R.string.default_search_text));
        }

        //搜索
        tvSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SearchActivity.class);
                startActivity(intent);
            }
        });

        //二维码扫描
        Button btnCamera = (Button) viewLayout.findViewById(R.id.btnCamera);
        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), CaptureActivity.class);
                startActivity(intent);
            }
        });

        //IM
        Button btnIm = (Button) viewLayout.findViewById(R.id.btnIm);
        btnIm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (ShopHelper.isLogin(getActivity(), myApplication.getLoginKey())) {
//                    startActivity(new Intent(getActivity(), IMFriendsListActivity.class));
                    startActivity(new Intent(getActivity(), IMNewListActivity.class));
                }
            }
        });


        return viewLayout;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGoodsClassRoot();
        loadBrandList();

    }

    Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    /**
     * 加载一级商品分类
     */
    public void loadGoodsClassRoot() {
        String url = Constants.URL_GOODSCLASS;
        LogUtils.i(url);
        RemoteDataHandler.asyncDataStringGet(url, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    LogUtils.i("分类返回数据：" + json);
                    try {
                        JSONObject obj = new JSONObject(json);

                        String array = obj.getString("class_list");
                        ArrayList<OneType> typeList = OneType.newInstanceList(array);
                        OneType oneType = new OneType("0", "品牌推荐", Constants.WAP_BRAND_ICON, "");
                        typeList.add(0, oneType);
                        llGoodsClassRoot.removeAllViews();
                        for (int i = 0; i < typeList.size(); i++) {
                            OneType classItem = typeList.get(i);
                            boolean isOn = false;
                            if (i == 0) {
                                isOn = true;
                            }
                            addGoodsClass(classItem, isOn);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(context, json);
                }
            }
        });
    }

    /**
     * 添加分类节点
     */
    private void addGoodsClass(OneType classItem, final boolean isOn) {
        try {
            LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View v = vi.inflate(R.layout.listivew_type_item, null);
            final ImageView ivGoodsClassImage = (ImageView) v.findViewById(R.id.ivGoodsClassImage);
            TextView tvGoodsClassId = (TextView) v.findViewById(R.id.tvGoodsClassId);
            TextView tvGoodsClassName = (TextView) v.findViewById(R.id.tvGoodsClassName);
            tvGoodsClassId.setText(classItem.getGc_id());
            tvGoodsClassName.setText(classItem.getGc_name());

            imageLoader.loadImage(classItem.getImage(), new SimpleImageLoadingListener() {
                @Override
                public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                    ivGoodsClassImage.setImageBitmap(loadedImage);
                    if (!isOn) {
                        ivGoodsClassImage.getDrawable().setColorFilter(context.getResources().getColor(R.color.nc_fg), PorterDuff.Mode.MULTIPLY);
                    }
                }

            });
            if (isOn) {
                currentGoodsClassView = v;
                setCurrentGoodsClass(v);
            } else {
                resetCurrentGoodsClass(v);
            }

            //点击
            v.setOnClickListener(view -> {
                resetCurrentGoodsClass(currentGoodsClassView);
                setCurrentGoodsClass(view);

                currentGoodsClassView = view;
                svGoodsClassRoot.smoothScrollTo(0, view.getTop());

                TextView tvGoodsClassId1 = (TextView) view.findViewById(R.id.tvGoodsClassId);
                String goodsClassId = tvGoodsClassId1.getText().toString();
                if (goodsClassId.equals("0")) {
                    gvBrand.setVisibility(View.VISIBLE);
                    svGoodsClass.setVisibility(View.GONE);
                } else {
                    showGoodsClass(goodsClassId);
                }
            });
            llGoodsClassRoot.addView(v);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 高亮显示选中分类
     */
    private void setCurrentGoodsClass(View view) {
        ImageView ivGoodsClassImage = (ImageView) view.findViewById(R.id.ivGoodsClassImage);
        TextView tvGoodsClassName = (TextView) view.findViewById(R.id.tvGoodsClassName);
        TextView tvLine = (TextView) view.findViewById(R.id.tvLine);
        tvGoodsClassName.setTextColor(getActivity().getResources().getColor(R.color.nc_btn_bg));
        tvLine.setBackgroundColor(getActivity().getResources().getColor(R.color.nc_btn_bg));
        ViewGroup.LayoutParams params = tvLine.getLayoutParams();
        params.height = getResources().getDimensionPixelSize(R.dimen.dimen_2);
        tvLine.setLayoutParams(params);
        ivGoodsClassImage.getDrawable().clearColorFilter();
    }

    /**
     * 重置一级分类选中状态
     */
    private void resetCurrentGoodsClass(View view) {
        ImageView ivGoodsClassImage = (ImageView) view.findViewById(R.id.ivGoodsClassImage);
        TextView tvGoodsClassName = (TextView) view.findViewById(R.id.tvGoodsClassName);
        TextView tvLine = (TextView) view.findViewById(R.id.tvLine);

        tvGoodsClassName.setTextColor(getActivity().getResources().getColor(R.color.nc_text));
        tvLine.setBackgroundColor(getActivity().getResources().getColor(R.color.nc_fg));
        ViewGroup.LayoutParams params = tvLine.getLayoutParams();
        params.height = getResources().getDimensionPixelSize(R.dimen.dimen_1);
        tvLine.setLayoutParams(params);
        ivGoodsClassImage.getDrawable().setColorFilter(getResources().getColor(R.color.nc_fg), PorterDuff.Mode.MULTIPLY);
    }

    /**
     * 显示品牌列表
     */
    private void loadBrandList() {
        RemoteDataHandler.asyncDataStringGet(Constants.URL_BRAND, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {

                        JSONObject obj = new JSONObject(json);
                        String brandList = obj.getString("brand_list");
                        if (brandList != "" && brandList != null && !brandList.equals("[]")) {
                            ArrayList<BrandInfo> brandArray = BrandInfo.newInstanceList(brandList);
                            BrandGridViewAdapter brandGridViewAdapter = new BrandGridViewAdapter(context);
                            brandGridViewAdapter.setBrandArray(brandArray);
                            gvBrand.setAdapter(brandGridViewAdapter);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(context, json);
                }
            }
        });
    }

    /**
     * 显示下级分类
     */
    private void showGoodsClass(String classId) {
        //隐藏品牌列表
        gvBrand.setVisibility(View.GONE);
        svGoodsClass.setVisibility(View.VISIBLE);
        svGoodsClass.scrollTo(0, 0);
        RemoteDataHandler.asyncDataStringGet(Constants.URL_GOODS_CLASS_CHILD_ALL + "&gc_id=" + classId, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject obj = new JSONObject(json);
                        String list = obj.getString("class_list");
                        if (list != "" && list != null && !list.equals("[]")) {
                            ArrayList<GoodsClassInfo> array = GoodsClassInfo.newInstanceList(list);
                            llGoodsClass.removeAllViews();
                            for (int i = 0; i < array.size(); i++) {
                                final GoodsClassInfo goodsClassInfo = array.get(i);

                                LayoutInflater vi = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                View view = vi.inflate(R.layout.item_goods_class, null);
                                if (i == 0) {
                                    TextView tvLine = (TextView) view.findViewById(R.id.tvLine);
                                    tvLine.setVisibility(View.INVISIBLE);
                                }

                                ImageView ivGoodsClassDot = (ImageView) view.findViewById(R.id.tvGoodsClassDot);
                                int index = i % 10;
                                switch (index) {
                                    case 0:
                                        ivGoodsClassDot.setImageDrawable(context.getResources().getDrawable(R.drawable.nc_sharp_dot0));
                                        break;
                                    case 1:
                                        ivGoodsClassDot.setImageDrawable(context.getResources().getDrawable(R.drawable.nc_sharp_dot1));
                                        break;
                                    case 2:
                                        ivGoodsClassDot.setImageDrawable(context.getResources().getDrawable(R.drawable.nc_sharp_dot2));
                                        break;
                                    case 3:
                                        ivGoodsClassDot.setImageDrawable(context.getResources().getDrawable(R.drawable.nc_sharp_dot3));
                                        break;
                                    case 4:
                                        ivGoodsClassDot.setImageDrawable(context.getResources().getDrawable(R.drawable.nc_sharp_dot4));
                                        break;
                                    case 5:
                                        ivGoodsClassDot.setImageDrawable(context.getResources().getDrawable(R.drawable.nc_sharp_dot5));
                                        break;
                                    case 6:
                                        ivGoodsClassDot.setImageDrawable(context.getResources().getDrawable(R.drawable.nc_sharp_dot6));
                                        break;
                                    case 7:
                                        ivGoodsClassDot.setImageDrawable(context.getResources().getDrawable(R.drawable.nc_sharp_dot7));
                                        break;
                                    case 8:
                                        ivGoodsClassDot.setImageDrawable(context.getResources().getDrawable(R.drawable.nc_sharp_dot8));
                                        break;
                                    case 9:
                                        ivGoodsClassDot.setImageDrawable(context.getResources().getDrawable(R.drawable.nc_sharp_dot9));
                                        break;
                                }

                                TextView tvGoodsClassName = (TextView) view.findViewById(R.id.tvGoodsClassName);
                                tvGoodsClassName.setText(goodsClassInfo.getGcName());

                                Button btnGoodsClass = (Button) view.findViewById(R.id.btnGoodsClass);
                                btnGoodsClass.setOnClickListener(view1 -> {
                                    Intent intent = new Intent(getActivity(), GoodsListFragmentManager.class);
                                    intent.putExtra("gc_id", goodsClassInfo.getGcId());
                                    intent.putExtra("gc_name", goodsClassInfo.getGcName());
                                    startActivity(intent);
                                });

                                GridView gvGoodsClass = (GridView) view.findViewById(R.id.gvGoodsClass);

                                ArrayList<GoodsClassInfo> goodsClassList = GoodsClassInfo.newInstanceList(goodsClassInfo.getChild());

                                final GoodsClassGridViewAdapter goodsClassGridViewAdapter = new GoodsClassGridViewAdapter(getActivity());
                                goodsClassGridViewAdapter.setGoodsClassInfoList(goodsClassList);
                                gvGoodsClass.setAdapter(goodsClassGridViewAdapter);
                                gvGoodsClass.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                    @Override
                                    public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                                        GoodsClassInfo goodsClassInfo = (GoodsClassInfo) goodsClassGridViewAdapter.getItem(i);
                                        Intent intent = new Intent(getActivity(), GoodsListFragmentManager.class);
                                        intent.putExtra("gc_id", goodsClassInfo.getGcId());
                                        intent.putExtra("gc_name", goodsClassInfo.getGcName());
                                        startActivity(intent);
                                    }
                                });
                                llGoodsClass.addView(view);
                            }
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(context, json);
                }
            }
        });
    }
}