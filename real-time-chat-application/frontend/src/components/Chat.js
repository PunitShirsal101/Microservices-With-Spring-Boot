import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import {
  Box,
  Grid,
  List,
  ListItem,
  ListItemAvatar,
  ListItemText,
  Avatar,
  Typography,
  TextField,
  IconButton,
  Paper,
  Divider,
  Fab,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
} from '@mui/material';
import SendIcon from '@mui/icons-material/Send';
import AttachFileIcon from '@mui/icons-material/AttachFile';
import ChatIcon from '@mui/icons-material/Chat';
import PersonAddIcon from '@mui/icons-material/PersonAdd';
import LogoutIcon from '@mui/icons-material/Logout';
import axios from 'axios';

const Chat = ({ user, token, onLogout }) => {
  const [chats, setChats] = useState([]);
  const [users, setUsers] = useState([]);
  const [selectedChat, setSelectedChat] = useState(null);
  const [messages, setMessages] = useState([]);
  const [message, setMessage] = useState('');
  const [openNewChat, setOpenNewChat] = useState(false);
  const clientRef = useRef(null);
  const subscriptionRef = useRef(null);
  const messagesEndRef = useRef(null);

  const scrollToBottom = () => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  };

  useEffect(() => {
    scrollToBottom();
  }, [messages]);

  useEffect(() => {
    fetchChats();
    fetchUsers();
  }, []);

  useEffect(() => {
    if (selectedChat) {
      loadMessages(selectedChat.id);
      subscribeToChat(selectedChat.id);
    }
  }, [selectedChat]);

  const fetchChats = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/chats', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setChats(response.data);
    } catch (error) {
      console.error('Failed to fetch chats', error);
    }
  };

  const fetchUsers = async () => {
    try {
      const response = await axios.get('http://localhost:8080/api/users', {
        headers: { Authorization: `Bearer ${token}` }
      });
      setUsers(response.data);
    } catch (error) {
      console.error('Failed to fetch users', error);
    }
  };

  const loadMessages = async (chatId) => {
    try {
      const response = await axios.get(`http://localhost:8080/api/chats/${chatId}/messages`, {
        headers: { Authorization: `Bearer ${token}` }
      });
      setMessages(response.data);
    } catch (error) {
      console.error('Failed to load messages', error);
    }
  };

  const subscribeToChat = (chatId) => {
    if (subscriptionRef.current) {
      subscriptionRef.current.unsubscribe();
    }
    if (clientRef.current) {
      subscriptionRef.current = clientRef.current.subscribe('/topic/chat/' + chatId, (msg) => {
        const receivedMessage = JSON.parse(msg.body);
        setMessages(prev => [...prev, receivedMessage]);
      });
    }
  };

  useEffect(() => {
    const client = new Client({
      webSocketFactory: () => new SockJS('http://localhost:8080/ws'),
      onConnect: () => {
        console.log('Connected to WebSocket');
      },
      onStompError: (frame) => {
        console.error('STOMP error', frame);
      }
    });

    client.activate();
    clientRef.current = client;

    return () => {
      if (clientRef.current) {
        clientRef.current.deactivate();
      }
    };
  }, []);

  const sendMessage = () => {
    if (clientRef.current && message.trim() && selectedChat) {
      const msg = {
        senderId: user,
        chatId: selectedChat.id,
        content: message,
        encrypted: false,
        fileUrl: null
      };
      clientRef.current.publish({
        destination: '/app/chat.sendMessage',
        body: JSON.stringify(msg)
      });
      setMessage('');
    }
  };

  const handleFileUpload = async (event) => {
    const file = event.target.files[0];
    if (file && selectedChat) {
      const formData = new FormData();
      formData.append('file', file);

      try {
        const response = await axios.post('http://localhost:8080/api/files/upload', formData, {
          headers: {
            'Content-Type': 'multipart/form-data',
            'Authorization': `Bearer ${token}`
          }
        });
        const fileUrl = response.data;
        const msg = {
          senderId: user,
          chatId: selectedChat.id,
          content: `File: ${file.name}`,
          encrypted: false,
          fileUrl: fileUrl
        };
        clientRef.current.publish({
          destination: '/app/chat.sendMessage',
          body: JSON.stringify(msg)
        });
      } catch (error) {
        console.error('File upload failed', error);
      }
    }
  };

  const handleNewChat = async (selectedUser) => {
    try {
      const response = await axios.post('http://localhost:8080/api/chats', [selectedUser.username], {
        headers: { Authorization: `Bearer ${token}` }
      });
      const newChat = response.data;
      setChats(prev => [...prev, newChat]);
      setSelectedChat(newChat);
      setOpenNewChat(false);
    } catch (error) {
      console.error('Failed to create chat', error);
    }
  };

  return (
    <Box sx={{ height: '100vh', display: 'flex' }}>
      {/* Sidebar */}
      <Box sx={{ width: 300, borderRight: 1, borderColor: 'divider', display: 'flex', flexDirection: 'column' }}>
        <Box sx={{ p: 2, bgcolor: 'primary.main', color: 'white', display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Typography variant="h6">ChatApp</Typography>
          <IconButton onClick={onLogout} color="inherit">
            <LogoutIcon />
          </IconButton>
        </Box>
        <Box sx={{ flex: 1, overflowY: 'auto' }}>
          <List>
            {chats.map((chat) => (
              <ListItem button key={chat.id} selected={selectedChat?.id === chat.id} onClick={() => setSelectedChat(chat)}>
                <ListItemAvatar>
                  <Avatar>
                    <ChatIcon />
                  </Avatar>
                </ListItemAvatar>
                <ListItemText primary={chat.isGroup ? chat.name : chat.participants.find(p => p !== user)} secondary={chat.participants.filter(p => p !== user).join(', ')} />
              </ListItem>
            ))}
          </List>
        </Box>
        <Box sx={{ p: 2 }}>
          <Button fullWidth variant="contained" startIcon={<PersonAddIcon />} onClick={() => setOpenNewChat(true)}>
            New Chat
          </Button>
        </Box>
      </Box>

      {/* Main Chat */}
      <Box sx={{ flex: 1, display: 'flex', flexDirection: 'column' }}>
        {selectedChat ? (
          <>
            {/* Chat Header */}
            <Box sx={{ p: 2, borderBottom: 1, borderColor: 'divider' }}>
              <Typography variant="h6">{selectedChat.name}</Typography>
            </Box>

            {/* Messages */}
            <Box sx={{ flex: 1, overflowY: 'auto', p: 2 }}>
              <List>
                {messages.map((msg, index) => (
                  <ListItem key={index} sx={{ justifyContent: msg.senderId === user ? 'flex-end' : 'flex-start' }}>
                    <Paper
                      elevation={1}
                      sx={{
                        p: 1,
                        maxWidth: '70%',
                        bgcolor: msg.senderId === user ? 'primary.main' : 'grey.700',
                        color: msg.senderId === user ? 'white' : 'text.primary',
                      }}
                    >
                      <Typography variant="body1">{msg.content}</Typography>
                      {msg.fileUrl && (
                        <Typography variant="body2" sx={{ mt: 1 }}>
                          <a href={`http://localhost:8080${msg.fileUrl}`} target="_blank" rel="noopener noreferrer" style={{ color: 'inherit' }}>
                            Download File
                          </a>
                        </Typography>
                      )}
                      <Typography variant="caption" sx={{ display: 'block', textAlign: 'right' }}>
                        {new Date(msg.timestamp).toLocaleTimeString()}
                      </Typography>
                    </Paper>
                  </ListItem>
                ))}
              </List>
              <div ref={messagesEndRef} />
            </Box>

            {/* Input */}
            <Box sx={{ p: 2, borderTop: 1, borderColor: 'divider' }}>
              <Box sx={{ display: 'flex', alignItems: 'center' }}>
                <IconButton component="label">
                  <AttachFileIcon />
                  <input type="file" hidden onChange={handleFileUpload} />
                </IconButton>
                <TextField
                  fullWidth
                  variant="outlined"
                  placeholder="Type a message..."
                  value={message}
                  onChange={(e) => setMessage(e.target.value)}
                  onKeyPress={(e) => e.key === 'Enter' && sendMessage()}
                />
                <IconButton onClick={sendMessage} color="primary">
                  <SendIcon />
                </IconButton>
              </Box>
            </Box>
          </>
        ) : (
          <Box sx={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: '100%' }}>
            <Typography variant="h6">Select a chat to start messaging</Typography>
          </Box>
        )}
      </Box>

      {/* New Chat Dialog */}
      <Dialog open={openNewChat} onClose={() => setOpenNewChat(false)}>
        <DialogTitle>Start New Chat</DialogTitle>
        <DialogContent>
          <List>
            {users.map((u) => (
              <ListItem button key={u.id} onClick={() => handleNewChat(u)}>
                <ListItemAvatar>
                  <Avatar>{u.username[0]}</Avatar>
                </ListItemAvatar>
                <ListItemText primary={u.username} />
              </ListItem>
            ))}
          </List>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setOpenNewChat(false)}>Cancel</Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};

export default Chat;
