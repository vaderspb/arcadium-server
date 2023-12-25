package com.vaderspb.api.endpoint;

import com.vaderspb.session.proto.CreateSessionRequest;
import com.vaderspb.session.proto.CreateSessionResponse;
import com.vaderspb.session.proto.SessionServiceGrpc;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {
    private final SessionServiceGrpc.SessionServiceBlockingStub sessionService;

    public SessionController(final SessionServiceGrpc.SessionServiceBlockingStub sessionService) {
        this.sessionService = sessionService;
    }

    @PostMapping("/create_session")
    public CreateSessionDto createSession() {
        final CreateSessionResponse session =
                sessionService.createSession(CreateSessionRequest.getDefaultInstance());
        return new CreateSessionDto(session.getId());
    }

    public record CreateSessionDto(String id) {
    }
}
