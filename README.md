# SupportSync Backend

This repository serves as the backend implementation for the [SupportSync SDK](https://github.com/krishnapoddar1592/SupportSync), providing a robust customer support chat system. It handles real-time messaging, file uploads, user management, and chat session administration through a WebSocket-based architecture.

## Purpose

The SupportSync Backend provides the server-side infrastructure necessary for the SupportSync SDK, enabling:
- Real-time chat functionality between customers and support agents
- File/image upload capabilities with AWS S3 integration
- Chat session management and persistence
- User role management (customers and agents)
- Message history tracking
- Support ticket categorization

## Requirements

- Java 17 or higher
- Maven
- PostgreSQL 13
- Docker and Docker Compose (for containerized deployment)
- AWS Account (for S3 file storage)

## API Endpoints

### WebSocket Endpoints

- **Connect**: `ws://localhost:8080/ws`
    - Endpoint for establishing WebSocket connections
    - Supports SockJS fallback

#### Message Channels

- `/app/chat.sendMessage`
    - Handles sending new chat messages
    - Requires message object with content and session ID

- `/app/chat.addAgent`
    - Assigns an agent to a chat session
    - Requires agent details and session ID

- `/topic/chat/{sessionId}`
    - Subscribe to receive messages for a specific chat session

### REST Endpoints

#### Chat Sessions

- **Start Session**
    - `POST /chat.startSession`
    - Creates a new chat session
    - Request body: `StartSessionRequest` with user details and issue category

- **Get All Sessions**
    - `GET /chat/sessions`
    - Returns list of all chat sessions

- **Get Session by ID**
    - `GET /chat/sessions/{id}`
    - Returns details of a specific chat session

- **End Session**
    - `POST /chat/sessions/{id}/end`
    - Marks a chat session as ended

- **Get Session Messages**
    - `GET /chat/sessions/{sessionId}/messages`
    - Returns all messages in a chat session

- **Get Session Summary**
    - `GET /chat/sessions/{id}/summary`
    - Returns session statistics and summary

#### Users

- **Get All Users**
    - `GET /users`
    - Returns list of all users

- **Get User by ID**
    - `GET /users/{id}`
    - Returns specific user details

#### File Upload

- **Upload Image**
    - `POST /chat/uploadImage`
    - Multipart form data with file and userId
    - Returns S3 URL of uploaded image

#### Health Check

- **Health Check**
    - `GET /health`
    - Returns application status

## Docker Setup

### Environment Variables

Create a `.env` file in the root directory with the following variables:

```env
# Application
APP_PORT=8080

# Database
DB_HOST=db
DB_PORT=5432
DB_NAME=supportSync
DB_USER=your_username
DB_PASSWORD=your_password

# AWS Configuration
AWS_ACCESS_KEY=your_aws_access_key
AWS_SECRET_KEY=your_aws_secret_key
AWS_S3_BUCKET=your_bucket_name
AWS_REGION=your_aws_region
```

### Local Deployment

1. Build the Docker images:
```bash
docker-compose build
```

2. Start the services:
```bash
docker-compose up -d
```

3. Check the logs:
```bash
docker-compose logs -f
```

4. Stop the services:
```bash
docker-compose down
```

The application will be available at `http://localhost:8080`

## Security

The application implements:
- Basic authentication for REST endpoints
- CORS configuration for specified origins
- WebSocket security configuration
- Input validation and error handling

## Error Handling

The application includes a global exception handler that provides consistent error responses for:
- Resource not found (404)
- Bad requests (400)
- Validation errors
- Internal server errors (500)

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request
