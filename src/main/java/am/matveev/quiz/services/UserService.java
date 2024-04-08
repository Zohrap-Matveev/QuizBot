package am.matveev.quiz.services;

import am.matveev.quiz.entity.UserEntity;
import am.matveev.quiz.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService{

    private final UserRepository userRepository;

    public void registerUser(Long chatId, String username) {
        UserEntity newUser = new UserEntity();
        newUser.setChatId(chatId);
        newUser.setUsername(username);
        userRepository.save(newUser);
    }
    public boolean existsByChatId(Long chatId) {
        return userRepository.existsByChatId(chatId);
    }
}
