package org.ar.rtcp_kit;

import android.content.Context;
import android.support.v4.BuildConfig;

import org.ar.common.enums.ARLogLevel;
import org.ar.common.utils.DeviceUtils;
import org.ar.common.utils.LooperExecutor;
import org.ar.common.utils.NetworkUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.ContextUtils;
import org.webrtc.EglBase;
import org.webrtc.MediaCodecVideoDecoder;
import org.webrtc.MediaCodecVideoEncoder;

/**
 * Created by liuxiaozhong on 2019/1/15.
 */
public class ARRtcpEngine {

    /**
     * 加载api所需要的动态库
     */
    static {
        System.loadLibrary("rtcp-jni");
    }

    private final LooperExecutor executor;
    private final EglBase eglBase;
    private Context context;
    private String strSvrAddr = "cloud.anyrtc.io";
    private ARRtcpOption option = new ARRtcpOption();
    private ARRtcpOption exOption = new ARRtcpOption();

    private static class SingletonHolder {
        private static final ARRtcpEngine INSTANCE = new ARRtcpEngine();
    }

    public static final ARRtcpEngine Inst() {
        return SingletonHolder.INSTANCE;
    }

    private ARRtcpEngine() {
        executor = new LooperExecutor();
        eglBase = EglBase.create();
//        DisableHWEncode();
//        disableHWDecode();
        executor.requestStart();
    }

    public ARRtcpOption getARRtcpOption() {
        return option;
    }

    public ARRtcpOption getExARRtcpOption() {
        return exOption;
    }

    public LooperExecutor Executor() {
        return executor;
    }

    public EglBase Egl() {
        return eglBase;
    }

    public Context context() {
        return context;
    }

    public void disableHWEncode() {
        MediaCodecVideoEncoder.disableVp8HwCodec();
        MediaCodecVideoEncoder.disableVp9HwCodec();
        MediaCodecVideoEncoder.disableH264HwCodec();
    }

    public void disableHWDecode() {
        MediaCodecVideoDecoder.disableVp8HwCodec();
        MediaCodecVideoDecoder.disableVp9HwCodec();
        MediaCodecVideoDecoder.disableH264HwCodec();
    }

    /**
     * 初始化引擎
     *
     * @param ctx
     */
    public void initEngine(final Context ctx) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                context = ctx;
                ContextUtils.initialize(ctx);
                nativeInitCtx(ctx, eglBase.getEglBaseContext());
            }
        });
    }

    public void initEngineWithARInfo(final Context ctx, final String strDeveloperId, final String strAppId,
                                         final String strAESKey, final String strToken) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                context = ctx;
                ContextUtils.initialize(ctx);
                nativeInitCtx(ctx, eglBase.getEglBaseContext());
                nativeInitEngineWithARInfo(strDeveloperId, strAppId, strAESKey, strToken);
            }
        });
    }
    /**
     * 初始化anyRTC信息
     *
     * @param appId
     * @param token
     */
    public void initAppInfo(final String appId, final String token) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeInitEngineWithAppInfo(appId, token);
            }
        });
    }

    /**
     * 初始化应用信息
     *
     * @param ctx
     * @param appId
     * @param token
     */
    public void initEngine(final Context ctx, final String appId, final String token) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                ContextUtils.initialize(ctx);
                nativeInitCtx(ctx, eglBase.getEglBaseContext());
                context = ctx;
                nativeInitEngineWithAppInfo(appId, token);
            }
        });
    }

    /**
     * 设置私有云地址
     * @param address 私有云ip地址或者域名
     * @param port 端口
     */
    public void configServerForPriCloud(final String address, final int port) {
        strSvrAddr = address;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeConfigServerForPriCloud(address, port);
            }
        });
    }

    /**
     * 设置日志显示级别
     *
     * @param logLevel 日志显示级别
     */
    public void setLogLevel(final ARLogLevel logLevel) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLogLevel(logLevel.type);
            }
        });
    }

    /**
     * 获取sdk版本号
     *
     * @return RTMPC版本号
     */
    public String getSdkVersion() {
        return BuildConfig.VERSION_NAME;
    }

    public String getPackageName() {
        return context.getPackageName();
    }

    private void dispose() {
        executor.requestStop();
    }

    /**
     * 获取设备信息
     * @return
     */
    protected String getDeviceInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("operatorName", NetworkUtils.getNetworkOperatorName()); //运营商名字
            jsonObject.put("devType", 0);//设备类型： 0/1/2/3/4:android/ios/web/wechat/pc
            jsonObject.put("devName", DeviceUtils.getManufacturer() + "-" +DeviceUtils.getModel());//设备名字：MI9，H8类似这些
            jsonObject.put("networkType", NetworkUtils.getNetworkType().toString().replace("NETWORK_", "")); //网络类型：2G/3G/4G/WIFI
            jsonObject.put("osType", "Android " + DeviceUtils.getSDKVersionName());//系统版本，类似Android 7.0
            jsonObject.put("sdkVer", getSdkVersion());//SDK版本
            jsonObject.put("rtcVer", 60);//服务版本：有了写上，没有了可以不写。
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    /**
     * Jni interface
     */
    private static native void nativeInitCtx(Context ctx, EglBase.Context context);

    private static native void nativeInitEngineWithARInfo(String strDeveloperId, String strAppId,
                                                              String strAESKey, String strToken);

    private static native void nativeInitEngineWithAppInfo(String appId, String token);

    private static native void nativeConfigServerForPriCloud(String address, int port);

    private static native void nativeSetLogLevel(int logLevel);

}
