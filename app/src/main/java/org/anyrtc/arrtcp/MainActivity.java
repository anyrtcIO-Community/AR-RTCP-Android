package org.anyrtc.arrtcp;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.gyf.barlibrary.ImmersionBar;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.anyrtc.arrtcp.zxing.ScanActivity;
import org.ar.common.utils.SharePrefUtil;
import org.ar.rtcp_kit.ARRtcpEngine;

import java.util.List;

public class MainActivity extends BaseActivity {
    private int mSecretNumber = 0;
    private static final long MIN_CLICK_INTERVAL = 600;
    private long mLastClickTime;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        ImmersionBar.with(this).statusBarDarkFont(true,0.2f).init();


        findViewById(R.id.tv_open_live).setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v){

                if (AndPermission.hasPermissions(MainActivity.this, Permission.CAMERA, Permission.RECORD_AUDIO)) {
                    startAnimActivity(LiveScreenActivity.class, "isPublish", true);
                } else {
                    AndPermission.with(MainActivity.this).runtime().permission(Permission.CAMERA, Permission.RECORD_AUDIO).onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            startAnimActivity(LiveScreenActivity.class, "isPublish", true);
                        }
                    }).onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            Toast.makeText(MainActivity.this, "请打开音视频权限", Toast.LENGTH_SHORT).show();
                        }
                    }).start();
                }
            }
        });

        findViewById(R.id.tv_watch_live).setOnClickListener(new PerfectClickListener() {
            @Override
            protected void onNoDoubleClick(View v) {
                if (AndPermission.hasPermissions(MainActivity.this, Permission.CAMERA, Permission.RECORD_AUDIO)) {
                    Intent i = new Intent(MainActivity.this, ScanActivity.class);
                    i.putExtra("isFirstScan", true);
                    startActivity(i);
                }else {
                    AndPermission.with(MainActivity.this).runtime().permission(Permission.CAMERA, Permission.RECORD_AUDIO).onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            Intent i = new Intent(MainActivity.this, ScanActivity.class);

                            i.putExtra("isFirstScan", true);
                            startActivity(i);
                        }
                    }).onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            Toast.makeText(MainActivity.this, "请打开音视频权限", Toast.LENGTH_SHORT).show();
                        }
                    }).start();
                }

            }
        });

        findViewById(R.id.tv_title).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long currentClickTime = SystemClock.uptimeMillis();
                long elapsedTime = currentClickTime - mLastClickTime;
                mLastClickTime = currentClickTime;

                if (elapsedTime < MIN_CLICK_INTERVAL) {
                    ++mSecretNumber;
                    if (9 == mSecretNumber) {
                        try {
                          Toast.makeText(MainActivity.this,"进入开发者模式",Toast.LENGTH_SHORT).show();
                          startAnimActivity(InputDevInfoActivity.class);
                        } catch (Exception e) {
                        }
                        mSecretNumber=0;
                    }
                } else {
                    mSecretNumber = 0;
                }
            }
        });

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            System.exit(0);
            RtcpCore.Inst().getmRtcpKit().clean();//程序退出时释放
            finishAnimActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
