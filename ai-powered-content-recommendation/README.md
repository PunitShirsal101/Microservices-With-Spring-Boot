# AI-Powered Content Recommendation System

This project implements a machine learning-based content recommendation system that analyzes user behavior and preferences to provide personalized content suggestions.

## Technologies Used
- Java (Spring Boot for backend APIs)
- Python (TensorFlow for ML service)
- RESTful API integration between services

## Structure
- `/backend` - Spring Boot microservice for user/content management and recommendation API
- `/ml-service` - Python microservice for ML model training and inference

## Getting Started
1. Build and run the Spring Boot backend
2. Build and run the Python ML service
3. Connect backend to ML service via REST API

## Features
- User and content management
- Personalized recommendations
- Extensible ML model (TensorFlow)

## ML Service (Port 5000)
GET http://127.0.0.1:5000/ → Returns service health status
POST http://127.0.0.1:5000/recommend → Returns AI-powered recommendations

## Spring Boot Backend (Port 8080)
POST http://localhost:8080/api/recommend

```
http://127.0.0.1:5000/recommend
POST payload
{
  "user_id": "1"
}

response
{
  "user_id": "1",
  "recommendations": ["content19", "content18", "content17"]
}

GET
http://127.0.0.1:5000/
response
{
  "message": "AI-Powered Content Recommendation ML Service is running",
  "status": "healthy"
}
```