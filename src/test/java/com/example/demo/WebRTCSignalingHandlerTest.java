package com.example.demo;

import com.example.demo.websocket.WebRTCSignalingHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import static org.mockito.Mockito.*;

public class WebRTCSignalingHandlerTest {

    private WebRTCSignalingHandler signalingHandler;
    private WebSocketSession session1;
    private WebSocketSession session2;

    @BeforeEach
    void setUp() {
        signalingHandler = new WebRTCSignalingHandler();
        
        session1 = mock(WebSocketSession.class);
        when(session1.getId()).thenReturn("session-1");
        when(session1.isOpen()).thenReturn(true);

        session2 = mock(WebSocketSession.class);
        when(session2.getId()).thenReturn("session-2");
        when(session2.isOpen()).thenReturn(true);
    }

    @Test
    void testJoinAndSignalRouting() throws Exception {
        // 1. Session 1 Joins
        TextMessage joinMsg1 = new TextMessage("{\"type\":\"join\",\"roomId\":\"room-1\"}");
        signalingHandler.handleMessage(session1, joinMsg1);

        // Verify no message is sent since session1 is the only peer
        verify(session1, never()).sendMessage(any());

        // 2. Session 2 Joins same room
        TextMessage joinMsg2 = new TextMessage("{\"type\":\"join\",\"roomId\":\"room-1\"}");
        signalingHandler.handleMessage(session2, joinMsg2);

        // Session 1 should receive Session 2's join message
        verify(session1, times(1)).sendMessage(joinMsg2);

        // 3. Send offer from Session 1 to Session 2
        TextMessage offerMsg = new TextMessage("{\"type\":\"offer\",\"roomId\":\"room-1\",\"sdp\":\"some-sdp\"}");
        signalingHandler.handleMessage(session1, offerMsg);

        // Session 2 should receive the offer
        verify(session2, times(1)).sendMessage(offerMsg);

        // 4. Session 1 disconnects (close session)
        signalingHandler.afterConnectionClosed(session1, CloseStatus.NORMAL);

        // 5. Send candidate from Session 2
        TextMessage candidateMsg = new TextMessage("{\"type\":\"ice-candidate\",\"roomId\":\"room-1\",\"candidate\":\"xyz\"}");
        signalingHandler.handleMessage(session2, candidateMsg);

        // Session 1 is disconnected, so it should not receive the candidate
        verify(session1, times(1)).sendMessage(any());
    }
}
