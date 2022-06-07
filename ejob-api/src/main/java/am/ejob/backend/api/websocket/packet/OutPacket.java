package am.ejob.backend.api.websocket.packet;

import java.io.Serializable;

/**
 * Базовый класс исходящего пакета клиенту
 */
public abstract class OutPacket implements Serializable {

    public abstract PacketType getPacketType();

}
