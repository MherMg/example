package am.ejob.backend.api.websocket.packet;


import am.ejob.backend.api.rest.vo.chat.MessageVO;

public class NewMessageResponse extends OutPacket {

    public MessageVO response;

    public NewMessageResponse(
            MessageVO message
    ) {
        this.response = message;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.NEW_MESSAGE;
    }
}
