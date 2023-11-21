package com.vaderspb.worker.nes;

import com.google.protobuf.ByteString;
import com.vaderspb.worker.nes.engine.NesEngine;
import com.vaderspb.worker.nes.engine.Subscription;
import com.vaderspb.worker.proto.Worker;
import com.vaderspb.worker.proto.WorkerInterfaceGrpc;
import io.grpc.stub.StreamObserver;

public class WorkerServer extends WorkerInterfaceGrpc.WorkerInterfaceImplBase {
    private final NesEngine nesEngine;

    public WorkerServer(final NesEngine nesEngine) {
        this.nesEngine = nesEngine;
    }

    @Override
    public StreamObserver<Worker.VideoSettings> videoChannel(final StreamObserver<Worker.VideoFrame> responseObserver) {
        return new StreamObserver<>() {
            private Subscription consumerSubscription;

            @Override
            public void onNext(final Worker.VideoSettings videoSettings) {
                unSubscribe();
                consumerSubscription = nesEngine.addVideoConsumer(nesVideoFrame ->
                        responseObserver.onNext(Worker.VideoFrame.newBuilder()
                                .setType(1)
                                .setData(ByteString.EMPTY)
                                .build()
                        ));
            }

            @Override
            public void onError(final Throwable throwable) {
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
}
