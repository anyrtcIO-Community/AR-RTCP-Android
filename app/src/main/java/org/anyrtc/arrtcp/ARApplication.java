package org.anyrtc.arrtcp;

import android.app.Application;
import android.text.TextUtils;

import org.ar.common.utils.SharePrefUtil;
import org.ar.rtcp_kit.ARRtcpEngine;


/**
 * Created by liuxiaozhong on 2019/3/11.
 */
public class ARApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //初始化RTCP引擎,配置开发者信息 可去anyrtc.io官网注册获取
        SharePrefUtil.init(this);
        boolean isDevMode = SharePrefUtil.getBoolean("isDevMode");
        if (!isDevMode) {
            ARRtcpEngine.Inst().initEngine(getApplicationContext(), DeveloperInfo.APPID, DeveloperInfo.APPTOKEN);
        }else {
            String appid = SharePrefUtil.getString("appid");
            String apptoken = SharePrefUtil.getString("apptoken");
            String ip = SharePrefUtil.getString("ip");
            ARRtcpEngine.Inst().initEngine(getApplicationContext(), appid, apptoken);
            if (!TextUtils.isEmpty(ip)) {
                ARRtcpEngine.Inst().configServerForPriCloud(ip, 9080);
            }

        }
    }




}
