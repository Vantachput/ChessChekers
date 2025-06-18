package org.sillylabs;

public class Pawn extends ChessPiece {
    public Pawn(String color, int x, int y) {
        super("Pawn", color, x, y);
    }

    @Override
    public boolean isValidMove(int toX, int toY, Piece[][] grid) {
        int direction = color.equals("White") ? 1 : -1;
        // Forward move
        if (toY == y && toX == x + direction && grid[toX][toY] == null) {
            return true;
        }
        // Capture diagonally
        if (Math.abs(toY - y) == 1 && toX == x + direction && grid[toX][toY] != null &&
                !grid[toX][toY].getColor().equals(color)) {
            return true;
        }
        // Double move from start
        if (toY == y && toX == x + 2 * direction && x == (color.equals("White") ? 1 : 6) &&
                grid[x + direction][y] == null && grid[toX][toY] == null) {
            return true;
        }
        return false;
    }
}