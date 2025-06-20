package org.sillylabs.pieces;

import org.sillylabs.Game;

public class Pawn extends ChessPiece {
    private Game game;

    public Pawn(Color color, int row, int column, Game game) {
        super("Pawn", color, row, column);
        this.game = game;
    }

    @Override
    public boolean isValidMove(int toRow, int toColumn, Piece[][] grid) {
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
            return grid[toRow][toColumn] == null && toRow == game.getEnPassantTargetRow() && toColumn == game.getEnPassantTargetColumn() && game.isEnPassantPossible();
        }

        return false;
    }
}