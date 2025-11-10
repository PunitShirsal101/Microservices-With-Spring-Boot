package com.example.chatbackend.controller;

import com.example.chatbackend.model.Chat;
import com.example.chatbackend.model.Message;
import com.example.chatbackend.repository.ChatRepository;
import com.example.chatbackend.repository.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/chats")
public class ChatController {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private ChatRepository chatRepository;

    @GetMapping
    public ResponseEntity<List<Chat>> getChatsForUser(Principal principal) {
        String userId = principal.getName(); // Assuming username is id, but actually need user id
        // Since User has id as String, and username, but in principal it's username
        // Need to get user id from username
        // For simplicity, assume username is id, or add service
        List<Chat> chats = chatRepository.findByParticipantsContaining(userId);
        return ResponseEntity.ok(chats);
    }

    @PostMapping
    public ResponseEntity<Chat> createChat(@RequestBody List<String> participants, Principal principal) {
        String currentUser = principal.getName();
        participants.add(0, currentUser); // Add current user
        Collections.sort(participants); // Sort for uniqueness
        Optional<Chat> existing = chatRepository.findByParticipantsContaining(participants.get(0))
                .stream()
                .filter(chat -> chat.getParticipants().equals(participants))
                .findFirst();
        if (existing.isPresent()) {
            return ResponseEntity.ok(existing.get());
        }
        Chat chat = new Chat(participants, participants.size() > 2 ? "Group Chat" : "Chat", participants.size() > 2);
        chat = chatRepository.save(chat);
        return ResponseEntity.ok(chat);
    }

    @GetMapping("/{chatId}/messages")
    public ResponseEntity<List<Message>> getMessages(@PathVariable String chatId, Principal principal) {
        // Check if user is participant
        Optional<Chat> chat = chatRepository.findById(chatId);
        if (chat.isEmpty() || !chat.get().getParticipants().contains(principal.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        List<Message> messages = messageRepository.findByChatIdOrderByTimestampAsc(chatId);
        return ResponseEntity.ok(messages);
    }

    @MessageMapping("/chat.sendMessage")
    public void sendMessage(@Payload Message message) {
        message.setTimestamp(System.currentTimeMillis());
        messageRepository.save(message);
        messagingTemplate.convertAndSend("/topic/chat/" + message.getChatId(), message);
    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public Message addUser(@Payload Message message) {
        return message;
    }
}
