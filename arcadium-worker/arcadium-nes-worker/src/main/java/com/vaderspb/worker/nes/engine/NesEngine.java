package com.vaderspb.worker.nes.engine;

import java.util.function.Consumer;

public interface NesEngine {
    NesJoystick getJoystick1();
    NesJoystick getJoystick2();

    Subscription addVideoConsumer(Consumer<NesVideoFrame> videoConsumer);
}
