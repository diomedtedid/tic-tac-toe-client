package com.example.tictactoeclient.handlers;

import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;

@Log4j2
public class HeartBeatHandler extends StompSessionHandlerAdapter {

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
