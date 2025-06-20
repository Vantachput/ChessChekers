package org.sillylabs;

import org.sillylabs.pieces.*;

public class CheckersMoveValidator implements MoveValidator {
    @Override
    public boolean isValidMove(Board board, Game game, int fromRow, int fromColumn, int toRow, int toColumn, boolean isWhiteTurn, boolean isMultiJump) {
        Piece piece = board.getPiece(fromRow, fromColumn);
        if (!isBasicMoveValid(piece, isWhiteTurn, board, toRow, toColumn)) {
            return false;
        }

        CheckersPiece checkersPiece = (CheckersPiece) piece;
        if (!isMultiJump && board.hasAvailableCaptures(isWhiteTurn, game.getGameMode())) {
            return checkersPiece.isValidMoveWithMultiJump(toRow, toColumn, board.getGrid(), false) && isCaptureMove(fromRow, fromColumn, toRow, toColumn);
        }
        return checkersPiece.isValidMoveWithMultiJump(toRow, toColumn, board.getGrid(), isMultiJump);
    }

    private boolean isBasicMoveValid(Piece piece, boolean isWhiteTurn, Board board, int toRow, int toColumn) {
        return piece != null && piece.getColor() == (isWhiteTurn ? Color.WHITE : Color.BLACK) && piece instanceof CheckersPiece && !(board.getPiece(toRow, toColumn) instanceof King);
    }

    private boolean isCaptureMove(int fromRow, int fromColumn, int toRow, int toColumn) {
        return Math.abs(toRow - fromRow) >= 2 && Math.abs(toColumn - fromColumn) >= 2;
    }
}