package org.anyrtc.arrtcp;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.gyf.barlibrary.ImmersionBar;
import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.anyrtc.arrtcp.zxing.ScanActivity;

import java.util.List;

public class MainActivity extends BaseActivity {


    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        ImmersionBar.with(this).statusBarDarkFont(true,0.2f).init();
        findViewById(R.id.tv_open_live).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (AndPermission.hasPermissions(MainActivity.this, Permission.CAMERA, Permission.RECORD_AUDIO)) {
                    startAnimActivity(LiveActivity.class, "isPublish", true);
                } else {
                    AndPermission.with(MainActivity.this).runtime().permission(Permission.CAMERA, Permission.RECORD_AUDIO).onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            startAnimActivity(LiveActivity.class, "isPublish", true);
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

        findViewById(R.id.tv_watch_live).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
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
