package org.sillylabs;

import org.sillylabs.pieces.*;

import java.util.List;

public class CheckersGameStateManager implements GameStateManager {
    @Override
    public boolean isKingInCheck(Color color, Board board) {
        return false; // Checkers has no king or check concept
    }

    @Override
    public boolean isCheckmate(Color color, Board board) {
        boolean isWhiteTurn = color == Color.WHITE;
        boolean hasPieces = false;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPiece(row, col);
                if (piece != null && piece.getColor() == color && piece instanceof CheckersPiece) {
                    hasPieces = true;
                    List<int[]> captureMoves = ((CheckersPiece) piece).getCaptureMoves(row, col, board.getGrid(), board.getGameMode());
                    if (!captureMoves.isEmpty()) {
                        return false; // Player has a capture move
                    }
                    // Check non-capture moves
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            if (board.isValidMove(row, col, toRow, toCol, isWhiteTurn, board.getGameMode(), false)) {
                                return false; // Player has a legal move
                            }
                        }
                    }
                }
            }
        }
        return hasPieces; // No legal moves and has pieces means stalemate; no pieces means loss
    }
}