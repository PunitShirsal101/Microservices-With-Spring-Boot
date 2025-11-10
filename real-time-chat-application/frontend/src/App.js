import React, { useState } from 'react';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import CssBaseline from '@mui/material/CssBaseline';
import Login from './components/Login';
import Chat from './components/Chat';
import './App.css';

const theme = createTheme({
  palette: {
    mode: 'dark',
    primary: {
      main: '#25D366', // WhatsApp green but unique
    },
    secondary: {
      main: '#128C7E',
    },
    background: {
      default: '#0A0A0A',
      paper: '#1A1A1A',
    },
  },
  typography: {
    fontFamily: '"Roboto", "Helvetica", "Arial", sans-serif',
  },
});

function App() {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);

  const handleLogout = () => {
    setUser(null);
    setToken(null);
  };

  return (
    <ThemeProvider theme={theme}>
      <CssBaseline />
      <div className="App">
        {!user ? (
          <Login setUser={setUser} setToken={setToken} />
        ) : (
          <Chat user={user} token={token} onLogout={handleLogout} />
        )}
      </div>
    </ThemeProvider>
  );
}

export default App;
