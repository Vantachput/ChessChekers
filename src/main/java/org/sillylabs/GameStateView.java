package org.sillylabs;

import org.sillylabs.pieces.*;

public interface GameStateView {
    Piece[][] getBoardState();
    boolean isWhiteTurn();
    GameMode getGameMode();
    boolean isMultiJump();
    int getMultiJumpFromRow();
    int getMultiJumpFromColumn();
    boolean isWaitingForPromotion();
    boolean isGameOver();
}