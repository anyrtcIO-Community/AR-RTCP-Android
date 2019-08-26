package org.ar.rtcp_kit;

import org.ar.common.enums.ARNetQuality;

/**
 * Created by liuxiaozhong on 2019/1/15.
 */

public abstract class ARRtcpEvent {

    /** Publish video OK
     * @param rtcpId
     */
    public  void onPublishOK(String rtcpId,String liveInfo){}

    /** Publish video Failed
     * @param code
     */
    public  void onPublishFailed(int code, String reason){}

    public  void onPublishExOK(String rtcpId, String liveInfo){}

    public  void onPublishExFailed(int code, String strReason){}

    /** Subscribe video OK
     * @param rtcpId
     */
    public  void onSubscribeOK(String rtcpId){}

    /** Publish video Failed
     * @param rtcpId
     * @param code
     */
    public  void onSubscribeFailed(String rtcpId, int code, String reason){}

    /** OnRTCOpenVideoRender
     * @param rtcpId
     */
    public  void onRTCOpenRemoteVideoRender(String rtcpId){}

    /** OnRTCCloseVideoRender
     * @param rtcpId
     */
    public  void onRTCCloseRemoteVideoRender(String rtcpId){}

    public  void onRTCOpenRemoteAudioTrack(String rtcpId){}

    public  void onRTCCloseRemoteAudioTrack(String rtcpId){}

    public  void onRTCRemoteAVStatus(String rtcpId, boolean bAudio, boolean bVideo){}

    public  void onRTCLocalAudioPcmData(String peerId, byte[] data, int nLen, int nSampleHz, int nChannel){}

    public  void onRTCRemoteAudioPcmData(String peerId, byte[] data, int nLen, int nSampleHz, int nChannel){}

    public  void onRTCRemoteAudioActive(String rtcpId, int nLevel, int nTime){}

    public  void onRTCLocalAudioActive( int nLevel, int nTime){}

    public  void onRTCRemoteNetworkStatus(String rtcpId, int nNetSpeed, int nPacketLost, ARNetQuality netQuality){}

    public  void onRTCLocalNetworkStatus(int nNetSpeed, int nPacketLost, ARNetQuality netQuality){}
}
