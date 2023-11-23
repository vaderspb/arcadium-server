package com.vaderspb.worker.nes.codec;

import com.google.protobuf.ByteString;
import com.grapeshot.halfnes.video.NesColors;
import com.vaderspb.worker.proto.VideoFrame;
import com.vaderspb.worker.proto.VideoFrameType;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class NesCompressingCodec implements NesCodec {
    private static final int[][] COLOR_TABLE_INDEX;
    private static final int COLOR_TABLE_SIZE;

    static {
        COLOR_TABLE_INDEX = new int[NesColors.col.length][];
        int size = 0;
        for (int i = 0; i < NesColors.col.length; i++) {
            final int[] colorCol = NesColors.col[i];

            COLOR_TABLE_INDEX[i] = new int[colorCol.length];
            for (int j = 0; j < colorCol.length; j++) {
                COLOR_TABLE_INDEX[i][j] = size++;
            }
        }
        COLOR_TABLE_SIZE = size;
    }

    private CodeTreeRoot codeTreeRoot = new CodeTreeRoot();
    private final AtomicInteger framesBeforeReset = new AtomicInteger();

    @Override
    public void codeVideoFrame(final int[] nesFrame, final Consumer<VideoFrame> videoFrameConsumer) {
        if (framesBeforeReset.getAndDecrement() <= 0) {
            framesBeforeReset.set(100);

            codeTreeRoot = new CodeTreeRoot();

            videoFrameConsumer.accept(VideoFrame.newBuilder()
                    .setType(VideoFrameType.INIT)
                    .build()
            );
        }

        try (final ByteString.Output frameData = ByteString.newOutput(nesFrame.length / 10)) {
            for (int currentPos = 0; currentPos < nesFrame.length;) {
                final CodeTreeNode node = codeTreeRoot.getLongestPattern(nesFrame, currentPos);
                currentPos += node.getLength();
                frameData.write(node.getValue() & 0xFF);
                frameData.write(node.getValue() >> 4 & 0xFF);
                frameData.write(node.getValue() >> 8 & 0xFF);
                frameData.write(node.getValue() >> 16 & 0xFF);
                if (node.getLength() < 25) {
                    codeTreeRoot.addPattern(nesFrame, currentPos, node.getLength() + 1);
                }
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

    @Override
    public void reset() {
        framesBeforeReset.set(0);
    }

    private static class CodeTreeRoot extends CodeTreeNode {
        private int size = 0;

        public CodeTreeRoot() {
            super(-1, 0);

            for (int color = 0; color < COLOR_TABLE_SIZE; color++) {
                size += addPattern(new int[]{color}, 0, 1, size);
            }
        }

        public void addPattern(final int[] pixels, final int startPos, final int len) {
            size += addPattern(pixels, startPos, len, size);
        }
    }

    private static class CodeTreeNode {
        private final CodeTreeNode[] subTree = new CodeTreeNode[COLOR_TABLE_SIZE];
        private final int value;
        private final int length;

        public CodeTreeNode(final int value, int length) {
            this.value = value;
            this.length = length;
        }

        public int getValue() {
            return value;
        }

        public int getLength() {
            return length;
        }

        public CodeTreeNode getLongestPattern(final int[] pixels, final int startPos) {
            if (startPos >= pixels.length) {
                return null;
            }
            final CodeTreeNode subNode = subTree[getColorIndex(pixels[startPos])];
            if (subNode == null) {
                return this;
            }
            final CodeTreeNode nextNode = subNode.getLongestPattern(pixels, startPos + 1);
            return nextNode == null ? subNode : nextNode;
        }

        protected int addPattern(final int[] pixels, final int startPos, final int len, final int value) {
            if (startPos >= pixels.length || len == 0) {
                return 0;
            }

            final int nodeIndex = getColorIndex(pixels[startPos]);
            final CodeTreeNode node = subTree[nodeIndex];
            if (node == null) {
                final CodeTreeNode newNode = new CodeTreeNode(value, length + 1);
                subTree[nodeIndex] = newNode;
                return 1 + newNode.addPattern(pixels, startPos + 1, len - 1, value + 1);
            } else {
                return node.addPattern(pixels, startPos + 1, len - 1, value);
            }
        }

        private static int getColorIndex(int pixel) {
            return COLOR_TABLE_INDEX[(pixel & 0x1c0) >> 6][pixel & 0x3f];
        }
    }
}
