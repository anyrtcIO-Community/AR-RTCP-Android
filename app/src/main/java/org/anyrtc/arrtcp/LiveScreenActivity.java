package org.anyrtc.arrtcp;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.media.AudioManager;
import android.media.projection.MediaProjectionManager;
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
import android.view.Surface;
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
import org.ar.common.enums.ARNetQuality;
import org.ar.common.enums.ARVideoCommon;
import org.ar.common.utils.AR_AudioManager;
import org.ar.rtcp_kit.ARRtcpEngine;
import org.ar.rtcp_kit.ARRtcpEvent;
import org.ar.rtcp_kit.ARRtcpKit;
import org.ar.rtcp_kit.ARRtcpOption;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public class LiveScreenActivity extends BaseActivity implements View.OnClickListener {

    ImageButton ibCamera, ibShare, btn_qr_code, btnScan, ibtn_log_close, btn_log;
    Button ibHangUp;
    TextView tv_status;
    View Space;
    RelativeLayout rl_video, rl_log_layout;
    ARRtcpKit rtcpKit;
    ARVideoView videoView;
    RecyclerView rvLogList;
    private String strPeerId = "";
    boolean isPublish;
    private AR_AudioManager mRtcAudioManager = null;
    private CustomDialog customDialog;
    List<String> rtcpIDList = new ArrayList<>();
    LogAdapter logAdapter;

    //[1920x1080, 1280x960, 1280x720, 960x720, 800x600, 720x480, 640x480, 640x360, 352x288, 320x240, 320x180, 176x144]

    private static Intent mediaProjectionPermissionResultData;
    private static int mediaProjectionPermissionResultCode;

    @TargetApi(21)
    private void startScreenCapture() {
        MediaProjectionManager mediaProjectionManager =
                (MediaProjectionManager) getApplication().getSystemService(
                        Context.MEDIA_PROJECTION_SERVICE);
        startActivityForResult(
                mediaProjectionManager.createScreenCaptureIntent(), 1);
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_live_screen;
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
        rvLogList = findViewById(R.id.rv_log);
        rl_log_layout = findViewById(R.id.rl_log_layout);
        ibtn_log_close = findViewById(R.id.ibtn_close_log);
        btn_log = findViewById(R.id.btn_log);
        ibHangUp = (Button) findViewById(R.id.ib_leave);
        ibCamera = (ImageButton) findViewById(R.id.btn_camare);
        ibShare = (ImageButton) findViewById(R.id.btn_share);
        rl_video = (RelativeLayout) findViewById(R.id.rl_video);
        tv_status = (TextView) findViewById(R.id.tv_status);
        btn_qr_code = (ImageButton) findViewById(R.id.btn_qr_code);
        btnScan = findViewById(R.id.btn_scan);
        rvLogList.setLayoutManager(new LinearLayoutManager(this));
        logAdapter = new LogAdapter();
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
        } else {
            btnScan.setVisibility(View.VISIBLE);
        }
        mRtcAudioManager = AR_AudioManager.create(this, new Runnable() {
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
        anyRTCRTCPOption.setOptionParams(true, ARVideoCommon.ARVideoOrientation.Portrait,
                ARVideoCommon.ARVideoProfile.ARVideoProfile720x960, ARVideoCommon.ARVideoFrameRate.ARVideoFrameRateFps30);
        //获取RTCP对象
        rtcpKit = RtcpCore.Inst().getmRtcpKit();
        //设置回调监听
        rtcpKit.setRtcpEvent(arRtcpEvent);

        //实例化视频窗口管理对象
        videoView = new ARVideoView(rl_video, ARRtcpEngine.Inst().Egl(), this);
        videoView.setVideoViewLayout(true, Gravity.CENTER, LinearLayout.VERTICAL);
        if (isPublish) {//如果是发布
            //设置本地视频采集
//            rtcpKit.setLocalVideoCapturer(videoView.openLocalVideoRender().GetRenderPointer());
//            rtcpKit.setLocalScreenVideoCapturer();
            //是否使用ARCamera进行本地显示， 如果设置为false，且不调用setByteBufferFrameCaptured方法的情况下，本地无视频显示
//            rtcpKit.setUsedARCamera(false);
//            rtcpKit.setARCameraCaptureObserver(new VideoCapturer.ARCameraCapturerObserver() {
//
//                @Override
//                public void onByteBufferFrameCaptured(byte[] data, int width, int height, int rotation, long timeStamp) {
////                    Log.e("LiveActivity", "[AR] " + data.toString());
////                    Log.e("LiveActivity", "[AR] width: " + width);
////                    Log.e("LiveActivity", "[AR] height: " + height);
////                    Log.e("LiveActivity", "[AR] rotation: " + rotation);
////                    Log.e("LiveActivity", "[AR] timeStamp: " + timeStamp);
//
//                    //数据塞回底层进行本地显示（如果没有设置setUsedARCamera(false)，不需要调用此方法）
////                    rtcpKit.setByteBufferFrameCaptured(data, width, height, rotation, timeStamp);
//                }
//            });

            /**
             * 设置使用外部数据采集
             */
            rtcpKit.setExternalCameraCapturer(true, ARVideoCommon.ARCaptureType.YUV420P);
            //发布
            rtcpKit.publishByToken("", ARVideoCommon.ARMediaType.Video);
//            rtcpKit.setLocalAudioEnable(false);
            logAdapter.addData("方法：publishByToken");
        } else {
            //订阅媒体
            strPeerId = getIntent().getStringExtra("id");
            rtcpKit.subscribe(strPeerId, "");
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
       startScreenCapture();
    }

    private int getDeviceOrientation() {
        int orientation = 0;

        WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
        switch (wm.getDefaultDisplay().getRotation()) {
            case Surface.ROTATION_90:
                orientation = 90;
                break;
            case Surface.ROTATION_180:
                orientation = 180;
                break;
            case Surface.ROTATION_270:
                orientation = 270;
                break;
            case Surface.ROTATION_0:
            default:
                orientation = 0;
                break;
        }
        return orientation;
    }

    private int getFrameOrientation() {
        int rotation = getDeviceOrientation();
        Camera.CameraInfo info = new Camera.CameraInfo();
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
            rotation = 360 - rotation;
        }
        if((info.orientation + rotation) % 360 == 180) {
            return 0;
        } else {
            return (info.orientation + rotation) % 360;
        }
    }

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {
        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            /**
             * 塞入相机采集数据， 视频的分辨率必须和配置时的分辨率一致， 如果配置时为竖屏的480*640，此处必须为480*640
             * 如果配置使用横屏的480*640，此处必须是640*480，setVideoRGB565Data接口与此接口相同
             */
//            int result = rtcpKit.setVideoYUV420PData(nv21ToI420(data, width, height), width, height);
//            Log.e(this.getClass().toString(), "setVideoYUV420PData Result:   " + result);
        }
    };

    public byte[] nv21ToI420(byte[] data, int width, int height) {
        byte[] ret = new byte[width * height * 3 / 2];
        int total = width * height;

        ByteBuffer bufferY = ByteBuffer.wrap(ret, 0, total);
        ByteBuffer bufferU = ByteBuffer.wrap(ret, total, total / 4);
        ByteBuffer bufferV = ByteBuffer.wrap(ret, total + total / 4, total / 4);

        bufferY.put(data, 0, total);
        for (int i=total; i<data.length; i+=2) {
            bufferV.put(data[i]);
            bufferU.put(data[i+1]);
        }
        return ret;
    }

    private byte[] NV21ToNV12(byte[] nv21, int width, int height) {
        byte[] nv12 = new byte[width * height * 3 / 2];
        if (nv21 == null || nv12 == null)
            return null;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
        return nv12;
    }

    private void onAudioManagerChangedState() {
        // TODO(henrika): disable video if
        // AppRTCAudioManager.AudioDevice.EARPIECE
        // is active.
        setVolumeControlStream(AudioManager.STREAM_VOICE_CALL);
    }

    private ARRtcpEvent arRtcpEvent = new ARRtcpEvent() {
        @Override
        public void onPublishOK(final String rtcpId, final String liveInfo) {
            LiveScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.e(this.getClass().toString(), "rtcpid:   " + rtcpId);
                    logAdapter.addData("回调：onPublishOK \n rtcpId：" + rtcpId + " liveInfo:" + liveInfo);
                    if (tv_status != null) {
                        strPeerId = rtcpId;
                        tv_status.setText("发布成功\n直播间ID:" + rtcpId);
                    }
                }
            });
        }

        @Override
        public void onPublishFailed(final int code, final String reason) {
            LiveScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(LiveScreenActivity.class.getName(), "OnPublishFailed:code=" + code);
                    logAdapter.addData("回调：onPublishFailed \nerrorCode=" + code + " reason=" + reason);
                    if (tv_status != null) {
                        tv_status.setText("发布失败 code=" + code);
                    }
                }
            });
        }

        @Override
        public void onPublishExOK(final String rtcpId, final String liveInfo) {
            LiveScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onPublishExOK rtcpId：" + rtcpId + " liveInfo:" + liveInfo);
                }
            });
        }

        @Override
        public void onPublishExFailed(final int code, final String strReason) {
            LiveScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onPublishExFailed errorCode=" + code + " reason=" + strReason);
                }
            });
        }

        @Override
        public void onSubscribeOK(final String rtcpId) {
            LiveScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d(LiveScreenActivity.class.getName(), "OnSubscribeOK:" + rtcpId);
                    logAdapter.addData("回调：onSubscribeOK rtcpId=" + rtcpId);
                    strPeerId = rtcpId;
                    if (tv_status != null) {
                        tv_status.setText("订阅成功 \n直播间ID:" + rtcpId);
                    }
                }
            });
        }

        @Override
        public void onSubscribeFailed(final String rtcpId, final int code, final String reason) {
            LiveScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onSubscribeFailed");
                    Log.d(LiveScreenActivity.class.getName(), "OnSubscribeFailed rtcpId" + rtcpId + "errorCode=" + code + "reason=" + reason);
                    if (tv_status != null) {
                        tv_status.setText("订阅失败 code=" + code);
                    }
                }
            });
        }

        @Override
        public void onRTCOpenRemoteVideoRender(final String rtcpId) {
            LiveScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onRTCOpenRemoteVideoRender");
                    Log.d(LiveScreenActivity.class.getName(), "OnRTCOpenVideoRender rtcpId=" + rtcpId);
//                    long renderPointer = videoView.subscribeRemoteVideo(rtcpId).GetRenderPointer();
//                    rtcpKit.setRemoteVideoRender(rtcpId, renderPointer);
                }
            });
        }

        @Override
        public void onRTCCloseRemoteVideoRender(final String rtcpId) {
            LiveScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onRTCCloseRemoteVideoRender rtcpId=" + rtcpId);
                    Log.d(LiveScreenActivity.class.getName(), "OnRTCCloseVideoRender:" + rtcpId);
                    if (rtcpKit != null) {
                        rtcpKit.setRemoteVideoRender(rtcpId, 0);
//                        videoView.removeRemoteRender(rtcpId);
                        finishAnimActivity();
                    }
                }
            });
        }

        @Override
        public void onRTCOpenRemoteAudioTrack(final String rtcpId) {
            LiveScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onRTCOpenRemoteAudioTrack rtcpId=" + rtcpId);
                    Log.d(LiveScreenActivity.class.getName(), "onRTCOpenRemoteAudioTrack:" + rtcpId);
                }
            });
        }

        @Override
        public void onRTCCloseRemoteAudioTrack(final String rtcpId) {
            LiveScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onRTCCloseRemoteAudioTrack rtcpId=" + rtcpId);
                    Log.d(LiveScreenActivity.class.getName(), "onRTCCloseRemoteAudioTrack:" + rtcpId);
                }
            });
        }

        @Override
        public void onRTCRemoteAVStatus(final String rtcpId, final boolean bAudio, final boolean bVideo) {
            LiveScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    logAdapter.addData("回调：onRTCRemoteAVStatus rtcpId=" + rtcpId + "audio=" + bAudio + "video=" + bVideo);
                }
            });
        }

        @Override
        public void onRTCLocalAudioPcmData(String peerId, byte[] data, int nLen, int nSampleHz, int nChannel) {

        }

        @Override
        public void onRTCRemoteAudioPcmData(String peerId, byte[] data, int nLen, int nSampleHz, int nChannel) {

        }

        @Override
        public void onRTCRemoteAudioActive(final String rtcpId, final int nLevel, final int nTime) {
            LiveScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onRTCLocalAudioActive(final int nLevel, final int nTime) {
            LiveScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onRTCRemoteNetworkStatus(String rtcpId, int nNetSpeed, int nPacketLost, ARNetQuality netQuality) {
            LiveScreenActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                }
            });
        }

        @Override
        public void onRTCLocalNetworkStatus(int nNetSpeed, int nPacketLost, ARNetQuality netQuality) {
            LiveScreenActivity.this.runOnUiThread(new Runnable() {
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
                if (!isPublish) {
                    //解除订阅
                    for (int i = 0; i < rtcpIDList.size(); i++) {
                        rtcpKit.unSubscribe(rtcpIDList.get(i));
                    }
                } else {
                    //取消发布
//                    rtcpKit.setExH264Capturer(false);
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
                if (AndPermission.hasPermissions(LiveScreenActivity.this, Permission.WRITE_EXTERNAL_STORAGE)) {
                    showQRcodeDialog();
                } else {
                    AndPermission.with(LiveScreenActivity.this).runtime().permission(Permission.WRITE_EXTERNAL_STORAGE).onGranted(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            showQRcodeDialog();
                        }
                    }).onDenied(new Action<List<String>>() {
                        @Override
                        public void onAction(List<String> data) {
                            Toast.makeText(LiveScreenActivity.this, "请给予写入文件权限，保存二维码需要", Toast.LENGTH_SHORT).show();
                        }
                    }).start();
                }

                break;
            case R.id.btn_scan:
                Intent i = new Intent(this, ScanActivity.class);
                i.putExtra("isFirstScan", false);
                startActivityForResult(i, 100);
                break;
            case R.id.ibtn_close_log:
                rl_log_layout.setVisibility(View.GONE);
                break;
            case R.id.btn_log:
                rl_log_layout.setVisibility(View.VISIBLE);
                break;

        }
    }


    private void showQRcodeDialog() {
        CustomDialog.Builder builder = new CustomDialog.Builder(LiveScreenActivity.this);
        customDialog = builder.setContentView(R.layout.qr_code)
                .setCancelable(true)
                .setLayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
                .setGravity(Gravity.CENTER)
                .setBackgroundDrawable(true)
                .show(new CustomDialog.Builder.onInitListener() {
                    @Override
                    public void init(final CustomDialog customDialog) {
                        ImageView iv = (ImageView) customDialog.findViewById(R.id.iv_code);
                        Bitmap bitmapQRCode = null;
                        if (null != iv && !TextUtils.isEmpty(strPeerId)) {
                            bitmapQRCode = QRCode.createQRCode(strPeerId);
                            iv.setImageBitmap(bitmapQRCode);
                        }
                        final Bitmap finalBitmapQRCode = bitmapQRCode;
                        iv.setOnLongClickListener(new View.OnLongClickListener() {
                            @Override
                            public boolean onLongClick(View view) {
                                saveImageToGallery(finalBitmapQRCode);
                                return true;
                            }
                        });
                        RelativeLayout root = customDialog.findViewById(R.id.rl_root);
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
//        videoView.removeLocalVideoRender();//移除视频图像
        rtcpKit.stopCapture();//停止采集
        if (mRtcAudioManager != null) {
            mRtcAudioManager.close();
            mRtcAudioManager = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isPublish) {
                for (int i = 0; i < rtcpIDList.size(); i++) {
                    rtcpKit.unSubscribe(rtcpIDList.get(i));
                }

            } else {
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
        if (requestCode != 1)
            return;
        mediaProjectionPermissionResultCode = resultCode;
        mediaProjectionPermissionResultData = data;
        rtcpKit.setLocalScreenVideoCapturer(videoView.openLocalVideoRender().GetRenderPointer(), mediaProjectionPermissionResultData);
//        createScreenCapturer();
        if (requestCode == 100) {
            if (resultCode == 200) {
                String strPeerId = data.getStringExtra("id");
                rtcpKit.subscribe(strPeerId, "");
                rtcpIDList.add(strPeerId);
            }
        }
    }


    //===========================保存二维码==============================

    public File getRootPath(Context context) {
        if (sdCardIsAvailable()) {
            return Environment.getExternalStorageDirectory(); // 取得sdcard文件路径
        } else {
            return context.getFilesDir();
        }
    }

    public boolean sdCardIsAvailable() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sd = new File(Environment.getExternalStorageDirectory().getPath());
            return sd.canWrite();
        } else
            return false;
    }

    public void saveImageToGallery(Bitmap bmp) {
        // 首先保存图片
        File appDir = new File(getRootPath(this).getAbsolutePath() + File.separator + "ARRTCP/QRCODE");
        if (!appDir.exists()) {
            appDir.mkdirs();
        }
        File file = new File(appDir, strPeerId + ".jpg");
        try {
            FileOutputStream fos = new FileOutputStream(file);
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos);
            fos.flush();
            fos.close();
            Toast.makeText(this, "已保存到 /ARRTCP/QRCODE 目录下", Toast.LENGTH_SHORT).show();
        } catch (FileNotFoundException e) {
            Toast.makeText(this, "出错了，请重试", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "出错了，请重试", Toast.LENGTH_SHORT).show();
        }
    }
}