package org.sillylabs;

import org.sillylabs.pieces.Color;

public interface GameObserver {
    void onBoardChanged();
    void onStatusUpdate(String message);
    void onPromotionRequested(int row, int column, Color color);
    void onGameOver(boolean isGameOver, Color winner);
}