package org.anyrtc.activity;

import android.Manifest;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import org.anyrtc.RtcpCore;
import org.anyrtc.rtcp.R;
import org.anyrtc.utils.PermissionsCheckUtil;
import org.anyrtc.zxing.ScanActivity;

import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    TextView tvStartLive, tvWatchLive, tvCall;
    public final static int REQUECT_CODE_CAMARE = 1;

    @Override
    public int getLayoutId() {
        return R.layout.activity_main;
    }


    @Override
    public void initView(Bundle savedInstanceState) {
        mImmersionBar.keyboardEnable(true).init();
        tvStartLive = (TextView) findViewById(R.id.tv_open_live);
        tvWatchLive = (TextView) findViewById(R.id.tv_watch_live);
        tvCall = (TextView) findViewById(R.id.tv_call);
        tvStartLive.setOnClickListener(this);
        tvWatchLive.setOnClickListener(this);
        tvCall.setOnClickListener(this);
    }


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_open_live:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AndPermission.with(this)
                            .requestCode(REQUECT_CODE_CAMARE)
                            .permission(Manifest.permission.CAMERA,
                                    Manifest.permission.RECORD_AUDIO)
                            .callback(new PermissionListener() {
                                @Override
                                public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                                    startAnimActivity(LiveActivity.class, "isPublish", true);
                                }

                                @Override
                                public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                                    if (deniedPermissions.size() == 2) {
                                        PermissionsCheckUtil.showMissingPermissionDialog(MainActivity.this, "请先开启录音和相机权限");
                                        return;
                                    }
                                    for (int i = 0; i < deniedPermissions.size(); i++) {
                                        if (deniedPermissions.get(i).equals(Manifest.permission.RECORD_AUDIO)) {
                                            PermissionsCheckUtil.showMissingPermissionDialog(MainActivity.this, "请先开启录音权限");
                                        } else {
                                            PermissionsCheckUtil.showMissingPermissionDialog(MainActivity.this, "请先开启相机权限");
                                        }
                                    }
                                }
                            }).start();
                } else {
                    startAnimActivity(LiveActivity.class, "isPublish", true);
                }

                break;
            case R.id.tv_watch_live:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    AndPermission.with(this)
                            .requestCode(REQUECT_CODE_CAMARE)
                            .permission(Manifest.permission.CAMERA,
                                    Manifest.permission.RECORD_AUDIO)
                            .callback(new PermissionListener() {
                                @Override
                                public void onSucceed(int requestCode, @NonNull List<String> grantPermissions) {
                                    startAnimActivity(ScanActivity.class);
                                }

                                @Override
                                public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                                    if (deniedPermissions.size() == 2) {
                                        PermissionsCheckUtil.showMissingPermissionDialog(MainActivity.this, "请先开启录音和相机权限");
                                        return;
                                    }
                                    for (int i = 0; i < deniedPermissions.size(); i++) {
                                        if (deniedPermissions.get(i).equals(Manifest.permission.RECORD_AUDIO)) {
                                            PermissionsCheckUtil.showMissingPermissionDialog(MainActivity.this, "请先开启录音权限");
                                        } else {
                                            PermissionsCheckUtil.showMissingPermissionDialog(MainActivity.this, "请先开启相机权限");
                                        }
                                    }
                                }
                            }).start();
                } else {
                    startAnimActivity(ScanActivity.class);
                }


                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {

            System.exit(0);
            RtcpCore.Inst().getmRtcpKit().clear();//程序退出时释放
            finishAnimActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
