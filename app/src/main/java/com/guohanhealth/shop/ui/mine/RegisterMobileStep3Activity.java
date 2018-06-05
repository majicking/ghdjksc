package com.guohanhealth.shop.ui.mine;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.guohanhealth.shop.BaseActivity;
import com.guohanhealth.shop.R;
import com.guohanhealth.shop.bean.Login;
import com.guohanhealth.shop.common.Constants;
import com.guohanhealth.shop.common.MyExceptionHandler;
import com.guohanhealth.shop.common.MyShopApplication;
import com.guohanhealth.shop.common.ShopHelper;
import com.guohanhealth.shop.http.RemoteDataHandler;
import com.guohanhealth.shop.http.ResponseData;

import org.apache.http.HttpStatus;

import java.util.HashMap;

public class RegisterMobileStep3Activity extends BaseActivity {

    private MyShopApplication myApplication;
    private EditText etPassword, username, checkcode, pwd2;
    private ImageButton btnShowPassowrd;
    private Button btnRegSubmit;

    private String phone;
    private String captcha;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_mobile_step3);
        MyExceptionHandler.getInstance().setContext(this);
        phone = getIntent().getStringExtra("phone");
        captcha = getIntent().getStringExtra("captcha");

        myApplication = (MyShopApplication) getApplicationContext();
        etPassword = (EditText) findViewById(R.id.etPassword);
        btnShowPassowrd = (ImageButton) findViewById(R.id.btnShowPassword);
        btnRegSubmit = (Button) findViewById(R.id.btnRegSubmit);
        username = (EditText) findViewById(R.id.edusername);
        checkcode = (EditText) findViewById(R.id.etcode);
        pwd2 = (EditText) findViewById(R.id.etPassword1);
        etPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if (etPassword.getText().toString().length() > 0&&pwd2.getText().toString().length()>0) {
                    btnRegSubmit.setActivated(true);
                } else {
                    btnRegSubmit.setActivated(false);
                }
            }
        });

        setCommonHeader("设置密码");
    }

    /**
     * 显示密码按钮点击
     */
    public void btnShowPasswordClick(View v) {
        if (btnShowPassowrd.isSelected()) {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            pwd2.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
            btnShowPassowrd.setSelected(false);
        } else {
            etPassword.setInputType(InputType.TYPE_CLASS_TEXT);
            pwd2.setInputType(InputType.TYPE_CLASS_TEXT);
            btnShowPassowrd.setSelected(true);
        }
    }

    /**
     * 注册按钮点击
     */
    public void btnRegSubmitClick(View v) {
        if (btnRegSubmit.isActivated()) {
            String password = etPassword.getText().toString();
            String password1 = pwd2.getText().toString();
            String name = username.getText().toString();
            String code = checkcode.getText().toString();
            int length = password.length();
            int length1 = password1.length();
            int length2 = name.length();
            if (length < 6 || length > 20) {
                ShopHelper.showMessage(RegisterMobileStep3Activity.this, "请输入6-20位密码");
                return;
            }
            if (length1 < 6 || length1 > 20) {
                ShopHelper.showMessage(RegisterMobileStep3Activity.this, "请输入6-20位密码");
                return;
            }
            if (length2 < 6 || length2 > 20) {
                ShopHelper.showMessage(RegisterMobileStep3Activity.this, "请输入6-20用户名");
                return;
            }

            HashMap<String, String> params = new HashMap<String, String>();
            params.put("referral_code", TextUtils.isEmpty(code) ? "" : code);
            params.put("username", name);
            params.put("phone", phone);
            params.put("captcha", captcha);
            params.put("password", password);
            params.put("client", "android");
            RemoteDataHandler.asyncPostDataString(Constants.URL_CONNECT_SMS_REGISTER, params, new RemoteDataHandler.Callback() {
                @Override
                public void dataLoaded(ResponseData data) {
                    String json = data.getJson();
                    if (data.getCode() == HttpStatus.SC_OK) {
                        Login login = Login.newInstanceList(json);
                        myApplication.setLoginKey(login.getKey());
                        myApplication.setUserName(login.getUsername());
                        myApplication.setMemberID(login.getUserid());

                        myApplication.loadingUserInfo(login.getKey(), login.getUserid());

                        myApplication.getmSocket().connect();
                        myApplication.UpDateUser();

                        Intent mIntent = new Intent(Constants.LOGIN_SUCCESS_URL);
                        sendBroadcast(mIntent);
                        RegisterMobileStep3Activity.this.finish();
                    } else {
                        ShopHelper.showApiError(RegisterMobileStep3Activity.this, json);
                    }
                }
            });

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_register_mobile_step3, menu);
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
