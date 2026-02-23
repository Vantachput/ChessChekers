package org.sillylabs.pieces;

public class Pawn extends ChessPiece {
    public Pawn(Color color, int row, int column) {
        super("Pawn", color, row, column);
    }

    @Override
    public boolean isValidMove(int toRow, int toColumn, MoveContext context) {
        Piece[][] grid = context.grid;
        // ВИПРАВЛЕНО: Білі стартують з 6 рядка і йдуть вгору (-1)
        int direction = color == Color.WHITE ? -1 : 1;
        int startRow = color == Color.WHITE ? 6 : 1;
        int dRow = toRow - row;
        int dColumn = toColumn - column;;

        // Звичайний хід вперед на 1 клітинку
        if (dColumn == 0 && dRow == direction && grid[toRow][toColumn] == null) {
            return true;
        }

        // Перший хід на 2 клітинки
        if (dColumn == 0 && dRow == 2 * direction && row == startRow && grid[toRow][toColumn] == null && grid[row + direction][column] == null) {
            return true;
        }

        // Взяття фігури (по діагоналі)
        if (Math.abs(dColumn) == 1 && dRow == direction) {
            if (grid[toRow][toColumn] != null && grid[toRow][toColumn].getColor() != color) {
                return true;
            }
            // En Passant (взяття на проході)
            return grid[toRow][toColumn] == null && toRow == context.enPassantTargetRow && toColumn == context.enPassantTargetColumn && context.isEnPassantPossible;
        }

        return false;
    }
}