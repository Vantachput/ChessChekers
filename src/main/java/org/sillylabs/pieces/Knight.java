package org.sillylabs.pieces;

public class Knight extends ChessPiece {
    public Knight(Color color, int row, int column) {
        super("Knight", color, row, column);
    }

    @Override
    public boolean isValidMove(int toRow, int toColumn, Piece[][] grid) {
        int dRow = Math.abs(toRow - row);
        int dColumn = Math.abs(toColumn - column);
        boolean isLShape = (dRow == 2 && dColumn == 1) || (dRow == 1 && dColumn == 2);
        return isLShape && (grid[toRow][toColumn] == null || grid[toRow][toColumn].getColor() != color);
    }
}