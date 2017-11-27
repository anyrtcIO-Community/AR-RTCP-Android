### anyRTCP SDK for Android
#### 简介
RTCP为您打造实时视频传输体验，支持秒开，集成简单，几行代码即可拥有音视频能力。
支持Android,Ios,Web，实时直播无延迟。

#### 特性
- 零秒级超低延迟
- 三端互通

#### 截图
![enter image description here](https://github.com/AnyRTC/anyRTC-RTCP-iOS/blob/master/RTCP.gif)
#### app体验

##### 扫码下载
![enter image description here](https://www.pgyer.com/app/qrcode/anyrtc_rtcp1)
##### [点击下载](https://www.pgyer.com/app/qrcode/anyrtc_rtcp1)
##### [WEB在线体验](https://www.anyrtc.cc/demo/rtcp)

#### 快速入门

以下演示了如何初始化RTCP引擎，发起直播。更多细节用法可以参考本[Demo工程](https://github.com/AnyRTC/anyRTC-RTCP-Android/tree/master/app)。

##### STEP-1.初始化RTCP引擎并配置开发者信息

>如果您还没有开发者信息或者还不了解anyRTC云平台，请登录[anyRTC官网](http://www.anyrtc.io)获取更多的帮助。

```
public class RTCPApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //初始化 RTCP SDK引擎,全局只需调用一次
        AnyRTCRTCPEngine.Inst().init(getApplicationContext());
        //配置开发者账号信息，可从AnyRTC官网管理中心获得
        AnyRTCRTCPEngine.Inst().InitEngineWithAnyrtcInfo(DEVELOPERID, APPID, APPKEY, APPTOKEN);
        //配置私有云信息。当使用私有云时才需要调用该接口配置，默认无需配置
        //AnyRTCRTCPEngine.Inst().ConfigServerForPriCloud("", 0000);
    }
}
```
##### STEP-2.实例化RTCP对象并发起直播
> 以下仅为简单api说明，推荐将rtcp对象写成单利模式，全局持有一个对象。参见[RtcpCore类](https://github.com/AnyRTC/anyRTC-RTCP-Android/blob/master/app/src/main/java/org/anyrtc/RtcpCore.java)
```
RtcpKit rtcpKit = new RtcpKit();
rtcpKit.publish("000000");
```
##### 完整文档
SDK集成，API介绍，详见官方完整文档：[点击查看](https://www.anyrtc.io/resoure)


##### 注意事项
1. RTCP SDK所有回调均在子线程中，所以在回调中操作UI等，应切换主线程。
2. 订阅媒体所需id为发布媒体成功回调中的strRtcpId。详见DEMO
3. 发布直播订阅直播注意安卓6.0+动态权限处理。
4. 常见错误代码请参考[错误码查询](https://www.anyrtc.io/resoure)
##### License

- MIT License

##### 技术支持 
- anyRTC官方网址：[https://www.anyrtc.io](https://www.anyrtc.io/resoure)
- QQ技术咨询群：580477436






   



 
