package org.sillylabs;

public interface MoveValidator {
    boolean isValidMove(Board board, Game game, int fromRow, int fromColumn, int toRow, int toColumn, boolean isWhiteTurn, boolean isMultiJump);
}