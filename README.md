### anyRTCP SDK for Android
### 简介
anyRTCP SDK为您打造实时视频传输体验，支持秒开，集成简单，几行代码即可拥有音视频能力。
支持Android,Ios,Web，实时直播无延迟。

### 项目展示
![image](https://github.com/AnyRTC/anyRTC-RTCP-Android/blob/master/images/rtcp1.jpg)
![image](https://github.com/AnyRTC/anyRTC-RTCP-Android/blob/master/images/rtcp2.jpg)
![image](https://github.com/AnyRTC/anyRTC-RTCP-Android/blob/master/images/rtcp3.jpg)

### app体验

##### 扫码下载
![image](https://github.com/AnyRTC/anyRTC-RTCP-Android/blob/master/images/demo_qrcode.png)
##### [点击下载](https://www.pgyer.com/app/qrcode/anyrtc_rtcp1)
##### [WEB在线体验](https://www.anyrtc.cc/demo/rtcp)

### SDK集成
# > 方式一（推荐）

添加Jcenter仓库 Gradle依赖：

```
dependencies {
   compile 'org.anyrtc:rtcp_kit:2.1'
}
```

或者 Maven
```
<dependency>
  <groupId>org.anyrtc</groupId>
  <artifactId>rtcp_kit</artifactId>
  <version>2.1</version>
  <type>pom</type>
</dependency>
```

>方式二

 [下载aar SDK](https://www.anyrtc.io/resoure)

>1. 将下载好的rtcp-release.aar文件放入项目的libs目录中
>2. 在Model下的build.gradle文件添加如下代码依赖rtcp SDK

```
android
{

 repositories {
        flatDir {dirs 'libs'}
    }
    
 }
    
```
```
dependencies {
    compile(name: 'rtcp-release', ext: 'aar')
}
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
**RTCPApplication**类中的开发者信息即可

### 操作步骤

1、一部手机开启直播，点击右上角复制按钮，将复制的内容发送给另一部手机；

2、另一部手机复制，点击观看直播，粘贴复制的内容到输入框，开始观看直播。

### 完整文档
SDK集成，API介绍，详见官方完整文档：[点击查看](https://www.anyrtc.io/resoure)

### Ios版anyRTC-RTCP实时直播

[anyRTC-RTCP-Ios](https://github.com/AnyRTC/anyRTC-RTCP-iOS)

### Web版anyRTC-RTCP实时直播在线体验

[anyRTC-RTCP-Web](https://www.anyrtc.io/demo/rtcp)


### 支持的系统平台
**Android** 4.0及以上

### 支持的CPU架构
**Android** armv7 arm64  


### 注意事项
1. RTCP SDK所有回调均在子线程中，所以在回调中操作UI等，应切换主线程。
2. 订阅媒体所需id为发布媒体成功回调中的strRtcpId。详见DEMO
3. 发布直播订阅直播注意安卓6.0+动态权限处理。
4. 常见错误代码请参考[错误码查询](https://www.anyrtc.io/resoure)

### 商业授权
程序发布需商用授权，业务咨询请联系 QQ:984630262 

QQ交流群:580477436

联系电话:021-65650071

Email:zhangjianqiang@dync.cc

### 技术支持 
- anyRTC官方网址：[https://www.anyrtc.io](https://www.anyrtc.io/resoure)
- QQ技术咨询群：580477436
- 

### 关于直播

本公司有一整套完整直播解决方案。本公司开发者平台www.anyrtc.io。除了基于RTMP协议的直播系统外，我公司还有基于WebRTC的时时交互直播系统、P2P呼叫系统、会议系统等。快捷集成SDK，便可让你的应用拥有时时通话功能。欢迎您的来电~

### License

- RTCPEngine is available under the MIT license. See the LICENSE file for more info.





   



 
