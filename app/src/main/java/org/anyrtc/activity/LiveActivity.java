package org.anyrtc.activity;

import android.content.Context;
import android.media.AudioManager;
import android.os.Bundle;
import android.text.ClipboardManager;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.anyrtc.RtcpCore;
import org.anyrtc.common.enums.AnyRTCCommonMediaType;
import org.anyrtc.common.enums.AnyRTCScreenOrientation;
import org.anyrtc.common.enums.AnyRTCVideoLayout;
import org.anyrtc.common.enums.AnyRTCVideoQualityMode;
import org.anyrtc.common.utils.AnyRTCAudioManager;
import org.anyrtc.rtcp.R;
import org.anyrtc.rtcp_kit.AnyRTCRTCPEngine;
import org.anyrtc.rtcp_kit.AnyRTCRTCPEvent;
import org.anyrtc.rtcp_kit.AnyRTCRTCPOption;
import org.anyrtc.rtcp_kit.RtcpKit;
import org.anyrtc.weight.RTCVideoView;
import org.anyrtc.zxing.utils.CustomDialog;
import org.anyrtc.zxing.utils.QRCode;

public class LiveActivity extends BaseActivity implements View.OnClickListener {

    ImageButton ibHangUp, ibCamera, ibShare,btn_qr_code;
    TextView tv_status;
    View Space;
    RelativeLayout rl_video;
    RtcpKit rtcpKit;
    RTCVideoView rtcVideoView;
    private String strPeerId = "";
    boolean isPublish;
    private AnyRTCAudioManager mRtcAudioManager = null;
    private CustomDialog customDialog;
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
        ibHangUp = (ImageButton) findViewById(R.id.ib_leave);
        ibCamera = (ImageButton) findViewById(R.id.btn_camare);
        ibShare = (ImageButton) findViewById(R.id.btn_share);
        rl_video = (RelativeLayout) findViewById(R.id.rl_video);
        tv_status = (TextView) findViewById(R.id.tv_status);
        btn_qr_code= (ImageButton) findViewById(R.id.btn_qr_code);
        btn_qr_code.setOnClickListener(this);
        ibHangUp.setOnClickListener(this);
        ibCamera.setOnClickListener(this);
        ibShare.setOnClickListener(this);
        isPublish = getIntent().getBooleanExtra("isPublish", false);
        if (isPublish) {
            ibCamera.setVisibility(View.VISIBLE);
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
        AnyRTCRTCPOption anyRTCRTCPOption = AnyRTCRTCPEngine.Inst().getAnyRTCRTCPOption();
        //设置前后置摄像头 视频横竖屏 视频质量 视频图像排列方式 发布媒体类型
        anyRTCRTCPOption.setOptionParams(false, AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait, AnyRTCVideoQualityMode.AnyRTCVideoQuality_Medium2, AnyRTCVideoLayout.AnyRTC_V_3X3_auto, AnyRTCCommonMediaType.AnyRTC_M_Video);
       //获取RTCP对象
        rtcpKit = RtcpCore.Inst().getmRtcpKit();
        //设置回调监听
        rtcpKit.setRtcpEvent(anyRTCRTCPEvent);
        //实例化视频窗口管理对象
        rtcVideoView = new RTCVideoView(rl_video, this, AnyRTCRTCPEngine.Inst().Egl());
        if (isPublish) {//如果是发布
            //设置本地视频采集
            rtcpKit.setLocalVideoCapturer(rtcVideoView.OnRtcOpenLocalRender().GetRenderPointer());
            //发布
            rtcpKit.publish((int) ((Math.random() * 9 + 1) * 100000) + "",false);
        } else {
            //订阅媒体
            strPeerId = getIntent().getStringExtra("id");
            rtcpKit.subscribe(strPeerId);
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
    private AnyRTCRTCPEvent anyRTCRTCPEvent=new AnyRTCRTCPEvent() {
        /**
         * 发布成功
         * @param strRtcpId 发布媒体id
         */
        @Override
        public void onPublishOK(final String strRtcpId,String strLiveInfo) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTCP", "OnPublishOK:" + strRtcpId);
                    if (tv_status != null) {
                        strPeerId = strRtcpId;
                        tv_status.setText("发布成功\n直播间ID:" + strRtcpId);
                    }
                }
            });
        }

        /**
         * 发布媒体失败
         * @param nCode 状态码
         */
        @Override
        public void onPublishFailed(final int nCode) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTCP", "OnPublishFailed:code=" + nCode );
                    if (tv_status != null) {
                        tv_status.setText("发布失败 code=" + nCode);
                    }
                }
            });
        }

        /**
         * 订阅媒体成功
         * @param strRtcpId 订阅的媒体的id
         */
        @Override
        public void onSubscribeOK(final String strRtcpId) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTCP", "OnSubscribeOK:" + strRtcpId);
                    strPeerId=strRtcpId;
                    if (tv_status != null) {
                        tv_status.setText("订阅成功 \n直播间ID:" + strRtcpId);
                    }
                }
            });
        }

        /**
         * 订阅失败
         * @param strRtcpId 订阅的媒体的id
         * @param nCode 状态码
         */
        @Override
        public void onSubscribeFailed(final String strRtcpId, final int nCode) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTCP", "OnSubscribeFailed:" + strRtcpId);
                    if (tv_status != null) {
                        tv_status.setText("订阅失败 code=" + nCode);
                    }
                }
            });
        }

        /**
         * 订阅的媒体视频即将显示
         * @param strLivePeerId 订阅的媒体的视频像id
         */
        @Override
        public void onRTCOpenVideoRender(final String strLivePeerId) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTCP", "OnRTCOpenVideoRender:" + strLivePeerId);
                    long renderPointer = rtcVideoView.OnRtcOpenRemoteRender(strLivePeerId).GetRenderPointer();
                    rtcpKit.setRTCVideoRender(strLivePeerId, renderPointer);
                }
            });
        }
        /**
         * 订阅的媒体视频关闭
         * @param strLivePeerId 订阅的媒体的视频像id
         */

        @Override
        public void onRTCCloseVideoRender(final String strLivePeerId) {
            LiveActivity.this.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Log.d("RTCP", "OnRTCCloseVideoRender:" + strLivePeerId);
                    if (rtcpKit!=null){
                        rtcpKit.setRTCVideoRender(strLivePeerId,0);
                        rtcVideoView.OnRtcRemoveRemoteRender(strLivePeerId);
                        finishAnimActivity();
                    }
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
                    rtcpKit.unSubscribe(strPeerId);
                }else {
                    //取消发布
                    rtcpKit.unPublish();
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
                showQRcodeDialog();
                break;
        }
    }


    private void showQRcodeDialog(){
        CustomDialog.Builder builder = new CustomDialog.Builder(LiveActivity.this);
        customDialog = builder.setContentView(R.layout.qr_code)
                .setCancelable(true)
                .setGravity(Gravity.CENTER)
                .setAnimId(R.style.dialog_live_style)
                .setBackgroundDrawable(true)
                .show(new CustomDialog.Builder.onInitListener() {
                    @Override
                    public void init(CustomDialog customDialog) {
                        ImageView iv= (ImageView) customDialog.findViewById(R.id.iv_code);
                        if (null!=iv&& !TextUtils.isEmpty(strPeerId)) {
                            iv.setImageBitmap(QRCode.createQRCode(strPeerId));
                        }
                    }
                });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        rtcVideoView.OnRtcRemoveLocalRender();//移除视频图像
        rtcpKit.stop();//停止采集
        if (mRtcAudioManager != null) {
            mRtcAudioManager.close();
            mRtcAudioManager = null;
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (!isPublish){
                rtcpKit.unSubscribe(strPeerId);
            }else {
                rtcpKit.unPublish();
            }
            finishAnimActivity();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
