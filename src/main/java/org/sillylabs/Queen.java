package org.sillylabs;

public class Queen extends ChessPiece {
    public Queen(String color, int x, int y) {
        super("Queen", color, x, y);
    }

    @Override
    public boolean isValidMove(int toX, int toY, Piece[][] grid) {
        // Rook-like or Bishop-like move
        if (toX == x || toY == y) {
            int start = toX == x ? Math.min(y, toY) : Math.min(x, toX);
            int end = toX == x ? Math.max(y, toY) : Math.max(x, toX);
            for (int i = start + 1; i < end; i++) {
                if (toX == x && grid[toX][i] != null) return false;
                if (toY == y && grid[i][toY] != null) return false;
            }
        } else if (Math.abs(toX - x) == Math.abs(toY - y)) {
            int dx = toX > x ? 1 : -1;
            int dy = toY > y ? 1 : -1;
            int steps = Math.abs(toX - x);
            for (int i = 1; i < steps; i++) {
                if (grid[x + i * dx][y + i * dy] != null) return false;
            }
        } else {
            return false;
        }
        return grid[toX][toY] == null || !grid[toX][toY].getColor().equals(color);
    }
}
