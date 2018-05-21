package com.guohanhealth.shop.ui.fenxiao;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.gson.reflect.TypeToken;
import com.guohanhealth.shop.common.StringUtils;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.bean.CateListBean;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.JsonUtil;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.common.ViewFindUtils;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.ResponseData;
import com.guohanhealth.shop.lib.tab.SlidingTabLayout;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by snm on 2016/8/29.
 */
public class FenXiaoAudeoListTabActivity extends FragmentActivity {
    private ImageButton btnBack;
    private TextView tvCommonTitle;
    private TextView tvCommonTitleBorder;
    private SlidingTabLayout slidingTabLayout;
    private ViewPager viewpager;
    List<CateListBean> cate_list = new ArrayList<CateListBean>();

    private ArrayList<Fragment> mFragments = new ArrayList<>();
    private MyPagerAdapter mAdapter;
    private String cate_id;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fenxiao_main_list);
        cate_id = getIntent().getStringExtra("cate_id");
        View decorView = getWindow().getDecorView();

        slidingTabLayout = ViewFindUtils.find(decorView, R.id.viewpagertab);
        viewpager = ViewFindUtils.find(decorView, R.id.viewpager);

        LoadTabDate();
    }


    private class MyPagerAdapter extends FragmentPagerAdapter {

        List<CateListBean> cate_list ;

        public MyPagerAdapter(FragmentManager fm,List<CateListBean> cate_list) {
            super(fm);
            this.cate_list = cate_list;
        }

        @Override
        public int getCount() {
            return cate_list == null ? 0 : cate_list.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return cate_list.get(position).getCate_name();
        }

        @Override
        public Fragment getItem(int position) {
            return SimpleCardFragment.getInstance(cate_list.get(position).getCate_id());
        }
    }

    /**
     * 设置Activity通用标题文字
     */
    protected void setCommonHeader(String title) {
        btnBack = (ImageButton) findViewById(R.id.btnBack);
        tvCommonTitle = (TextView) findViewById(R.id.tvCommonTitle);
        tvCommonTitleBorder = (TextView) findViewById(R.id.tvCommonTitleBorder);

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        tvCommonTitle.setText(title);
    }

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
                            cate_list = cateListBeen;
                            mAdapter = new MyPagerAdapter(getSupportFragmentManager(),cate_list);
                            viewpager.setAdapter(mAdapter);
//                            mAdapter.notifyDataSetChanged();
                            slidingTabLayout.setViewPager(viewpager);
                            if(!StringUtils.isEmpty(cate_id)) {
                                setTablayout();
                            }
                        }
                    }catch (Exception e){
                        e.printStackTrace();
                    }
//                    Logger.d(json);
                }else {
                    ShopHelper.showApiError(getApplicationContext(),data.getJson());
                }
            }
        });

    }

    public void setTablayout(){
        if(!StringUtils.isEmpty(cate_id)){
            for (int i = 0;i<cate_list.size();i++){
                CateListBean bean = cate_list.get(i);
                if(cate_id.equals(bean.getCate_id())){
                    viewpager.setCurrentItem(i);
                    return;
                }
            }
        }

    }

}
