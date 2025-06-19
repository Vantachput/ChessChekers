package org.sillylabs;


public class Rook extends ChessPiece {
    private boolean hasMoved;

    public Rook(String color, int x, int y) {
        super("Rook", color, x, y);
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
        // Ensure move is horizontal or vertical
        if (toX != x && toY != y) return false;

        int start, end;
        if (toX == x) {
            // Horizontal move
            start = Math.min(y, toY);
            end = Math.max(y, toY);
            for (int i = start + 1; i < end; i++) {
                if (grid[toX][i] != null) return false;
            }
        } else {
            // Vertical move
            start = Math.min(x, toX);
            end = Math.max(x, toX);
            for (int i = start + 1; i < end; i++) {
                if (grid[i][toY] != null) return false;
            }
        }
        // Target is empty or opponent's piece
        return (grid[toX][toY] == null || !grid[toX][toY].getColor().equals(color));
    }
}
