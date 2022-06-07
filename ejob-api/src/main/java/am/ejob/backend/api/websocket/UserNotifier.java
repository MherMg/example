package am.ejob.backend.api.websocket;


import am.ejob.backend.api.websocket.packet.OutPacket;

public interface UserNotifier {

    /**
     * Отправить пакет пользователю
     *
     * @param userId пользователь
     * @param packet пакет
     * @param <T>    тип
     */
    <T extends OutPacket> void notify(String userId, T packet);
}
