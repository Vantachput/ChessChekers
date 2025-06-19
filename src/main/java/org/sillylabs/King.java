
package org.sillylabs;


public class King extends ChessPiece {
    private boolean hasMoved;

    public King(String color, int x, int y) {
        super("King", color, x, y);
        this.hasMoved = false;
    }

    public boolean getHasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    @Override
    public boolean isValidMove(int toX, int toY, Piece[][] grid) {
        int dx = Math.abs(toX - x);
        int dy = Math.abs(toY - y);

        // Обычный ход короля (на одну клетку в любом направлении)
        if (dx <= 1 && dy <= 1) {
            return grid[toX][toY] == null || !grid[toX][toY].getColor().equals(color);
        }

        // Рокировка (король движется на две клетки по горизонтали)
        if (!hasMoved && dx == 0 && Math.abs(dy) == 2) {
            // Проверяем, что это горизонтальное перемещение на два поля
            // Король может быть на позиции 3 или 4, в зависимости от начальной расстановки
            if ((y == 3 && (toY == 1 || toY == 5)) || (y == 4 && (toY == 2 || toY == 6))) {
                return true; // Дополнительные проверки будут в Board.isValidMove
            }
        }

        return false;
    }

    public boolean isInCheck(Piece[][] grid) {
        // Проверяем все клетки на доске
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = grid[i][j];
                // Если на клетке есть фигура, и она принадлежит противнику
                if (piece != null && !piece.getColor().equals(color)) {
                    // И эта фигура может походить на клетку, где находится король
                    if (piece.isValidMove(x, y, grid)) {
                        return true; // Король в шахе
                    }
                }
            }
        }
        return false; // Король не в шахе
    }
}