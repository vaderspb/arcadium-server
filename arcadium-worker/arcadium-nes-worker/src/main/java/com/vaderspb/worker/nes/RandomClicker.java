package com.vaderspb.worker.nes;

import com.google.common.util.concurrent.Uninterruptibles;
import com.vaderspb.worker.nes.engine.NesEngineImpl;
import com.vaderspb.worker.nes.engine.NesJoystick;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class RandomClicker {
    private static final Logger LOG = LoggerFactory.getLogger(RandomClicker.class);

    private RandomClicker() {
    }

    public static void clickButtons(final NesEngineImpl nesEngine) {
        final NesJoystick.JoystickButton[] allButtons = NesJoystick.JoystickButton.values();
        final Random random = new Random();
        for (int i = 0; i < 1000; i++) {
            NesJoystick.JoystickButton btn = allButtons[Math.abs(random.nextInt()) % allButtons.length];
            LOG.info("Button=" + btn);

            nesEngine.getJoystick1().pressButton(btn);
            Uninterruptibles.sleepUninterruptibly(random.nextInt() % 500, TimeUnit.MILLISECONDS);

            nesEngine.getJoystick1().releaseButton(btn);
            Uninterruptibles.sleepUninterruptibly(random.nextInt() % 500, TimeUnit.MILLISECONDS);
        }
    }
}
