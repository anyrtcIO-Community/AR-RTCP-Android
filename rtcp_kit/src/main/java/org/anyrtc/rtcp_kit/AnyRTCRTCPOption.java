package org.anyrtc.rtcp_kit;

import org.anyrtc.common.enums.AnyRTCCommonMediaType;
import org.anyrtc.common.enums.AnyRTCScreenOrientation;
import org.anyrtc.common.enums.AnyRTCVideoQualityMode;

/**
 * Created by Skyline on 2017/11/10.
 */
@Deprecated
public class AnyRTCRTCPOption {
    /**
     * 前置摄像头；默认：true（前置摄像头）
     */
    public boolean mBFront = true;
    /**
     * anyRTC屏幕方向；默认：竖屏
     */
    private AnyRTCScreenOrientation mScreenOriention = AnyRTCScreenOrientation.AnyRTC_SCRN_Portrait;
    /**
     * anyRTC视频清晰标准；默认：标清（AnyRTC_Video_SD）
     */
    private AnyRTCVideoQualityMode mVideoMode = AnyRTCVideoQualityMode.AnyRTCVideoQuality_Medium1;
    /**
     * anyRTC会议通讯类型：默认：视频
     */
    private AnyRTCCommonMediaType mMediaType = AnyRTCCommonMediaType.AnyRTC_M_Video;

    public AnyRTCRTCPOption(AnyRTCCommonMediaType mMediaType, boolean mBFront, AnyRTCScreenOrientation mScreenOriention, AnyRTCVideoQualityMode mVideoMode
                           ) {
        this.mBFront = mBFront;
        this.mScreenOriention = mScreenOriention;
        this.mVideoMode = mVideoMode;
        this.mMediaType = mMediaType;
    }

    public AnyRTCRTCPOption(AnyRTCCommonMediaType mMediaType) {
        this.mMediaType = mMediaType;
    }

    public AnyRTCRTCPOption() {
    }

    public boolean ismBFront() {
        return mBFront;
    }

    public  void setmBFront(boolean mBFront) {
        this.mBFront = mBFront;
    }

    public AnyRTCScreenOrientation getmScreenOriention() {
        return mScreenOriention;
    }

    public void setmScreenOriention(AnyRTCScreenOrientation mScreenOriention) {
        this.mScreenOriention = mScreenOriention;
    }

    public AnyRTCVideoQualityMode getmVideoMode() {
        return mVideoMode;
    }

    public void setmVideoMode(AnyRTCVideoQualityMode mVideoMode) {
        this.mVideoMode = mVideoMode;
    }



    public AnyRTCCommonMediaType getmMediaType() {
        return mMediaType;
    }

    public void setmMediaType(AnyRTCCommonMediaType mMediaType) {
        this.mMediaType = mMediaType;
    }

    public void setOptionParams(boolean mBFront, AnyRTCScreenOrientation mScreenOriention, AnyRTCVideoQualityMode mVideoMode,  AnyRTCCommonMediaType mMediaType) {
        this.mBFront = mBFront;
        this.mScreenOriention = mScreenOriention;
        this.mVideoMode = mVideoMode;
        this.mMediaType = mMediaType;
    }
}
