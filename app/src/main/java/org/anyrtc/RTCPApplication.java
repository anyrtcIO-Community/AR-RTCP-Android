package org.anyrtc;

import android.app.Application;

import org.anyrtc.rtcp_kit.AnyRTCRTCPEngine;

/**
 * Created by liuxiaozhong on 2017-10-24.
 */

public class RTCPApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //初始化RTCP引擎
        //配置开发者信息 可去anyrtc.io官网注册获取
        AnyRTCRTCPEngine .Inst().initEngineWithAnyrtcInfo(getApplicationContext(),"DeveloperID", "APPID", "APPKEY", "APPTOKEN");
        //配置私有云  没有可不填写
//        AnyRTCRTCPEngine.Inst().configServerForPriCloud("", 0);

    }
}
