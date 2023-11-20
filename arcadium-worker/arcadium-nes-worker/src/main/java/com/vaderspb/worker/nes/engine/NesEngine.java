package com.vaderspb.worker.nes.engine;

import java.io.Closeable;
import java.util.function.Consumer;

public interface NesEngine {
    NesJoystick getJoystick1();
    NesJoystick getJoystick2();

    Closeable addVideoConsumer(Consumer<NesVideoFrame> videoConsumer);
}
