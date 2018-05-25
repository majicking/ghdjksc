package com.guohanhealth.shop.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.guohanhealth.shop.BaseActivity;
import com.guohanhealth.shop.R;
import com.guohanhealth.shop.adapter.FavoritesStoreListViewAdapter;
import com.guohanhealth.shop.bean.FavStoreList;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.MyExceptionHandler;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.custom.NCDialog;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.RemoteDataHandler.Callback;
import com.guohanhealth.shop.http.ResponseData;
import com.guohanhealth.shop.ncinterface.INCOnDialogConfirm;
import com.guohanhealth.shop.ncinterface.INCOnItemDel;
import com.guohanhealth.shop.ui.store.newStoreInFoActivity;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * 收藏店铺列表
 *
 * @author dqw
 * @Time 2015/8/12
 */
public class FavStoreListActivity extends BaseActivity {

    private MyShopApplication myApplication;
    public int pageno = 1;
    private ArrayList<FavStoreList> favoritesLists;
    private FavoritesStoreListViewAdapter adapter;
    private ListView lvFavStore;
    private INCOnItemDel incOnItemDel;
    boolean isHasMore = true;
    boolean isLastRow = false;
    private NCDialog ncDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favstore_list_view);

        myApplication = (MyShopApplication) getApplicationContext();
        MyExceptionHandler.getInstance().setContext(this);
        setTabButton();

        lvFavStore = (ListView) findViewById(R.id.lvFavStore);
        favoritesLists = new ArrayList<FavStoreList>();
        incOnItemDel = new INCOnItemDel() {
            @Override
            public void onDel(String id, int position) {
                deleteFav(id, position);
            }
        };
        adapter = new FavoritesStoreListViewAdapter(FavStoreListActivity.this, incOnItemDel);
        lvFavStore.setAdapter(adapter);
        loadingfavoritesListData();//加载列表数据

        lvFavStore.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                final FavStoreList bean = (FavStoreList) lvFavStore.getItemAtPosition(i);
                Intent intent = new Intent(FavStoreListActivity.this, newStoreInFoActivity.class);
                intent.putExtra("store_id", bean.getStore_id());
                startActivity(intent);
            }
        });

        lvFavStore.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                if (isHasMore && isLastRow && scrollState == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    isLastRow = false;
                    pageno += 1;
                    loadingfavoritesListData();
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                if ((firstVisibleItem + visibleItemCount) == totalItemCount && totalItemCount > 0) {
                    isLastRow = true;
                }
            }
        });

        setCommonHeader("");
        setListEmpty(R.drawable.nc_icon_fav_store, "您还没有关注任何店铺", "可以去看看哪些店铺值得收藏");
    }

    /**
     * 设置头部切换按钮
     */
    private void setTabButton() {
        Button btnFavGoods = (Button) findViewById(R.id.btnFavGoods);
        btnFavGoods.setActivated(false);
        btnFavGoods.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(FavStoreListActivity.this, FavGoodsListActivity.class));
                finish();
            }
        });
        Button btnFavStore = (Button) findViewById(R.id.btnFavStore);
        btnFavStore.setActivated(true);
    }

    /**
     * 加载列表数据
     */
    public void loadingfavoritesListData() {
        String url = Constants.URL_STORE_FAV_LIST + "&curpage=" + pageno + "&page=" + Constants.PAGESIZE;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());

        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {

                    if (!data.isHasMore()) {
                        isHasMore = false;
                    } else {
                        isHasMore = true;
                    }

                    if (pageno == 1) {
                        favoritesLists.clear();
                        hideListEmpty();
                    }

                    try {
                        JSONObject obj = new JSONObject(json);
                        String objJson = obj.getString("favorites_list");
                        ArrayList<FavStoreList> fList = FavStoreList.newInstanceList(objJson);
                        if (fList.size() > 0) {
                            favoritesLists.addAll(fList);
                            adapter.setfList(favoritesLists);
                            adapter.notifyDataSetChanged();
                        } else {
                            showListEmpty();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }catch (Exception e){
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(FavStoreListActivity.this, json);
                }
            }
        });
    }

    /**
     * 删除店铺收藏
     */
    public void deleteFav(final String store_id, final int position) {

        ncDialog = new NCDialog(FavStoreListActivity.this);
        ncDialog.setText1("确认删除该店铺收藏?");
        ncDialog.setOnDialogConfirm(new INCOnDialogConfirm() {
            @Override
            public void onDialogConfirm() {
                String url = Constants.URL_STORE_DELETE;

                HashMap<String, String> params = new HashMap<String, String>();
                params.put("store_id", store_id);
                params.put("key", myApplication.getLoginKey());

                RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new Callback() {
                    @Override
                    public void dataLoaded(ResponseData data) {
                        String json = data.getJson();
                        if (data.getCode() == HttpStatus.SC_OK) {
                            Toast.makeText(FavStoreListActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                            adapter.removeItem(position);
                            adapter.notifyDataSetChanged();
                        } else {
                            ShopHelper.showApiError(FavStoreListActivity.this, json);
                        }
                    }
                });
            }
        });
        ncDialog.showPopupWindow();

    }
}
