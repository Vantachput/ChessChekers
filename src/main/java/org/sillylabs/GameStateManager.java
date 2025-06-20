package org.sillylabs;

import org.sillylabs.pieces.Color;

public interface GameStateManager {
    boolean isKingInCheck(Color color, Board board);
    boolean isCheckmate(Color color, Board board);
}