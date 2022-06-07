package am.ejob.backend.api.websocket.packet;


import am.ejob.backend.api.rest.vo.chat.ReadMessageVO;

public class MessageStateResponse extends OutPacket {

    public ReadMessageVO response;

    public MessageStateResponse(
            ReadMessageVO readMessage
    ) {
        this.response = readMessage;
    }


    @Override
    public PacketType getPacketType() {
        return PacketType.MESSAGE_READ;
    }
}
