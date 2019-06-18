package org.anyrtc.rtcp_kit;

import android.content.Context;
import android.support.v4.BuildConfig;

import org.anyrtc.common.enums.AnyRTCLogLevel;
import org.anyrtc.common.utils.DeviceUtils;
import org.anyrtc.common.utils.LooperExecutor;
import org.anyrtc.common.utils.NetworkUtils;
import org.json.JSONException;
import org.json.JSONObject;
import org.webrtc.ContextUtils;
import org.webrtc.EglBase;
import org.webrtc.MediaCodecVideoDecoder;
import org.webrtc.MediaCodecVideoEncoder;

import java.util.concurrent.Exchanger;

/**
 * Created by Eric on 2017/10/17.
 */
@Deprecated
public class AnyRTCRTCPEngine {

    /**
     * 加载api所需要的动态库
     */
    static {
        System.loadLibrary("rtcp-jni");
    }

    private final LooperExecutor executor;
    private final EglBase eglBase;
    private Context context;
    private String developerId, appId, appKey, appToken;
    private String strSvrAddr = "cloud.anyrtc.io";
    private AnyRTCRTCPOption option = new AnyRTCRTCPOption();
    private AnyRTCRTCPOption exOption = new AnyRTCRTCPOption();

    private static class SingletonHolder {
        private static final AnyRTCRTCPEngine INSTANCE = new AnyRTCRTCPEngine();
    }

    public static final AnyRTCRTCPEngine Inst() {
        return SingletonHolder.INSTANCE;
    }

    private AnyRTCRTCPEngine() {
        executor = new LooperExecutor();
        eglBase = EglBase.create();
//        DisableHWEncode();
//        disableHWDecode();
        executor.requestStart();
    }

    public AnyRTCRTCPOption getAnyRTCRTCPOption() {
        return option;
    }

    public AnyRTCRTCPOption getExAnyRTCRTCPOption() {
        return exOption;
    }

    public void setAnyRTCRTCPOption(AnyRTCRTCPOption option) {
        this.option = option;
    }

    public void setExAnyRTCRTCPOption(AnyRTCRTCPOption option) {
        this.exOption = option;
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

    public static void disableHWEncode() {
        MediaCodecVideoEncoder.disableVp8HwCodec();
        MediaCodecVideoEncoder.disableVp9HwCodec();
        MediaCodecVideoEncoder.disableH264HwCodec();
    }

    public static void disableHWDecode() {
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

    /**
     * 初始化anyRTC信息
     *
     * @param strDeveloperId
     * @param strAppId
     * @param strAESKey
     * @param strToken
     */
    public void initAnyrtcInfo(final String strDeveloperId, final String strAppId,
                               final String strAESKey, final String strToken) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                developerId = strDeveloperId;
                appId = strAppId;
                appKey = strAESKey;
                appToken = strToken;
                nativeInitEngineWithAnyrtcInfo(strDeveloperId, strAppId, strAESKey, strToken);
            }
        });
    }

    public void initEngineWithAnyrtcInfo(final Context ctx, final String strDeveloperId, final String strAppId,
                                         final String strAESKey, final String strToken) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                developerId = strDeveloperId;
                appId = strAppId;
                appKey = strAESKey;
                appToken = strToken;
                context = ctx;
                ContextUtils.initialize(ctx);
                nativeInitCtx(ctx, eglBase.getEglBaseContext());
                nativeInitEngineWithAnyrtcInfo(strDeveloperId, strAppId, strAESKey, strToken);
            }
        });
    }

    /**
     * 初始化应用信息
     *
     * @param ctx
     * @param strAppId
     * @param strToken
     */
    public void initEngineWithAppInfo(final Context ctx, final String strAppId, final String strToken) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                appId = strAppId;
                appToken = strToken;
                ContextUtils.initialize(ctx);
                nativeInitCtx(ctx, eglBase.getEglBaseContext());
                context = ctx;
                nativeInitEngineWithAppInfo(strAppId, strToken);
            }
        });
    }

    public void configServerForPriCloud(final String strAddr, final int nPort) {
        strSvrAddr = strAddr;
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeConfigServerForPriCloud(strAddr, nPort);
            }
        });
    }

    /**
     * 设置日志显示级别
     *
     * @param logLevel 日志显示级别
     */
    public void setLogLevel(final AnyRTCLogLevel logLevel) {
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetLogLevel(logLevel.type);
            }
        });
    }

    public void setAuidoModel(final boolean bEnabled, final boolean bAudioDetect) {
        executor.execute(new Runnable() {
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
        executor.execute(new Runnable() {
            @Override
            public void run() {
                nativeSetCameraMirror(bEnable);
            }
        });
    }

    /**
     * 打开或关闭网络状态监测
     *
     * @param bEnable true: 打开; false: 关闭
     */
    public void setNetworkStatus(final boolean bEnable) {
        executor.execute(new Runnable() {
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
        executor.execute(new Runnable() {
            @Override
            public void run() {
                boolean ret = nativeNetworkStatusEnabled();
                LooperExecutor.exchange(result, ret);
            }
        });
        return LooperExecutor.exchange(result, false);
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

    public void dispose() {
        executor.requestStop();
    }

    /**
     * 获取设备信息
     * @return
     */
    protected String getDeviceInfo() {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("operatorName", NetworkUtils.getNetworkOperatorName()); //运营商名字，类似于中国移动，中国联通，这些运营商信息
            jsonObject.put("devType", 0);//设备类型： 0/1/2/3/4:android/ios/web/wechat/pc
            jsonObject.put("devName", DeviceUtils.getManufacturer() + "-" + DeviceUtils.getModel());//设备名字：MI9，H8类似这些
            jsonObject.put("networkType", NetworkUtils.getNetworkType().toString().replace("NETWORK_", "")); //网络类型：2G/3G/4G/WIFI等
            jsonObject.put("osType", "Android " + DeviceUtils.getSDKVersionName());//系统版本，类似Android 7.0这些版本信息
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

    private static native void nativeInitEngineWithAnyrtcInfo(String strDeveloperId, String strAppId,
                                                              String strAESKey, String strToken);

    private static native void nativeInitEngineWithAppInfo(String strAppId, String strToken);

    private static native void nativeConfigServerForPriCloud(String strAddr, int nPort);

    private native void nativeSetLogLevel(int logLevel);

    private static native void nativeSetAuidoModel(boolean enabled, boolean audioDetect);

    private native void nativeSetCameraMirror(boolean bEnable);

    private native void nativeSetNetworkStatus(boolean bEnable);

    private native boolean nativeNetworkStatusEnabled();
}
