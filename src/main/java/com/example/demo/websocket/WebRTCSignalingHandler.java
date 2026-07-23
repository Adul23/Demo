package com.example.demo.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

@Component
public class WebRTCSignalingHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Set<WebSocketSession>> rooms = new ConcurrentHashMap<>();
    private final Map<String, String> sessionRoomMap = new ConcurrentHashMap<>();

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        Map<String, Object> messageMap = objectMapper.readValue(payload, Map.class);
        
        String type = (String) messageMap.get("type");
        String roomId = (String) messageMap.get("roomId");
        
        if (roomId == null || type == null) {
            return;
        }

        switch (type) {
            case "join":
                rooms.computeIfAbsent(roomId, k -> new CopyOnWriteArraySet<>()).add(session);
                sessionRoomMap.put(session.getId(), roomId);
                broadcastToRoomExceptSender(roomId, session, message);
                break;
                
            case "offer":
            case "answer":
            case "ice-candidate":
                broadcastToRoomExceptSender(roomId, session, message);
                break;
                
            default:
                break;
        }
    }

    private void broadcastToRoomExceptSender(String roomId, WebSocketSession sender, TextMessage message) {
        Set<WebSocketSession> sessions = rooms.get(roomId);
        if (sessions != null) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen() && !session.getId().equals(sender.getId())) {
                    try {
                        session.sendMessage(message);
                    } catch (IOException e) {
                        System.err.println("Error forwarding signal to peer: " + session.getId());
                    }
                }
            }
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String roomId = sessionRoomMap.remove(session.getId());
        if (roomId != null) {
            Set<WebSocketSession> sessions = rooms.get(roomId);
            if (sessions != null) {
                sessions.remove(session);
                if (sessions.isEmpty()) {
                    rooms.remove(roomId);
                }
            }
        }
    }
}
