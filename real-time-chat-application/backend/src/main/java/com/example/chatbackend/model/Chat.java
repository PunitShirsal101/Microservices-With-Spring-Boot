package com.example.chatbackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.List;

@Document(collection = "chats")
public class Chat {
    @Id
    private String id;
    private List<String> participants; // user ids
    private String name; // for group chats
    private boolean isGroup;

    public Chat() {}

    public Chat(List<String> participants, String name, boolean isGroup) {
        this.participants = participants;
        this.name = name;
        this.isGroup = isGroup;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public List<String> getParticipants() { return participants; }
    public void setParticipants(List<String> participants) { this.participants = participants; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public boolean isGroup() { return isGroup; }
    public void setGroup(boolean group) { isGroup = group; }
}
