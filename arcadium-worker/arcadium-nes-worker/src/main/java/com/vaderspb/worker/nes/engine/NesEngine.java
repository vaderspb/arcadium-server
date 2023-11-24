package com.vaderspb.worker.nes.engine;

import com.vaderspb.worker.proto.VideoFrame;
import com.vaderspb.worker.proto.VideoQuality;

import java.util.concurrent.ExecutionException;
import java.util.function.Consumer;

public interface NesEngine {
    void start();
    void shutdown();
    void awaitTermination() throws InterruptedException, ExecutionException;

    NesJoystick getJoystick1();
    NesJoystick getJoystick2();

    Subscription addVideoConsumer(VideoQuality videoQuality, Consumer<VideoFrame> videoConsumer);
}
