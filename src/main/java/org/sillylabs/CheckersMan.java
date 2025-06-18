package org.sillylabs;

public class CheckersMan extends CheckersPiece {
    public CheckersMan(String color, int x, int y) {
        super("CheckersMan", color, x, y);
    }

    @Override
    public boolean isValidMove(int toX, int toY, Piece[][] grid) {
        int direction = color.equals("White") ? -1 : 1;
        if (isKing) {
            direction = 0; // Can move both directions
        }
        // Non-capture move: diagonal, one square
        if (Math.abs(toX - x) == 1 && Math.abs(toY - y) == 1 && (direction == 0 || toX - x == direction) && grid[toX][toY] == null) {
            return true;
        }
        // Capture move: diagonal, two squares
        if (Math.abs(toX - x) == 2 && Math.abs(toY - y) == 2) {
            int midX = (x + toX) / 2;
            int midY = (y + toY) / 2;
            return grid[midX][midY] != null && !grid[midX][midY].getColor().equals(color) && grid[toX][toY] == null;
        }
        return false;
    }
}