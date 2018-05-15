package com.xinyuangongxiang.shop.ui.type;

import android.app.Activity;
import android.support.v4.app.Fragment;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.adapter.GoodsEvaluateDetailListViewAdapter;
import com.xinyuangongxiang.shop.bean.GoodsEvaluateInfo;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.MyExceptionHandler;
import com.xinyuangongxiang.shop.common.ShopHelper;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.ResponseData;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/**
 * 商品详细评价Fragment
 */
public class GoodsDetailEvaluateFragment extends Fragment {
    private static final String ARG_GOODS_ID = "goods_id";
    private static final int EVALUATE_TYPE_ALL = 0;
    private static final int EVALUATE_TYPE_GOOD = 1;
    private static final int EVALUATE_TYPE_NORMAL = 2;
    private static final int EVALUATE_TYPE_BAD = 3;
    private static final int EVALUATE_TYPE_IMAGE = 4;
    private static final int EVALUATE_TYPE_AGAIN = 5;

    private String goodsId;
    ArrayList<GoodsEvaluateInfo> goodsEvaluateInfoList = new ArrayList<GoodsEvaluateInfo>();
    private String currentType;
    private int currentPage;
    boolean isHasMore = true;
    boolean isLastRow = false;

    private Button btnEvaluateAll;
    private Button btnEvaluateGood;
    private Button btnEvaluateNormal;
    private Button btnEvaluateBad;
    private Button btnEvaluateImage;
    private Button btnEvaluateAgain;

    private ListView lvEvaluateList;
    private GoodsEvaluateDetailListViewAdapter goodsEvaluateDetailListViewAdapter;

    //空列表
    private LinearLayout llListEmpty;

    private OnFragmentInteractionListener mListener;

    public static GoodsDetailEvaluateFragment newInstance(String goodsId) {
        GoodsDetailEvaluateFragment fragment = new GoodsDetailEvaluateFragment();
        Bundle args = new Bundle();
        args.putString(ARG_GOODS_ID, goodsId);
        fragment.setArguments(args);
        return fragment;
    }

    public GoodsDetailEvaluateFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            goodsId = getArguments().getString(ARG_GOODS_ID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_goods_detail_evaluate, container, false);
        MyExceptionHandler.getInstance().setContext(getActivity());
        btnEvaluateAll = (Button) layout.findViewById(R.id.btnEvaluateAll);
        btnEvaluateAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeType(EVALUATE_TYPE_ALL);
            }
        });
        btnEvaluateGood = (Button) layout.findViewById(R.id.btnEvaluateGood);
        btnEvaluateGood.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeType(EVALUATE_TYPE_GOOD);
            }
        });
        btnEvaluateNormal = (Button) layout.findViewById(R.id.btnEvaluateNormal);
        btnEvaluateNormal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeType(EVALUATE_TYPE_NORMAL);
            }
        });
        btnEvaluateBad = (Button) layout.findViewById(R.id.btnEvaluateBad);
        btnEvaluateBad.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeType(EVALUATE_TYPE_BAD);
            }
        });
        btnEvaluateImage = (Button) layout.findViewById(R.id.btnEvaluateImage);
        btnEvaluateImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeType(EVALUATE_TYPE_IMAGE);
            }
        });
        btnEvaluateAgain = (Button) layout.findViewById(R.id.btnEvaluateAgain);
        btnEvaluateAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changeType(EVALUATE_TYPE_AGAIN);
            }
        });

        lvEvaluateList = (ListView) layout.findViewById(R.id.lvEvaluateList);
        lvEvaluateList.setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int i) {
                if (isHasMore && isLastRow && i == AbsListView.OnScrollListener.SCROLL_STATE_IDLE) {
                    isLastRow = false;
                    currentPage += 1;
                    loadingGoodsEvaluate();
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
                //判断是否滚到最后一行
                if (firstVisibleItem + visibleItemCount == totalItemCount && totalItemCount > 0) {
                    isLastRow = true;
                }
            }
        });
        goodsEvaluateDetailListViewAdapter = new GoodsEvaluateDetailListViewAdapter(getActivity());
        lvEvaluateList.setAdapter(goodsEvaluateDetailListViewAdapter);

        //空列表
        llListEmpty = (LinearLayout) layout.findViewById(R.id.llListEmpty);
        ImageView ivListEmpty = (ImageView) layout.findViewById(R.id.ivListEmpty);
        ivListEmpty.setImageResource(R.drawable.nc_icon_eval);
        TextView tvListEmptyTitle = (TextView) layout.findViewById(R.id.tvListEmptyTitle);
        TextView tvListEmptySubTitle = (TextView) layout.findViewById(R.id.tvListEmptySubTitle);
        tvListEmptyTitle.setText("该商品未收到任何评价");
        tvListEmptySubTitle.setText("期待您的购买与评论！");

        changeType(EVALUATE_TYPE_ALL);
        loadingGoodsEvaluate();
        return layout;
    }

    /**
     * 初始化加载数据
     */
    public void loadingGoodsEvaluate() {
        String url = Constants.URL_GOODS_EVALUATE + "&goods_id=" + goodsId + "&type=" + currentType + "&curpage=" + String.valueOf(currentPage) + "&page=" + Constants.PAGESIZE;

        RemoteDataHandler.asyncDataStringGet(url, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    if (data.isHasMore()) {
                        isHasMore = true;
                    } else {
                        isHasMore = false;
                    }
                    if (currentPage == 1) {
                        goodsEvaluateInfoList.clear();
                        lvEvaluateList.smoothScrollToPosition(0);
                    }
                    try {
                        JSONObject jsonObject = new JSONObject(json);
                        goodsEvaluateInfoList.addAll(GoodsEvaluateInfo.newInstanceList(jsonObject.getString("goods_eval_list")));
                        if (goodsEvaluateInfoList.size() > 0) {
                            llListEmpty.setVisibility(View.GONE);
                            goodsEvaluateDetailListViewAdapter.setList(goodsEvaluateInfoList);
                            goodsEvaluateDetailListViewAdapter.notifyDataSetChanged();
                        } else {
                            llListEmpty.setVisibility(View.VISIBLE);
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    ShopHelper.showApiError(getActivity(), json);
                }
            }
        });
    }

    /**
     * 设置按钮状态
     *
     * @param type
     */
    private void changeType(int type) {
        currentType = String.valueOf(type);
        currentPage = 1;

        btnEvaluateAll.setActivated(false);
        btnEvaluateGood.setActivated(false);
        btnEvaluateNormal.setActivated(false);
        btnEvaluateBad.setActivated(false);
        btnEvaluateImage.setActivated(false);
        btnEvaluateAgain.setActivated(false);

        switch (type) {
            case EVALUATE_TYPE_ALL:
                btnEvaluateAll.setActivated(true);
                loadingGoodsEvaluate();
                break;
            case EVALUATE_TYPE_GOOD:
                btnEvaluateGood.setActivated(true);
                loadingGoodsEvaluate();
                break;
            case EVALUATE_TYPE_NORMAL:
                btnEvaluateNormal.setActivated(true);
                loadingGoodsEvaluate();
                break;
            case EVALUATE_TYPE_BAD:
                btnEvaluateBad.setActivated(true);
                loadingGoodsEvaluate();
                break;
            case EVALUATE_TYPE_IMAGE:
                btnEvaluateImage.setActivated(true);
                loadingGoodsEvaluate();
                break;
            case EVALUATE_TYPE_AGAIN:
                btnEvaluateAgain.setActivated(true);
                loadingGoodsEvaluate();
                break;
        }
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
//        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction(Uri uri);
    }

}
