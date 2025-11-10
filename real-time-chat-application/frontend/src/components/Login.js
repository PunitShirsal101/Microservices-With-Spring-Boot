import React, { useState } from 'react';
import axios from 'axios';
import {
  Container,
  Paper,
  TextField,
  Button,
  Typography,
  Box,
  Avatar,
  IconButton,
  Link,
} from '@mui/material';
import ChatIcon from '@mui/icons-material/Chat';

const Login = ({ setUser, setToken }) => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [email, setEmail] = useState('');
  const [isRegister, setIsRegister] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    const url = isRegister ? 'http://localhost:8080/api/auth/register' : 'http://localhost:8080/api/auth/login';
    const data = isRegister ? { username, password, email } : { username, password };

    try {
      const response = await axios.post(url, data);
      if (!isRegister) {
        setToken(response.data.token);
        setUser(username);
      } else {
        alert('Registered successfully');
        setIsRegister(false);
      }
    } catch (error) {
      alert('Error: ' + (error.response ? error.response.data : error.message));
    }
  };

  return (
    <Container component="main" maxWidth="sm">
      <Box
        sx={{
          marginTop: 8,
          display: 'flex',
          flexDirection: 'column',
          alignItems: 'center',
        }}
      >
        <Avatar sx={{ m: 1, bgcolor: 'primary.main' }}>
          <ChatIcon />
        </Avatar>
        <Typography component="h1" variant="h5">
          {isRegister ? 'Join ChatApp' : 'Welcome Back'}
        </Typography>
        <Paper elevation={3} sx={{ p: 4, mt: 2, width: '100%' }}>
          <Box component="form" onSubmit={handleSubmit} sx={{ mt: 1 }}>
            <TextField
              margin="normal"
              required
              fullWidth
              id="username"
              label="Username"
              name="username"
              autoComplete="username"
              autoFocus
              value={username}
              onChange={(e) => setUsername(e.target.value)}
            />
            <TextField
              margin="normal"
              required
              fullWidth
              name="password"
              label="Password"
              type="password"
              id="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
            />
            {isRegister && (
              <TextField
                margin="normal"
                required
                fullWidth
                id="email"
                label="Email Address"
                name="email"
                autoComplete="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
              />
            )}
            <Button
              type="submit"
              fullWidth
              variant="contained"
              sx={{ mt: 3, mb: 2 }}
            >
              {isRegister ? 'Sign Up' : 'Sign In'}
            </Button>
            <Box sx={{ textAlign: 'center' }}>
              <Link
                component="button"
                variant="body2"
                onClick={() => setIsRegister(!isRegister)}
              >
                {isRegister ? 'Already have an account? Sign In' : "Don't have an account? Sign Up"}
              </Link>
            </Box>
          </Box>
        </Paper>
      </Box>
    </Container>
  );
};

export default Login;
