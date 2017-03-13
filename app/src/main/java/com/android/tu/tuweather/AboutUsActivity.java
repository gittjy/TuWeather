package com.android.tu.tuweather;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by tjy on 2017/3/13.
 */
public class AboutUsActivity extends AppCompatActivity{

    @BindView(R.id.about_us_toolbar)
    Toolbar mToolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_us_layout);
        ButterKnife.bind(this);
        setSupportActionBar(mToolbar);
    }
}
