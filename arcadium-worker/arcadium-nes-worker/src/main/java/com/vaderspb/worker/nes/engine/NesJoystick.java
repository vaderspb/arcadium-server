package com.vaderspb.worker.nes.engine;

public interface NesJoystick {
    enum JoystickButton {
        UP, DOWN, LEFT, RIGHT, A, B, SELECT, START
    }

    void releaseButton(JoystickButton button);

    void pressButton(JoystickButton button);
}
