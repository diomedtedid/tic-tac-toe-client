package com.example.tictactoeclient.service;

import com.example.tictactoeclient.model.*;
import com.google.gson.Gson;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.messaging.simp.stomp.StompHeaders;
import org.springframework.messaging.simp.stomp.StompSession;
import org.springframework.messaging.simp.stomp.StompSessionHandlerAdapter;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.util.Map;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

@Service
@RequiredArgsConstructor
@Log4j2
public class GameplayService {
    private static final BlockingQueue<Game> BLOCKING_QUEUE = new LinkedBlockingDeque<>(3);
    private static final Figure USER_FIGURE = Figure.X;

    private final NotificationClientService clientService;

    private Game game;
    private Scanner scanner;


    @PostConstruct
    public void startGame() {
        createNewGame();

        playGame();

        endGame();
    }

    private void playGame() {
        scanner = new Scanner(System.in);

        System.out.println(String.format("Lets play tic tac toe! Your figure is %s. " +
                "Put the figure on the board.", USER_FIGURE));

        printBoard();
        while (Objects.isNull(game.getWinner())) {

            processUserAction();

            if (Objects.nonNull(game.getWinner())) break;

            getOpponentAction();
        }

        System.out.println("The winner is: " + game.getWinner() + "!");

    }

    private void getOpponentAction() {
        System.out.println("Waiting for opponent response...");
        try {
            game = BLOCKING_QUEUE.take();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        printBoard();
    }

    void printBoard() {
        String currentBoard = Board.BOARD_TEMPLATE;
        Set<Map.Entry<Position, Figure>> entries = game.getBoard().entrySet();

        for (Map.Entry<Position, Figure> entry : entries) {
            currentBoard = currentBoard.replace(entry.getKey().toString(), entry.getValue().getReplaysment());
        }

        System.out.println(currentBoard);
    }

    void processUserAction() {
        System.out.println("Enter the coordinate (A1, C2, etc): ");
        String str = scanner.nextLine();

        if (str.equalsIgnoreCase("exit")) {
            endGame();
            return;
        }

        GameAction gameAction = new GameAction(USER_FIGURE, Position.valueOf(str.toUpperCase()));

        game = new RestTemplate().exchange("http://localhost:8080/game/" + game.getGameId(),
                HttpMethod.PUT,
                new HttpEntity<>(gameAction),
                Game.class)
                .getBody();

        printBoard();
    }

    private void createNewGame() {
        game = new RestTemplate().postForEntity("http://localhost:8080/game/", null, Game.class).getBody();
        log.info("New game with id: {}", game.getGameId());
        clientService.getStompSession(new GameActionHandler(game.getGameId().toString()));
    }

    private void endGame() {
        System.out.println("Goodbye!");
        long pid = ProcessHandle.current().pid();
        String os = System.getProperty("os.name");
        Runtime runtime = Runtime.getRuntime();

        try {
            if (os.toLowerCase().contains("win")) {
                runtime.exec("taskkill /F /PID " + pid);
            } else {
                runtime.exec("kill -9 " + pid);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    static class GameActionHandler extends StompSessionHandlerAdapter {
        private static final Logger logger = LoggerFactory.getLogger("GameActionHandler");

        private static final String TOPIC_PREFIX = "/topic/game/";
        private final String topic;

        public GameActionHandler(String gameId) {
            this.topic = TOPIC_PREFIX + gameId;
            logger.debug("Game action handler created for topic: {}", topic );
        }

        @Override
        public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
            logger.info("New session established. Session Id -> {}", session.getSessionId());
            session.subscribe(topic, this);
            logger.info("Subscribed to topic: {}", topic);
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            Game gameNotification = new Gson().fromJson((String) payload, Game.class);
            if (Objects.equals(USER_FIGURE, gameNotification.getNextMove()) || Objects.nonNull(gameNotification.getWinner())) {
                log.info("Opponent action: {}", payload);
                BLOCKING_QUEUE.add(gameNotification);
            }
        }

    }
}
