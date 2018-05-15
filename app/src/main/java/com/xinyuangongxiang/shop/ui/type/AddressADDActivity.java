package com.xinyuangongxiang.shop.ui.type;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.xinyuangongxiang.shop.BaseActivity;
import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.bean.AddressDetails;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.MyExceptionHandler;
import com.xinyuangongxiang.shop.common.MyShopApplication;
import com.xinyuangongxiang.shop.common.ShopHelper;
import com.xinyuangongxiang.shop.custom.NCAddressDialog;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.RemoteDataHandler.Callback;
import com.xinyuangongxiang.shop.http.ResponseData;
import com.xinyuangongxiang.shop.ncinterface.INCOnAddressDialogConfirm;
import com.xinyuangongxiang.shop.newpackage.ProgressDialog;

import org.apache.http.HttpStatus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * 新增收货地址
 *
 * @author dqw
 * @Time 2015/7/16
 */
public class AddressADDActivity extends BaseActivity {

    private MyShopApplication myApplication;
    private NCAddressDialog myAddressDialog;
    private String addressId;
    private String city_id = "-1";
    private String area_id = "-1";
    private TextView editAddressInfo;
    private EditText editAddressName, editJieDaoAddress, editAddressMobPhone;
    private ImageButton btnDefaultAddress;
    private Button btnAdd;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.address_add_view);
        MyExceptionHandler.getInstance().setContext(this);
        myApplication = (MyShopApplication) getApplicationContext();

        editAddressInfo = (TextView) findViewById(R.id.editAddressInfo);
        editAddressName = (EditText) findViewById(R.id.editAddressName);
        editJieDaoAddress = (EditText) findViewById(R.id.editJieDaoAddress);
        editAddressMobPhone = (EditText) findViewById(R.id.editAddressMobPhone);
        setTextChange();

        //默认收货地址
        btnDefaultAddress = (ImageButton) findViewById(R.id.btnDefaultAddress);
        btnDefaultAddress.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                if (btnDefaultAddress.isSelected()) {
                    btnDefaultAddress.setSelected(false);
                } else {
                    btnDefaultAddress.setSelected(true);
                }
            }
        });

        //提交
        btnAdd = (Button) findViewById(R.id.btnAdd);
        btnAdd.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                saveAddress();
            }
        });

        //地区选择对话框
        myAddressDialog = new NCAddressDialog(AddressADDActivity.this);
        editAddressInfo.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                myAddressDialog.show();
            }
        });
        myAddressDialog.setOnAddressDialogConfirm(new INCOnAddressDialogConfirm() {
            @Override
            public void onAddressDialogConfirm(String cityId, String areaId, String ThreeareaId, String areaInfo) {
                city_id = areaId;
                area_id = ThreeareaId;
                editAddressInfo.setText(areaInfo);
            }
        });

        addressId = getIntent().getStringExtra("address_id");
        if (addressId != null && !addressId.equals("")) {
            //编辑收货地址
            setCommonHeader("编辑收货地址");
            loadAddressDetail();
        } else {
            //新增收货地址
            setCommonHeader("新增收货地址");
        }
    }

    /**
     * 设置输入框变动事件
     */
    private void setTextChange() {
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                setBtnAddState();
            }
        };
        editAddressInfo.addTextChangedListener(textWatcher);
        editAddressName.addTextChangedListener(textWatcher);
        editJieDaoAddress.addTextChangedListener(textWatcher);
        editAddressMobPhone.addTextChangedListener(textWatcher);
    }

    /**
     * 设置按钮提交按钮状态
     */
    private void setBtnAddState() {
        String true_name = editAddressName.getText().toString();//姓名
        String area_info = editAddressInfo.getText().toString();//地区信息，例：天津 天津市 红桥区
        String address = editJieDaoAddress.getText().toString();//地址信息，例：水游城8层
        String mob_phone = editAddressMobPhone.getText().toString();//手机
        if (true_name.equals("") || area_info.equals("") || address.equals("") || mob_phone.equals("")) {
            btnAdd.setActivated(false);
        } else {
            btnAdd.setActivated(true);
        }
    }

    /**
     * 保存收货地址
     */
    private void saveAddress() {
        if (!btnAdd.isActivated()) {
            return;
        }
        String true_name = editAddressName.getText().toString();//姓名
        String area_info = editAddressInfo.getText().toString();//地区信息，例：天津 天津市 红桥区
        String address = editJieDaoAddress.getText().toString();//地址信息，例：水游城8层
        String mob_phone = editAddressMobPhone.getText().toString();//手机
        String is_default = "0";
        if (btnDefaultAddress.isSelected()) {
            is_default = "1";
        }

        if (city_id.equals("-1") || city_id.equals("") || city_id.equals("null") || city_id == null) {
            ShopHelper.showMessage(AddressADDActivity.this, "请选择地区");
            return;
        }
        if (address.equals("") || address.equals("null") || address == null) {
            ShopHelper.showMessage(AddressADDActivity.this, "详细地址不能为空");
            return;
        }
        if (true_name.equals("") || true_name.equals("null") || true_name == null) {
            ShopHelper.showMessage(AddressADDActivity.this, "收货人不能为空");
            return;
        }
        if ((mob_phone.equals("") || mob_phone.equals("null") || mob_phone == null || mob_phone.length() != 11)) {
            ShopHelper.showMessage(AddressADDActivity.this, "请正确填写联系手机");
            return;
        }

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("true_name", true_name);
        params.put("area_id", area_id);
        params.put("city_id", city_id);
        params.put("area_info", area_info);
        params.put("address", address);
        params.put("mob_phone", mob_phone);
        params.put("is_default", is_default);

        if (addressId == null || addressId.equals("")) {
            addAddress(params);
        } else {
            editAddress(params);
        }
    }

    /**
     * 添加收货地址
     */
    private void addAddress(HashMap<String, String> params) {
        String url = Constants.URL_ADDRESS_ADD;
        Dialog dialog = ProgressDialog.showLoadingProgress(this, "正在添加...");
        dialog.show();
        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                ProgressDialog.dismissDialog(dialog);
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    if (json.contains("address_id")) {
                        Toast.makeText(AddressADDActivity.this, "保存成功", Toast.LENGTH_SHORT).show();

                        Intent mIntent = getIntent();
                        setResult(Constants.ADD_ADDRESS_SUCC, mIntent);
                        AddressADDActivity.this.finish();
                    }

                } else {
                    ShopHelper.showApiError(AddressADDActivity.this, json);
                }

            }
        });
    }

    /**
     * 编辑收货地址
     */
    private void editAddress(HashMap<String, String> params) {
        String url = Constants.URL_ADDRESS_EDIT;
        params.put("address_id", addressId);
        Dialog dialog = ProgressDialog.showLoadingProgress(this, "正在修改...");
        dialog.show();
        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                ProgressDialog.dismissDialog(dialog);
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    if (json.equals("1")) {
                        Toast.makeText(AddressADDActivity.this, "保存成功", Toast.LENGTH_SHORT).show();
                        Intent mIntent = new Intent(AddressADDActivity.this, AddressListActivity.class);
                        setResult(Constants.ADD_ADDRESS_SUCC, mIntent);
                        finish();
                    }
                } else {
                    ShopHelper.showApiError(AddressADDActivity.this, json);
                }
            }
        });
    }

    /**
     * 加载收货地址详细信息
     */
    public void loadAddressDetail() {
        String url = Constants.URL_ADDRESS_DETAILS;
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("address_id", addressId);
        RemoteDataHandler.asyncLoginPostDataString(url, params, myApplication, new Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    try {
                        JSONObject obj = new JSONObject(json);
                        String objJson = obj.getString("address_info");
                        AddressDetails addressDetails = AddressDetails.newInstanceDetails(objJson);

                        if (addressDetails != null) {
                            city_id = addressDetails.getCity_id() == null ? "" : addressDetails.getCity_id();
                            area_id = addressDetails.getArea_id() == null ? "" : addressDetails.getArea_id();
                            editAddressName.setText(addressDetails.getTrue_name() == null ? "" : addressDetails.getTrue_name());
                            editAddressInfo.setText(addressDetails.getArea_info() == null ? "" : addressDetails.getArea_info());
                            editAddressMobPhone.setText(addressDetails.getMob_phone() == null ? "" : addressDetails.getMob_phone());
                            editJieDaoAddress.setText(addressDetails.getAddress() == null ? "" : addressDetails.getAddress());
                            String isDefault = addressDetails.getIs_default();
                            if (isDefault.equals("1")) {
                                btnDefaultAddress.setSelected(true);
                            } else {
                                btnDefaultAddress.setSelected(false);
                            }
                        }

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }

                } else {
                    ShopHelper.showApiError(AddressADDActivity.this, json);
                }
            }
        });
    }
}
