package com.chatSDK.SupportSync.ChatSession;

import com.chatSDK.SupportSync.Repositories.ChatSessionRepository;
import com.chatSDK.SupportSync.Repositories.MessageRepository;
import com.chatSDK.SupportSync.Repositories.UserRepository;
import com.chatSDK.SupportSync.User.AddAgentRequest;
import com.chatSDK.SupportSync.User.AppUser;
import com.chatSDK.SupportSync.messages.Message;
import com.chatSDK.SupportSync.messages.UploadImageRequest;
import com.chatSDK.SupportSync.services.S3Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Controller
@CrossOrigin(origins =  {"http://localhost:8081","http://localhost:8082"})
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


    // This method handles incoming messages and stores them in the database.
    @MessageMapping("/chat.sendMessage")
    public void sendMessage(Message message) {
        AppUser user=message.getSender();
        if(user!=null && userRepository.findById(user.getId()).isEmpty()){
            userRepository.save(user);
        }
        ChatSession session = chatSessionRepository.findById(message.getChatSession().getId())
                .orElseThrow(() -> new RuntimeException("Chat session not found"));


        message.setTimestamp(System.currentTimeMillis());
        messageRepository.save(message);

        // Send to a session-specific topic
        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getChatSession().getId(),
                message
        );
    }



    @MessageMapping("/chat.addAgent")
    public void addAgent(@Payload AddAgentRequest request) {
        System.out.println(request.toString());
        ChatSession session = chatSessionRepository.findById(request.getSessionTemp().getId())
                .orElseThrow(() -> new RuntimeException("Chat session not found"));
        AppUser agent = userRepository.findById(request.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        session.setAgent(agent);
        session.setStartedAt(System.currentTimeMillis());
        chatSessionRepository.save(session);

        Message joinMessage = new Message(
                "Agent " + agent.getUsername() + " has joined the chat.",
                agent,
                session,
                System.currentTimeMillis()
        );

        // Send to session-specific topic
        messagingTemplate.convertAndSend(
                "/topic/chat/" + session.getId(),
                joinMessage
        );
    }

    @PostMapping("/chat.startSession")
    @CrossOrigin(origins = "http://localhost:8081")
    public ResponseEntity<ChatSession> startChatSession(@RequestBody StartSessionRequest sessionRequest) {

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

        // Return a response entity with the created chat session
        return ResponseEntity.ok(chatSession);
    }

    @Autowired
    private S3Service s3Service;

    @PostMapping("/chat/uploadImage")
    @CrossOrigin(origins = "http://localhost:8081")
    public ResponseEntity<?> uploadImage(@ModelAttribute UploadImageRequest request) {
        MultipartFile file = request.getFile();
        Long userId = request.getUserId();

        if (file.isEmpty() || userId == null || userId == -1) {
            return ResponseEntity.badRequest().body("Invalid request. File or User ID is missing.");
        }

        try {
            // Upload to S3 and get the URL
            String fileUrl = s3Service.uploadFile(file);
            return ResponseEntity.ok(Map.of("filePath", fileUrl));
        } catch (IOException e) {
            System.out.println("Error uploading image: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image");
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
                .orElse(ResponseEntity.notFound().build());
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
                .orElse(ResponseEntity.notFound().build());
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
    public ResponseEntity<List<Message> >getMessagesBySession(@PathVariable Long sessionId) {
        List<Message> result=messageRepository.findAll()
                .stream()
                .filter(message -> message.getChatSession() != null && message.getChatSession().getId().equals(sessionId))
                .toList();
        System.out.println(result);
        return ResponseEntity.ok(result);
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
                .orElse(ResponseEntity.notFound().build());
    }



    @GetMapping("/health")
    public ResponseEntity<String> healthCheck() {
        return ResponseEntity.ok("Application is running");
    }
}
