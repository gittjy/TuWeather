package com.android.tu.tuweather;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.Toast;

import com.android.tu.tuweather.service.AutoUpdateService;
import com.kyleduo.switchbutton.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tjy on 2017/3/14.
 */
public class SettingActivity extends AppCompatActivity{

    @BindView(R.id.set_toolbar)
    Toolbar setToolbar;
    @BindView(R.id.switch_btn)
    SwitchButton switchBtn;

    private Context mContext;



    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.setting_layout);
        ButterKnife.bind(this);
        mContext=this;
        setSupportActionBar(setToolbar);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Intent intent=new Intent(mContext, AutoUpdateService.class);
                if(b){
                    startService(intent);
                    makeToast("已开启数据自动更新");
                }else{
                    stopService(intent);
                    makeToast("已关闭数据自动更新");
                }
            }
        });
    }

    private void makeToast(String msg) {
        Toast.makeText(mContext,msg,Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                overridePendingTransition(R.anim.in_left,R.anim.out_right);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
