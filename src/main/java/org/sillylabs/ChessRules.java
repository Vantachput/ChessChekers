package org.sillylabs;

import org.sillylabs.pieces.*;

public class ChessRules implements GameRules {
    private GameCoordinator coordinator;

//    @Override
//    public void setGame(GameCoordinator coordinator) {
//        this.coordinator = coordinator;
//    }

    @Override
    public boolean isValidMove(Board board, int fromRow, int fromColumn, int toRow, int toColumn, boolean isWhiteTurn, boolean isMultiJump) {
        Piece piece = board.getPieceAt(fromRow, fromColumn);
        if (!isBasicMoveValid(piece, isWhiteTurn, board, toRow, toColumn)) {
            return false;
        }

        if (piece instanceof King && isCastlingMove(piece, fromRow, fromColumn, toRow, toColumn)) {
            return validateCastling(board, (King) piece, fromRow, fromColumn, toRow, toColumn);
        }

        MoveContext context = new MoveContext(board.getGrid(), coordinator.getEnPassantTargetRow(), coordinator.getEnPassantTargetColumn(), coordinator.isEnPassantPossible());
        if (!piece.isValidMove(toRow, toColumn, context)) {
            return false;
        }

        return !leavesKingInCheck(board, piece, fromRow, fromColumn, toRow, toColumn);
    }

    boolean isBasicMoveValid(Piece piece, boolean isWhiteTurn, Board board, int toRow, int toColumn) {
        return piece != null && piece.getColor() == (isWhiteTurn ? Color.WHITE : Color.BLACK) && !(board.getPieceAt(toRow, toColumn) instanceof King);
    }

    boolean isCastlingMove(Piece piece, int fromRow, int fromColumn, int toRow, int toColumn) {
        return piece instanceof King && !((King) piece).getHasMoved() && fromRow == toRow && Math.abs(toColumn - fromColumn) == 2;
    }

    boolean validateCastling(Board board, King king, int fromRow, int fromColumn, int toRow, int toColumn) {
        boolean isKingside = toColumn > fromColumn;
        int rookColumn = isKingside ? 7 : 0;
        Piece rookPiece = board.getPieceAt(fromRow, rookColumn);
        if (!(rookPiece instanceof Rook) || ((Rook) rookPiece).getHasMoved() || rookPiece.getColor() != king.getColor()) {
            return false;
        }

        int start = Math.min(fromColumn, toColumn);
        int end = Math.max(fromColumn, toColumn);
        for (int column = start + 1; column < end; column++) {
            if (board.getPieceAt(fromRow, column) != null) {
                return false;
            }
        }

        if (isKingInCheck(board, king.getColor())) {
            return false;
        }

        for (int column = fromColumn; column <= toColumn; column += (toColumn > fromColumn ? 1 : -1)) {
            if (isSquareAttacked(board, fromRow, column, king.getColor())) {
                return false;
            }
        }
        return true;
    }

    boolean leavesKingInCheck(Board board, Piece piece, int fromRow, int fromColumn, int toRow, int toColumn) {
        Piece[][] grid = board.getGrid();
        Piece tempTarget = grid[toRow][toColumn];
        grid[toRow][toColumn] = piece;
        grid[fromRow][fromColumn] = null;
        piece.setPosition(toRow, toColumn);

        boolean inCheck = isKingInCheck(board, piece.getColor());

        piece.setPosition(fromRow, fromColumn);
        grid[fromRow][fromColumn] = piece;
        grid[toRow][toColumn] = tempTarget;
        return inCheck;
    }

    @Override
    public boolean isKingInCheck(Board board, Color color) {
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
    public boolean isGameOver(Board board, Color color) {
        if (!isKingInCheck(board, color)) {
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
                            if (isValidMove(board, fromRow, fromColumn, toRow, toColumn, isWhiteTurn, false)) {
                                return false;
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

        if (capturedPawnRow != -1 && capturedPawnColumn != -1) {
            board.setPieceAt(capturedPawnRow, capturedPawnColumn, null);
            System.out.println(piece.getColor() + " Pawn captures en passant at (" + capturedPawnRow + ", " + capturedPawnColumn + ")");
        }

        if (piece instanceof King && Math.abs(toColumn - fromColumn) == 2 && fromRow == toRow) {
            performCastling(board, fromRow, fromColumn, toRow, toColumn, (King) piece);
            return;
        }

        if (board.getPieceAt(toRow, toColumn) != null) {
            System.out.println(piece.getColor() + " " + piece.getType() + " captures " + board.getPieceAt(toRow, toColumn).getColor() + " " + board.getPieceAt(toRow, toColumn).getType());
        }
        board.setPieceAt(toRow, toColumn, piece);
        board.setPieceAt(fromRow, fromColumn, null);

        updatePieceMoveStatus(piece);
    }

    void performCastling(Board board, int fromRow, int fromColumn, int toRow, int toColumn, King king) {
        boolean isKingside = toColumn > fromColumn;
        int rookFromColumn = isKingside ? 7 : 0;
        int rookToColumn = isKingside ? (fromColumn == 3 ? 4 : 5) : (fromColumn == 3 ? 2 : 3);
        Rook rook = (Rook) board.getPieceAt(fromRow, rookFromColumn);

        board.setPieceAt(toRow, toColumn, king);
        board.setPieceAt(fromRow, fromColumn, null);
        board.setPieceAt(toRow, rookToColumn, rook);
        board.setPieceAt(fromRow, rookFromColumn, null);

        System.out.println("Performed " + (isKingside ? "Kingside" : "Queenside") + " castling.");
    }

    void updatePieceMoveStatus(Piece piece) {
        if (piece instanceof King) {
            ((King) piece).setHasMoved(true);
        } else if (piece instanceof Rook) {
            ((Rook) piece).setHasMoved(true);
        }
    }

    boolean isSquareAttacked(Board board, int targetRow, int targetColumn, Color friendlyKingColor) {
        Color opponentColor = friendlyKingColor == Color.WHITE ? Color.BLACK : Color.WHITE;
        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Piece attackingPiece = board.getPieceAt(row, column);
                if (attackingPiece != null && attackingPiece.getColor() == opponentColor) {
                    Piece[][] tempGrid = board.getGrid();
                    tempGrid[targetRow][targetColumn] = null;
                    MoveContext context = new MoveContext(tempGrid, coordinator.getEnPassantTargetRow(), coordinator.getEnPassantTargetColumn(), coordinator.isEnPassantPossible());
                    if (attackingPiece instanceof Pawn) {
                        int pawnRow = attackingPiece.getRow();
                        int pawnColumn = attackingPiece.getColumn();
                        int direction = attackingPiece.getColor() == Color.WHITE ? 1 : -1;
                        if (targetRow == pawnRow + direction && (targetColumn == pawnColumn - 1 || targetColumn == pawnColumn + 1)) {
                            return true;
                        }
                    } else {
                        if (attackingPiece.isValidMove(targetRow, targetColumn, context)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void setGameCoordinator(GameCoordinator coordinator) {
        this.coordinator = coordinator;
    }
}