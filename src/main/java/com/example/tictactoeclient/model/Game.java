package com.example.tictactoeclient.model;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@NoArgsConstructor
public class Game {
    private UUID gameId;
    private Map<Position, Figure> board;
    private Figure nextMove;
    private Figure winner;
}
