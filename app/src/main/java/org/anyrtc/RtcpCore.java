package org.anyrtc;

import org.anyrtc.rtcp_kit.RtcpKit;

/**
 * Created by Skyline on 2017/6/14.
 */

public class RtcpCore {

    private static RtcpCore mInstance;

    public static RtcpCore Inst() {
        if(null == mInstance) {
            mInstance = new RtcpCore();
        }
        return mInstance;
    }



    private RtcpKit mRtcpKit;

    public RtcpKit getmRtcpKit() {
        //写成单例模式 全局持有一个rtcp对象
        if(null == mRtcpKit) {
            mRtcpKit = new RtcpKit();
        }

        return mRtcpKit;
    }
}
