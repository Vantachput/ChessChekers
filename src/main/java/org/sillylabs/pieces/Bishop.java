package org.sillylabs.pieces;

public class Bishop extends ChessPiece {
    public Bishop(Color color, int row, int column) {
        super("Bishop", color, row, column);
    }

    @Override
    public boolean isValidMove(int toRow, int toColumn, MoveContext context) {
        Piece[][] grid = context.grid;
        if (Math.abs(toRow - row) != Math.abs(toColumn - column)) return false;
        int dRow = toRow > row ? 1 : -1;
        int dColumn = toColumn > column ? 1 : -1;
        int steps = Math.abs(toRow - row);
        for (int i = 1; i < steps; i++) {
            if (grid[row + i * dRow][column + i * dColumn] != null) return false;
        }
        return grid[toRow][toColumn] == null || grid[toRow][toColumn].getColor() != color;
    }
}