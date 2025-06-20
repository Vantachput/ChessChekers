package org.sillylabs;

import org.sillylabs.pieces.*;

public class HybridMoveValidator implements MoveValidator {
    private final ChessMoveValidator chessValidator = new ChessMoveValidator();
    private final CheckersMoveValidator checkersValidator = new CheckersMoveValidator();

    @Override
    public boolean isValidMove(Board board, Game game, int fromRow, int fromColumn, int toRow, int toColumn, boolean isWhiteTurn, boolean isMultiJump) {
        Piece piece = board.getPiece(fromRow, fromColumn);
        if (piece == null || piece.getColor() != (isWhiteTurn ? Color.WHITE : Color.BLACK) || board.getPiece(toRow, toColumn) instanceof King) {
            return false;
        }

        boolean isValid;
        if (piece instanceof ChessPiece) {
            isValid = chessValidator.isValidMove(board, game, fromRow, fromColumn, toRow, toColumn, isWhiteTurn, isMultiJump);
        } else if (piece instanceof CheckersPiece) {
            isValid = checkersValidator.isValidMove(board, game, fromRow, fromColumn, toRow, toColumn, isWhiteTurn, isMultiJump);
        } else {
            return false;
        }

        if (isValid && board.getPiece(toRow, toColumn) != null) {
            Piece target = board.getPiece(toRow, toColumn);
            if (!((piece instanceof ChessPiece && target instanceof ChessPiece) || (piece instanceof CheckersPiece && target instanceof CheckersPiece))) {
                return false;
            }
        }
        return isValid;
    }
}