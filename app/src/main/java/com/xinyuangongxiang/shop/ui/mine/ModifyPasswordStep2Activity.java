package com.xinyuangongxiang.shop.ui.mine;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.xinyuangongxiang.shop.BaseActivity;
import com.xinyuangongxiang.shop.R;
import com.xinyuangongxiang.shop.common.Constants;
import com.xinyuangongxiang.shop.common.MyExceptionHandler;
import com.xinyuangongxiang.shop.common.MyShopApplication;
import com.xinyuangongxiang.shop.common.ShopHelper;
import com.xinyuangongxiang.shop.http.RemoteDataHandler;
import com.xinyuangongxiang.shop.http.ResponseData;

import org.apache.http.HttpStatus;

import java.util.HashMap;

/**
 * 修改登录密码第二步
 *
 * @author dqw
 * @date 2015/9/2
 */
public class ModifyPasswordStep2Activity extends BaseActivity {
    MyShopApplication myApplication;
    EditText etPassword;
    EditText etPassword1;
    Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_modify_password_step2);
        setCommonHeader("修改登录密码");
        myApplication = (MyShopApplication) getApplicationContext();
        MyExceptionHandler.getInstance().setContext(this);
        TextWatcher textWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String password = etPassword.getText().toString();
                String password1 = etPassword1.getText().toString();
                if (password.length() > 0 && password1.length() > 0) {
                    btnSubmit.setActivated(true);
                } else {
                    btnSubmit.setActivated(false);
                }
            }
        };

        etPassword = (EditText) findViewById(R.id.etPassword);
        etPassword.addTextChangedListener(textWatcher);
        etPassword1 = (EditText) findViewById(R.id.etPassword1);
        etPassword1.addTextChangedListener(textWatcher);
        btnSubmit = (Button) findViewById(R.id.btnSubmit);
    }

    public void btnSubmitClick(View view) {
        if(!btnSubmit.isActivated()) {
           return;
        }

        String password = etPassword.getText().toString();
        String password1 = etPassword1.getText().toString();
        if(!password.equals(password1)) {
            ShopHelper.showMessage(ModifyPasswordStep2Activity.this,"两次密码输入不一致");
            return;
        }

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("key", myApplication.getLoginKey());
        params.put("password", password);
        params.put("password1", password1);
        RemoteDataHandler.asyncLoginPostDataString(Constants.URL_MEMBER_ACCOUNT_MODIFY_PASSWORD_STEP5, params, myApplication, new RemoteDataHandler.Callback() {
            @Override
            public void dataLoaded(ResponseData data) {
                String json = data.getJson();
                if (data.getCode() == HttpStatus.SC_OK) {
                    ShopHelper.showMessage(ModifyPasswordStep2Activity.this, "登录密码修改成功");
                    finish();
                } else {
                    ShopHelper.showApiError(ModifyPasswordStep2Activity.this, json);
                }
            }
        });
    }

}
