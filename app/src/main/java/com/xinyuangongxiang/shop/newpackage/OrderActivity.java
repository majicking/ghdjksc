package com.xinyuangongxiang.shop.newpackage;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.common.Constants;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.xinyuangongxiang.shop.common.Constants.ORDERNUMBER;
import static com.xinyuangongxiang.shop.common.Constants.ORDERTYPE;


public class OrderActivity extends FragmentActivity implements OrderFragment.OnFragmentInteractionListener {


    @BindView(R.id.tab)
    TabLayout tab;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    @BindView(R.id.back)
    ImageButton btnBack;
    @BindView(R.id.realorder)
    Button realorder;
    @BindView(R.id.virtualorder)
    Button virtualorder;
    @BindView(R.id.editsearchorder)
    EditText editsearchorder;
    @BindView(R.id.searchorder)
    ImageButton searchorder;
    private ViewPagerAdapter adapter;
    private String url;
    private String searchtext;
    private int mCurrentPosition;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);
        ButterKnife.bind(this);
        initView();
        initData();
    }


    String[] reatitle = {"全部", "待付款", "待收货", "待自提", "待评价"};
    String[] reatype = {"", "state_new", "state_send", "state_notakes", "state_noeval"};
    String[] viltitle = {"全部", "待付款", "待使用"};
    String[] viltype = {"", "state_new", "state_pay"};
    String[] titles;
    String[] ordertype;//订单标识
    boolean select = false;//当前是虚拟订单还是实物订单  虚拟订单true      实物订单false
    boolean selecttype;//跳转到虚拟订单还是实物订单
    int num = 0;//选中默认项
    List<Fragment> fragmentlist;

    //初始化数据
    private void initView() {
        selecttype = getIntent().getBooleanExtra(ORDERTYPE, false);
        fragmentlist = new ArrayList<>();

        if (!selecttype) {
            realorder.setActivated(true);//默认实物订单
            virtualorder.setActivated(false);
            titles = reatitle;
            ordertype = reatype;
            select = false;
            url = Constants.URL_ORDER_LIST;
        } else {
            realorder.setActivated(false);
            virtualorder.setActivated(true);
            url = Constants.URL_MEMBER_VR_ORDER;
            ordertype = viltype;
            select = true;
            titles = viltitle;
        }

        viewpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tab));
        adapter = new ViewPagerAdapter(this, getSupportFragmentManager(), fragmentlist, titles);
        viewpager.setAdapter(adapter);
        tab.setupWithViewPager(viewpager);
        notitydata();
    }

    private void notitydata() {
        searchtext = editsearchorder.getText().toString().trim();
        fragmentlist.clear();
        for (int i = 0; i < titles.length; i++) {
            fragmentlist.add(OrderFragment.newInstance(titles[i], ordertype[i], TextUtils.isEmpty(searchtext) ? "" : searchtext, url, select));
        }
        adapter.updataAdapter(fragmentlist, titles);
        adapter.notifyDataSetChanged();
    }


    public void initData() {
        num = getIntent().getIntExtra(ORDERNUMBER, 0);
        viewpager.setCurrentItem(num);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @OnClick(R.id.back)
    public void back() {
        finish();
    }


    @OnClick(R.id.realorder)
    public void real() {
        realorder.setActivated(true);
        virtualorder.setActivated(false);
        titles = reatitle;
        ordertype = reatype;
        select = false;
        url = Constants.URL_ORDER_LIST;
        notitydata();
    }

    @OnClick(R.id.virtualorder)
    public void virtual() {
        realorder.setActivated(false);
        virtualorder.setActivated(true);
        titles = viltitle;
        ordertype = viltype;
        select = true;
        url = Constants.URL_MEMBER_VR_ORDER;
        notitydata();
    }

    @OnClick(R.id.searchorder)
    public void searchorder() {
        notitydata();
    }


    @Override
    public void onFragmentInteraction(String title, String flag) {

    }


}
