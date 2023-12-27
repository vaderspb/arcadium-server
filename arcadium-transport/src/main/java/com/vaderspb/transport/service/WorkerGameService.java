package com.vaderspb.transport.service;

import com.vaderspb.worker.proto.VideoFrame;
import com.vaderspb.worker.proto.VideoSettings;
import reactor.core.publisher.Flux;

public interface WorkerGameService {
    Flux<VideoFrame> videoChannel(String sessionId, VideoSettings videoSettings);
}
