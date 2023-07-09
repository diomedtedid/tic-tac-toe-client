package com.example.tictactoeclient.service;

import com.example.tictactoeclient.handlers.HeartBeatHandler;
import com.example.tictactoeclient.service.NotificationClientService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Log4j2
public class HeartbeatService {

    private final NotificationClientService notificationClientService;
    private StompSession session;

    @PostConstruct
    void postConstruct() {
        session = notificationClientService.getStompSession(new HeartBeatHandler());
    }

    static class HeartBeatHandler extends StompSessionHandlerAdapter {

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            System.out.println("Connected to the tic tac toe server. Starting the game.");
            session.subscribe("/topic/heartbeat", this);
            log.debug("Subscribed to topic: /topic/heartbeat");
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            System.out.println("Server state: " + payload);
        }
    }
}
