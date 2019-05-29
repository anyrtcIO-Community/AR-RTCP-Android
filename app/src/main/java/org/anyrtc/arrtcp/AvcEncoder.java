package org.anyrtc.arrtcp;

import android.annotation.SuppressLint;
import android.media.MediaCodec;
import android.media.MediaCodecInfo;
import android.media.MediaFormat;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.concurrent.ArrayBlockingQueue;


public class AvcEncoder {
    private final static String TAG = "AvcEncoder";

    private int TIMEOUT_USEC = 10000;

    private MediaCodec mediaCodec;
    int m_width;
    int m_height;
    int m_framerate;
    byte[] m_info = null;

    public byte[] configbyte;

    private static int yuvqueuesize = 30;

    public static ArrayBlockingQueue<byte[]> YUVQueue = new ArrayBlockingQueue<byte[]>(yuvqueuesize);
    public interface OutPutCallback {
        public void callback(byte[] data);
    }


    private OutPutCallback mCallback;

    public void setmCallback(OutPutCallback mCallback) {
        this.mCallback = mCallback;
    }

    @SuppressLint("NewApi")
    public AvcEncoder(int width, int height, int framerate, int bitrate) {

        m_width = width;
        m_height = height;
        m_framerate = framerate;

        MediaFormat mediaFormat = MediaFormat.createVideoFormat("video/avc", width, height);
        mediaFormat.setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420SemiPlanar);
//        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, width * height * 5);
        mediaFormat.setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 0);
        mediaFormat.setInteger(MediaFormat.KEY_BIT_RATE, bitrate);
        mediaFormat.setInteger(MediaFormat.KEY_FRAME_RATE, m_framerate);
        mediaFormat.setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 3);
        try {
//            mediaCodec = MediaCodec.createEncoderByType("video/avc");
            //OMX.Intel.hw_ve.h264
            mediaCodec = MediaCodec.createByCodecName("OMX.Intel.hw_ve.h264");
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        mediaCodec.configure(mediaFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE);
        mediaCodec.start();
        //createfile();
    }

    private static String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/test1.h264";
    private BufferedOutputStream outputStream;
    FileOutputStream outStream;

    private void createfile() {
        File file = new File(path);
        if (file.exists()) {
            file.delete();
        }
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(file));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private byte[] sps;
    private byte[] pps;
    private byte[] h264;

    public void fireVideo(byte[] data) {
        h264 = new byte[m_width * m_height * 3 / 2];
        byte[] rawData = nv212nv12(data);

//        byte[] rawData = data;
        // 获得编码器输入输出数据缓存区 API:21之后可以使用
        // mediaCodec.getInputBuffer(mediaCodec.dequeueInputBuffer(-1));直接获得缓存数据
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
        // 获得有效输入缓存区数组下标 -1表示一直等待
        int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
        Log.d("DEMO", "输入:" + inputBufferIndex);
        if (inputBufferIndex >= 0) {
            // 将原始数据填充 inputbuffers
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(rawData);
            //将此数据加入编码队列 参数3：需要一个增长的时间戳，不然无法持续编码
            mediaCodec.queueInputBuffer(inputBufferIndex, 0, rawData.length,
                    System.nanoTime(), 0);
        }
        //获得编码后的数据
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        //有效数据下标
        int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
        Log.d("DEMO", "输出:" + outputBufferIndex);
        while (outputBufferIndex >= 0) {
            ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
            byte[] outData = new byte[bufferInfo.size];
            outputBuffer.get(outData);
            System.out.println("type:" + outData[4]);
//            if ((outData[4] & 0x1f) == 7) { // sps pps MediaCodec会在编码第一帧之前输出sps+pps sps pps加在一起
//                // sps = new byte[outData.length - 4];
//                // System.arraycopy(outData, 4, sps, 0, outData.length - 4);
//                Log.d("DEMO", "sps pps:" + Arrays.toString(outData));
//                for (int i = 0; i < outData.length; i++) {
//                    if (i + 4 < outData.length) { // 保证不越界
//                        if (outData[i] == 0x00 && outData[i + 1] == 0x00
//                                && outData[i + 2] == 0x00
//                                && outData[i + 3] == 0x01) {
//                            //在这里将sps pps分开
//                            // if ((outData[i + 4] & 0x1f) == 7) { // & 0x1f =7
//                            // sps
//                            //
//                            // } else
//                            //sps pps数据如下: 0x00 0x00 0x00 0x01 7 sps 0x00 0x00 0x00 0x01 8 pps
//                            if ((outData[i + 4] & 0x1f) == 8) {// & 0x1f =8 pps
//                                //去掉界定符
//                                sps = new byte[i - 4];
//                                System.arraycopy(outData, 4, sps, 0, sps.length);
//                                pps = new byte[outData.length
//                                        - (4 + sps.length) - 4];
//                                System.arraycopy(outData, 4 + sps.length + 4,
//                                        pps, 0, pps.length);
//                                break;
//                            }
//                        }
//                    }
//                }
//                Log.d("DEMO", "sps :" + Arrays.toString(sps));
//                Log.d("DEMO", "sps :" + Arrays.toString(pps));
//            } else {
//                // (outData[4] & 0x1f) == 5) 关键帧 outData[4] == 0x65
//                System.arraycopy(outData, 0, h264, 0, outData.length);
//                Log.d("DEMO", outData.length + "");
//                Log.d("DEMO", "帧数据 sps:" + sps.length + "  pps:" + pps.length
//                        + " 264:" + h264.length);
//            }

            if (null != mCallback) {
                mCallback.callback(outData);
            }
            // if (sps_pps != null) { // 已经获得过sps pps
            // System.arraycopy(outData, 0, h264, 0, outData.length);
            // if (h264[4] == 0x65) {// 关键帧 h264[4]&0x1f==5 关键帧
            // Log.d("DEMO", "关键帧");
            // } else {
            // Log.d("DEMO", "不是关键帧");
            // }
            // send(sps_pps, h264);
            // } else {
            // ByteBuffer spsPpsBuffer = ByteBuffer.wrap(outData);
            // // 0x00 0x00 0x00 0x01
            // if (spsPpsBuffer.getInt() == 0x00000001) {
            // sps_pps = new byte[outData.length];
            // System.arraycopy(outData, 0, sps_pps, 0, outData.length);
            // Log.d("DEMO", "sps pps信息");
            // } else {
            // Log.d("DEMO", "错误 未获得sps pps信息 丢帧");
            // }
            // }
            // 释放编码后的数据
            mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
            // 重新获得编码bytebuffer下标
            outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, 0);
            Log.d("DEMO", "完成 输出:" + outputBufferIndex);
        }
        // send(sps_pps, h264);
        // mediaCodec.flush();
    }

    private byte[] nv212nv12(byte[] data) {
        int len = m_width * m_height;
        byte[] buffer = new byte[len * 3 / 2];
        byte[] y = new byte[len];
        byte[] uv = new byte[len / 2];
        System.arraycopy(data, 0, y, 0, len);
        for (int i = 0; i < len / 4; i++) {
            uv[i * 2] = data[len + i * 2 + 1];
            uv[i * 2 + 1] = data[len + i * 2];
        }
        System.arraycopy(y, 0, buffer, 0, y.length);
        System.arraycopy(uv, 0, buffer, y.length, uv.length);
        return buffer;
    }



    @SuppressLint("NewApi")
    private void StopEncoder() {
        try {
            mediaCodec.stop();
            mediaCodec.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    ByteBuffer[] inputBuffers;
    ByteBuffer[] outputBuffers;

    public boolean isRuning = false;

    public void StopThread() {
        isRuning = false;
        try {
            StopEncoder();
            if (null != outputStream) {

                outputStream.flush();
                outputStream.close();
            }
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    int count = 0;

    public void StartEncoderThread() {
        Thread EncoderThread = new Thread(new Runnable() {

            @SuppressLint("NewApi")
            @Override
            public void run() {
                isRuning = true;
                byte[] input = null;
                long pts = 0;
                long generateIndex = 0;

                while (isRuning) {
                    if (YUVQueue.size() > 0) {
                        input = YUVQueue.poll();
                       byte[] yuv420sp = new byte[m_width * m_height * 3 / 2];
                        NV21ToNV12(input, yuv420sp, m_width, m_height);
                        input = yuv420sp;
//                        if (input != null) {
//                            input = Arrays.copyOf(input, m_width * m_height * 3 / 2);
//                        }
                    }
                    if (input != null) {
                        try {
                            long startMs = System.currentTimeMillis();
                            ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                            ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
                            int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                            if (inputBufferIndex >= 0) {
                                pts = computePresentationTime(generateIndex);
                                ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                                inputBuffer.clear();
                                inputBuffer.put(input);
                                mediaCodec.queueInputBuffer(inputBufferIndex, 0, input.length, pts, 0);
                                generateIndex += 1;
                            }

                            MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                            int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                            while (outputBufferIndex >= 0) {
                                //Log.i("AvcEncoder", "Get H264 Buffer Success! flag = "+bufferInfo.flags+",pts = "+bufferInfo.presentationTimeUs+"");
                                ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];
                                byte[] outData = new byte[bufferInfo.size];
                                outputBuffer.get(outData);
                                if (bufferInfo.flags == 2) {
                                    configbyte = new byte[bufferInfo.size];
                                    configbyte = outData;
                                } else if (bufferInfo.flags == 1) {
                                    byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
                                    System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
                                    System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);

//									outputStream.write(keyframe, 0, keyframe.length);
                                } else {
//									outputStream.write(outData, 0, outData.length);
                                }
                                if (null != mCallback) {
                                    mCallback.callback(outData);
                                }
                                mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                                outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                            }
                        } catch (Throwable t) {
                            t.printStackTrace();
                        }
                    } else {
                        try {
                            Thread.sleep(10);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
        EncoderThread.start();

    }


    private void StartEncoder(final byte[] data) {

        long pts = 0;
        long generateIndex = 0;

        byte[] yuv420sp = new byte[m_width * m_height * 3 / 2];
        NV21ToNV12(data, yuv420sp, m_width, m_height);

        if (yuv420sp != null) {
            try {
                long startMs = System.currentTimeMillis();
                ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
                ByteBuffer[] outputBuffers = mediaCodec.getOutputBuffers();
                int inputBufferIndex = mediaCodec.dequeueInputBuffer(-1);
                if (inputBufferIndex >= 0) {
                    pts = computePresentationTime(generateIndex);
                    ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
                    inputBuffer.clear();
                    inputBuffer.put(yuv420sp);
                    mediaCodec.queueInputBuffer(inputBufferIndex, 0, yuv420sp.length, pts, 0);
                    generateIndex += 1;
                }

                MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
                int outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                while (outputBufferIndex >= 0) {
                    //Log.i("AvcEncoder", "Get H264 Buffer Success! flag = "+bufferInfo.flags+",pts = "+bufferInfo.presentationTimeUs+"");
                    ByteBuffer outputBuffer = outputBuffers[outputBufferIndex];

                    byte[] outData = new byte[bufferInfo.size];
                    outputBuffer.get(outData);
//                    if (bufferInfo.flags == 2) {
//                        configbyte = new byte[bufferInfo.size];
//                        configbyte = outData;
//                    } else if (bufferInfo.flags == 1) {
//                        byte[] keyframe = new byte[bufferInfo.size + configbyte.length];
//                        System.arraycopy(configbyte, 0, keyframe, 0, configbyte.length);
//                        System.arraycopy(outData, 0, keyframe, configbyte.length, outData.length);
//
////									outputStream.write(keyframe, 0, keyframe.length);
//                    } else {
////									outputStream.write(outData, 0, outData.length);
//                    }
                    if (null != mCallback) {
                        mCallback.callback(outData);
                    }
                    mediaCodec.releaseOutputBuffer(outputBufferIndex, false);
                    outputBufferIndex = mediaCodec.dequeueOutputBuffer(bufferInfo, TIMEOUT_USEC);
                }
            } catch (Throwable t) {
                t.printStackTrace();
            }
        } else {
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void NV21ToNV12(byte[] nv21, byte[] nv12, int width, int height) {
        if (nv21 == null || nv12 == null) return;
        int framesize = width * height;
        int i = 0, j = 0;
        System.arraycopy(nv21, 0, nv12, 0, framesize);
        for (i = 0; i < framesize; i++) {
            nv12[i] = nv21[i];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j - 1] = nv21[j + framesize];
        }
        for (j = 0; j < framesize / 2; j += 2) {
            nv12[framesize + j] = nv21[j + framesize - 1];
        }
    }

    /**
     * Generates the presentation time for frame N, in microseconds.
     */
    private long computePresentationTime(long frameIndex) {
        return 132 + frameIndex * 1000000 / m_framerate;
    }
}
