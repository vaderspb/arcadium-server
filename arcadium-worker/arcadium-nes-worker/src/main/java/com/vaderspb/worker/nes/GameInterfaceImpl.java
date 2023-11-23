package com.vaderspb.worker.nes;

import com.google.protobuf.Empty;
import com.vaderspb.worker.nes.engine.NesEngine;
import com.vaderspb.worker.nes.engine.NesJoystick;
import com.vaderspb.worker.nes.engine.Subscription;
import com.vaderspb.worker.proto.ControlButton;
import com.vaderspb.worker.proto.ControlRequest;
import com.vaderspb.worker.proto.GameInterfaceGrpc;
import com.vaderspb.worker.proto.VideoFrame;
import com.vaderspb.worker.proto.VideoSettings;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static com.vaderspb.worker.proto.ControlJoystick.UNRECOGNIZED;

public class GameInterfaceImpl extends GameInterfaceGrpc.GameInterfaceImplBase {
    private static final Logger LOG = LoggerFactory.getLogger(GameInterfaceImpl.class);

    private final NesEngine nesEngine;

    public GameInterfaceImpl(final NesEngine nesEngine) {
        this.nesEngine = nesEngine;
    }

    @Override
    public StreamObserver<VideoSettings> videoChannel(final StreamObserver<VideoFrame> responseObserver) {
        return new StreamObserver<>() {
            private Subscription consumerSubscription;

            @Override
            public void onNext(final VideoSettings videoSettings) {
                unSubscribe();

                consumerSubscription = nesEngine.addVideoConsumer(
                        videoSettings.getQuality(),
                        responseObserver::onNext
                );
            }

            @Override
            public void onError(final Throwable throwable) {
                LOG.warn("Game stream error", throwable);

                unSubscribe();
            }

            @Override
            public void onCompleted() {
                unSubscribe();

                responseObserver.onCompleted();
            }

            private void unSubscribe() {
                if (consumerSubscription != null) {
                    consumerSubscription.unSubscribe();
                }
            }
        };
    }

    @Override
    public StreamObserver<ControlRequest> controlChannel(final StreamObserver<Empty> responseObserver) {

        responseObserver.onCompleted();

        return new StreamObserver<>() {
            @Override
            public void onNext(final ControlRequest controlRequest) {
                if (controlRequest.getControllerId() == UNRECOGNIZED) {
                    LOG.info("Unrecognized controller id: request=[{}]", controlRequest);
                    return;
                }
                if (controlRequest.getButton() == ControlButton.UNRECOGNIZED) {
                    LOG.info("Unrecognized controller button: request=[{}]", controlRequest);
                    return;
                }
                final NesJoystick joystick = getNesJoystick(controlRequest);
                final NesJoystick.JoystickButton joystickButton = getJoystickButton(controlRequest);
                switch (controlRequest.getState()) {
                    case PRESSED -> joystick.pressButton(joystickButton);
                    case RELEASED -> joystick.releaseButton(joystickButton);
                    default -> LOG.info("Unrecognized controller button state: request=[{}]", controlRequest);
                }
            }

            private static NesJoystick.JoystickButton getJoystickButton(final ControlRequest controlRequest) {
                return switch (controlRequest.getButton()) {
                    case UP -> NesJoystick.JoystickButton.UP;
                    case DOWN -> NesJoystick.JoystickButton.DOWN;
                    case LEFT -> NesJoystick.JoystickButton.LEFT;
                    case RIGHT -> NesJoystick.JoystickButton.RIGHT;
                    case A -> NesJoystick.JoystickButton.A;
                    case B -> NesJoystick.JoystickButton.B;
                    case SELECT -> NesJoystick.JoystickButton.SELECT;
                    case START -> NesJoystick.JoystickButton.START;
                    default -> throw new IllegalStateException("Unexpected value: " + controlRequest.getButton());
                };
            }

            private NesJoystick getNesJoystick(final ControlRequest controlRequest) {
                return switch (controlRequest.getControllerId()) {
                    case JOYSTICK1 -> nesEngine.getJoystick1();
                    case JOYSTICK2 -> nesEngine.getJoystick2();
                    default -> throw new IllegalStateException("Unexpected value: " + controlRequest.getControllerId());
                };
            }

            @Override
            public void onError(final Throwable throwable) {
                LOG.warn("Joystick stream error", throwable);
            }

            @Override
            public void onCompleted() {
            }
        };
    }
}