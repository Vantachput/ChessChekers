package org.sillylabs;


public class King extends ChessPiece {
    public King(String color, int x, int y) {
        super("King", color, x, y);
    }

    @Override
    public boolean isValidMove(int toX, int toY, Piece[][] grid) {
        int dx = Math.abs(toX - x);
        int dy = Math.abs(toY - y);
        return dx <= 1 && dy <= 1 && (grid[toX][toY] == null || !grid[toX][toY].getColor().equals(color));
    }

    public boolean isInCheckmate(Piece[][] grid) {
        // Simplified: check if king is in check and no moves escape
        if (!isInCheck(grid)) return false;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dy = -1; dy <= 1; dy++) {
                int newX = x + dx, newY = y + dy;
                if (isWithinBoard(newX, newY) && isValidMove(newX, newY, grid)) {
                    Piece temp = grid[newX][newY];
                    grid[newX][newY] = this;
                    grid[x][y] = null;
                    boolean safe = !isInCheck(grid);
                    grid[x][y] = this;
                    grid[newX][newY] = temp;
                    if (safe) return false;
                }
            }
        }
        return true;
    }

    private boolean isInCheck(Piece[][] grid) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = grid[i][j];
                if (piece != null && !piece.getColor().equals(color)) {
                    if (piece.isValidMove(x, y, grid)) return true;
                }
            }
        }
        return false;
    }
}
