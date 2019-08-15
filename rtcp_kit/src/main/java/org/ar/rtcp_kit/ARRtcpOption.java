package org.ar.rtcp_kit;

import org.ar.common.enums.ARVideoCommon;

/**
 * Created by liuxiaozhong on 2019/1/15.
 */
public class ARRtcpOption {
    /**
     * 前置摄像头；默认：true（前置摄像头）
     */
    private boolean isDefaultFrontCamera = true;

    /**
     * 屏幕方向；默认：竖屏
     */
    private ARVideoCommon.ARVideoOrientation videoOrientation = ARVideoCommon.ARVideoOrientation.Portrait;
    /**
     * 视频清晰标准；默认：标清（_Video_SD）
     */
    private ARVideoCommon.ARVideoProfile videoProfile = ARVideoCommon.ARVideoProfile.ARVideoProfile360x640;
    /**
     * 视频帧率；默认：15帧（ARVideoFrameRateFps15）
     */
    private ARVideoCommon.ARVideoFrameRate videoFps = ARVideoCommon.ARVideoFrameRate.ARVideoFrameRateFps15;

    /**
     * 会议媒体类型
     */
    private ARVideoCommon.ARMediaType mediaType = ARVideoCommon.ARMediaType.Video;


    public void setOptionParams(boolean isDefaultFrontCamera, ARVideoCommon.ARVideoOrientation videoOrientation, ARVideoCommon.ARVideoProfile videoProfile, ARVideoCommon.ARVideoFrameRate videoFps) {
        this.isDefaultFrontCamera = isDefaultFrontCamera;
        this.videoOrientation = videoOrientation;
        this.videoProfile = videoProfile;
        this.videoFps = videoFps;
    }

    public ARRtcpOption() {
    }

    protected boolean isDefaultFrontCamera() {
        return isDefaultFrontCamera;
    }

    public void setDefaultFrontCamera(boolean defaultFrontCamera) {
        isDefaultFrontCamera = defaultFrontCamera;
    }

    public ARVideoCommon.ARVideoOrientation getVideoOrientation() {
        return videoOrientation;
    }

    public void setVideoOrientation(ARVideoCommon.ARVideoOrientation videoOrientation) {
        this.videoOrientation = videoOrientation;
    }

    protected ARVideoCommon.ARVideoProfile getVideoProfile() {
        return videoProfile;
    }

    public void setVideoProfile(ARVideoCommon.ARVideoProfile videoProfile) {
        this.videoProfile = videoProfile;
    }

    protected ARVideoCommon.ARVideoFrameRate getVideoFps() {
        return videoFps;
    }

    public void setVideoFps(ARVideoCommon.ARVideoFrameRate videoFps) {
        this.videoFps = videoFps;
    }

    protected ARVideoCommon.ARMediaType getMediaType() {
        return mediaType;
    }

    protected void setMediaType(ARVideoCommon.ARMediaType mediaType) {
        this.mediaType = mediaType;
    }



}
