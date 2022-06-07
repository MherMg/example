package am.ejob.backend.common.repository.chat;

import am.ejob.backend.common.model.chat.Chat;
import am.ejob.backend.common.model.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface ChatRepository extends PagingAndSortingRepository<Chat, String> {

    Chat findByFromAndTo(User from, User to);

    Page<Chat> findAllByFromOrderByUpdatedAtDesc(User from, Pageable pageable);

    Page<Chat> findAllByToOrderByUpdatedAtDesc(User to, Pageable pageable);
}
