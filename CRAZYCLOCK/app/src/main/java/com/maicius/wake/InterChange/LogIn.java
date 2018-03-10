package com.maicius.wake.InterChange;
/**
 * Created by Maicius on 2016/6/7.
 */

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.app.Activity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.maicius.wake.DBmanager.AppUser;
import com.maicius.wake.DBmanager.DBManager;
import com.maicius.wake.alarmClock.MainActivity;
import com.maicius.wake.alarmClock.R;
import com.maicius.wake.web.ConnectionDetector;
import com.maicius.wake.web.NetEventActivity;
import com.maicius.wake.web.WebService;

import java.util.StringTokenizer;
import java.util.regex.Pattern;

public class LogIn extends NetEventActivity {
    //创建等待框
    private ProgressDialog dialog;
    //返回的数据
    private String info;
    EditText username, password;
    Button SignIn;
    private boolean netState;
    private static Handler handler = new Handler();
    private TextView netStateView;
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.log_in);
        netStateView = (TextView)findViewById(R.id.Inter_detector);
        TextView Register = (TextView) findViewById(R.id.register);
        SignIn = (Button) findViewById(R.id.signin_button);
        username = (EditText) findViewById(R.id.username_edit);
        password = (EditText) findViewById(R.id.password_edit);
        password.addTextChangedListener(textWatcher);
        SignIn.setEnabled(false);
        netState = this.isNetConnect();
        if(netState){
            netStateView.setVisibility(View.GONE);
        }else{
            netStateView.setVisibility(View.VISIBLE);
        }
        //点击注册
        Register.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Register();
            }
        });

        //点击登录
        SignIn.setOnClickListener(new View.OnClickListener() {

            public void onClick(View V) {
                if(!isUserName(username.getText().toString())){
                    raiseAlertDialog("提示","不能识别的手机号码");
                }
                else if(!netState){
                    raiseAlertDialog("提示","都说了没联网啊");
                }
                else {
                    dialog = new ProgressDialog(LogIn.this);
                    dialog.setTitle("提示");
                    dialog.setMessage("正在登陆，请稍后...");
                    dialog.setCancelable(false);
                    dialog.show();
                    //创建子线程
                    new Thread(new MyThread()).start();
                }
            }
        });
    }
    public void onNetChange(int netState){
        super.onNetChange(netState);
        if(netState == ConnectionDetector.NETWORK_NONE){
            netStateView.setVisibility(View.VISIBLE);
            this.netState = false;
        }else{
            netStateView.setVisibility(View.GONE);
            this.netState = true;
        }
    }
    // 子线程接收数据，主线程修改数据
    public class MyThread implements Runnable {
        @Override
        public void run() {
                info = WebService.executeHttpGet(username.getText().toString(), password.getText().toString(), WebService.State.LogIn);
//                Log.v("sss", "login:" + info);
                Log.i("ReturnData", "返回结果是：" + info);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dialog.dismiss();
                        if (info.equals("failed")) {
                            AlertDialog.Builder alertDialog = new AlertDialog.Builder(LogIn.this);
                            alertDialog.setTitle("登陆信息").setMessage("登陆失败：用户名或密码错误！");
                            alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                            alertDialog.create().show();
                        } else {
                            StringTokenizer st = new StringTokenizer(info, "#");
                            String info_success = st.nextToken();
                            String info_nickname = st.nextToken();
                            if (info_success.equals("success")) {
//                                Log.v("sss", "start AppUser space!");
                            /*更新Mainactivity的用户名和昵称，同时将登陆信息保存到本地Sqlite数据库*/
                                DBManager dbManager = new DBManager(LogIn.this);
                                MainActivity.s_nickname = info_nickname;
                                MainActivity.s_userName = username.getText().toString();
                                AppUser user = new AppUser(
                                        MainActivity.s_userName,
                                        password.getText().toString(),
                                        MainActivity.s_nickname);
                                dbManager.updateAppUser(user);
                                MainActivity.s_isLogged = true;
                                startActivity(new Intent(LogIn.this, UserSpace.class));
                                LogIn.this.finish();
                            }

                        }
                    }
                });
        }
    }

    private void Register() {
        startActivity(new Intent(this, Register.class));
    }

    private TextWatcher textWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
               SignIn.setEnabled(false);
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
              if(s.length() >= 6)
                  SignIn.setEnabled(true);
        }

        @Override
        public void afterTextChanged(Editable s) {
             SignIn.setEnabled(true);
        }
    };
    private boolean isUserName(String username){
        return Pattern.matches("[1][3578]\\d{9}", username);
    }
    private void raiseAlertDialog(String title, String message){
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(LogIn.this);
        alertDialog.setTitle(title).setMessage(message);
        alertDialog.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });
        alertDialog.create().show();
    }
}

