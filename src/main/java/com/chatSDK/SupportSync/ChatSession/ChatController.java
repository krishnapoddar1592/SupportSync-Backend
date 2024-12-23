package com.chatSDK.SupportSync.ChatSession;

import com.chatSDK.SupportSync.Repositories.ChatSessionRepository;
import com.chatSDK.SupportSync.Repositories.MessageRepository;
import com.chatSDK.SupportSync.Repositories.UserRepository;
import com.chatSDK.SupportSync.User.AddAgentRequest;
import com.chatSDK.SupportSync.User.AppUser;
import com.chatSDK.SupportSync.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@CrossOrigin(origins =  {"http://localhost:8081/","http://localhost:8082/"})
public class ChatController {
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
    public ChatSession startChatSession(@RequestBody AppUser user) {

        if (user.getId() == null) {
            userRepository.save(user);
        }

        // Create and save the chat session
        ChatSession chatSession = new ChatSession();
        chatSession.setUser(user);
        chatSession.setStartedAt(System.currentTimeMillis());
        chatSessionRepository.save(chatSession);
        
        messagingTemplate.convertAndSend("/topic/activeSessions", chatSession);

        // Return a response entity with the created chat session
        return chatSession;
    }




}
