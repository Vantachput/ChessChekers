package org.sillylabs;

import org.sillylabs.pieces.Color;

public class TurnManager {
    private boolean isWhiteTurn;
    private long turnStartTime;

    public TurnManager() {
        isWhiteTurn = true;
        turnStartTime = System.currentTimeMillis();
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public Color getCurrentPlayerColor() {
        return isWhiteTurn ? Color.WHITE : Color.BLACK;
    }

    public void switchTurn() {
        isWhiteTurn = !isWhiteTurn;
        turnStartTime = System.currentTimeMillis();
    }

    public long getTurnStartTime() {
        return turnStartTime;
    }
}