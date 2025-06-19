package org.sillylabs;

public class Pawn extends ChessPiece {
    private Game game;

    public Pawn(String color, int x, int y, Game game) {
        super("Pawn", color, x, y);
        this.game = game;
    }

    @Override
    public boolean isValidMove(int toX, int toY, Piece[][] grid) {
        int direction = color.equals("White") ? 1 : -1;
        int startRow = color.equals("White") ? 1 : 6;
        int dx = toX - x;
        int dy = toY - y;

        // Forward move: one square
        if (dy == 0 && dx == direction && grid[toX][toY] == null) {
            return true;
        }

        // Forward move: two squares from starting rank
        if (dy == 0 && dx == 2 * direction && x == startRow && grid[toX][toY] == null && grid[x + direction][y] == null) {
            return true;
        }

        // Diagonal capture
        if (Math.abs(dy) == 1 && dx == direction) {
            if (grid[toX][toY] != null && !grid[toX][toY].getColor().equals(color)) {
                return true;
            }
            // En passant
            if (grid[toX][toY] == null && toX == game.getEnPassantTargetX() && toY == game.getEnPassantTargetY() && game.isEnPassantPossible()) {
                return true;
            }
        }

        return false;
    }
}