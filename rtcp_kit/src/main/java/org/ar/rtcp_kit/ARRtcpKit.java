package org.ar.rtcp_kit;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Build;
import android.support.annotation.RequiresApi;
import android.support.v4.content.PermissionChecker;
import android.util.Log;

import org.ar.common.enums.ARCaptureType;
import org.ar.common.enums.ARNetQuality;
import org.ar.common.enums.ARVideoCommon;
import org.ar.common.utils.ARUtils;
import org.ar.common.utils.LooperExecutor;
import org.webrtc.CameraEnumerationAndroid;
import org.webrtc.EglBase;
import org.webrtc.ScreenCapturerAndroid;
import org.webrtc.VideoCapturer;
import org.webrtc.VideoCapturer.ARCameraCapturerObserver;
import org.webrtc.VideoCapturerAndroid;

import java.util.concurrent.Exchanger;

import static android.Manifest.permission.CAMERA;
import static android.Manifest.permission.RECORD_AUDIO;

/**
 * Created by liuxiaozhong on 2019/1/15.
 */
public class ARRtcpKit {

    private static final String TAG = "ARRtcpKit";

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
    private ARRtcpEvent rtcpEvent;
    private final LooperExecutor mExecutor;
    private final EglBase mEglBase;

    private int mCameraId = 0;
    private VideoCapturerAndroid mVideoCapturer;
    private ScreenCapturerAndroid mVideoScreenCapturer;
    private VideoCapturerAndroid mVideoCapturerEx;

    private boolean bFront = true;
    private boolean mIsOpenExtra;

    private boolean isFrontOpenMirrorEnable = false;
    private boolean isopenAudioCheck = true;

    /**
     * 初始化RTCP对象
     */
    public ARRtcpKit(final String userId, final String userData) {
        mExecutor = ARRtcpEngine.Inst().Executor();
        mEglBase = ARRtcpEngine.Inst().Egl();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fNativeAppId = nativeCreate(mRtcpHelper);
                nativeSetUserInfo(userId, userData);

                if (ARRtcpEngine.Inst().getARRtcpOption().getVideoOrientation() == ARVideoCommon.ARVideoOrientation.Portrait) {
                    nativeSetScreenToPortrait();
                } else {
                    nativeSetScreenToLandscape();
                }
                nativeSetVideoProfileMode(ARRtcpEngine.Inst().getARRtcpOption().getVideoProfile().level);
                nativeSetVideoFpsProfile(ARRtcpEngine.Inst().getARRtcpOption().getVideoFps().level);
            }
        });
    }

    public ARRtcpKit(final String userId, final String userData, ARRtcpEvent rtcpEvent) {
        ARUtils.assertIsTrue(rtcpEvent != null);
        this.rtcpEvent = rtcpEvent;
        mExecutor = ARRtcpEngine.Inst().Executor();
        mEglBase = ARRtcpEngine.Inst().Egl();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                fNativeAppId = nativeCreate(mRtcpHelper);
                nativeSetUserInfo(userId, userData);

                if (ARRtcpEngine.Inst().getARRtcpOption().getVideoOrientation() == ARVideoCommon.ARVideoOrientation.Portrait) {
                    nativeSetScreenToPortrait();
                } else {
                    nativeSetScreenToLandscape();
                }
                nativeSetVideoProfileMode(ARRtcpEngine.Inst().getARRtcpOption().getVideoProfile().level);
                nativeSetVideoFpsProfile(ARRtcpEngine.Inst().getARRtcpOption().getVideoFps().level);
            }
        });
    }

    /**
     * 设置回调接口对象
     *
     * @param rtcpEvent
     */
    public void setRtcpEvent(final ARRtcpEvent rtcpEvent) {
        ARUtils.assertIsTrue(rtcpEvent != null);
        this.rtcpEvent = rtcpEvent;
    }

    public void clean() {
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

                if (mVideoCapturerEx != null) {
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
                    nativeSetVideoCapturerEx(null, 0);
                    mVideoCapturerEx = null;
                }
            }
        });
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
                    nativeSetVideoCapturer(null, 0);
                    mVideoCapturer = null;
                }
            }
        });
    }

    /**
     * 设置ARCamera视频回调数据
     *
     * @param capturerObserver
     */
    public void setARCameraCaptureObserver(final ARCameraCapturerObserver capturerObserver) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null) {
                    mVideoCapturer.setARCameraObserver(capturerObserver);
                }
            }
        });
    }

    /**
     * 设置是否采用ARCamera，默认使用ARCamera， 如果设置为false，必须调用setByteBufferFrameCaptured才能本地显示
     *
     * @param usedARCamera true：使用ARCamera，false：不使用ARCamera采集的数据
     */
    public void setUsedARCamera(final boolean usedARCamera) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null) {
                    mVideoCapturer.setUsedARCamera(usedARCamera);
                }
            }
        });
    }

    /**
     * 设置本地显示的视频数据
     *
     * @param data      相机采集数据
     * @param width     宽
     * @param height    高
     * @param rotation  旋转角度
     * @param timeStamp 时间戳
     */
    public void setByteBufferFrameCaptured(final byte[] data, final int width, final int height, final int rotation, final long timeStamp) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer != null) {
                    mVideoCapturer.setByteBufferFrameCaptured(data, width, height, rotation, timeStamp);
                }
            }
        });
    }

    /**
     * 是否打开音频监测
     *
     * @return
     */
    public boolean isOpenAudioCheck() {
        return isopenAudioCheck;
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
                if (ARRtcpEngine.Inst().getARRtcpOption().getMediaType() == ARVideoCommon.ARMediaType.Audio) {
                    nativeSetAuidoModel(true, open);
                } else {
                    nativeSetAuidoModel(false, open);
                }
            }
        });
    }

    /**
     * 打开或关闭前置摄像头镜面
     *
     * @param enable true: 打开; false: 关闭
     */
    public void setFrontCameraMirrorEnable(final boolean enable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                isFrontOpenMirrorEnable = enable;
                nativeSetCameraMirror(enable);
            }
        });
    }

    /**
     * 前置摄像头是否打开镜变
     *
     * @return
     */
    public boolean getFrontCameraMirror() {
        return isFrontOpenMirrorEnable;
    }

    /**
     * 打开或关闭网络状态监测
     *
     * @param enable true: 打开; false: 关闭
     */
    public void setNetworkStatus(final boolean enable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetNetworkStatus(enable);
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
    public void setScreenToLandscape() {
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
    public void setScreenToPortrait() {
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
     * @param token token字符串:客户端向自己服务器申请
     * @return true：设置成功；false：设置失败
     */
    private boolean setUserToken(final String token) {
        final Exchanger<Boolean> result = new Exchanger<Boolean>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = false;
                if (null == token || token.equals("")) {
                    ret = false;
                } else {
                    nativeSetUserToken(token);
                    ret = true;
                }
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
    }

    /**
     * 设置本地音频是否可用
     *
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
     *
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
     *
     * @return
     */
    public boolean getLocalAudioEnabled() {
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
     *
     * @return
     */
    public boolean getLocalVideoEnabled() {
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
     * @param render 底层图像地址
     * @return 打开本地预览返回值：0/1/2：没有相机权限/打开成功/打开相机失败
     */
    public int setLocalVideoCapturer(final long render) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = ARRtcpEngine.Inst().context().checkCallingOrSelfPermission( CAMERA);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    if (mVideoCapturer == null) {
                        mCameraId = 0;
                        String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(mCameraId);
                        String frontCameraDeviceName =
                                CameraEnumerationAndroid.getNameOfFrontFacingDevice();
                        int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
                        if (numberOfCameras > 1 && frontCameraDeviceName != null && ARRtcpEngine.Inst().getARRtcpOption().isDefaultFrontCamera()) {
                            cameraDeviceName = frontCameraDeviceName;
                            mCameraId = 1;
                        }
                        Log.d(TAG, "Opening camera: " + cameraDeviceName);
                        mVideoCapturer = VideoCapturerAndroid.create(cameraDeviceName, null);
                        if (mVideoCapturer == null) {
                            Log.e("sys", "Failed to open camera");
                            LooperExecutor.exchange(result, 2);
                        }
                        nativeSetVideoCapturer(mVideoCapturer, render);
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
     * 加载本地屏幕共享
     * @param render 底层图像地址
     * @param mediaProjectionPermissionResultData
     * @return 打开本地预览返回值：0/1/2：没有相机权限/打开成功/打开相机失败
     */
    public int setLocalScreenVideoCapturer(final long render, final Intent mediaProjectionPermissionResultData) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
            @Override
            public void run() {
                int ret = 0;
                int permission = ARRtcpEngine.Inst().context().checkCallingOrSelfPermission( CAMERA);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    if (mVideoScreenCapturer == null) {
                        MediaProjectionManager mediaProjectionManager =
                                null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
                            mediaProjectionManager = (MediaProjectionManager) ARRtcpEngine.Inst().context().getSystemService(
                                    Context.MEDIA_PROJECTION_SERVICE);
                        }
                        mVideoScreenCapturer = new ScreenCapturerAndroid(mediaProjectionPermissionResultData,  new MediaProjection.Callback() {
                            @Override
                            public void onStop() {
                                Log.e("sys", "User revoked permission to capture the screen.");
                            }
                        });

                        if (mVideoScreenCapturer == null) {
                            Log.e("sys", "Failed to open camera");
                            LooperExecutor.exchange(result, 2);
                        }
                        nativeSetVideoCapturer(mVideoScreenCapturer, render);
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

//    private ScreenCapturerAndroid.ScreenCallback mScreenCallback = new ScreenCapturerAndroid.ScreenCallback() {
//        @Override
//        public void ScreenData(byte[] data, int width, int height) {
//            int result = nativeSetYUV420PData(data, width, height);
//            Log.e(this.getClass().toString(), "nativeSetYUV420PData Result:   " + result);
//        }
//    };

    /**
     * 加载本地附加摄像头
     *
     * @param render 底层图像地址
     * @return 打开本地附加摄像头预览返回值：0/1/2：没有相机权限/打开成功/打开相机失败
     */
    public int setLocalExVideoCapturer(final long render) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                bFront = ARRtcpEngine.Inst().getExARRtcpOption().isDefaultFrontCamera();
                int ret = 0;
                int permission = ARRtcpEngine.Inst().context().checkCallingOrSelfPermission( CAMERA);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    // We don't have permission so prompt the user
                    int numberOfCameras = CameraEnumerationAndroid.getDeviceCount();
                    if (numberOfCameras < 2) {
                        LooperExecutor.exchange(result, 0);
                    }
                    if (mVideoCapturerEx == null) {
                        int camId = 0;
                        if (mCameraId == 0) {
                            camId = 1;
                        }
                        String cameraDeviceName = CameraEnumerationAndroid.getDeviceName(camId);
                        Log.d(TAG, "Opening extra camera: " + cameraDeviceName);
                        mVideoCapturerEx = VideoCapturerAndroid.create(cameraDeviceName, null);
                        if (mVideoCapturerEx == null) {
                            Log.e("sys", "Failed to open extra camera");
                            LooperExecutor.exchange(result, 2);
                        }
                        nativeSetExVideoProfileMode(ARRtcpEngine.Inst().getExARRtcpOption().getVideoProfile().level);
                        nativeSetExVideoFpsProfile(ARRtcpEngine.Inst().getExARRtcpOption().getVideoFps().level);
                        nativeSetVideoCapturerEx(mVideoCapturerEx, render);
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
     * 切换相机
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
     * @param userId
     * @param userData
     */
    private void setUserInfo(final String userId, final String userData) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetUserInfo(userId, userData);
            }
        });
    }

    /**
     * 打开或关闭音频数据回调开关
     *
     * @param bEnable true: 打开; false: 关闭
     */
    public void setAudioNeedPcm(final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetAudioNeedPcm(bEnable);
            }
        });
    }

    //* RTC function for rtcp

    /**
     * 发布视频
     *
     * @param token 令牌:客户端向自己服务申请获得，参考企业级安全指南
     * @return 发布结果；0/1:发布失败（没有RECORD_AUDIO权限）/发布成功
     */
    public int publishByToken(final String token, final ARVideoCommon.ARMediaType mediaType) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = ARRtcpEngine.Inst().context().checkCallingOrSelfPermission( RECORD_AUDIO);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    if (null != token && !token.equals("")) {
                        nativeSetUserToken(token);
                    }
                    ARRtcpEngine.Inst().getARRtcpOption().setMediaType(mediaType);
                    if (ARRtcpEngine.Inst().getARRtcpOption().getMediaType() == ARVideoCommon.ARMediaType.Audio) {
                        nativeSetAuidoModel(true, true);
                    } else {
                        nativeSetAuidoModel(false, true);
                    }
                    nativeSetDeviceInfo(ARRtcpEngine.Inst().getDeviceInfo());
                    // We have permission granted to the user
                    nativePublish(ARRtcpEngine.Inst().getARRtcpOption().getMediaType().type, (int) ((Math.random() * 9 + 1) * 100000) + "", false);

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
     * 发布视频
     * @param token 令牌:客户端向自己服务申请获得，参考企业级安全指南
     * @param arId 用户自定义字符串， 整个系统保持唯一，可通过arId查询频道信息
     * @param mediaType 发布类型
     * @return 发布结果；0/1:发布失败（没有RECORD_AUDIO权限）/发布成功
     */
    public int publishByToken(final String token, final String arId, final ARVideoCommon.ARMediaType mediaType) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = ARRtcpEngine.Inst().context().checkCallingOrSelfPermission( RECORD_AUDIO);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    if (null != token && !token.equals("")) {
                        nativeSetUserToken(token);
                    }
                    ARRtcpEngine.Inst().getARRtcpOption().setMediaType(mediaType);
                    if (ARRtcpEngine.Inst().getARRtcpOption().getMediaType() == ARVideoCommon.ARMediaType.Audio) {
                        nativeSetAuidoModel(true, true);
                    } else {
                        nativeSetAuidoModel(false, true);
                    }
                    nativeSetDeviceInfo(ARRtcpEngine.Inst().getDeviceInfo());
                    // We have permission granted to the user
                    nativePublish(ARRtcpEngine.Inst().getARRtcpOption().getMediaType().type, arId, false);

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
     * 发布视频
     *
     * @param token 令牌:客户端向自己服务申请获得，参考企业级安全指南
     * @param mediaType 发布媒体类型
     * @param bIsNeedTransform 是否需要转码
     * @return
     */
    public int publishByToken(final String token, final ARVideoCommon.ARMediaType mediaType, final boolean bIsNeedTransform) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = ARRtcpEngine.Inst().context().checkCallingOrSelfPermission( RECORD_AUDIO);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    if (null != token && !token.equals("")) {
                        nativeSetUserToken(token);
                    }
                    ARRtcpEngine.Inst().getARRtcpOption().setMediaType(mediaType);
                    if (ARRtcpEngine.Inst().getARRtcpOption().getMediaType() == ARVideoCommon.ARMediaType.Audio) {
                        nativeSetAuidoModel(true, true);
                    } else {
                        nativeSetAuidoModel(false, true);
                    }
                    nativeSetDeviceInfo(ARRtcpEngine.Inst().getDeviceInfo());
                    // We have permission granted to the user
                    nativePublish(ARRtcpEngine.Inst().getARRtcpOption().getMediaType().type, (int) ((Math.random() * 9 + 1) * 100000) + "", bIsNeedTransform);
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
     * 发布视频
     *
     * @param token 令牌:客户端向自己服务申请获得，参考企业级安全指南
     * @param arId 用户自定义字符串， 整个系统保持唯一，可通过arId查询频道信息
     * @param mediaType 发布媒体类型
     * @param bIsNeedTransform 是否需要转码
     * @return
     */
    public int publishByToken(final String token, final String arId, final ARVideoCommon.ARMediaType mediaType, final boolean bIsNeedTransform) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = ARRtcpEngine.Inst().context().checkCallingOrSelfPermission( RECORD_AUDIO);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    if (null != token && !token.equals("")) {
                        nativeSetUserToken(token);
                    }
                    ARRtcpEngine.Inst().getARRtcpOption().setMediaType(mediaType);
                    if (ARRtcpEngine.Inst().getARRtcpOption().getMediaType() == ARVideoCommon.ARMediaType.Audio) {
                        nativeSetAuidoModel(true, true);
                    } else {
                        nativeSetAuidoModel(false, true);
                    }
                    nativeSetDeviceInfo(ARRtcpEngine.Inst().getDeviceInfo());
                    // We have permission granted to the user
                    nativePublish(ARRtcpEngine.Inst().getARRtcpOption().getMediaType().type, arId, bIsNeedTransform);
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
     * 取消发布
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
     * 监听房间号
     * @param arId 房间号
     * @return 发布结果；0/1:监听失败/监听成功
     */
    public int listen(final String arId) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeListenIt(arId);
                LooperExecutor.exchange(result, 1);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }

    /**
     * 取消监听房间号
     * @param arId 房间号
     * @return 发布结果；0/1:监听失败/监听成功
     */
    public int unListen(final String arId) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeUnListen(arId);
                LooperExecutor.exchange(result, 1);
            }
        });
        return LooperExecutor.exchange(result, 0);
    }
    /**
     * 订阅视频
     *
     * @param rtcpId 发布的rtcpId
     * @return 订阅结果；0/1:订阅失败（没有RECORD_AUDIO权限）/订阅成功
     */
    public int subscribe(final String rtcpId, final String token) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                int ret = 0;
                int permission = ARRtcpEngine.Inst().context().checkCallingOrSelfPermission( RECORD_AUDIO);
                if (permission == PackageManager.PERMISSION_GRANTED) {
                    if (null != token && !token.equals("")) {
                        nativeSetUserToken(token);
                    }
                    nativeSetDeviceInfo(ARRtcpEngine.Inst().getDeviceInfo());
                    // We have permission granted to the user
                    nativeSubscribe(rtcpId);
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
     * 取消订阅
     *
     * @param rtcpId
     */
    public void unSubscribe(final String rtcpId) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeUnSubscribe(rtcpId);
            }
        });
    }

    /**
     * 自定义视频数据接入时，本地显示
     * @param lRender 底层图像地址
     */
    public void setLocalVideoRender(final long lRender) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalVideoRender(lRender);
            }
        });
    }

    /**
     * 按照旋转角度显示渲染本地之定义视频数据
     * @param lRender 底层图像地址
     * @param rotation ARVideoRotation 视频的角度
     */
    public void setLocalVideoRotationRender(final long lRender, final ARVideoCommon.ARVideoRotation rotation) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalVideoRotationRender(lRender, rotation.rotation);
            }
        });
    }


    /**
     * 自定义视频数据接入时，本地辐流显示
     * @param lRender 底层图像地址
     */
    public void setLocalVideoExRender(final long lRender) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalVideoExRender(lRender);
            }
        });
    }

    /**
     * 按照旋转角度显示渲染本地之定义视频数据
     * @param lRender 底层图像地址
     * @param rotation ARVideoRotation 视频的角度
     */
    public void setLocalVideoExRotationRender(final long lRender, final ARVideoCommon.ARVideoRotation rotation) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLocalVideoExRotationRender(lRender, rotation.rotation);
            }
        });
    }

    /**
     * 设置外部数据流接口
     *
     * @param enable true：打开， false：关闭
     * @param type
     */
    public void setExternalCameraCapturer(final boolean enable, final ARCaptureType type) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetExternalCameraCapturer(enable, type.type);
            }
        });
    }

    /**
     * 外部yuv数据
     * @param p_yuv
     * @param width
     * @param height
     * @return -1:分辨率或者塞流模式不正确，0：设置成功；
     */
    public int setVideoYUV420PData(byte[] p_yuv, int width, int height) {
        return nativeSetYUV420PData(p_yuv, width, height);
    }

    public int setVideoYUV420PData(byte[] y, int stride_y, byte[] u, int stride_u, byte[] v, int stride_v, int width, int height) {
        return nativeSetVideoYUV420PData(y, stride_y, u, stride_u, v, stride_v, width, height);
    }

    /**
     * 外部yuv数据
     * @param p_yuv
     * @param width
     * @param height
     * @param rotation
     * @return -1:分辨率或者塞流模式不正确，0：设置成功；
     */
    public int setVideoYUV420PRotationData(byte[] p_yuv, int width, int height, ARVideoCommon.ARVideoRotation rotation) {
        return nativeSetYUV420PRotationData(p_yuv, width, height, rotation.rotation);
    }

    /**
     * 辐流外部yuv数据
     * @param p_yuv
     * @param width
     * @param height
     * @param rotation
     * @return -1:分辨率或者塞流模式不正确，0：设置成功；
     */
    public int setVideoExYUV420PRotationData(byte[] p_yuv, int width, int height, ARVideoCommon.ARVideoRotation rotation) {
        return nativeSetExYUV420PRotationData(p_yuv, width, height, rotation.rotation);
    }

    /**
     * 外部rgb数据
     * @param p_rgb
     * @param width
     * @param height
     * @return -1:分辨率或者塞流模式不正确，0：设置成功；
     */
    private int setVideoRGB565Data(byte[] p_rgb, int width, int height) {
        return nativeSetVideoRGB565Data(p_rgb, width, height);
    }

    /**
     * @param bEnable
     */
    public void setExH264Capturer(final boolean bEnable) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetExH264Capturer(bEnable);
            }
        });
    }

    public void setVideoH264Data(final byte[] data, final int length) {
        nativeSetVideoH264Data(data, length);
    }

    /**
     * 渲染远端视频
     * @param rtcpId
     * @param render
     */
    public void setRemoteVideoRender(final String rtcpId, final long render) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRTCVideoRender(rtcpId, render);
            }
        });
    }

    /**
     * 按照指定角度渲染远端视频
     * @param rtcpId
     * @param render
     * @param rotation ARVideoRotation 视频的角度
     */
    public void setRemoteVideoRotationRender(final String rtcpId, final long render, final ARVideoCommon.ARVideoRotation rotation) {
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetRTCVideoRotationRender(rtcpId, render, rotation.rotation);
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


    /**
     * 获取UVC Camera的采集数据
     *
     * @return
     */
    public long getUVCCallabck() {
        return nativeGetAnyrtcUvcCallabck();
    }


    /**
     * UVC相机数据与RTC对接
     *
     * @param usbCamera usb相机
     * @return
     */
    public int setUvcVideoCapturer(final Object usbCamera) {
        final Exchanger<Integer> result = new Exchanger<Integer>();
        mExecutor.execute(new Runnable() {
            @Override
            public void run() {
                if (mVideoCapturer == null) {
                    mCameraId = 0;
                    nativeSetUvcVideoCapturer(usbCamera, "");
                    LooperExecutor.exchange(result, 0);
                }
            }
        });
        return LooperExecutor.exchange(result, 1);
    }

    //Jni interface
    private native long nativeCreate(Object obj);

    private native void nativeSetUserToken(String token);

    private native void nativeSetDeviceInfo(String strDevInfo);

    private native void nativeSetAuidoModel(boolean enabled, boolean audioDetect);

    private native void nativeSetCameraMirror(boolean enable);

    private native void nativeSetNetworkStatus(boolean enable);

    private native boolean nativeNetworkStatusEnabled();

    private native void nativeSetScreenToLandscape();

    private native void nativeSetScreenToPortrait();

    private native void nativeSetAudioEnable(boolean enabled);

    private native void nativeSetVideoEnable(boolean enabled);

    private native boolean nativeGetAudioEnable();

    private native boolean nativeGetVideoEnable();

    private native void nativeSetLocalPeerAudioEnable(String strRtcpId, boolean bAudioEnable);

    private native void nativeSetLocalPeerVideoEnable(String strRtcpId, boolean bVideoEnable);

    private native void nativeSetVideoCapturer(Object capturer, long nativeRenderer);

    private native void nativeSetVideoCapturerEx(Object capturer, long nativeRenderer);

    private native void nativeSetLocalVideoRender(long nativeRenderer);

    private native void nativeSetLocalVideoRotationRender(long nativeRenderer, int rotation);

    private native void nativeSetLocalVideoExRender(long nativeRenderer);

    private native void nativeSetLocalVideoExRotationRender(long nativeRenderer, int rotation);

    private native void nativeSetVideoSize(int width, int height);

    private native void nativeSetVideoMode(int nVideoMode);

    /**
     * 此接口仅使用VideoQualityMode枚举类型
     *
     * @param nVideoMode
     */
    private native void nativeSetVideoModeExcessive(int nVideoMode);

    private native void nativeSetVideoBitrate(int bitrate);

    private native void nativeSetVideoFps(int fps);

    /**
     * 此接口仅使用枚举ARVideoProfile枚举类型
     *
     * @param nVideoMode
     */
    private native void nativeSetVideoProfileMode(int nVideoMode);

    private native void nativeSetVideoFpsProfile(int nFpsMode);


    private native void nativeSetExVideoMode(int nVideoMode);

    /**
     * 此接口仅使用VideoQualityMode枚举类型
     *
     * @param nVideoMode
     */
    private native void nativeSetExVideoModeExcessive(int nVideoMode);

    private native void nativeSetExVideoBitrate(int bitrate);

    private native void nativeSetExVideoFps(int fps);

    /**
     * 此接口仅使用枚举ARVideoProfile枚举类型
     *
     * @param nVideoMode
     */
    private native void nativeSetExVideoProfileMode(int nVideoMode);

    private native void nativeSetExVideoFpsProfile(int nFpsMode);

    private native void nativeSetUserInfo(String strUserid, String strUserData);

    private native void nativeSetAudioNeedPcm(boolean enable);

    private native void nativePublish(int mType, String arId, boolean bLive);

    private native void nativeUnPublish();

    private native void nativeListenIt(String arId);

    private native void nativeUnListen(String arId);

    private native void nativePublishEx();

    private native void nativeUnPublishEx();

    private native void nativeSubscribe(String strRtcpId);

    private native void nativeUnSubscribe(String strRtcpId);

    private native void nativeSetExternalCameraCapturer(boolean enable, int type);

    private native int nativeSetYUV420PData(byte[] p_yuv, int width, int height);

    private native int nativeSetVideoYUV420PData(byte[] y, int stride_y, byte[] u, int stride_u, byte[] v, int stride_v, int width, int height);

    private native int nativeSetYUV420PRotationData(byte[] p_yuv, int width, int height, int rotation);

    private native int nativeSetExYUV420PRotationData(byte[] p_yuv, int width, int height, int rotation);

    private native int nativeSetVideoRGB565Data(byte[] p_rgb, int width, int height);

    private native void nativeSetExH264Capturer(boolean bEnable);

    private native void nativeSetVideoH264Data(byte[] data, int length);

    private native void nativeSetRTCVideoRender(String strRtcpId, long nativeRenderer);

    private native void nativeSetRTCVideoRotationRender(String strRtcpId, long nativeRenderer, int rotation);

    private native void nativeDestroy();

    private native long nativeGetAnyrtcUvcCallabck();

    private native void nativeSetUvcVideoCapturer(Object capturer, String strImg);


    private ARRtcpHelper mRtcpHelper = new ARRtcpHelper() {
        @Override
        public void OnPublishOK(String rtcpId, String liveInfo) {
            if (null != rtcpEvent) {
                rtcpEvent.onPublishOK(rtcpId, liveInfo);
            }
        }

        @Override
        public void OnPublishFailed(int code, String reason) {
            if (null != rtcpEvent) {
                rtcpEvent.onPublishFailed(code, reason);
            }
        }

        @Override
        public void OnPublishExOK(String rtcpId, String liveInfo) {
            rtcpEvent.onPublishExOK(rtcpId, liveInfo);
        }

        @Override
        public void OnPublishExFailed(int nCode, String strReason) {
            rtcpEvent.onPublishExFailed(nCode, strReason);
        }

        @Override
        public void OnSubscribeOK(String rtcp) {
            if (null != rtcpEvent) {
                rtcpEvent.onSubscribeOK(rtcp);
            }
        }

        @Override
        public void OnSubscribeFailed(String rtcpId, int code, String reason) {
            if (null != rtcpEvent) {
                rtcpEvent.onSubscribeFailed(rtcpId, code, reason);
            }
        }

        @Override
        public void OnRtcOpenVideoRender(String rtcpId) {
            if (null != rtcpEvent) {
                rtcpEvent.onRTCOpenRemoteVideoRender(rtcpId);
            }
        }

        @Override
        public void OnRtcCloseVideoRender(String rtcpId) {
            if (null != rtcpEvent) {
                rtcpEvent.onRTCCloseRemoteVideoRender(rtcpId);
            }
        }

        @Override
        public void OnRtcOpenAudioTrack(String rtcpId, String userId) {
            if (null != rtcpEvent) {
                rtcpEvent.onRTCOpenRemoteAudioTrack(rtcpId);
            }
        }

        @Override
        public void OnRtcCloseAudioTrack(String rtcpId, String userId) {
            if (null != rtcpEvent) {
                rtcpEvent.onRTCCloseRemoteAudioTrack(rtcpId);
            }
        }

        @Override
        public void OnRtcAudioPcmData(String strRtcpId, byte[] data, int nLen, int nSampleHz, int nChannel) {
            if (null != rtcpEvent) {
                if(strRtcpId.equals("localAudio")) {
                    rtcpEvent.onRTCLocalAudioPcmData(strRtcpId, data, nLen, nSampleHz, nChannel);
                } else {
                    rtcpEvent.onRTCRemoteAudioPcmData(strRtcpId, data, nLen, nSampleHz, nChannel);
                }
            }
        }

        @Override
        public void OnRtcAVStatus(String rtcpId, boolean bAudio, boolean bVideo) {
            if (null != rtcpEvent) {
                rtcpEvent.onRTCRemoteAVStatus(rtcpId, bAudio, bVideo);
            }

        }

        @Override
        public void OnRtcAudioActive(String rtcpId, String userId, int nLevel, int nShowTime) {
            if (null != rtcpEvent) {
                if (rtcpId.equals("RtcPublisher")) {
                    rtcpEvent.onRTCLocalAudioActive(nLevel, nShowTime);
                } else {
                    rtcpEvent.onRTCRemoteAudioActive(rtcpId, nLevel, nShowTime);
                }
            }
        }

        @Override
        public void OnRtcNetworkStatus(String rtcpId, String userId, int netSpeed, int packetLost) {
            if (null != rtcpEvent) {
                ARNetQuality netQuality = null;
                if (packetLost <= 1) {
                    netQuality = ARNetQuality.ARNetQualityExcellent;
                } else if (packetLost > 1 && packetLost <= 3) {
                    netQuality = ARNetQuality.ARNetQualityGood;
                } else if (packetLost > 3 && packetLost <= 5) {
                    netQuality = ARNetQuality.ARNetQualityAccepted;
                } else if (packetLost > 5 && packetLost <= 10) {
                    netQuality = ARNetQuality.ARNetQualityBad;
                } else {
                    netQuality = ARNetQuality.ARNetQualityVBad;
                }
                if (rtcpId.equals("RtcPublisher")) {
                    rtcpEvent.onRTCLocalNetworkStatus(netSpeed, packetLost, netQuality);
                } else {
                    rtcpEvent.onRTCRemoteNetworkStatus(rtcpId, netSpeed, packetLost, netQuality);
                }
            }
        }
    };
}
