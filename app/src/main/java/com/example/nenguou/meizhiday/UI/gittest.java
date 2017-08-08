package com.example.nenguou.meizhiday.UI;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.nenguou.meizhiday.Bean.GitUserBean;
import com.example.nenguou.meizhiday.R;
import com.example.nenguou.meizhiday.Rx.GetGitInfoUtils;
import com.example.nenguou.meizhiday.Utils.CallTokenBack;

public class gittest extends AppCompatActivity {

    private Button getgithubinfo,GetCode,GetGitInfoBtn,SetGitUser;
    private GetGitInfoUtils getGitInfoUtils = null;
    private TextView github_code,github_token,GitUserInfo;
    public String code,mytoken;
    private GitUserBean gitUserBean;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(R.style.AppTheme_github);
        setContentView(R.layout.activity_gittest);
        initId();
        setListener();
        showGitCode();

    }

    Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    github_code.setText("code:"+code);
                    break;
                case 1:
                    github_token.setText("token:"+mytoken);
            }
        }
    };


    private void showGitCode() {
        SharedPreferences sharedPreferences = getSharedPreferences("gitCode", Context.MODE_PRIVATE);
        code = sharedPreferences.getString("code",null);
        Message message = new Message();
        message.what = 0;
        handler.sendMessage(message);
    }

    private void initId() {
        SetGitUser = (Button) findViewById(R.id.SetGitUser);
        GitUserInfo = (TextView) findViewById(R.id.GitUserInfo);
        GetGitInfoBtn = (Button) findViewById(R.id.GetGitInfoBtn);
        github_token = (TextView) findViewById(R.id.github_token);
        github_code = (TextView) findViewById(R.id.github_code);
        getgithubinfo = (Button) findViewById(R.id.getgithubinfo);
        GetCode = (Button) findViewById(R.id.GetCode);
    }

    private void setListener(){
        getgithubinfo.setOnClickListener(view -> {
            getGitInfoUtils = new GetGitInfoUtils(gittest.this);
            getGitInfoUtils.getGitInfo();
        });

        GetCode.setOnClickListener(view -> {
            getGitInfoUtils = new GetGitInfoUtils(gittest.this,code);
            Log.d("showCode",getGitInfoUtils.showCode().toString());
            try {
                getGitInfoUtils.getGitToken();
            }catch (Exception e){
                Log.d("GetTokenError","ErrorOnToken: " + e);
            }

            /*getGitInfoUtils.setCallBack(token -> {
                mytoken = token;
                //Log.d("hahahahahahhaffha",mytoken);
                Message message = new Message();
                message.what = 1;
                handler.sendMessage(message);
            });*/

            getGitInfoUtils.setCallBack(new CallTokenBack() {
                @Override
                public void callBackToken(String token) {
                    mytoken = token;
                    Message message = new Message();
                    message.what = 1;
                    handler.sendMessage(message);

                }

                @Override
                public void callUserBeanBack(GitUserBean gitUserBean) {

                }
            });
        });

        GetGitInfoBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGitInfoUtils = new GetGitInfoUtils(gittest.this,code);
                getGitInfoUtils.getGitUser(mytoken);
                getGitInfoUtils.setCallBack(new CallTokenBack() {
                    @Override
                    public void callBackToken(String token) {

                    }

                    @Override
                    public void callUserBeanBack(GitUserBean gitUserBean) {
                        gittest.this.gitUserBean = gitUserBean;
                        SharedPreferences sharedPreferences = getSharedPreferences("UseerBean",gitUserBean);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.commit();
                        //Log.d("ggggg",gittest.this.gitUserBean.getEmail());
                    }
                });
            }
        });


        SetGitUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getGitInfoUtils = new GetGitInfoUtils(gittest.this,code);
               //getGitInfoUtils.setCallBackUser();
                getGitInfoUtils.SetGitUser();
            }
        });
    }

}
