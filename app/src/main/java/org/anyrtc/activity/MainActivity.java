package org.anyrtc.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;

import org.anyrtc.utils.PermissionsCheckUtil;
import org.anyrtc.RtcpCore;
import org.anyrtc.rtcp.R;

import java.util.List;

public class MainActivity extends BaseActivity implements View.OnClickListener {

    TextView tvStartLive, tvWatchLive, tvCall,tv_back;
    public final static int REQUECT_CODE_CAMARE = 1;
    EditText editText;
    boolean isEtShow=false;
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
        editText= (EditText) findViewById(R.id.et_id);
        tv_back= (TextView) findViewById(R.id.tv_back);
        tvStartLive.setOnClickListener(this);
        tvWatchLive.setOnClickListener(this);
        tv_back.setOnClickListener(this);
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
                                    if (isEtShow){
                                        if (TextUtils.isEmpty(editText.getText().toString())){
                                            Toast.makeText(MainActivity.this,"直播室ID不能为空",Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        Intent intent = new Intent(MainActivity.this, LiveActivity.class);
                                        intent.putExtra("isPublish", false);
                                        intent.putExtra("id", editText.getText().toString());
                                        startActivity(intent);
                                    }else {
                                        tvStartLive.setVisibility(View.GONE);
                                        editText.setVisibility(View.VISIBLE);
                                        tv_back.setVisibility(View.VISIBLE);
                                        isEtShow=true;
                                    }
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
                    if (isEtShow){
                        if (TextUtils.isEmpty(editText.getText().toString())){
                            Toast.makeText(this,"直播室ID不能为空",Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Intent intent = new Intent(MainActivity.this, LiveActivity.class);
                        intent.putExtra("isPublish", false);
                        intent.putExtra("id", editText.getText().toString());
                        startActivity(intent);
                    }else {
                        tvStartLive.setVisibility(View.GONE);
                        editText.setVisibility(View.VISIBLE);
                        tv_back.setVisibility(View.VISIBLE);
                        isEtShow=true;
                    }
                }



                break;
            case R.id.tv_back:
                tvStartLive.setVisibility(View.VISIBLE);
                editText.setVisibility(View.INVISIBLE);
                tv_back.setVisibility(View.INVISIBLE);
                isEtShow=false;
                break;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {


           if (isEtShow){
               tvStartLive.setVisibility(View.VISIBLE);
               editText.setVisibility(View.INVISIBLE);
               tv_back.setVisibility(View.INVISIBLE);
               isEtShow=false;
           }else {
               System.exit(0);
               RtcpCore.Inst().getmRtcpKit().clear();//程序退出时释放
               finishAnimActivity();
           }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
