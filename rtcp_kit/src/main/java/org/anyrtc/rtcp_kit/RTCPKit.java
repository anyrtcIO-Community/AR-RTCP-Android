package org.anyrtc.rtcp_kit;

import android.content.pm.PackageManager;
import android.support.annotation.RequiresPermission;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import org.anyrtc.common.enums.AnyRTCNetQuality;
import org.anyrtc.common.enums.AnyRTCScreenOrientation;
import org.anyrtc.common.enums.AnyRTCVideoMode;
import org.anyrtc.common.utils.AnyRTCUtils;
import org.anyrtc.common.utils.LooperExecutor;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.EglBase;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturerAndroid;

import java.util.concurrent.Exchanger;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;

/**
 * Created by Eric on 2017/6/2.
 */
@Deprecated
public class RTCPKit {

    private static final String TAG = "RtcpKit";

    /**
     * 加载api所需要的动态库
     */
    static {
        System.loadLibrary("rtcp-jni");
    }

    /**
     * 构造访问jni底层库的对象
     */
    private long fNativeAppId;
    private AnyRTCRTCPEvent mRtcpEvent;
    private final LooperExecutor mExecutor;
    private final EglBase mEglBase;

    private int mCameraId = 0;
    private VideoCapturerAndroid mVideoCapturer;
    private VideoCapturerAndroid mVideoCapturerEx;

    private boolean bFront = true;
    private boolean mIsOpenExtra;

    private boolean isFrontOpenMirrorEnable = false;
    private boolean isopenAudioCheck = true;

    /**
     * 初始化RTCP对象
     */
    public RTCPKit(final String userId, final String userData) {
        mExecutor = AnyRTCRTCPEngine.Inst().Executor();
        mEglBase = AnyRTCRTCPEngine.Inst().Egl();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fNativeAppId = nativeCreate(mRtcpHelper);
                nativeSetUserInfo(userId, userData);
            }
        });
    }

    /**
     * 设置回调接口对象
     *
     * @param rtcpEvent
     */
    public void setRtcpEvent(final AnyRTCRTCPEvent rtcpEvent) {
        AnyRTCUtils.assertIsTrue(rtcpEvent != null);
        mRtcpEvent = rtcpEvent;
    }

    public void clear() {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null) {
                    try {
                        mVideoCapturer.stopCapture();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    nativeSetVideoCapturer(null, 0);
                    mVideoCapturer = null;
                }

                if(mVideoCapturerEx != null) {
                    try {
                        mVideoCapturerEx.stopCapture();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    nativeSetVideoCapturerEx(null, 0);
                    mVideoCapturerEx = null;
                }

                nativeDestroy();
                LooperExecutor.exchange(result, true);
            }
        });
        LooperExecutor.exchange(result, false);
    }

    /**
     * 停止摄像头预览和传输
     */
    public void stopCapture() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null) {
                    try {
                        mVideoCapturer.stopCapture();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    nativeSetVideoCapturerEx(null, 0);
                    mVideoCapturer = null;
                }
            }
        });
    }

    /**
     * 停止辅助摄像头预览和传输
     */
    public void stopExCapture() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturerEx != null) {
                    try {
                        mVideoCapturerEx.stopCapture();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    nativeSetVideoCapturer(null, 0);
                    mVideoCapturerEx = null;
                }
            }
        });
    }

    /**
     * 是否打开音频检测
     * @return
     */
    public boolean isOpenAudioCheck() {
        return isopenAudioCheck;
    }


    private void setAuidoModel(final boolean bEnabled, final boolean bAudioDetect) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetAuidoModel(bEnabled, bAudioDetect);
            }
        });
    }

    /**
     * 打开或关闭前置摄像头镜面
     *
     * @param bEnable true: 打开; false: 关闭
     */
    public void setFrontCameraMirrorEnable(final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                isFrontOpenMirrorEnable = bEnable;
                nativeSetCameraMirror(bEnable);
            }
        });
    }

    /**
     * 前置摄像头是否打开镜变
     * @return
     */
    public boolean getFrontCameraMirror() {
        return isFrontOpenMirrorEnable;
    }

    /**
     * 打开或关闭网络状态监测
     *
     * @param bEnable true: 打开; false: 关闭
     */
    public void setNetworkStatus(final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetNetworkStatus(bEnable);
            }
        });
    }

    /**
     * 网络监测是否打开
     *
     * @return true:可用， false：不可用
     */
    public boolean networkStatusEnabled() {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = nativeNetworkStatusEnabled();
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }


    /**
     * 设置视频横屏模式
     */
    private void setScreenToLandscape() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetScreenToLandscape();
            }
        });
    }

    /**
     * 设置视频竖屏模式
     */
    private void setScreenToPortrait() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetScreenToPortrait();
            }
        });
    }

    /**
     * 设置验证token
     *
     * @param strUserToken token字符串:客户端向自己服务器申请
     * @return true：设置成功；false：设置失败
     */
    public boolean setUserToken(final String strUserToken) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                if (null == strUserToken || strUserToken.equals("")) {
                    ret = false;
                } else {
                    nativeSetUserToken(strUserToken);
                    ret = true;
                }
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    /**
     * 是否打开音频检测
     *
     * @param open true：打开，false：关闭
     */
    public void setAudioActiveCheck(final boolean open) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                isopenAudioCheck = open;
                nativeSetAuidoModel(false, open);
            }
        });
    }

    /**
     * 设置本地音频是否可用
     * @param enabled
     */
    public void setLocalAudioEnable(final boolean enabled) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetAudioEnable(enabled);
            }
        });
    }

    /**
     * 设置本地视频是否可用
     * @param enabled
     */
    public void setLocalVideoEnable(final boolean enabled) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetVideoEnable(enabled);
            }
        });
    }

    /**
     * 获取本地音频状态
     * @return
     */
    public boolean getAudioEnabled() {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                ret = nativeGetAudioEnable();

                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    /**
     * 获取本地视频状态
     * @return
     */
    public boolean getVideoEnabled() {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                ret = nativeGetVideoEnable();

                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    /**
     * 加载本地摄像头
     *
     * @param renderPointer 底层图像地址
     * @return 打开本地预览返回值：0/1/2：没有相机权限/打开成功/打开相机失败
     */
    public int setLocalVideoCapturer(final long renderPointer) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                bFront = AnyRTCRTCPEngine.Inst().getAnyRTCRTCPOption().ismBFront();
                int ret = 0;
                int permission = PermissionChecker.checkSelfPermission(AnyRTCRTCPEngine.Inst().context(), CAMERA);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    if (mVideoCapturer == null) {
                        mCameraId = 0;
                        String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(mCameraId);
                        String frontCameraDeviceName =
                                CameraEnumerationAndroid.getNameOfFrontFacingDevice();
                        int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
                        if (numberOfCameras > 1 && frontCameraDeviceName != null && bFront) {
                            cameraDeviceName = frontCameraDeviceName;
                            mCameraId = 1;
                        }
                        Log.d(TAG, "Opening camera: " + cameraDeviceName);
                        mVideoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null);
                        if (mVideoCapturer == null) {
                            Log.e("sys", "Failed to open camera");
                            LooperExecutor.exchange(result, 2);
                        }
                        if (AnyRTCRTCPEngine.Inst().getAnyRTCRTCPOption().getmScreenOriention() == AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait) {
                            nativeSetScreenToPortrait();
                        } else {
                            nativeSetScreenToLandscape();
                        }
                        nativeSetVideoModeExcessive(AnyRTCRTCPEngine.Inst().getAnyRTCRTCPOption().getmVideoMode().level);
                        nativeSetVideoCapturer(mVideoCapturer, renderPointer);
                        LooperExecutor.exchange(result, 1);
                    } else {
                        LooperExecutor.exchange(result, 3);
                    }

                } else {
                    LooperExecutor.exchange(result, 0);
                }
            }
        });
        return LooperExecutor.exchange(result, 0);
    }


    /**
     * 加载本地附加摄像头
     * @param renderPointer 底层图像地址
     * @return 打开本地附加摄像头预览返回值：0/1/2：没有相机权限/打开成功/打开相机失败
     */
    public int setLocalExVideoCapturer(final long renderPointer){
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                bFront=AnyRTCRTCPEngine.Inst().getAnyRTCRTCPOption().ismBFront();
                int ret = 0;
                int permission = PermissionChecker.checkSelfPermission(AnyRTCRTCPEngine.Inst().context(), CAMERA);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
                    if(numberOfCameras < 2) {
                        LooperExecutor.exchange(result, 0);
                    }
                    if(mVideoCapturerEx == null) {
                        int camId = 0;
                        if(mCameraId == 0) {
                            camId = 1;
                        }
                        String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(camId);
                        Log.d(TAG, "Opening extra camera: " + cameraDeviceName);
                        mVideoCapturerEx = VideoCapturerAndroid.create(cameraDeviceName, null);
                        if (mVideoCapturerEx == null) {
                            Log.e("sys", "Failed to open extra camera");
                            LooperExecutor.exchange(result, 2);
                        }
                        nativeSetExVideoModeExcessive(AnyRTCRTCPEngine.Inst().getExAnyRTCRTCPOption().getmVideoMode().level);
                        nativeSetVideoCapturerEx(mVideoCapturerEx, renderPointer);
                        LooperExecutor.exchange(result, 1);
                    } else {
                        LooperExecutor.exchange(result, 3);
                    }

                } else {
                    LooperExecutor.exchange(result, 0);
                }
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 切换摄像头
     */
    public void switchCamera() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null && CameraEnumerationAndroid.getDeviceCount() > 1) {
                    mCameraId = (mCameraId + 1) % CameraEnumerationAndroid.getDeviceCount();
                    mVideoCapturer.switchCamera(null);
                }
            }
        });
    }

    /**
     * 设置用户信息
     *
     * @param strUserId
     * @param strUserData
     */
    private void setUserInfo(final String strUserId, final String strUserData) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetUserInfo(strUserId, strUserData);
            }
        });
    }

    //* RTC function for rtcp

    /**
     * 发布视频
     *
     * @param strAnyrtcID 发布的anyRTCid
     * @return 发布结果；0/1:发布失败（没有RECORD_AUDIO权限）/发布成功
     */
    public int publish(final String strAnyrtcID) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = PermissionChecker.checkSelfPermission(AnyRTCRTCPEngine.Inst().context(), RECORD_AUDIO);
                if (permission == PackageManager.PERMISSION_GRANTED) {

                    nativeSetDeviceInfo(AnyRTCRTCPEngine.Inst().getDeviceInfo());
                    // We have permission granted to the user
                    nativePublish(AnyRTCRTCPEngine.Inst().getAnyRTCRTCPOption().getmMediaType().type, strAnyrtcID, false);
                    ret = 1;
                } else {
                    ret = 0;
                }

                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 发布视频流
     * @param strAnyrtcID
     * @param bIsNeedTransform
     * @return
     */
    public int publish(final String strAnyrtcID, final boolean bIsNeedTransform) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = PermissionChecker.checkSelfPermission(AnyRTCRTCPEngine.Inst().context(), RECORD_AUDIO);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    nativeSetDeviceInfo(AnyRTCRTCPEngine.Inst().getDeviceInfo());
                    // We have permission granted to the user
                    nativePublish(AnyRTCRTCPEngine.Inst().getAnyRTCRTCPOption().getmMediaType().type, strAnyrtcID, bIsNeedTransform);
                    ret = 1;
                } else {
                    ret = 0;
                }

                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 取消发布视频
     */
    public void unPublish() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeUnPublish();
            }
        });
    }

    /**
     * 发布摄像头的视频流
     */
    public void publishEx() {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativePublishEx();
            }
        });
    }

    /**
     * 取消发布辅助摄像头视频流
     */
    public void unPublishEx() {
        mIsOpenExtra = false;
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeUnPublishEx();
            }
        });
    }

    /**
     * 订阅视频
     *
     * @param strRtcpId 发布的anyRTCid
     * @return 订阅结果；0/1:订阅失败（没有RECORD_AUDIO权限）/订阅成功
     */
    public int subscribe(final String strRtcpId) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = PermissionChecker.checkSelfPermission(AnyRTCRTCPEngine.Inst().context(), RECORD_AUDIO);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    nativeSetDeviceInfo(AnyRTCRTCPEngine.Inst().getDeviceInfo());
                    // We have permission granted to the user
                    nativeSubscribe(strRtcpId);
                    ret = 1;
                } else {
                    ret = 0;
                }

                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 取消订阅某路多媒体流
     * @param strRtcpId
     */
    public void unSubscribe(final String strRtcpId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeUnSubscribe(strRtcpId);
            }
        });
    }

    public void setRTCVideoRender(final String strRtcpId, final long renderPointer) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRTCVideoRender(strRtcpId, renderPointer);
            }
        });
    }

    /**
     * 不接收某路的音频
     *
     * @param rtcpId
     * @param mute   true:接收远端音频， false，不接收远端音频
     */
    public void muteRemoteAudioStream(final String rtcpId, final boolean mute) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalPeerAudioEnable(rtcpId, mute);
            }
        });
    }

    /**
     * 不接收某路的视频
     *
     * @param rtcpId
     * @param mute   true:接收远端视频， false，不接收远端视频
     */
    public void muteRemoteVideoStream(final String rtcpId, final boolean mute) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalPeerVideoEnable(rtcpId, mute);
            }
        });
    }

//    public void SetVideoSize(final int width, final int height) {
//        mExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                nativeSetVideoSize(width, height);
//            }
//        });
//    }

//    public void SetVideoMode(final AnyRTCVideoMode videoMode) {
//        mExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
////                nativeSetVideoMode(videoMode.level);
//            }
//        });
//    }


    public void setExVideoMode(final AnyRTCVideoMode videoMode) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetExVideoMode(videoMode.level);
            }
        });
    }


    //Jni interface

    private native long nativeCreate(Object obj);

    private native void nativeSetUserToken(String strUserToken);

    private native void nativeSetDeviceInfo(String strDevInfo);

    private static native void nativeSetAuidoModel(boolean enabled, boolean audioDetect);

    private native void nativeSetCameraMirror(boolean bEnable);

    private native void nativeSetNetworkStatus(boolean bEnable);

    private native boolean nativeNetworkStatusEnabled();

    private native void nativeSetScreenToLandscape();

    private native void nativeSetScreenToPortrait();

    private native void nativeSetAudioEnable(boolean enabled);

    private native void nativeSetVideoEnable(boolean enabled);

    private native boolean nativeGetAudioEnable();

    private native boolean nativeGetVideoEnable();

    private native void nativeSetLocalPeerAudioEnable(String strRtcpId, boolean bAudioEnable);

    private native void nativeSetLocalPeerVideoEnable(String strRtcpId, boolean bVideoEnable);

    private native void nativeSetVideoCapturer(VideoCapturer capturer, long nativeRenderer);
    private native void nativeSetVideoCapturerEx(VideoCapturer capturer, long nativeRenderer);

    private native void nativeSetVideoSize(int width, int height);

    private native void nativeSetVideoMode(int nVideoMode);
    private native void nativeSetExVideoMode(int nVideoMode);

    private native void nativeSetVideoModeExcessive(int nVideoMode);
    private native void nativeSetExVideoModeExcessive(int nVideoMode);

    private native void nativeSetUserInfo(String strUserId, String strUserData);

    private native void nativePublish(int mType, String strAnyrtcId, boolean bLive);
    private native void nativeUnPublish();

    private native void nativePublishEx();
    private native void nativeUnPublishEx();

    private native void nativeSubscribe(String strRtcpId);

    private native void nativeUnSubscribe(String strRtcpId);

    private native void nativeSetRTCVideoRender(String strRtcpId, long nativeRenderer);

    private native void nativeDestroy();


    private RtcpHelper mRtcpHelper = new RtcpHelper() {
        @Override
        public void OnPublishOK(String strRtcpId, String strLiveInfo) {
            if (null != mRtcpEvent) {
                mRtcpEvent.onPublishOK(strRtcpId, strLiveInfo);
            }
        }

        @Override
        public void OnPublishFailed(int nCode, String strReason) {
            if (null != mRtcpEvent) {
                mRtcpEvent.onPublishFailed(nCode, strReason);
            }
        }

        @Override
        public void OnPublishExOK(String strRtcpId, String strLiveInfo) {
            if (null != mRtcpEvent) {
                mRtcpEvent.onPublishExOK(strRtcpId, strLiveInfo);
            }
        }

        @Override
        public void OnPublishExFailed(int nCode, String strReason) {
            if(null != mRtcpEvent) {
                mRtcpEvent.onPublishExFailed(nCode, strReason);
            }
        }

        @Override
        public void OnSubscribeOK(String strRtcpId) {
            if (null != mRtcpEvent) {
                mRtcpEvent.onSubscribeOK(strRtcpId);
            }
        }

        @Override
        public void OnSubscribeFailed(String strRtcpId, int nCode, String strReason) {
            if (null != mRtcpEvent) {
                mRtcpEvent.onSubscribeFailed(strRtcpId, nCode, strReason);
            }
        }

        @Override
        public void OnRtcOpenVideoRender(String strRtcpId) {
            if (null != mRtcpEvent) {
                mRtcpEvent.onRTCOpenVideoRender(strRtcpId);
            }
        }

        @Override
        public void OnRtcCloseVideoRender(String strRtcpId) {
            if (null != mRtcpEvent) {
                mRtcpEvent.onRTCCloseVideoRender(strRtcpId);
            }
        }

        @Override
        public void OnRtcOpenAudioTrack(String strRtcpId, String strUserId) {
            if (null != mRtcpEvent) {
                mRtcpEvent.onRTCOpenAudioTrack(strRtcpId, strUserId);
            }
        }

        @Override
        public void OnRtcCloseAudioTrack(String strRtcpId, String strUserId) {
            if (null != mRtcpEvent) {
                mRtcpEvent.onRTCCloseAudioTrack(strRtcpId, strUserId);
            }
        }

        @Override
        public void OnRtcAVStatus(String strRtcpId, boolean bAudio, boolean bVideo) {
            if (null != mRtcpEvent) {
                mRtcpEvent.onRTCAVStatus(strRtcpId, bAudio, bVideo);
            }
        }

        @Override
        public void OnRtcAudioActive(String strRtcpId, String strUserId, int nLevel, int nShowTime) {
            if (null != mRtcpEvent) {
                mRtcpEvent.onRTCAudioActive(strRtcpId, strUserId, nLevel, nShowTime);
            }
        }

        @Override
        public void OnRtcNetworkStatus(String strRtcpId, String strUserId, int nNetSpeed, int nPacketLost) {
            if (null != mRtcpEvent) {
                AnyRTCNetQuality netQuality = null;
                if (nPacketLost <= 1) {
                    netQuality = AnyRTCNetQuality.AnyRTCNetQualityExcellent;
                } else if (nPacketLost > 1 && nPacketLost <= 3) {
                    netQuality = AnyRTCNetQuality.AnyRTCNetQualityGood;
                } else if (nPacketLost > 3 && nPacketLost <= 5) {
                    netQuality = AnyRTCNetQuality.AnyRTCNetQualityAccepted;
                } else if (nPacketLost > 5 && nPacketLost <= 10) {
                    netQuality = AnyRTCNetQuality.AnyRTCNetQualityBad;
                } else {
                    netQuality = AnyRTCNetQuality.AnyRTCNetQualityVBad;
                }

                mRtcpEvent.onRTCNetworkStatus(strRtcpId, nNetSpeed, nPacketLost, netQuality);
            }
        }
    };
}
