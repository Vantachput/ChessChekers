package org.sillylabs;

import org.sillylabs.pieces.*;

public interface GameRules {
    boolean isValidMove(Board board, int fromRow, int fromColumn, int toRow, int toColumn, boolean isWhiteTurn, boolean isMultiJump);
    boolean isKingInCheck(Board board, Color color);
    boolean isGameOver(Board board, Color color);
    void movePiece(Board board, int fromRow, int fromColumn, int toRow, int toColumn, boolean isMultiJump, int capturedPawnRow, int capturedPawnColumn);
    void setGameCoordinator(GameCoordinator coordinator);
}