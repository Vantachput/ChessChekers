package org.sillylabs.pieces;

public class Queen extends ChessPiece {
    public Queen(Color color, int row, int column) {
        super("Queen", color, row, column);
    }

    @Override
    public boolean isValidMove(int toRow, int toColumn, Piece[][] grid) {
        if (toRow == row || toColumn == column) {
            int start = toRow == row ? Math.min(column, toColumn) : Math.min(row, toRow);
            int end = toRow == row ? Math.max(column, toColumn) : Math.max(row, toRow);
            for (int i = start + 1; i < end; i++) {
                if (toRow == row && grid[toRow][i] != null) return false;
                if (toColumn == column && grid[i][toColumn] != null) return false;
            }
        } else if (Math.abs(toRow - row) == Math.abs(toColumn - column)) {
            int dRow = toRow > row ? 1 : -1;
            int dColumn = toColumn > column ? 1 : -1;
            int steps = Math.abs(toRow - row);
            for (int i = 1; i < steps; i++) {
                if (grid[row + i * dRow][column + i * dColumn] != null) return false;
            }
        } else {
            return false;
        }
        return grid[toRow][toColumn] == null || grid[toRow][toColumn].getColor() != color;
    }
}