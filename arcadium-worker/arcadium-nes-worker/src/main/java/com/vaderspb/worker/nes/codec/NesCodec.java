package com.vaderspb.worker.nes.codec;

import com.vaderspb.worker.proto.VideoFrame;

import java.util.function.Consumer;

public interface NesCodec {
    void codeVideoFrame(int[] nesFrame, Consumer<VideoFrame> videoFrameConsumer);

    void reset();
}
