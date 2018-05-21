package com.guohanhealth.shop.newpackage;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.ResponseData;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.guohanhealth.shop.common.Constants.ORDERNUMBER;

public class PredepositActivity extends FragmentActivity implements OnFragmentInteractionListener {

    @BindView(R.id.tvPredeposit)
    TextView tvPredeposit;
    @BindView(R.id.tab)
    TabLayout tab;
    @BindView(R.id.viewpager)
    ViewPager viewpager;
    @BindView(R.id.btnBack)
    ImageButton btnBack;
    @BindView(R.id.tvCommonTitle)
    TextView tvCommonTitle;
    @BindView(R.id.tvCommonTitleBorder)
    TextView tvCommonTitleBorder;
    private ViewPagerAdapter adapter;
    private int mCurrentPosition;//当前页码

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_predeposit2);
        ButterKnife.bind(this);
        initData();
        initView();
    }

    private int num = 0;//选中默认项
    private List<Fragment> fragmentlist;
    private String[] titles = {"账户充值", "账户余额", "充值明细", "提现明细", "余额提现"};


    private void initView() {

        fragmentlist = new ArrayList<>();
        fragmentlist.add(PredAddFragment.newInstance("", ""));
        fragmentlist.add(PredOtherFragment.newInstance("1", Constants.URL_MEMBER_FUND_PREDEPOSITLOG));
        fragmentlist.add(PredOtherFragment.newInstance("2", Constants.URL_MEMBER_FUND_PDRECHARGELIST));
        fragmentlist.add(PredOtherFragment.newInstance("3", Constants.URL_MEMBER_FUND_PDCASHLIST));
        fragmentlist.add(PredPutforwardFragment.newInstance("", ""));
        viewpager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tab));
        adapter = new ViewPagerAdapter(this, getSupportFragmentManager(), fragmentlist, titles);
        viewpager.setAdapter(adapter);
        tab.setupWithViewPager(viewpager);
    }

    private void initData() {
        btnBack.setOnClickListener(v -> finish());
        tvCommonTitle.setText("预存款");
        num = getIntent().getIntExtra(ORDERNUMBER, 0);
        viewpager.setCurrentItem(num);
        viewpager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentPosition = position;
                tvCommonTitle.setText(titles[position]);
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPredeposit();
    }
    public String  predepoit="0.00";
    /**
     * 读取预存款
     */
    public void loadPredeposit() {
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", MyShopApplication.getInstance().getLoginKey());
        RemoteDataHandler.asyncLoginPostDataString(Constants.URL_MEMBER_MY_ASSET + "&fields=predepoit", params, MyShopApplication.getInstance(), data -> {
            String json = data.getJson();
            if (data.getCode() == HttpStatus.SC_OK) {
                try {
                    JSONObject obj = new JSONObject(json);
                    predepoit=obj.optString("predepoit");
                    tvPredeposit.setText("¥" + obj.optString("predepoit"));
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                ShopHelper.showApiError(this, json);
            }
        });
    }

    @Override
    public void onFragmentInteraction(String key, int value) {
        if (key.equals("success"))
            viewpager.setCurrentItem(value);
        else if (key.equals("error"))
            viewpager.setCurrentItem(value);
        loadPredeposit();
    }

}
