package org.ar.rtcp_kit;

/**
 *
 */
public enum ARCaptureType {
    /**
     * YUV
     */
    YUV420P(0),
    /**
     * RGB
     */
    RGB565(1);

    public final int type;

    private ARCaptureType(int type) {
        this.type = type;
    }
}
