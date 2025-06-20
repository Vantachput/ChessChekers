package org.sillylabs;

import org.sillylabs.pieces.*;

public class ChessGameStateManager implements GameStateManager {
    @Override
    public boolean isKingInCheck(Color color, Board board) {
        Piece[][] grid = board.getGrid();
        King king = null;
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                if (grid[row][column] instanceof King && grid[row][column].getColor() == color) {
                    king = (King) grid[row][column];
                    break;
                }
            }
            if (king != null) break;
        }
        return king != null && king.isInCheck(grid);
    }

    @Override
    public boolean isCheckmate(Color color, Board board) {
        if (!isKingInCheck(color, board)) {
            return false;
        }

        boolean isWhiteTurn = color == Color.WHITE;
        Piece[][] grid = board.getGrid();
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromColumn = 0; fromColumn < 8; fromColumn++) {
                Piece piece = grid[fromRow][fromColumn];
                if (piece != null && piece.getColor() == color) {
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toColumn = 0; toColumn < 8; toColumn++) {
                            if (board.isValidMove(fromRow, fromColumn, toRow, toColumn, isWhiteTurn, board.getGameMode(), false)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }
}