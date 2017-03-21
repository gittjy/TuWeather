package com.android.tu.tuweather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tjy on 2017/3/13.
 */
public class AboutUsActivity extends AppCompatActivity{

    @BindView(R.id.about_us_toolbar)
    Toolbar mToolbar;
    @BindView(R.id.collpasing_toolbar)
    CollapsingToolbarLayout collpasingToolbar;
    @BindView(R.id.about_text)
    TextView aboutText;
    @BindView(R.id.about_image)
    ImageView aboutImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us_layout);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
        ActionBar actionBar=getSupportActionBar();
        if(actionBar!=null){
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        collpasingToolbar.setTitle("关于我");
        aboutText.setText(generateText());
        Glide.with(this).load(R.mipmap.wallhaven).into(aboutImage);
    }

    private String generateText() {
        StringBuilder text=new StringBuilder();
        for (int i = 0; i <500 ; i++) {
            text.append("abcdefg");
        }
        return text.toString();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
