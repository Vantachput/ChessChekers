package org.sillylabs;

import org.sillylabs.pieces.*;

public class ChessMoveValidator implements MoveValidator {
    @Override
    public boolean isValidMove(Board board, Game game, int fromRow, int fromColumn, int toRow, int toColumn, boolean isWhiteTurn, boolean isMultiJump) {
        Piece piece = board.getPiece(fromRow, fromColumn);
        if (!isBasicMoveValid(piece, isWhiteTurn, board, toRow, toColumn)) {
            return false;
        }

        if (piece instanceof King && isCastlingMove(board, piece, fromRow, fromColumn, toRow, toColumn)) {
            return validateCastling(board, (King) piece, fromRow, fromColumn, toRow, toColumn);
        }

        MoveContext context = new MoveContext(board.getGrid(), game.getEnPassantTargetRow(), game.getEnPassantTargetColumn(), game.isEnPassantPossible());
        if (!piece.isValidMove(toRow, toColumn, context)) {
            return false;
        }

        return !leavesKingInCheck(board, piece, fromRow, fromColumn, toRow, toColumn);
    }

    private boolean isBasicMoveValid(Piece piece, boolean isWhiteTurn, Board board, int toRow, int toColumn) {
        if (piece == null || piece.getColor() != (isWhiteTurn ? Color.WHITE : Color.BLACK) || board.getPiece(toRow, toColumn) instanceof King) {
            return false;
        }
        return true;
    }

    private boolean isCastlingMove(Board board, Piece piece, int fromRow, int fromColumn, int toRow, int toColumn) {
        return piece instanceof King && !((King) piece).getHasMoved() && fromRow == toRow && Math.abs(toColumn - fromColumn) == 2;
    }

    private boolean validateCastling(Board board, King king, int fromRow, int fromColumn, int toRow, int toColumn) {
        boolean isKingside = toColumn > fromColumn;
        int rookColumn = isKingside ? 7 : 0;
        Piece rookPiece = board.getPiece(fromRow, rookColumn);
        if (!(rookPiece instanceof Rook) || ((Rook) rookPiece).getHasMoved() || rookPiece.getColor() != king.getColor()) {
            return false;
        }

        int start = Math.min(fromColumn, toColumn);
        int end = Math.max(fromColumn, toColumn);
        for (int column = start + 1; column < end; column++) {
            if (board.getPiece(fromRow, column) != null) {
                return false;
            }
        }

        ChessGameStateManager stateManager = new ChessGameStateManager();
        if (stateManager.isKingInCheck(king.getColor(), board)) {
            return false;
        }

        for (int column = fromColumn; column <= toColumn; column += (toColumn > fromColumn ? 1 : -1)) {
            if (board.isSquareAttacked(fromRow, column, king.getColor())) {
                return false;
            }
        }
        return true;
    }

    private boolean leavesKingInCheck(Board board, Piece piece, int fromRow, int fromColumn, int toRow, int toColumn) {
        Piece[][] grid = board.getGrid();
        Piece tempTarget = grid[toRow][toColumn];
        grid[toRow][toColumn] = piece;
        grid[fromRow][fromColumn] = null;
        piece.setPosition(toRow, toColumn);

        ChessGameStateManager stateManager = new ChessGameStateManager();
        boolean inCheck = stateManager.isKingInCheck(piece.getColor(), board);

        piece.setPosition(fromRow, fromColumn);
        grid[fromRow][fromColumn] = piece;
        grid[toRow][toColumn] = tempTarget;
        return inCheck;
    }
}