package com.vaderspb.worker.nes.codec;

import com.vaderspb.worker.nes.engine.NesVideoFrame;

public interface NesCodec {
    NesCodecFrame codeVideoFrame(NesVideoFrame videoFrame);
}
