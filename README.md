
### ARRTCP SDK for Android
### 简介
ARRTCP SDK为您打造实时视频传输体验，支持秒开，集成简单，几行代码即可拥有音视频能力。
支持Android,Ios,Web，实时直播无延迟。



### app体验

##### 扫码下载
![image](https://www.pgyer.com/app/qrcode/so6a)
##### [点击下载](https://www.pgyer.com/so6a)
##### [WEB在线体验](https://beyond.anyrtc.cc/demo/rtcp)

### SDK集成
# > 方式一 [ ![Download](https://api.bintray.com/packages/dyncanyrtc/ar_dev/rtcp/images/download.svg) ](https://bintray.com/dyncanyrtc/ar_dev/rtcp/_latestVersion)

添加Jcenter仓库 Gradle依赖：

```
dependencies {
  compile 'org.ar:rtcp_kit:3.0.1'
}
```

或者 Maven
```
<dependency>
  <groupId>org.ar</groupId>
  <artifactId>rtcp_kit</artifactId>
  <version>3.0.1</version>
  <type>pom</type>
</dependency>
```


### 安装

##### 编译环境

AndroidStudio

##### 运行环境

Android API 16+
真机运行

### 如何使用

##### 注册开发者信息

>如果您还未注册anyRTC开发者账号，请登录[anyRTC官网](http://www.anyrtc.io)注册及获取更多的帮助。

##### 替换开发者账号
在[anyRTC官网](http://www.anyrtc.io)获取了开发者账号，AppID等信息后，替换DEMO中
**DeveloperInfo**类中的开发者信息即可

### 操作步骤

1、一部手机开启直播，点击右上角展示二维码按钮。

2、另一部手机复制，点击观看直播，扫码，开始观看直播。

### 完整文档
SDK集成，API介绍，详见官方完整文档：[点击查看](https://docs.anyrtc.io/v1/RTCP/android.html)

### iOS版RTCP实时直播

[AR-RTCP-iOS](https://github.com/AnyRTC/anyRTC-RTCP-iOS)

### Web版RTCP实时直播

[AR-RTCP-Web](https://github.com/anyRTC/anyRTC-RTCP-Web)


### 支持的系统平台
**Android** 4.0及以上

### 支持的CPU架构
**Android** armv7 arm64  


### 注意事项
1. RTCP SDK所有回调均在子线程中，所以在回调中操作UI等，应切换主线程。
2. 订阅媒体所需id为发布媒体成功回调中的strRtcpId。详见DEMO
3. 发布直播订阅直播注意安卓6.0+动态权限处理。
4. 常见错误代码请参考[错误码查询](https://www.anyrtc.io/resoure)

### 技术支持
- anyRTC官方网址：[https://www.anyrtc.io](https://www.anyrtc.io/resoure)
- QQ技术咨询群：554714720
- 联系电话:021-65650071-816
- Email:hi@dync.cc

### 关于直播

本公司有一整套完整直播解决方案。本公司开发者平台www.anyrtc.io。除了基于RTMP协议的直播系统外，我公司还有基于WebRTC的时时交互直播系统、P2P呼叫系统、会议系统等。快捷集成SDK，便可让你的应用拥有时时通话功能。欢迎您的来电~

### License

- RTCPEngine is available under the MIT license. See the LICENSE file for more info.





   



 
