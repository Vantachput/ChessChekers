package org.sillylabs;

import org.sillylabs.pieces.*;

import java.util.List;

public class CheckersRules implements GameRules {
    private GameCoordinator coordinator;

    public void setGame(GameCoordinator coordinator) {
        this.coordinator = coordinator;
    }

    @Override
    public boolean isValidMove(Board board, int fromRow, int fromColumn, int toRow, int toColumn, boolean isWhiteTurn, boolean isMultiJump) {
        Piece piece = board.getPieceAt(fromRow, fromColumn);
        if (!isBasicMoveValid(piece, isWhiteTurn, board, toRow, toColumn)) {
            return false;
        }

        CheckersPiece checkersPiece = (CheckersPiece) piece;
        if (!isMultiJump && hasAvailableCaptures(board, isWhiteTurn)) {
            return checkersPiece.isValidMoveWithMultiJump(toRow, toColumn, board.getGrid(), false) && isCaptureMove(fromRow, fromColumn, toRow, toColumn);
        }
        return checkersPiece.isValidMoveWithMultiJump(toRow, toColumn, board.getGrid(), isMultiJump);
    }

    boolean isBasicMoveValid(Piece piece, boolean isWhiteTurn, Board board, int toRow, int toColumn) {
        return piece != null && piece.getColor() == (isWhiteTurn ? Color.WHITE : Color.BLACK) && piece instanceof CheckersPiece && !(board.getPieceAt(toRow, toColumn) instanceof King);
    }

    boolean isCaptureMove(int fromRow, int fromColumn, int toRow, int toColumn) {
        return Math.abs(toRow - fromRow) >= 2 && Math.abs(toColumn - fromColumn) >= 2;
    }

    boolean hasAvailableCaptures(Board board, boolean isWhiteTurn) {
        Color color = isWhiteTurn ? Color.WHITE : Color.BLACK;
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Piece piece = board.getPieceAt(row, column);
                if (piece != null && piece.getColor() == color && piece instanceof CheckersPiece) {
                    List<int[]> captureMoves = ((CheckersPiece) piece).getCaptureMoves(row, column, board.getGrid(), GameMode.CHECKERS);
                    if (!captureMoves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean isKingInCheck(Board board, Color color) {
        return false; // Checkers has no king or check concept
    }

    @Override
    public boolean isGameOver(Board board, Color color) {
        boolean isWhiteTurn = color == Color.WHITE;
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Piece piece = board.getPieceAt(row, col);
                // Якщо знайшли фігуру поточного гравця
                if (piece != null && piece.getColor() == color && piece instanceof CheckersPiece) {

                    // Перевіряємо, чи є взяття
                    List<int[]> captureMoves = ((CheckersPiece) piece).getCaptureMoves(row, col, board.getGrid(), GameMode.CHECKERS);
                    if (!captureMoves.isEmpty()) {
                        return false; // Є хід, гра триває
                    }

                    // Перевіряємо, чи є звичайні ходи
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toCol = 0; toCol < 8; toCol++) {
                            if (isValidMove(board, row, col, toRow, toCol, isWhiteTurn, false)) {
                                return false; // Є хід, гра триває
                            }
                        }
                    }
                }
            }
        }

        return true;
    }

    @Override
    public void movePiece(Board board, int fromRow, int fromColumn, int toRow, int toColumn, boolean isMultiJump, int capturedPawnRow, int capturedPawnColumn) {
        Piece piece = board.getPieceAt(fromRow, fromColumn);
        System.out.println("Moving piece: " + piece.getType() + " from (" + fromRow + ", " + fromColumn + ") to (" + toRow + ", " + toColumn + ")");

        if (Math.abs(toRow - fromRow) >= 2) {
            executeCheckersCapture(board, fromRow, fromColumn, toRow, toColumn, piece);
        } else {
            if (board.getPieceAt(toRow, toColumn) != null) {
                System.out.println(piece.getColor() + " " + piece.getType() + " captures " + board.getPieceAt(toRow, toColumn).getColor() + " " + board.getPieceAt(toRow, toColumn).getType());
            }
            board.setPieceAt(toRow, toColumn, piece);
            board.setPieceAt(fromRow, fromColumn, null);
        }

        // ВАЖЛИВО: Оновлюємо координати фігури після ходу!
        piece.setPosition(toRow, toColumn);
    }

    private void executeCheckersCapture(Board board, int fromRow, int fromColumn, int toRow, int toColumn, Piece piece) {
        int dRow = (toRow - fromRow) > 0 ? 1 : -1;
        int dColumn = (toColumn - fromColumn) > 0 ? 1 : -1;
        int steps = Math.abs(toRow - fromRow);
        for (int i = 1; i < steps; i++) {
            int midRow = fromRow + i * dRow;
            int midColumn = fromColumn + i * dColumn;
            if (board.getPieceAt(midRow, midColumn) != null && board.getPieceAt(midRow, midColumn).getColor() != piece.getColor()) {
                System.out.println(piece.getColor() + " CheckersMan captures " + board.getPieceAt(midRow, midColumn).getColor() + " piece at (" + midRow + ", " + midColumn + ")");
                board.setPieceAt(midRow, midColumn, null);
            }
        }
        board.setPieceAt(toRow, toColumn, piece);
        board.setPieceAt(fromRow, fromColumn, null);
    }

    @Override
    public void setGameCoordinator(GameCoordinator coordinator) {
        this.coordinator = coordinator;
    }
}