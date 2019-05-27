package org.anyrtc.arrtcp;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.yanzhenjie.permission.Action;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.runtime.Permission;

import org.anyrtc.arrtcp.zxing.ScanActivity;
import org.anyrtc.arrtcp.zxing.utils.CustomDialog;
import org.anyrtc.arrtcp.zxing.utils.QRCode;
import org.anyrtc.common.utils.AnyRTCAudioManager;
import org.ar.common.enums.ARNetQuality;
import org.ar.common.enums.ARVideoCommon;
import org.ar.rtcp_kit.ARRtcpEngine;
import org.ar.rtcp_kit.ARRtcpEvent;
import org.ar.rtcp_kit.ARRtcpKit;
import org.ar.rtcp_kit.ARRtcpOption;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LiveActivity extends BaseActivity implements View.OnClickListener {

    ImageButton ibCamera, ibShare, btn_qr_code,btnScan,ibtn_log_close,btn_log;
    Button ibHangUp;
    TextView tv_status;
    View Space;
    RelativeLayout rl_video,rl_log_layout;
    ARRtcpKit rtcpKit;
    ARVideoView videoView;
    RecyclerView rvLogList;
    private String strPeerId = "";
    boolean isPublish;
    private AnyRTCAudioManager mRtcAudioManager = null;
    private CustomDialog customDialog;
    List<String> rtcpIDList=new ArrayList<>();
    LogAdapter logAdapter;
    @Override
    public int getLayoutId() {
        return R.layout.activity_live;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //保持屏幕常亮
        super.onCreate(savedInstanceState);
    }

    @Override
    public void initView(Bundle savedInstanceState) {
        Space = findViewById(R.id.view_space);
        mImmersionBar.titleBar(Space).init();
        rvLogList=findViewById(R.id.rv_log);
        rl_log_layout=findViewById(R.id.rl_log_layout);
        ibtn_log_close=findViewById(R.id.ibtn_close_log);
        btn_log=findViewById(R.id.btn_log);
        ibHangUp = (Button) findViewById(R.id.ib_leave);
        ibCamera = (ImageButton) findViewById(R.id.btn_camare);
        ibShare = (ImageButton) findViewById(R.id.btn_share);
        rl_video = (RelativeLayout) findViewById(R.id.rl_video);
        tv_status = (TextView) findViewById(R.id.tv_status);
        btn_qr_code = (ImageButton) findViewById(R.id.btn_qr_code);
        btnScan=findViewById(R.id.btn_scan);
        rvLogList.setLayoutManager(new LinearLayoutManager(this));
        logAdapter=new LogAdapter();
        rvLogList.setAdapter(logAdapter);
        btnScan.setOnClickListener(this);
        btn_qr_code.setOnClickListener(this);
        ibHangUp.setOnClickListener(this);
        ibCamera.setOnClickListener(this);
        ibShare.setOnClickListener(this);
        ibtn_log_close.setOnClickListener(this);
        btn_log.setOnClickListener(this);
        isPublish = getIntent().getBooleanExtra("isPublish", false);
        if (isPublish) {
            ibCamera.setVisibility(View.VISIBLE);
        }else {
            btn_qr_code.setVisibility(View.GONE);
            btnScan.setVisibility(View.VISIBLE);
            ibShare.setVisibility(View.GONE);
        }
        mRtcAudioManager = AnyRTCAudioManager.create(this, new Runnable() {
            @Override
            public void run() {
                onAudioManagerChangedState();
            }
        });
        mRtcAudioManager.init();

        /**
         * 视频
         */

        //获取配置类
        ARRtcpOption anyRTCRTCPOption = ARRtcpEngine.Inst().getARRtcpOption();
        //设置前后置摄像头 视频横竖屏 视频质量 视频图像排列方式 发布媒体类型
        anyRTCRTCPOption.setOptionParams(true, ARVideoCommon.ARVideoOrientation.Portrait, ARVideoCommon.ARVideoProfile.ARVideoProfile720x960, ARVideoCommon.ARVideoFrameRate.ARVideoFrameRateFps15);
        //获取RTCP对象
        rtcpKit = RtcpCore.Inst().getmRtcpKit();
        //设置回调监听
        rtcpKit.setRtcpEvent(arRtcpEvent);
        //实例化视频窗口管理对象
        videoView = new ARVideoView(rl_video,  ARRtcpEngine.Inst().Egl(),this,false);
        videoView.setVideoViewLayout(true,Gravity.CENTER, LinearLayout.VERTICAL);
        if (isPublish) {//如果是发布
            //设置本地视频采集
            rtcpKit.setLocalVideoCapturer(videoView.openLocalVideoRender().GetRenderPointer());
            //发布
            rtcpKit.publishByToken("", ARVideoCommon.ARMediaType.Video);
            logAdapter.addData("方法：publishByToken");
        } else {
            //订阅媒体
            strPeerId = getIntent().getStringExtra("id");
            rtcpKit.subscribe(strPeerId,"");
            logAdapter.addData("方法：subscribe");
            rtcpIDList.add(strPeerId);
        }
       /*音频
        AnyRTCRTCPEngine.Inst().getAnyRTCRTCPOption().setmMediaType(AnyRTCCommonMediaType.AnyRTC_M_Audio);
        rtcpKit = RtcpCore.Inst().getmRtcpKit();
        rtcpKit.setRtcpEvent(anyRTCRTCPEvent);
        if (isLive) {
            rtcpKit.publish((int) ((Math.random() * 9 + 1) * 100000) + "");
        } else {
            rtcpKit.subscribe(strPeerId);
        }
        */
    }

    private void onAudioManagerChangedState() {
        // TODO(henrika): disable video if
        // AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    private ARRtcpEvent arRtcpEvent=new ARRtcpEvent() {
        @Override
        public void onPublishOK(final String rtcpId, final String liveInfo) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onPublishOK \n rtcpId："+rtcpId +" liveInfo:"+liveInfo);
                    if (tv_status != null) {
                        strPeerId = rtcpId;
                        tv_status.setText("发布成功\n直播间ID:" + rtcpId);
                    }
                }
            });
        }

        @Override
        public void onPublishFailed(final int code, final String reason) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(LiveActivity.class.getName(), "OnPublishFailed:code=" + code );
                    logAdapter.addData("回调：onPublishFailed \nerrorCode="+code+" reason="+reason);
                    if (tv_status != null) {
                        tv_status.setText("发布失败 code=" + code);
                    }
                }
            });
        }

        @Override
        public void onPublishExOK(final String rtcpId, final String liveInfo) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onPublishExOK rtcpId："+rtcpId +" liveInfo:"+liveInfo);
                }
            });
        }

        @Override
        public void onPublishExFailed(final int code, final String strReason) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onPublishExFailed errorCode="+code+" reason="+strReason);
                }
            });
        }

        @Override
        public void onSubscribeOK(final String rtcpId) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(LiveActivity.class.getName(), "OnSubscribeOK:" + rtcpId);
                    logAdapter.addData("回调：onSubscribeOK rtcpId="+rtcpId);
                    strPeerId=rtcpId;
                    if (tv_status != null) {
                        tv_status.setText("订阅成功 \n直播间ID:" + rtcpId);
                    }
                }
            });
        }

        @Override
        public void onSubscribeFailed(final String rtcpId, final int code, final String reason) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onSubscribeFailed");
                    Log.d(LiveActivity.class.getName(), "OnSubscribeFailed rtcpId" + rtcpId+"errorCode="+code+"reason="+reason);
                    if (tv_status != null) {
                        tv_status.setText("订阅失败 code=" + code);
                    }
                }
            });
        }

        @Override
        public void onRTCOpenRemoteVideoRender(final String rtcpId) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onRTCOpenRemoteVideoRender");
                    Log.d(LiveActivity.class.getName(), "OnRTCOpenVideoRender rtcpId=" + rtcpId);
                    long renderPointer = videoView.subscribeRemoteVideo(rtcpId).GetRenderPointer();
                    rtcpKit.setRemoteVideoRender(rtcpId, renderPointer);
                }
            });
        }

        @Override
        public void onRTCCloseRemoteVideoRender(final String rtcpId) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onRTCCloseRemoteVideoRender rtcpId="+rtcpId);
                    Log.d(LiveActivity.class.getName(), "OnRTCCloseVideoRender:" + rtcpId);
                    if (rtcpKit!=null){
                        rtcpKit.setRemoteVideoRender(rtcpId,0);
                        videoView.removeRemoteRender(rtcpId);
                        if (videoView.getRemoteVideoSize()==0) {
                            finishAnimActivity();
                        }
                    }
                }
            });
        }

        @Override
        public void onRTCOpenRemoteAudioTrack(final String rtcpId) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onRTCOpenRemoteAudioTrack rtcpId="+rtcpId);
                    Log.d(LiveActivity.class.getName(), "onRTCOpenRemoteAudioTrack:" + rtcpId);
                }
            });
        }

        @Override
        public void onRTCCloseRemoteAudioTrack(final String rtcpId) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onRTCCloseRemoteAudioTrack rtcpId="+rtcpId);
                    Log.d(LiveActivity.class.getName(), "onRTCCloseRemoteAudioTrack:" + rtcpId);
                }
            });
        }

        @Override
        public void onRTCRemoteAVStatus(final String rtcpId, final boolean bAudio, final boolean bVideo) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onRTCRemoteAVStatus rtcpId="+rtcpId +"audio="+bAudio+ "video="+bVideo);
                }
            });
        }

        @Override
        public void onRTCRemoteAudioActive(final String rtcpId, final int nLevel, final int nTime) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onRTCLocalAudioActive(final int nLevel, final int nTime) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onRTCRemoteNetworkStatus(String rtcpId, int nNetSpeed, int nPacketLost, ARNetQuality netQuality) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onRTCLocalNetworkStatus(int nNetSpeed, int nPacketLost, ARNetQuality netQuality) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }
    };


    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.ib_leave:
                if (!isPublish){
                    //解除订阅
                    for (int i=0;i<rtcpIDList.size();i++){
                        rtcpKit.unSubscribe(rtcpIDList.get(i));
                    }
                }else {
                    //取消发布
                    rtcpKit.unPublish();
                    rtcpKit.stopCapture();
                }
                finishAnimActivity();
                break;
            case R.id.btn_camare:
                if (rtcpKit != null) {
                    rtcpKit.switchCamera();//切换摄像头
                    if (ibCamera.isSelected()) {
                        ibCamera.setSelected(false);
                    } else {
                        ibCamera.setSelected(true);
                    }
                }
                break;
            case R.id.btn_share:
                ClipboardManager peerid = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                // 将文本内容放到系统剪贴板里。
                peerid.setText(strPeerId);
                Toast.makeText(this, "直播室ID已复制到剪贴板~", Toast.LENGTH_SHORT).show();

                break;
            case R.id.btn_qr_code:
                if (AndPermission.hasPermissions(LiveActivity.this,Permission.WRITE_EXTERNAL_STORAGE)){
                    showQRcodeDialog();
            }else {
                    AndPermission.with(LiveActivity.this).runtime().permission(Permission.WRITE_EXTERNAL_STORAGE).onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            showQRcodeDialog();
                        }
                    }).onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            Toast.makeText(LiveActivity.this, "请给予写入文件权限，保存二维码需要", Toast.LENGTH_SHORT).show();
                        }
                    }).start();
                }

                break;
            case R.id.btn_scan:
                if (rtcpIDList.size()==2){
                    Toast.makeText(LiveActivity.this, "DEMO演示，仅展示订阅两路流", Toast.LENGTH_SHORT).show();
                    return;
                }
                Intent i=new Intent(this,ScanActivity.class);
                i.putExtra("isFirstScan",false);
                startActivityForResult(i,100);
                break;
            case R.id.ibtn_close_log:
                rl_log_layout.setVisibility(View.GONE);
                break;
            case R.id.btn_log:
                rl_log_layout.setVisibility(View.VISIBLE);
                break;

        }
    }


    private void showQRcodeDialog(){
        CustomDialog.Builder builder = new CustomDialog.Builder(LiveActivity.this);
        customDialog = builder.setContentView(R.layout.qr_code)
                .setCancelable(true)
                .setLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                .setGravity(Gravity.CENTER)
                .setBackgroundDrawable(true)
                .show(new CustomDialog.Builder.onInitListener() {
                    @Override
                    public void init(final CustomDialog customDialog) {
                        ImageView iv= (ImageView) customDialog.findViewById(R.id.iv_code);
                        Bitmap bitmapQRCode=null;
                        if (null!=iv&& !TextUtils.isEmpty(strPeerId)) {
                            bitmapQRCode=QRCode.createQRCode(strPeerId);
                            iv.setImageBitmap(bitmapQRCode);
                        }
                        final Bitmap finalBitmapQRCode = bitmapQRCode;
                        iv.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                if (finalBitmapQRCode!=null) {
                                    saveImageToGallery(finalBitmapQRCode);
                                }
                                return true;
                            }
                        });
                        RelativeLayout root=customDialog.findViewById(R.id.rl_root);
                        root.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                customDialog.dismiss();
                            }
                        });
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        videoView.removeLocalVideoRender();//移除视频图像
        rtcpKit.stopCapture();//停止采集
        if (mRtcAudioManager != null) {
            mRtcAudioManager.close();
            mRtcAudioManager = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isPublish){
                for (int i=0;i<rtcpIDList.size();i++){
                    rtcpKit.unSubscribe(rtcpIDList.get(i));
                }

            }else {
                rtcpKit.unPublish();
            }
            finishAnimActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode==100){
            if (resultCode==200){
                String strPeerId = data.getStringExtra("id");
                rtcpKit.subscribe(strPeerId,"");
                rtcpIDList.add(strPeerId);
            }
        }
    }


    //===========================保存二维码==============================

    public  File getRootPath(Context context) {
        if (sdCardIsAvailable()) {
            return Environment.getExternalStorageDirectory(); // 取得sdcard文件路径
        } else {
            return context.getFilesDir();
        }
    }

    public  boolean sdCardIsAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sd = new File(Environment.getExternalStorageDirectory().getPath());
            return sd.canWrite();
        } else
            return false;
    }
    public void saveImageToGallery(Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(getRootPath(this).getAbsolutePath()+File.separator+"ARRTCP/QRCODE");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        File file = new File(appDir, strPeerId+".jpg");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this,"已保存到 /ARRTCP/QRCODE 目录下",Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(this,"出错了，请重试",Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this,"出错了，请重试",Toast.LENGTH_SHORT).show();
        }
    }
}
