package org.anyrtc.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.gyf.barlibrary.ImmersionBar;

/**
 * Created by Skyline on 2016/5/24.
 */
public abstract class BaseActivity extends AppCompatActivity {
    protected ImmersionBar mImmersionBar;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(this.getLayoutId());
        mImmersionBar = ImmersionBar.with(this);
        mImmersionBar.init();
        this.initView(savedInstanceState);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mImmersionBar != null)
            mImmersionBar.destroy();
    }


    @Override
    public void setContentView(int layoutResID) {
        super.setContentView(layoutResID);
    }


    public void startAnimActivity(Class<?> cls) {
        startActivity(new Intent(this, cls));
    }

    public void finishAnimActivity() {
        finish();
    }

    public void startAnimActivity(Class<?> cls, String key,boolean value) {
        Intent intent = new Intent(this, cls);
        intent.putExtra(key,value);
        startActivity(intent);
    }

    public abstract int getLayoutId();

    public abstract void initView(Bundle savedInstanceState);

}
