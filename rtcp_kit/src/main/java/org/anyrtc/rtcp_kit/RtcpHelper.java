package org.anyrtc.rtcp_kit;

/**
 * Created by Eric on 2017/6/2.
 */
@Deprecated
public interface RtcpHelper {
    /**
     * Publish video OK
     *
     * @param strRtcpId
     */
    public void OnPublishOK(String strRtcpId, String strLiveInfo);

    /**
     * Publish video Failed
     *
     * @param nCode
     * @param strReason
     */
    public void OnPublishFailed(int nCode, String strReason);

    /**
     * Publish video OK
     *
     * @param strRtcpId
     */
    public void OnPublishExOK(String strRtcpId, String strLiveInfo);

    /**
     * Publish video Failed
     *
     * @param nCode
     * @param strReason
     */
    public void OnPublishExFailed(int nCode, String strReason);

    /**
     * Subscribe video OK
     *
     * @param strRtcpId
     */
    public void OnSubscribeOK(String strRtcpId);

    /**
     * Publish video Failed
     *
     * @param strRtcpId
     * @param nCode
     * @param strReason
     */
    public void OnSubscribeFailed(String strRtcpId, int nCode, String strReason);

    /**
     * OnRTCOpenVideoRender
     *
     * @param strRtcpId
     */
    public void OnRtcOpenVideoRender(String strRtcpId);

    /**
     * OnRTCCloseVideoRender
     *
     * @param strRtcpId
     */
    public void OnRtcCloseVideoRender(String strRtcpId);

    public void OnRtcOpenAudioTrack(String strRtcpId, String strUserId);

    public void OnRtcCloseAudioTrack(String strRtcpId, String strUserId);

    public void OnRtcAVStatus(String strRtcpId, boolean bAudio, boolean bVideo);

    public void OnRtcAudioActive(String strRtcpId, String strUserId, int nLevel, int nShowTime);

    public void OnRtcNetworkStatus(String strRtcpId, String strUserId, int nNetSpeed, int nPacketLost);

}
