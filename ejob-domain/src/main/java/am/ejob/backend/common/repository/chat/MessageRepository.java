package am.ejob.backend.common.repository.chat;

import am.ejob.backend.common.model.chat.Chat;
import am.ejob.backend.common.model.chat.Message;
import am.ejob.backend.common.model.chat.MessageState;
import am.ejob.backend.common.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

import java.util.List;

public interface MessageRepository extends PagingAndSortingRepository<Message, String> {

    Page<Message> findAllByChatOrderBySendAtDesc(Chat chat, Pageable pageable);

    List<Message> findAllByChatIdAndSenderIdAndMessageState(String chatId, String userId, MessageState messageState);

    List<Message> findAllBySenderIdAndMessageState(String userId,MessageState messageState);
}
