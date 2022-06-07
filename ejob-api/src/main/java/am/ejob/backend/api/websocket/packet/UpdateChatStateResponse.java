package am.ejob.backend.api.websocket.packet;


import am.ejob.backend.api.rest.vo.chat.ChatVO;

public class UpdateChatStateResponse extends OutPacket {

    public ChatVO response;

    public UpdateChatStateResponse(
            ChatVO chat
    ) {
        this.response = chat;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.NEW_CHAT;
    }
}
