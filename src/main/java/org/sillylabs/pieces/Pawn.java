package org.sillylabs.pieces;

public class Pawn extends ChessPiece {
    public Pawn(Color color, int row, int column) {
        super("Pawn", color, row, column);
    }

    @Override
    public boolean isValidMove(int toRow, int toColumn, MoveContext context) {
        Piece[][] grid = context.grid;
        int direction = color == Color.WHITE ? 1 : -1;
        int startRow = color == Color.WHITE ? 1 : 6;
        int dRow = toRow - row;
        int dColumn = toColumn - column;

        if (dColumn == 0 && dRow == direction && grid[toRow][toColumn] == null) {
            return true;
        }

        if (dColumn == 0 && dRow == 2 * direction && row == startRow && grid[toRow][toColumn] == null && grid[row + direction][column] == null) {
            return true;
        }

        if (Math.abs(dColumn) == 1 && dRow == direction) {
            if (grid[toRow][toColumn] != null && grid[toRow][toColumn].getColor() != color) {
                return true;
            }
            return grid[toRow][toColumn] == null && toRow == context.enPassantTargetRow && toColumn == context.enPassantTargetColumn && context.isEnPassantPossible;
        }

        return false;
    }
}