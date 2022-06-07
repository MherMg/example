package am.ejob.backend.api.websocket.packet;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class WebSocketModelsExampleController {
    @GetMapping
    public void justForSwagger(
            @RequestBody UpdateChatStateResponse updateChatStateResponse
    ) {

    }
}
