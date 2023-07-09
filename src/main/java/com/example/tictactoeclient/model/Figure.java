package com.example.tictactoeclient.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum Figure {
    X("X"),
    O("O"),
    EMPTY("·");

    private final String replaysment;
}
