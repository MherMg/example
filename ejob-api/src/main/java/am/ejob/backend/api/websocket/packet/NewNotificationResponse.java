package am.ejob.backend.api.websocket.packet;


public class NewNotificationResponse extends OutPacket {

    public NotificationCountResponseVo response;

    public NewNotificationResponse(
            NotificationCountResponseVo notificationCount
    ) {
        this.response = notificationCount;
    }

    @Override
    public PacketType getPacketType() {
        return PacketType.NEW_NOTIFICATION;
    }
}
