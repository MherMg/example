package am.ejob.backend.api.security;

import am.ejob.backend.common.model.user.User;
import am.ejob.backend.common.repository.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class CurrentUserDetailService implements UserDetailsService {

    private final UserRepository userRepository;

    @Autowired
    public CurrentUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        Optional<User> user = Optional.ofNullable(userRepository.findByPhoneNumber(s));
        user.orElseThrow(() -> new UsernameNotFoundException("User does not exist"));
        return new CurrentUser(user.get());
    }
}
