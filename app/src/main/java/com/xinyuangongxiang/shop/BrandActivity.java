package com.xinyuangongxiang.shop;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.GridView;
import android.widget.Toast;

import com.xinyuangongxiang.shop.R;

import com.xinyuangongxiang.shop.adapter.BrandGridViewAdapter;
import com.xinyuangongxiang.shop.bean.BrandInfo;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.MyExceptionHandler;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.ResponseData;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;


public class BrandActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_brand);
        setCommonHeader("推荐品牌");
		MyExceptionHandler.getInstance().setContext(this);
		RemoteDataHandler.asyncDataStringGet(Constants.URL_BRAND, new RemoteDataHandler.Callback() {
			@Override
			public void dataLoaded(ResponseData data) {

				if (data.getCode() == HttpStatus.SC_OK) {

					String json = data.getJson();

					try {

						JSONObject obj = new JSONObject(json);
						String brandList = obj.getString("brand_list");
						if (brandList != "" && brandList != null && !brandList.equals("[]")) {
							ArrayList<BrandInfo> brandArray = BrandInfo.newInstanceList(brandList);
							BrandGridViewAdapter brandGridViewAdapter = new BrandGridViewAdapter(BrandActivity.this);
							brandGridViewAdapter.setBrandArray(brandArray);
							GridView gvBrand = (GridView)findViewById(R.id.gvBrand);
							gvBrand.setAdapter(brandGridViewAdapter);
						}
					} catch (JSONException e) {
						e.printStackTrace();
					}
				} else {
					Toast.makeText(BrandActivity.this, getResources().getString(R.string.load_error), Toast.LENGTH_SHORT).show();
				}
			}
		});
	}

		@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_brand, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
