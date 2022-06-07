package am.ejob.backend.common.repository.chat;

import am.ejob.backend.common.model.chat.ChatAttachment;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ChatAttachmentRepository extends PagingAndSortingRepository<ChatAttachment, String> {

    ChatAttachment findByChatIdAndUserIdAndId(String chatId, String userId, String id);
    ChatAttachment findByChatIdAndId(String chatId,  String id);

}

