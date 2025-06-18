package org.sillylabs;

public class Knight extends ChessPiece {
    public Knight(String color, int x, int y) {
        super("Knight", color, x, y);
    }

    @Override
    public boolean isValidMove(int toX, int toY, Piece[][] grid) {
        int dx = Math.abs(toX - x);
        int dy = Math.abs(toY - y);
        boolean isLShape = (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
        return isLShape && (grid[toX][toY] == null || !grid[toX][toY].getColor().equals(color));
    }
}
