package am.matveev.quiz.repositories;

import am.matveev.quiz.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<UserEntity,Long>{

    boolean existsByChatId(Long chatId);

    UserEntity findByChatId(long chatId);
}
