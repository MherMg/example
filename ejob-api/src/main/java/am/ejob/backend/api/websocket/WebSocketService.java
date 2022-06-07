package am.ejob.backend.api.websocket;

import am.ejob.backend.api.security.CurrentUser;
import am.ejob.backend.api.security.JwtTokenUtil;
import am.ejob.backend.api.websocket.packet.InPacket;
import am.ejob.backend.api.websocket.packet.OutPacket;
import am.ejob.backend.api.websocket.packet.PacketType;
import am.ejob.backend.common.model.user.User;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.*;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
@EnableWebSocket
public class WebSocketService implements WebSocketHandler, UserNotifier, WebSocketConfigurer {

    private static final Logger log = LoggerFactory.getLogger(WebSocketService.class);
    private static final CloseStatus AUTHORIZATION_REQUIRED = new CloseStatus(4999, "AUTHORIZATION_REQUIRED");
    private final JwtTokenUtil jwtTokenUtil;
    private final UserDetailsService userDetailsService;
    private final Map<String, List<WebSocketSession>> userSessionMap = new ConcurrentHashMap<>();
    private final Map<String, User> sessionUserMap = new ConcurrentHashMap<>();
    private final Map<PacketType, PacketHandler<InPacket>> handlerMap = new HashMap<>();

    @Autowired
    public WebSocketService(JwtTokenUtil jwtTokenUtil,
                            UserDetailsService userDetailsService) {
        this.jwtTokenUtil = jwtTokenUtil;
        this.userDetailsService = userDetailsService;
    }

//    @Autowired
//    public void setPacketHandlers(List<PacketHandler<? extends InPacket>> packetHandlers) {
//        Map<PacketType, PacketHandler<InPacket>> map = new HashMap<>();
//        for (var packetHandler : packetHandlers) {
//            log.debug("Register handler for type [{}]: {}", packetHandler.getPacketType(), packetHandler);
//            map.put(packetHandler.getPacketType(), (PacketHandler<InPacket>) packetHandler);
//        }
//        this.handlerMap.putAll(map);
//    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(this, "/ws").setAllowedOriginPatterns("*").withSockJS();
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        if (session.getUri() == null || session.getUri().getQuery() == null || session.getUri().getQuery().isBlank()) {
            session.close(AUTHORIZATION_REQUIRED);
            log.warn("Unable to process session as it doesn't have URL query: {}", session);
            return;
        }
        String[] split = session.getUri().getQuery().split("=");
        if (split.length != 2) {
            session.close(AUTHORIZATION_REQUIRED);
            log.warn("Unable to process session as it doesn't have URL query: {}", session);
            return;
        }

        String authToken = split[1];
        Claims claims = jwtTokenUtil.parseToken(authToken);
        if (claims.getExpiration().before(new Date())) {
            session.close(AUTHORIZATION_REQUIRED);
            log.warn("Unable to process session as it's access token expired: {}", authToken);
            return;
        }

        CurrentUser user = (CurrentUser) userDetailsService.loadUserByUsername(claims.getSubject());
        if (user == null) {
            session.close(AUTHORIZATION_REQUIRED);
            log.warn("Unable to find user with email: {}", claims.getSubject());
            return;
        }

        userSessionMap.compute(user.getId(), (userId, webSocketSessions) -> {
            if (webSocketSessions == null) {
                webSocketSessions = new ArrayList<>(1);
            }
            webSocketSessions.add(session);
            sessionUserMap.put(session.getId(), user.getUser());

            log.trace("User [id:{};phoneNumber:{}] has [{}] active web socket sessions", user.getUser().getId(), user.getUser().getPhoneNumber(), webSocketSessions.size());

            return webSocketSessions;
        });

    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, @NotNull CloseStatus closeStatus) throws Exception {
        User user = sessionUserMap.remove(session.getId());
        if (user == null) {
            log.warn("Closing connection for unknown user with status: {}", closeStatus);
            return;
        }

        log.trace("Closing user connection [id:{};phoneNumber:{}] with status: {}", user.getId(), user.getPhoneNumber(), closeStatus);

        userSessionMap.computeIfPresent(user.getId(), (s, webSocketSessions) -> {
            webSocketSessions.removeIf(userSession -> userSession.getId().equals(session.getId()));
            if (webSocketSessions.isEmpty()) {
                return null;
            }


            return webSocketSessions;
        });
    }

    //
    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
//
//        Object payload = message.getPayload();
//
//        User user = sessionUserMap.get(session.getId());
//        if (user == null) {
//            log.warn("Illegal state: Unable to find who is connected with session [id:{}]", session.getId());
//            return;
//        }
//
//        log.debug("Handle user session message [sessionId:{};userId:{};phoneNumber:{}]: {}", session.getId(), user.getId(), user.getPhoneNumber(), message);
//
//        TransportPacket packet = Serializer.INSTANCE.deserialize(payload, TransportPacket.class);
//        PacketHandler<InPacket> handler = handlerMap.get(packet.packetType);
//        if (handler == null) {
//            throw new IllegalStateException("Unable to find handler for packetType " + packet.packetType);
//        }
//
//        InPacket request = Serializer.INSTANCE.deserialize(payload, handler.getPacketClass());
//
//        log.debug("Handle user session message [sessionId:{};userId:{};phoneNumber:{}] using handler [{}]: {}",
//                session.getId(),
//                user.getId(),
//                user.getPhoneNumber(),
//                handler,
//                request
//        );
//
//        handler.handle(user, request);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        User user = sessionUserMap.remove(session.getId());
        if (user == null) {
            return;
        }

        userSessionMap.computeIfPresent(user.getId(), (s, webSocketSessions) -> {
            webSocketSessions.removeIf(userSession -> userSession.getId().equals(session.getId()));
            if (webSocketSessions.isEmpty()) {
                return null;
            }
            return webSocketSessions;
        });
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    @Override
    public <T extends OutPacket> void notify(String userId, T packet) {
        List<WebSocketSession> webSocketSessions = userSessionMap.get(userId);

        if (webSocketSessions == null || webSocketSessions.isEmpty()) {
            return;
        }

        TextMessage message = createMessageForSending(packet);

        for (WebSocketSession webSocketSession : webSocketSessions) {
            try {
                webSocketSession.sendMessage(message);
                log.debug("User [id:{};sessionId:{}] notified with message [{}]", userId, webSocketSession.getId(), message);
            } catch (IOException e) {
                log.error(e.getMessage(), e);
            }
        }
    }

    @NotNull
    private <T extends OutPacket> TextMessage createMessageForSending(T packet) {
        try {
            byte[] payload = Serializer.INSTANCE.serialize(packet);
            return new TextMessage(payload);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }

    public static class TransportPacket implements Serializable {
        public PacketType packetType;
    }

    private enum Serializer {
        INSTANCE;

        private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

        static {
            OBJECT_MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        }

        private <T extends OutPacket> byte[] serialize(T packet) throws JsonProcessingException {
            return OBJECT_MAPPER.writeValueAsBytes(packet);
        }

        private <T> T deserialize(Object payload, Class<T> clazz) throws JsonProcessingException {
            return OBJECT_MAPPER.readValue(payload.toString(), clazz);
        }

    }


}
