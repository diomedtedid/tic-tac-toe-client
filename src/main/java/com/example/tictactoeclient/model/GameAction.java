package com.example.tictactoeclient.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GameAction {
    Figure figure;
    Position position;
}
