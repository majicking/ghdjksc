package com.guohanhealth.shop.ui.type;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.guohanhealth.shop.BaseActivity;
import com.guohanhealth.shop.R;
import com.guohanhealth.shop.adapter.StoreO2oAddressListViewAdapter;
import com.guohanhealth.shop.bean.GpsInfo;
import com.guohanhealth.shop.bean.StoreO2oAddressInfo;
import com.guohanhealth.shop.common.MyExceptionHandler;

import java.util.ArrayList;

public class StoreO2oAddressActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_o2o_address);
        MyExceptionHandler.getInstance().setContext(this);
        setCommonHeader("商家信息");

        TextView tvAllStore = (TextView) findViewById(R.id.tvAllStore);
        ListView lvStoreList = (ListView) findViewById(R.id.lvStoreList);

        String addressString = getIntent().getStringExtra("address");
        final ArrayList<StoreO2oAddressInfo> storeO2oAddressList = StoreO2oAddressInfo.newInstanceList(addressString);

        tvAllStore.setText("全部实体分店共" + String.valueOf(storeO2oAddressList.size()) + "家");
        tvAllStore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(StoreO2oAddressActivity.this, BaiduMapActivity.class);
                ArrayList<GpsInfo> gpsList = new ArrayList<GpsInfo>();
                for (int i = 0; i < storeO2oAddressList.size(); i++) {
                    StoreO2oAddressInfo bean = storeO2oAddressList.get(i);
                    double lng = Double.valueOf(bean.getLng());
                    double lat = Double.valueOf(bean.getLat());
                    gpsList.add(new GpsInfo(lng, lat));
                }
                intent.putParcelableArrayListExtra("gps_list", gpsList);
                startActivity(intent);
            }
        });

        StoreO2oAddressListViewAdapter adapter = new StoreO2oAddressListViewAdapter(StoreO2oAddressActivity.this);
        adapter.setlist(storeO2oAddressList);
        lvStoreList.setAdapter(adapter);
    }

}
