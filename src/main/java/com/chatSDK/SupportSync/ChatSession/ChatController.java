package com.chatSDK.SupportSync.ChatSession;

import com.chatSDK.SupportSync.Repositories.ChatSessionRepository;
import com.chatSDK.SupportSync.Repositories.MessageRepository;
import com.chatSDK.SupportSync.Repositories.UserRepository;
import com.chatSDK.SupportSync.User.AddAgentRequest;
import com.chatSDK.SupportSync.User.AppUser;
import com.chatSDK.SupportSync.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
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
    @SendTo("/topic/messages")
    public Message sendMessage(Message message) {
        System.out.println(message.toString());
        // Assuming the message is already created with the content and sender.
        ChatSession session = chatSessionRepository.findById(message.getChatSession().getId())
                .orElseThrow(() -> new RuntimeException("Chat session not found"));

        // Saving the message into the database
        message.setTimestamp(System.currentTimeMillis());  // You can use a proper timestamp mechanism here
        messageRepository.save(message);

        // Return the message to be sent to subscribed clients
        return message;
    }

    // Optional: Method to join a user into a session (e.g., a customer or agent entering a chat)
    @MessageMapping("/chat.addUser")
    @SendTo("/topic/messages")
    public Message addUser(AppUser user, ChatSession sessionTemp) {
        ChatSession session = chatSessionRepository.findById(sessionTemp.getId())
                .orElseThrow(() -> new RuntimeException("Chat session not found"));
        AppUser agent =userRepository.findById(user.getId()).orElseThrow(()-> new RuntimeException("Agent not found"));
        // Logic to add user to a session
        // Logic to add user to a session
        session.setAgent(agent);  // Can set an agent if needed
        session.setStartedAt(System.currentTimeMillis());

        chatSessionRepository.save(session);
        long timestamp = System.currentTimeMillis();

        // Send a message back with user info
        return new Message("Agent " + agent.getUsername() + " has joined the chat.", agent, session, timestamp);
    }

    @MessageMapping("/chat.addAgent")
    @SendTo("/topic/messages")
    public Message addAgent(@Payload AddAgentRequest request) {
        System.out.println(request.toString());
        System.out.println(request.getUser().toString() + " " + request.getSessionTemp().toString());
        ChatSession session = chatSessionRepository.findById(request.getSessionTemp().getId())
                .orElseThrow(() -> new RuntimeException("Chat session not found"));
        AppUser agent = userRepository.findById(request.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Agent not found"));

        session.setAgent(agent);
        session.setStartedAt(System.currentTimeMillis());

        chatSessionRepository.save(session);
        long timestamp = System.currentTimeMillis();

        return new Message("Agent " + agent.getUsername() + " has joined the chat.", agent, session, timestamp);
    }

    @PostMapping("/chat.startSession")
    @CrossOrigin(origins = "http://localhost:8081")
    public ResponseEntity<ChatSession> startChatSession(@RequestBody AppUser user) {

        if (user.getId()!=null && userRepository.findById(user.getId()).isEmpty()) {
            userRepository.save(user);
        }

        // Create and save the chat session
        ChatSession chatSession = new ChatSession();
        chatSession.setUser(user);
        chatSession.setStartedAt(System.currentTimeMillis());
        chatSessionRepository.save(chatSession);
        
        messagingTemplate.convertAndSend("/topic/activeSessions", chatSession);

        // Return a response entity with the created chat session
        return ResponseEntity.ok(chatSession);
    }

    @PostMapping("/chat/uploadImage")
    @CrossOrigin(origins = "http://localhost:8081")
    public ResponseEntity<String> uploadImage(@RequestParam("file") MultipartFile file) {
        System.out.println(file.getOriginalFilename());
        try {
            // Save file to the server
            String imagePath = uploadDir + file.getOriginalFilename();
            String accessPath=accessDir+file.getOriginalFilename();
            file.transferTo(new File(imagePath));
            return ResponseEntity.ok(accessPath); // Return the image path
        } catch (IOException e) {
            System.out.println(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading image");
        }
        catch (Exception e){
            System.out.println(e.getMessage());
            return ResponseEntity.internalServerError().body("error");
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
