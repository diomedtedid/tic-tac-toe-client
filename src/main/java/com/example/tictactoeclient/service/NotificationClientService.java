package com.example.tictactoeclient.service;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.converter.StringMessageConverter;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandler;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.client.WebSocketClient;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.util.concurrent.ExecutionException;

@Service
@RequiredArgsConstructor
@Log4j2
public class NotificationClientService {

    private static final String URL = "ws://localhost:8080/tic-tac-toe/websocket";

    private final WebSocketStompClient client;

    public StompSession getStompSession (StompSessionHandler sessionHandler) {

        try {
            return client.connectAsync(URL, sessionHandler).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
