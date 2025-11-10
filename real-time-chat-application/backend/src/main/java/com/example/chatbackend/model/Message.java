package com.example.chatbackend.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "messages")
public class Message {
    @Id
    private String id;
    private String senderId;
    private String chatId;
    private String content;
    private long timestamp;
    private boolean encrypted;
    private String fileUrl;

    public Message() {}

    public Message(String senderId, String chatId, String content, boolean encrypted, String fileUrl) {
        this.senderId = senderId;
        this.chatId = chatId;
        this.content = content;
        this.timestamp = System.currentTimeMillis();
        this.encrypted = encrypted;
        this.fileUrl = fileUrl;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getSenderId() { return senderId; }
    public void setSenderId(String senderId) { this.senderId = senderId; }

    public String getChatId() { return chatId; }
    public void setChatId(String chatId) { this.chatId = chatId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }

    public boolean isEncrypted() { return encrypted; }
    public void setEncrypted(boolean encrypted) { this.encrypted = encrypted; }

    public String getFileUrl() { return fileUrl; }
    public void setFileUrl(String fileUrl) { this.fileUrl = fileUrl; }
}
