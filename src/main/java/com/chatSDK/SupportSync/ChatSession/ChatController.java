package com.chatSDK.SupportSync.ChatSession;

import com.chatSDK.SupportSync.Repositories.ChatSessionRepository;
import com.chatSDK.SupportSync.Repositories.MessageRepository;
import com.chatSDK.SupportSync.Repositories.UserRepository;
import com.chatSDK.SupportSync.User.AddAgentRequest;
import com.chatSDK.SupportSync.User.AppUser;
import com.chatSDK.SupportSync.exceptionhandler.BadRequestException;
import com.chatSDK.SupportSync.exceptionhandler.ErrorResponse;
import com.chatSDK.SupportSync.exceptionhandler.ResourceNotFoundException;
import com.chatSDK.SupportSync.messages.Message;
import com.chatSDK.SupportSync.messages.UploadImageRequest;
import com.chatSDK.SupportSync.services.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@CrossOrigin(origins =  {"http://localhost:8081","http://localhost:8082"})
@Slf4j
public class ChatController {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @Value("${file.access-dir}")
    private String accessDir;
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @Autowired
    private ChatSessionRepository chatSessionRepository;

    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private UserRepository userRepository;


    // Example of handling WebSocket message with error handling
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(Message message) {
        log.debug("Processing new chat message for session: {}", message.getChatSession().getId());

        try {
            ChatSession session = chatSessionRepository.findById(message.getChatSession().getId())
                    .orElseThrow(() -> new ResourceNotFoundException("Chat session not found"));

            // Validate message content
            if (message.getContent() == null || message.getContent().trim().isEmpty()) {
                throw new BadRequestException("Message content cannot be empty");
            }

            message.setTimestamp(System.currentTimeMillis());
            messageRepository.save(message);

            log.info("Successfully sent message in session: {}", session.getId());
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + message.getChatSession().getId(),
                    message
            );
        } catch (Exception e) {
            log.error("Error processing message", e);
            // For WebSocket, we need to send error through WebSocket channel
            messagingTemplate.convertAndSend(
                    "/topic/errors/" + message.getChatSession().getId(),
                    new ErrorResponse("Error processing message", e.getMessage(), null)
            );
        }
    }



    @MessageMapping("/chat.addAgent")
    public void addAgent(@Payload AddAgentRequest request) {
        log.debug("Adding agent {} to the session {}", request.getUser().getUsername(), request.getSessionTemp().getId());
        ChatSession session = chatSessionRepository.findById(request.getSessionTemp().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + request.getSessionTemp().getId()));
        AppUser agent = userRepository.findById(request.getUser().getId())
                .orElseThrow(() -> new ResourceNotFoundException("Agent not found with id: "+request.getUser().getId()));

        try{
            session.setAgent(agent);
            session.setStartedAt(System.currentTimeMillis());
            chatSessionRepository.save(session);

            Message joinMessage = new Message(
                    "Agent " + agent.getUsername() + " has joined the chat.",
                    agent,
                    session,
                    System.currentTimeMillis()
            );

            log.info("Successfully added agent {} to the session {}", agent.getUsername(), session.getId());

            // Send to session-specific topic
            messagingTemplate.convertAndSend(
                    "/topic/chat/" + session.getId(),
                    joinMessage
            );
        }catch (Exception e){
            log.error("Error adding agent to the session", e);
            throw new BadRequestException("Failed to add agent to chat session: " + e.getMessage());
        }

    }

    @PostMapping("/chat.startSession")
    @CrossOrigin(origins = "http://localhost:8081")
    public ResponseEntity<ChatSession> startChatSession(@RequestBody StartSessionRequest sessionRequest) {

        log.debug("Starting new chat session for user: {}", sessionRequest.getUser().getUsername());

        if (sessionRequest.getUser().getUsername() == null || sessionRequest.getUser().getUsername().trim().isEmpty()) {
            throw new BadRequestException("Username cannot be empty");
        }
        try{
            if (sessionRequest.getUser().getId()!=null && userRepository.findById(sessionRequest.getUser().getId()).isEmpty()) {
                userRepository.save(sessionRequest.getUser());
            }

            // Create and save the chat session
            ChatSession chatSession = new ChatSession();
            chatSession.setUser(sessionRequest.getUser());
            chatSession.setStartedAt(System.currentTimeMillis());
            chatSession.setIssueCategory(sessionRequest.getCategory());
            chatSessionRepository.save(chatSession);

            messagingTemplate.convertAndSend("/topic/activeSessions", chatSession);
            log.info("Successfully created chat session with id: {}", chatSession.getId());

            // Return a response entity with the created chat session
            return ResponseEntity.ok(chatSession);
        }catch (Exception e){
            log.error("Error creating chat session", e);
            throw new BadRequestException("Failed to create chat session: " + e.getMessage());
        }


    }

    @Autowired
    private S3Service s3Service;

    @PostMapping("/chat/uploadImage")
    public ResponseEntity<?> uploadImage(@ModelAttribute UploadImageRequest request) {
        log.debug("Processing image upload request for user: {}", request.getUserId());

        if (request.getFile() == null || request.getFile().isEmpty()) {
            throw new BadRequestException("No file provided");
        }

        if (request.getUserId() == null || request.getUserId() <= 0) {
            throw new BadRequestException("Invalid user ID");
        }

        // Validate file type
        String contentType = request.getFile().getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new BadRequestException("Only image files are allowed");
        }

        // Validate file size (e.g., max 5MB)
        if (request.getFile().getSize() > 5 * 1024 * 1024) {
            throw new BadRequestException("File size exceeds maximum limit of 5MB");
        }

        try {
            String fileUrl = s3Service.uploadFile(request.getFile());
            log.info("Successfully uploaded image for user: {}", request.getUserId());
            return ResponseEntity.ok(Map.of("filePath", fileUrl));
        } catch (IOException e) {
            log.error("Error uploading file", e);
            throw new BadRequestException("Failed to upload file: " + e.getMessage());
        }
    }

    @GetMapping("/users")
    public ResponseEntity<List<AppUser>> getAllUsers() {
        List<AppUser> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<AppUser> getUserById(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(()->new ResourceNotFoundException("User with id "+id+" not found"));
    }

    @GetMapping("/chat/sessions")
    public ResponseEntity<List<ChatSession>> getAllChatSessions() {
        List<ChatSession> sessions = chatSessionRepository.findAll();
        return ResponseEntity.ok(sessions);
    }

    @GetMapping("/chat/sessions/{id}")
    public ResponseEntity<ChatSession> getChatSessionById(@PathVariable Long id) {
        return chatSessionRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + id));
    }

    @PostMapping("/chat/sessions/{id}/end")
    public ResponseEntity<ChatSession> endChatSession(@PathVariable Long id) {
        return chatSessionRepository.findById(id).map(session -> {
            session.setEndedAt(LocalDateTime.now());
            chatSessionRepository.save(session);
            return ResponseEntity.ok(session);
        }).orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/chat/sessions/{sessionId}/messages")
    public ResponseEntity<List<Message>> getMessagesBySession(@PathVariable Long sessionId) {
        log.debug("Fetching messages for session: {}", sessionId);

        // First verify the session exists
        chatSessionRepository.findById(sessionId)
                .orElseThrow(() -> new ResourceNotFoundException("Chat session not found with id: " + sessionId));

        List<Message> messages = messageRepository.findAll()
                .stream()
                .filter(message -> message.getChatSession() != null &&
                        message.getChatSession().getId().equals(sessionId))
                .toList();

        log.info("Retrieved {} messages for session: {}", messages.size(), sessionId);
        return ResponseEntity.ok(messages);
    }


    @GetMapping("/chat/sessions/{id}/summary")
    public ResponseEntity<?> getChatSessionSummary(@PathVariable Long id) {
        return chatSessionRepository.findById(id)
                .map(session -> {
                    long totalMessages = messageRepository.findAll()
                            .stream()
                            .filter(message -> message.getChatSession() != null &&
                                    Objects.equals(message.getChatSession().getId(), id))
                            .count();

                    return ResponseEntity.ok(Map.of(
                            "sessionId", session.getId(),
                            "startedAt", session.getStartedAt(),
                            "endedAt", session.getEndedAt() != null ? session.getEndedAt() : "Still ongoing",
                            "totalMessages", totalMessages
                    ));
                })
                .orElseThrow(()-> new ResourceNotFoundException("Chat session with id "+id+" not found"));
    }


    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Application is running");
    }
}
