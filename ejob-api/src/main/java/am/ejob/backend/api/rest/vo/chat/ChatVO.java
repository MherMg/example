package am.ejob.backend.api.rest.vo.chat;

import am.ejob.backend.api.rest.vo.LastLoginVO;
import am.ejob.backend.api.rest.vo.UserVO;
import am.ejob.backend.common.model.chat.MessageState;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class ChatVO {

    public String chatId;
    public UserVO userVO;
    public MessageState messageState;
    public byte[] avatar;
    public LastLoginVO userLastLogin;

    public ChatVO(String chatId, UserVO userVO, MessageState messageState, byte[] avatar) {
        this.chatId = chatId;
        this.userVO = userVO;
        this.messageState = messageState;
        this.avatar = avatar;
    }
}
