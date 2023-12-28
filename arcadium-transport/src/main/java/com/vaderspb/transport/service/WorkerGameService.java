package com.vaderspb.transport.service;

import com.vaderspb.worker.proto.ControlRequest;
import com.vaderspb.worker.proto.VideoFrame;
import com.vaderspb.worker.proto.VideoSettings;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface WorkerGameService {
    Flux<VideoFrame> videoChannel(String sessionId, VideoSettings videoSettings);

    Mono<Void> controlChannel(String sessionId, Flux<ControlRequest> controlRequests);
}
