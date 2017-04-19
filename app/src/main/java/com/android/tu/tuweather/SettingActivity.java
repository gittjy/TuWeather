package com.android.tu.tuweather;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.tu.tuweather.service.AutoUpdateService;
import com.kyleduo.switchbutton.SwitchButton;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import es.dmoral.toasty.Toasty;

/**
 * Created by tjy on 2017/3/14.
 */
public class SettingActivity extends AppCompatActivity implements View.OnClickListener{

    public static final String SETTING="Setting_Activity";

    public static final int CHOOSE_PHOTO=2;

    @BindView(R.id.set_toolbar)
    Toolbar setToolbar;
    @BindView(R.id.switch_btn)
    SwitchButton switchBtn;
    @BindView(R.id.set_bg_layout)
    RelativeLayout setBgLayout;
    @BindView(R.id.bg_resource_text)
    TextView bgResTextView;

    private Context mContext;
    private SharedPreferences prefs;
    private Dialog bgSetDialog;


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
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        Boolean isUpdate= prefs.getBoolean("isAutoUpdate",false);
        switchBtn.setChecked(isUpdate);
        boolean isAlbum=prefs.getBoolean("isAlbum",false);
        if(isAlbum){
            bgResTextView.setText("相册图片");
        } else {
            bgResTextView.setText("系统图片");
        }

        switchBtn.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Intent intent=new Intent(mContext, AutoUpdateService.class);
                if(b){
                    startService(intent);
                    makeSuccessToast("已开启数据自动更新");
                    SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(mContext).edit();
                    editor.putBoolean("isAutoUpdate",true);
                    editor.commit();
                }else{
                    stopService(intent);
                    makecloseToast("已关闭数据自动更新");
                    SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(mContext).edit();
                    editor.putBoolean("isAutoUpdate",false);
                    editor.commit();
                }
            }
        });
    }

    private void makeSuccessToast(String msg) {
        Toasty.success(mContext,msg,Toast.LENGTH_SHORT).show();
    }

    private void makecloseToast(String msg) {
        Toasty.info(mContext,msg,Toast.LENGTH_SHORT).show();
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

    @OnClick(R.id.set_bg_layout)
    public void layoutOnclik(View view){
        showChooseDialog();
    }

    private void showChooseDialog() {
        bgSetDialog = new Dialog(mContext, R.style.BottomDialogStyle);
        //填充对话框的布局
        View view = LayoutInflater.from(mContext).inflate(R.layout.bg_set_dialog, null);
        //初始化控件
        TextView fromAlbum = (TextView) view.findViewById(R.id.from_album_text);
        TextView fromSystem = (TextView) view.findViewById(R.id.from_system_text);
        fromAlbum.setOnClickListener(SettingActivity.this);
        fromSystem.setOnClickListener(this);
        //将布局设置给Dialog
        bgSetDialog.setContentView(view);
        //获取当前Activity所在的窗体
        Window dialogWindow = bgSetDialog.getWindow();
        //设置Dialog从窗体底部弹出
        dialogWindow.setGravity(Gravity.BOTTOM);
        //获得窗体的属性
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = (int) (getScreenWidth()*0.95);
        lp.y = 20;//设置Dialog距离底部的距离
//       将属性设置给窗体
        dialogWindow.setAttributes(lp);
        bgSetDialog.show();//显示对话框
    }

    @Override
    public void onClick(View view) {

        switch (view.getId()){
            case R.id.from_album_text:
                if(ContextCompat.checkSelfPermission(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(SettingActivity.this,new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},1);
                }else{
                    openAlbum();
                }
                bgSetDialog.dismiss();
                break;
            case R.id.from_system_text:
                SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(mContext).edit();
                editor.putBoolean("isAlbum",false);
                editor.commit();
                bgResTextView.setText("系统图片");
                bgSetDialog.dismiss();
                Toasty.success(mContext,"已设置为系统图片",Toast.LENGTH_SHORT).show();
                break;
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch(requestCode){
            case 1:
                if(grantResults.length>0&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                    openAlbum();
                }else{
                    Toasty.warning(mContext,"未获得授权，无法打卡相册",Toast.LENGTH_SHORT).show();
                }
        }
    }

    /**
     * 打开相册
     */
    private void openAlbum() {
        Intent intent=new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent,CHOOSE_PHOTO);

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch(requestCode){
            case CHOOSE_PHOTO:
                if(resultCode==RESULT_OK){
                    Uri uri=data.getData();
                    SharedPreferences.Editor editor=PreferenceManager.getDefaultSharedPreferences(mContext).edit();
                    editor.putString("imageUri",uri.toString());
                    editor.putBoolean("isAlbum",true);
                    editor.commit();
                    bgResTextView.setText("相册图片");

                }
        }
    }



    public int getScreenWidth(){
        WindowManager windowManager=this.getWindowManager();
        DisplayMetrics displayMetrics=new DisplayMetrics();
        windowManager.getDefaultDisplay().getMetrics(displayMetrics);
        return displayMetrics.widthPixels;
    }
}
