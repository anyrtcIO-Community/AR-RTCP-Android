package org.anyrtc.rtcp_kit;

import org.anyrtc.common.enums.AnyRTCNetQuality;

/**
 * Created by Skyline on 2017/11/13.
 */
@Deprecated
public abstract class AnyRTCRTCPEvent {

    /**
     * Publish video OK
     *
     * @param strRtcpId
     */
    public abstract void onPublishOK(String strRtcpId, String strLiveInfo);

    /**
     * Publish video Failed
     *
     * @param nCode
     */
    public abstract void onPublishFailed(int nCode, String strReason);

    /**
     * Publish extra video OK
     *
     * @param strRtcpId
     */
    public abstract void onPublishExOK(String strRtcpId, String strLiveInfo);

    /**
     * Publish extra video Failed
     *
     * @param nCode
     * @param strReason
     */
    public abstract void onPublishExFailed(int nCode, String strReason);

    /**
     * Subscribe video OK
     *
     * @param strRtcpId
     */
    public abstract void onSubscribeOK(String strRtcpId);

    /**
     * Publish video Failed
     *
     * @param strRtcpId
     * @param nCode
     */
    public abstract void onSubscribeFailed(String strRtcpId, int nCode, String strReason);

    /**
     * OnRTCOpenVideoRender
     *
     * @param strRtcpId
     */
    public abstract void onRTCOpenVideoRender(String strRtcpId);

    /**
     * OnRTCCloseVideoRender
     *
     * @param strRtcpId
     */
    public abstract void onRTCCloseVideoRender(String strRtcpId);

    public abstract void onRTCOpenAudioTrack(String strRtcpId, String strUserId);

    public abstract void onRTCCloseAudioTrack(String strRtcpId, String strUserId);

    public abstract void onRTCAVStatus(String strRtcpId, boolean bAudio, boolean bVideo);

    public abstract void onRTCAudioActive(String strRtcpId, String strUserId, int nLevel, int nTime);

    public abstract void onRTCNetworkStatus(String strRtcpId, int nNetSpeed, int nPacketLost, AnyRTCNetQuality netQuality);
}
