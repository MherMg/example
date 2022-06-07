package am.ejob.backend.api.websocket.packet;

import java.io.Serializable;

/**
 * Базовый класс для входящего от клиента пакета
 */
public abstract class InPacket implements Serializable {

    public abstract PacketType getPacketType();

}
