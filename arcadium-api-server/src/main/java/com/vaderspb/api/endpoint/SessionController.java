package com.vaderspb.api.endpoint;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SessionController {
    @PostMapping("/create_session")
    public CreateSessionResponse createSession() {
        return new CreateSessionResponse("sessionId");
    }

    public record CreateSessionResponse(String id){
    }
}
