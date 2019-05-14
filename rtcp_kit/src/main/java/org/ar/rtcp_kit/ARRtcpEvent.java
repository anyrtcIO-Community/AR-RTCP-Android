package org.ar.rtcp_kit;

import org.ar.common.enums.ARNetQuality;

/**
 * Created by liuxiaozhong on 2019/1/15.
 */

public abstract class ARRtcpEvent {

    /** Publish video OK
     * @param rtcpId
     */
    public abstract void onPublishOK(String rtcpId,String liveInfo);

    /** Publish video Failed
     * @param code
     */
    public abstract void onPublishFailed(int code, String reason);

    public abstract void onPublishExOK(String rtcpId, String liveInfo);

    public abstract void onPublishExFailed(int code, String strReason);

    /** Subscribe video OK
     * @param rtcpId
     */
    public abstract void onSubscribeOK(String rtcpId);

    /** Publish video Failed
     * @param rtcpId
     * @param code
     */
    public abstract void onSubscribeFailed(String rtcpId, int code, String reason);

    /** OnRTCOpenVideoRender
     * @param rtcpId
     */
    public abstract void onRTCOpenRemoteVideoRender(String rtcpId);

    /** OnRTCCloseVideoRender
     * @param rtcpId
     */
    public abstract void onRTCCloseRemoteVideoRender(String rtcpId);

    public abstract void onRTCOpenRemoteAudioTrack(String rtcpId);

    public abstract void onRTCCloseRemoteAudioTrack(String rtcpId);

    public abstract void onRTCRemoteAVStatus(String rtcpId, boolean bAudio, boolean bVideo);

    public abstract void onRTCRemoteAudioActive(String rtcpId, int nLevel, int nTime);

    public abstract void onRTCLocalAudioActive( int nLevel, int nTime);

    public abstract void onRTCRemoteNetworkStatus(String rtcpId, int nNetSpeed, int nPacketLost, ARNetQuality netQuality);

    public abstract void onRTCLocalNetworkStatus(int nNetSpeed, int nPacketLost, ARNetQuality netQuality);
}
