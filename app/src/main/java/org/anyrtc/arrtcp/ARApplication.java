package org.anyrtc.arrtcp;

import android.app.Application;

import org.ar.rtcp_kit.ARRtcpEngine;


/**
 * Created by liuxiaozhong on 2019/3/11.
 */
public class ARApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化RTCP引擎,配置开发者信息 可去anyrtc.io官网注册获取
        ARRtcpEngine.Inst().initEngineWithARInfo(getApplicationContext(),  DeveloperInfo.DEVELOPERID,DeveloperInfo.APPID, DeveloperInfo.APPKEY, DeveloperInfo.APPTOKEN);
        ARRtcpEngine.Inst().configServerForPriCloud("pro.anyrtc.io",9060);
    }




}
