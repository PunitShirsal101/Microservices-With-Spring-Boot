package com.example.chatbackend.repository;

import com.example.chatbackend.model.Message;
import org.springframework.data.mongodb.repository.MongoRepository;
import java.util.List;

public interface MessageRepository extends MongoRepository<Message, String> {
    List<Message> findByChatIdOrderByTimestampAsc(String chatId);
}
