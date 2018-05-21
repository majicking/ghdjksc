package com.guohanhealth.shop.ui.type;


import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.RadioButton;

import com.guohanhealth.shop.R;
import com.guohanhealth.shop.adapter.VpAdapter;
import com.guohanhealth.shop.common.DialogHelper;
import com.guohanhealth.shop.common.MyExceptionHandler;

import java.util.ArrayList;


/**
 * 商品详细页Activity
 *
 * @author dqw
 * @Time 2015-7-14
 */
public class GoodsDetailsActivity extends FragmentActivity implements GoodsDetailFragment.OnFragmentInteractionListener, GoodsDetailBodyFragment.OnFragmentInteractionListener, GoodsDetailEvaluateFragment.OnFragmentInteractionListener, View.OnClickListener {

    FragmentManager fragmentManager = getSupportFragmentManager();

    private String goods_id;
    private RadioButton btnGoodsDetail, btnGoodsBody, btnGoodsEvaluate;
    private GoodsDetailFragment goodsDetailFragment;
    private GoodsDetailBodyFragment goodsDetailBodyFragment;
    private GoodsDetailEvaluateFragment goodsDetailEvaluateFragment;
    private ViewPager vp;
    ArrayList<Fragment> list = new ArrayList<Fragment>();
    int currfragment = 0;
    public ImageView moremenu;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.goods_details_view);
        MyExceptionHandler.getInstance().setContext(this);
        goods_id = GoodsDetailsActivity.this.getIntent().getStringExtra("goods_id");
        btnGoodsDetail = (RadioButton) findViewById(R.id.btnGoodsDetail);
        btnGoodsBody = (RadioButton) findViewById(R.id.btnGoodsBody);
        btnGoodsEvaluate = (RadioButton) findViewById(R.id.btnGoodsEvaluate);
        moremenu = (ImageView) findViewById(R.id.moremenu);
        vp = (ViewPager) findViewById(R.id.main_viewpager);
        goodsDetailFragment = GoodsDetailFragment.newInstance(goods_id);
        goodsDetailBodyFragment = GoodsDetailBodyFragment.newInstance(goods_id);
        goodsDetailEvaluateFragment = GoodsDetailEvaluateFragment.newInstance(goods_id);
        list.add(goodsDetailFragment);
        list.add(goodsDetailBodyFragment);
        list.add(goodsDetailEvaluateFragment);
        VpAdapter vpadapter = new VpAdapter(fragmentManager);
        vpadapter.setList(list);
        vp.setAdapter(vpadapter);
        vp.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i2) {

            }

            @Override
            public void onPageSelected(int position) {
                currfragment = position;
                ChangeGoodsBackgroud(position);
            }

            @Override
            public void onPageScrollStateChanged(int position) {

            }
        });
        ChangeGoodsBackgroud(0);
        moremenu.setOnClickListener(this);
        initPopupWindow();
    }

    public void ChangeGoodsBackgroud(int poistion) {
        switch (poistion) {
            case 0:
                btnGoodsDetail.setChecked(true);
                btnGoodsBody.setChecked(false);
                btnGoodsEvaluate.setChecked(false);
                break;
            case 1:
                btnGoodsDetail.setChecked(false);
                btnGoodsBody.setChecked(true);
                btnGoodsEvaluate.setChecked(false);
                break;
            case 2:
                btnGoodsDetail.setChecked(false);
                btnGoodsBody.setChecked(false);
                btnGoodsEvaluate.setChecked(true);
                break;

        }
    }

    /**
     * 更换新商品
     */
    public void changeGoods(String goodsId) {
        goods_id = goodsId;
    }

    public void changeFreagemt(int num) {
        vp.setCurrentItem(num);
    }

    /**
     * 返回按钮点击
     */
    public void btnBackClick(View view) {
        finish();
    }

    //    更多加载
    private PopupWindow popupWindow;

    /**
     * 显示popupwindow
     */
    private void showPopupWindow() {
        if (!popupWindow.isShowing()) {
            popupWindow.showAsDropDown(moremenu, moremenu.getLayoutParams().width / 2, 0);
        } else {
            popupWindow.dismiss();
        }
    }

    /**
     * 初始化popupwindow
     */
    private void initPopupWindow() {
        popupWindow = DialogHelper.initPopupWindow(this);
    }

    /**
     * 商品详细按钮点击
     */
    public void btnGoodsDetailClick(View view) {
        changeFreagemt(0);
    }

    /**
     * 商品描述按钮点击
     */
    public void btnGoodsBodyClick(View view) {
        changeFreagemt(1);
    }

    /**
     * 商品评价按钮点击
     */
    public void btnGoodsEvaluateClick(View view) {
        changeFreagemt(2);
    }


    @Override
    public void onFragmentInteraction(Uri uri) {

    }



    @Override
    public void onClick(View view) {
            switch (view.getId()) {
                case R.id.moremenu:
                    showPopupWindow();
                    break;
            }
    }
}
