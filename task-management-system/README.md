# Testing the Application:
Frontend: http://localhost:4200
Backend API: http://localhost:8080

# Docker
- https://hub.docker.com/r/spunit555/task-management-backend
- https://hub.docker.com/r/spunit555/task-management-frontend

# API Endpoints Working:
Authentication endpoints: /api/auth/*
User management: /api/users/*
Task management: /api/tasks/*
Project management: /api/projects/*

Test Commands:
# Check backend health
curl http://localhost:8080/api/auth/status

# Check MongoDB connection (should return 403 - expected for unauthenticated)
curl http://localhost:8080/api/users

# Check frontend
curl -I http://localhost:4200

Container Management:
# View logs
docker-compose logs

# Stop services
docker-compose down

# Restart services
docker-compose up -d

# Rebuild and restart
docker-compose up --build -d
