package org.anyrtc.arrtcp;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import org.anyrtc.arrtcp.hw.EncoderDebugger;
import org.anyrtc.arrtcp.hw.NV21Convertor;
import org.anyrtc.arrtcp.utils.Util;
import org.ar.common.enums.ARNetQuality;
import org.ar.common.enums.ARVideoCommon;
import org.ar.rtcp_kit.ARRtcpEngine;
import org.ar.rtcp_kit.ARRtcpEvent;
import org.ar.rtcp_kit.ARRtcpKit;
import org.ar.rtcp_kit.ARRtcpOption;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.ByteBuffer;
import java.util.Iterator;
import java.util.List;

public class MediaCodecExtraActivity extends AppCompatActivity implements SurfaceHolder.Callback, View.OnClickListener {

//    String path = Environment.getExternalStorageDirectory() + "/easy.h264";

    private Button btnSwitch;
    private SurfaceView surfaceView;

    private int width = 640, height = 480;
    private int framerate, bitrate;

    private int mCameraId = Camera.CameraInfo.CAMERA_FACING_FRONT;

    private MediaCodec mMediaCodec;
    private SurfaceHolder surfaceHolder;
    private Camera mCamera;
    private NV21Convertor mConvertor;
    private boolean started = false;

    private ARRtcpKit rtcpKit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mediacodec_extra);
        btnSwitch = (Button) findViewById(R.id.btn_switch);
        btnSwitch.setOnClickListener(this);


        initMediaCodec();
        surfaceView = (SurfaceView) findViewById(R.id.sv_surfaceview);
        surfaceView.getHolder().addCallback(this);
        surfaceView.getHolder().setFixedSize(getResources().getDisplayMetrics().widthPixels,
                getResources().getDisplayMetrics().heightPixels);

        //获取配置类
        ARRtcpOption anyRTCRTCPOption = ARRtcpEngine.Inst().getARRtcpOption();
        //设置前后置摄像头 视频横竖屏 视频质量 视频图像排列方式 发布媒体类型
        anyRTCRTCPOption.setOptionParams(true, ARVideoCommon.ARVideoOrientation.Landscape,
                ARVideoCommon.ARVideoProfile.ARVideoProfile480x640, ARVideoCommon.ARVideoFrameRate.ARVideoFrameRateFps10);
        //获取RTCP对象
        rtcpKit = RtcpCore.Inst().getmRtcpKit();
        //设置回调监听
        rtcpKit.setRtcpEvent(arRtcpEvent);
        //设置开启使用264编码数据
        rtcpKit.setExH264Capturer(true);
        //发布
        rtcpKit.publishByToken("", ARVideoCommon.ARMediaType.Video);
    }

    ARRtcpEvent arRtcpEvent = new ARRtcpEvent() {
        @Override
        public void onPublishOK(String rtcpId, String liveInfo) {
            Log.e(this.getClass().toString(), "rtcpid:   " + rtcpId);
        }

        @Override
        public void onPublishFailed(int code, String reason) {

        }

        @Override
        public void onPublishExOK(String rtcpId, String liveInfo) {

        }

        @Override
        public void onPublishExFailed(int code, String strReason) {

        }

        @Override
        public void onSubscribeOK(String rtcpId) {

        }

        @Override
        public void onSubscribeFailed(String rtcpId, int code, String reason) {

        }

        @Override
        public void onRTCOpenRemoteVideoRender(String rtcpId) {

        }

        @Override
        public void onRTCCloseRemoteVideoRender(String rtcpId) {

        }

        @Override
        public void onRTCOpenRemoteAudioTrack(String rtcpId) {

        }

        @Override
        public void onRTCCloseRemoteAudioTrack(String rtcpId) {

        }

        @Override
        public void onRTCRemoteAVStatus(String rtcpId, boolean bAudio, boolean bVideo) {

        }

        @Override
        public void onRTCLocalAudioPcmData(String peerId, byte[] data, int nLen, int nSampleHz, int nChannel) {

        }

        @Override
        public void onRTCRemoteAudioPcmData(String peerId, byte[] data, int nLen, int nSampleHz, int nChannel) {

        }

        @Override
        public void onRTCRemoteAudioActive(String rtcpId, int nLevel, int nTime) {

        }

        @Override
        public void onRTCLocalAudioActive(int nLevel, int nTime) {

        }

        @Override
        public void onRTCRemoteNetworkStatus(String rtcpId, int nNetSpeed, int nPacketLost, ARNetQuality netQuality) {

        }

        @Override
        public void onRTCLocalNetworkStatus(int nNetSpeed, int nPacketLost, ARNetQuality netQuality) {

        }
    };

    /**
     * 初始化编码器
     */
    private void initMediaCodec() {
        int dgree = getDgree();
        framerate = 10;
//        bitrate = 2 * width * height * framerate / 20;

        bitrate = 512 * 1024;
        EncoderDebugger debugger = EncoderDebugger.debug(getApplicationContext(), width, height);
        mConvertor = debugger.getNV21Convertor();
        try {
            mMediaCodec = MediaCodec.createByCodecName(debugger.getEncoderName());
//            mMediaCodec = MediaCodec.createByCodecName("OMX.Intel.hw_ve.h264");
            MediaFormat mediaFormat;
            if (dgree == 0) {
                mediaFormat = MediaFormat.createVideoFormat("video/avc", height, width);
            } else {
                mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
            }
            mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
            mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, framerate);
            mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT,
                    debugger.getEncoderColorFormat());
            mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3);
            mMediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
            mMediaCodec.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取相机的最大帧率范围
     * @param parameters
     * @return
     */
    private int[] determineMaximumSupportedFramerate(Camera.Parameters parameters) {
        int[] maxFps = new int[]{0, 0};
        List<int[]> supportedFpsRanges = parameters.getSupportedPreviewFpsRange();
        for (Iterator<int[]> it = supportedFpsRanges.iterator(); it.hasNext(); ) {
            int[] interval = it.next();
            if (interval[1] > maxFps[1] || (interval[0] > maxFps[0] && interval[1] == maxFps[1])) {
                maxFps = interval;
            }
        }
        return maxFps;
    }

    /**
     * 打开相机
     * @param surfaceHolder
     * @return
     */
    private boolean openCamera(SurfaceHolder surfaceHolder) {
        try {
            mCamera = Camera.open(mCameraId);
            Camera.Parameters parameters = mCamera.getParameters();
            int[] max = determineMaximumSupportedFramerate(parameters);
            Camera.CameraInfo camInfo = new Camera.CameraInfo();
            Camera.getCameraInfo(mCameraId, camInfo);
            int cameraRotationOffset = camInfo.orientation;
            int rotate = (360 + cameraRotationOffset - getDgree()) % 360;
            parameters.setRotation(rotate);
            parameters.setPreviewFormat(ImageFormat.NV21);
            List<Camera.Size> sizes = parameters.getSupportedPreviewSizes();
            parameters.setPreviewSize(width, height);
//            parameters.setPreviewFpsRange(max[0], max[1]);
            mCamera.setParameters(parameters);
//            mCamera.autoFocus(null);
            int displayRotation;
            displayRotation = (cameraRotationOffset - getDgree() + 360) % 360;
            mCamera.setDisplayOrientation(90);
            mCamera.setPreviewDisplay(surfaceHolder);
            return true;
        } catch (Exception e) {
            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);
            e.printStackTrace(pw);
            String stack = sw.toString();
            Toast.makeText(this, stack, Toast.LENGTH_LONG).show();
            destroyCamera();
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        surfaceHolder = holder;
        openCamera(surfaceHolder);
        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        stopPreview();
        destroyCamera();
    }

    private Camera.PreviewCallback previewCallback = new Camera.PreviewCallback() {

        byte[] mPpsSps = new byte[0];

        @Override
        public void onPreviewFrame(byte[] data, Camera camera) {
            if (data == null) {
                return;
            }
                ByteBuffer[] inputBuffers = mMediaCodec.getInputBuffers();
                ByteBuffer[] outputBuffers = mMediaCodec.getOutputBuffers();
                byte[] dst = new byte[data.length];
                Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
                if (getDgree() == 0) {
                    dst = Util.rotateNV21Degree90(data, previewSize.width, previewSize.height);
                } else {
                    dst = data;
                }
                try {
                    int bufferIndex = mMediaCodec.dequeueInputBuffer(5000000);
                    if (bufferIndex >= 0) {
                        inputBuffers[bufferIndex].clear();
                        mConvertor.convert(dst, inputBuffers[bufferIndex]);
                        mMediaCodec.queueInputBuffer(bufferIndex, 0,
                                inputBuffers[bufferIndex].position(),
                                System.nanoTime() / 1000, 0);
                        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                        int outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                        while (outputBufferIndex >= 0) {
                            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                            byte[] outData = new byte[bufferInfo.size];
                            outputBuffer.get(outData);
                            //记录pps和sps
                            if (outData[0] == 0 && outData[1] == 0 && outData[2] == 0 && outData[3] == 1 && outData[4] == 103) {
                                //arm 和mips平台 pps和sps
                                mPpsSps = outData;
                            } else if (outData[0] == 0 && outData[1] == 0 && outData[2] == 0 && outData[3] == 1 && outData[4] == 101) {
                                //在关键帧前面加上pps和sps数据  arm和mips平台 pps和sps
                                byte[] iframeData = new byte[mPpsSps.length + outData.length];
                                System.arraycopy(mPpsSps, 0, iframeData, 0, mPpsSps.length);
                                System.arraycopy(outData, 0, iframeData, mPpsSps.length, outData.length);
                                outData = iframeData;
                            } else if (outData[0] == 0 && outData[1] == 0 && outData[2] == 0 && outData[3] == 1 && outData[4] == 39) {
                                //x86平台 pps和sps
                                mPpsSps = outData;
                            } else if (outData[0] == 0 && outData[1] == 0 && outData[2] == 0 && outData[3] == 1 && outData[4] == 37) {
                                //在关键帧前面加上pps和sps数据 x86平台
                                byte[] iframeData = new byte[mPpsSps.length + outData.length];
                                System.arraycopy(mPpsSps, 0, iframeData, 0, mPpsSps.length);
                                System.arraycopy(outData, 0, iframeData, mPpsSps.length, outData.length);
                                outData = iframeData;
                            }
                            //保存264文件
//                        Util.save(outData, 0, outData.length, path, true);

                            //塞入底层编码后的264数据。
                            rtcpKit.setVideoH264Data(outData, outData.length);

                            mMediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                            outputBufferIndex = mMediaCodec.dequeueOutputBuffer(bufferInfo, 0);
                        }
                    } else {
                        Log.e("easypusher", "No buffer available !");
                    }
                } catch (Exception e) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    e.printStackTrace(pw);
                    String stack = sw.toString();
                    Log.e("save_log", stack);
                    e.printStackTrace();
                } finally {
                    mCamera.addCallbackBuffer(dst);
                }
        }

    };

    /**
     * 开启预览
     */
    public synchronized void startPreview() {
        if (mCamera != null && !started) {
            mCamera.startPreview();
            int previewFormat = mCamera.getParameters().getPreviewFormat();
            Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
            int size = previewSize.width * previewSize.height
                    * ImageFormat.getBitsPerPixel(previewFormat)
                    / 8;
            mCamera.addCallbackBuffer(new byte[size]);
            mCamera.setPreviewCallbackWithBuffer(previewCallback);
            started = true;
            btnSwitch.setText("停止");
        }
    }

    /**
     * 停止预览
     */
    public synchronized void stopPreview() {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallbackWithBuffer(null);
            started = false;
            btnSwitch.setText("开始");
        }
    }

    /**
     * 销毁Camera
     */
    protected synchronized void destroyCamera() {
        if (mCamera != null) {
            mCamera.stopPreview();
            try {
                mCamera.release();
            } catch (Exception e) {

            }
            mCamera = null;
        }
    }

    /**
     * 获取屏幕方向
     * @return
     */
    private int getDgree() {
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break; // Natural orientation
            case Surface.ROTATION_90:
                degrees = 90;
                break; // Landscape left
            case Surface.ROTATION_180:
                degrees = 180;
                break;// Upside down
            case Surface.ROTATION_270:
                degrees = 270;
                break;// Landscape right
        }
        return degrees;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_switch:
                if (!started) {
                    startPreview();
                } else {
                    stopPreview();
                }
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyCamera();
        mMediaCodec.stop();
        mMediaCodec.release();
        mMediaCodec = null;
        rtcpKit.setExH264Capturer(false);
    }
}
