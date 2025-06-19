package org.sillylabs;

import java.util.List;

public abstract class CheckersPiece extends Piece {
    protected boolean isKing;

    public CheckersPiece(String type, String color, int x, int y) {
        super(type, color, x, y);
        isKing = false;
    }

    public boolean isKing() {
        return isKing;
    }

    public void setKing(boolean king) {
        isKing = king;
    }

    public abstract List<int[]> getCaptureMoves(int fromX, int fromY, Piece[][] grid, String gameMode);

    // Новый метод для проверки хода с учетом состояния множественного боя
    public boolean isValidMoveWithMultiJump(int toX, int toY, Piece[][] grid, boolean isMultiJump) {
        // Если не в режиме множественного боя, проверяем обязательность взятия
        if (!isMultiJump) {
            List<int[]> availableCaptures = getCaptureMoves(x, y, grid, "Checkers");
            if (!availableCaptures.isEmpty()) {
                // Если есть доступные взятия, можно делать только ходы со взятием
                // Проверяем, является ли текущий ход взятием
                return isCapture(toX, toY, grid);
            }
        }

        // По умолчанию используем обычную логику
        return isValidMove(toX, toY, grid);
    }

    // Вспомогательный метод для проверки, является ли ход взятием
    protected boolean isCapture(int toX, int toY, Piece[][] grid) {
        int dx = toX - x;
        int dy = toY - y;
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);

        if (absDx != absDy) {
            return false; // Must move diagonally
        }

        int stepX = dx > 0 ? 1 : -1;
        int stepY = dy > 0 ? 1 : -1;

        if (!isKing) {
            if (absDx == 2) {
                int midX = x + stepX;
                int midY = y + stepY;
                return midX >= 0 && midX < 8 && midY >= 0 && midY < 8 &&
                        grid[midX][midY] != null && !grid[midX][midY].getColor().equals(color);
            }
        } else {
            // Дамки: проверяем взятие на любом расстоянии
            boolean foundOpponent = false;
            int opponentCount = 0;
            
            for (int i = 1; i < absDx; i++) {
                int checkX = x + i * stepX;
                int checkY = y + i * stepY;
                if (checkX < 0 || checkX >= 8 || checkY < 0 || checkY >= 8) {
                    break;
                }
                if (grid[checkX][checkY] != null) {
                    if (grid[checkX][checkY].getColor().equals(color)) {
                        break; // Friendly piece blocks path
                    }
                    opponentCount++;
                    foundOpponent = true;
                }
            }
            return foundOpponent && opponentCount == 1;
        }
        return false;
    }
}