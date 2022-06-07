package am.ejob.backend.api.websocket;


import am.ejob.backend.api.websocket.packet.InPacket;
import am.ejob.backend.api.websocket.packet.PacketType;
import am.ejob.backend.common.model.user.User;

public interface PacketHandler<T extends InPacket> {
    /**
     * @return Возрвщает тип пакета
     */
    PacketType getPacketType();

    /**
     * @return Возвращает класс обрабатываемого пакета
     */
    Class<T> getPacketClass();

    /**
     * Обрабатывает входящий от клиента пакет
     *
     * @param user   пользователь
     * @param packet пакет
     * @throws Exception ошибка
     */
    void handle(User user, T packet) throws Exception;
}
