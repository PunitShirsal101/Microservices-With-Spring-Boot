package com.example.chatbackend.repository;

import com.example.chatbackend.model.Chat;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface ChatRepository extends MongoRepository<Chat, String> {
    List<Chat> findByParticipantsContaining(String userId);
}
