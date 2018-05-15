package com.xinyuangongxiang.shop.newpackage;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;

import java.util.List;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    private List<Fragment> fragments = null;
    private Context context;
    private String[] titles;

    public ViewPagerAdapter(Context context, FragmentManager fm, List<Fragment> fragments, String[] titles) {
        super(fm);
        this.context = context;
        this.fragments = fragments;
        this.titles = titles;
        notifyDataSetChanged();
    }

    public ViewPagerAdapter(Context context, FragmentManager fm,String[] titles) {
        super(fm);
        this.context = context;
        this.titles = titles;
        notifyDataSetChanged();
    }

    @Override
    public Fragment getItem(int arg0) {

        return fragments.get(arg0);
    }

    @Override
    public int getItemPosition(Object object) {
// TODO Auto-generated method stub
        return PagerAdapter.POSITION_NONE;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public int getCount() {
        return fragments.size();//hotIssuesList.size();
    }
}
