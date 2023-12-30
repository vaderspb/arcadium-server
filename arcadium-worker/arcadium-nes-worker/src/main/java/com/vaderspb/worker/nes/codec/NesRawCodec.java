package com.vaderspb.worker.nes.codec;

import com.google.protobuf.ByteString;
import com.grapeshot.halfnes.video.NesColors;
import com.vaderspb.worker.proto.VideoFrame;
import com.vaderspb.worker.proto.VideoFrameType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.function.Consumer;

public class NesRawCodec implements NesCodec {
    @Override
    public void codeVideoFrame(final int[] nesFrame,
                               final Consumer<VideoFrame> videoFrameConsumer) {
        try (final ByteString.Output frameData = ByteString.newOutput(nesFrame.length * 3)) {
            for (final int pixelCode : nesFrame) {
                final int color = getColorIndex(pixelCode);
                frameData.write(color & 0xFF);
                frameData.write((color >> 8) & 0xFF);
                frameData.write((color >> 16) & 0xFF);
            }
            videoFrameConsumer.accept(VideoFrame.newBuilder()
                    .setType(VideoFrameType.DATA)
                    .setData(frameData.toByteString())
                    .build()
            );
        } catch (final IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static int getColorIndex(int pixel) {
        return NesColors.col[(pixel & 0x1c0) >> 6][pixel & 0x3f];
    }

    @Override
    public void reset() {
    }
}
