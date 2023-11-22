package com.vaderspb.worker.nes.engine;

import com.grapeshot.halfnes.NES;
import com.grapeshot.halfnes.ui.ControllerInterface;
import com.grapeshot.halfnes.ui.GUIInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.grapeshot.halfnes.utils.BIT0;
import static com.grapeshot.halfnes.utils.BIT1;
import static com.grapeshot.halfnes.utils.BIT2;
import static com.grapeshot.halfnes.utils.BIT3;
import static com.grapeshot.halfnes.utils.BIT4;
import static com.grapeshot.halfnes.utils.BIT5;
import static com.grapeshot.halfnes.utils.BIT6;
import static com.grapeshot.halfnes.utils.BIT7;

public class NesEngineImpl implements NesEngine {
    private static final Logger LOG = LoggerFactory.getLogger(NesEngineImpl.class);

    private final NES nes;
    private final EngineControllerInterface controller1;
    private final EngineControllerInterface controller2;

    private final CopyOnWriteArrayList<Consumer<NesVideoFrame>> videoConsumerList = new CopyOnWriteArrayList<>();

    private final ExecutorService executor = Executors.newSingleThreadExecutor(action -> {
        final Thread thread = Executors.defaultThreadFactory().newThread(action);
        thread.setDaemon(true);
        return thread;
    });
    private Future<?> gameFuture;

    public NesEngineImpl(final String romFilePath) {
        nes = new NES(new EngineGUIInterface());

        controller1 = new EngineControllerInterface();
        controller2 = new EngineControllerInterface();
        nes.setControllers(controller1, controller2);

        nes.loadROM(romFilePath);
    }

    @Override
    public synchronized void start() {
        if (gameFuture == null) {
            gameFuture = executor.submit(() -> nes.run());
        }
    }

    @Override
    public synchronized void shutdown() {
        nes.quit();
    }

    @Override
    public synchronized void awaitTermination() throws InterruptedException, ExecutionException {
        if (gameFuture != null) {
            gameFuture.get();
        }
    }

    @Override
    public NesJoystick getJoystick1() {
        return controller1;
    }

    @Override
    public NesJoystick getJoystick2() {
        return controller2;
    }

    @Override
    public Subscription addVideoConsumer(final Consumer<NesVideoFrame> videoConsumer) {
        checkNotNull(videoConsumer, "videoConsumer must not be null");

        videoConsumerList.add(videoConsumer);

        return new Subscription() {
            private final AtomicBoolean unSubscribed = new AtomicBoolean();

            @Override
            public void unSubscribe() {
                if (!unSubscribed.getAndSet(true)) {
                    videoConsumerList.remove(videoConsumer);
                }
            }
        };
    }

    private void processFrame(final int[] frame, final int[] bgcolor, final boolean dotcrawl) {
        for (final Consumer<NesVideoFrame> videoConsumer : videoConsumerList) {
            videoConsumer.accept(new NesVideoFrame(frame));
        }
    }

    private class EngineGUIInterface implements GUIInterface {
        @Override
        public NES getNes() {
            return nes;
        }

        @Override
        public void setNES(final NES nes) {
        }

        @Override
        public void setFrame(final int[] frame, final int[] bgcolor, final boolean dotcrawl) {
            processFrame(frame, bgcolor, dotcrawl);
        }

        @Override
        public void messageBox(final String message) {
            LOG.info(message);
        }

        @Override
        public void run() {
        }

        @Override
        public void render() {
        }

        @Override
        public void loadROMs(final String path) {
            throw new IllegalStateException("loadROMs should not be called");
        }
    }

    private static class EngineControllerInterface implements ControllerInterface, NesJoystick {
        private int latchbyte, controllerbyte, prevbyte, outbyte;

        @Override
        public synchronized void pressButton(final JoystickButton button) {
            prevbyte = controllerbyte;

            controllerbyte |= getButtonCode(button);

            if ((controllerbyte & (BIT4 | BIT5)) == (BIT4 | BIT5)) {
                controllerbyte &= ~(BIT4 | BIT5);
                controllerbyte |= (prevbyte & ~(BIT4 | BIT5));
            }

            if ((controllerbyte & (BIT6 | BIT7)) == (BIT6 | BIT7)) {
                controllerbyte &= ~(BIT6 | BIT7);
                controllerbyte |= (prevbyte & ~(BIT6 | BIT7));
            }
        }

        @Override
        public synchronized void releaseButton(final JoystickButton button) {
            prevbyte = controllerbyte;
            controllerbyte &= ~getButtonCode(button);
        }

        private static int getButtonCode(final JoystickButton button) {
            return switch (button) {
                case UP -> BIT4;
                case DOWN -> BIT5;
                case LEFT -> BIT6;
                case RIGHT -> BIT7;
                case A -> BIT0;
                case B -> BIT1;
                case SELECT -> BIT2;
                case START -> BIT3;
            };
        }

        @Override
        public synchronized void strobe() {
            outbyte = latchbyte & 1;
            latchbyte = ((latchbyte >> 1) | 0x100);
        }

        @Override
        public synchronized void output(final boolean state) {
            latchbyte = controllerbyte;
        }

        @Override
        public synchronized int getbyte() {
            return outbyte;
        }

        @Override
        public synchronized int peekOutput() {
            return latchbyte;
        }
    }
}
