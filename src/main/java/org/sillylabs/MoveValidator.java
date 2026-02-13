package org.sillylabs;

public interface MoveValidator {
    void setGameCoordinator(GameCoordinator coordinator);
    boolean isValidMove(Board board, int fromRow, int fromColumn, int toRow, int toColumn, boolean isWhiteTurn, boolean isMultiJump);
}