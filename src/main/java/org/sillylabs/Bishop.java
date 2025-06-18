package org.sillylabs;

public class Bishop extends ChessPiece {
    public Bishop(String color, int x, int y) {
        super("Bishop", color, x, y);
    }

    @Override
    public boolean isValidMove(int toX, int toY, Piece[][] grid) {
        if (Math.abs(toX - x) != Math.abs(toY - y)) return false;
        int dx = toX > x ? 1 : -1;
        int dy = toY > y ? 1 : -1;
        int steps = Math.abs(toX - x);
        for (int i = 1; i < steps; i++) {
            if (grid[x + i * dx][y + i * dy] != null) return false;
        }
        return grid[toX][toY] == null || !grid[toX][toY].getColor().equals(color);
    }
}