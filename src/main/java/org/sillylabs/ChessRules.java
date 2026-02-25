package org.sillylabs;

import org.sillylabs.pieces.*;

public class ChessRules implements GameRules {
    private GameCoordinator coordinator;

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

        int minCol = Math.min(fromColumn, rookColumn);
        int maxCol = Math.max(fromColumn, rookColumn);
        for (int column = minCol + 1; column < maxCol; column++) {
            if (board.getPieceAt(fromRow, column) != null) {
                return false;
            }
        }

        if (isKingInCheck(board, king.getColor())) {
            return false;
        }

        int step = isKingside ? 1 : -1;
        if (isSquareAttacked(board, fromRow, fromColumn + step, king.getColor())) {
            return false;
        }
        if (isSquareAttacked(board, fromRow, toColumn, king.getColor())) {
            return false;
        }
        return true;
    }

    boolean leavesKingInCheck(Board board, Piece piece, int fromRow, int fromColumn, int toRow, int toColumn) {
        Piece tempTarget = board.getPieceAt(toRow, toColumn);

        Piece enPassantCapturedPiece = null;
        int epRow = -1, epCol = -1;

        if (piece instanceof Pawn && Math.abs(toColumn - fromColumn) == 1 && tempTarget == null) {
            int direction = piece.getColor() == Color.WHITE ? -1 : 1;
            epRow = toRow - direction;
            epCol = toColumn;
            enPassantCapturedPiece = board.getPieceAt(epRow, epCol);
            board.setPieceAt(epRow, epCol, null);
        }

        if (piece instanceof King && Math.abs(toColumn - fromColumn) == 2) {
            if (isKingInCheck(board, piece.getColor())) {
                return true;
            }

            int step = (toColumn > fromColumn) ? 1 : -1;
            int passColumn = fromColumn + step;

            board.setPieceAt(fromRow, passColumn, piece);
            board.setPieceAt(fromRow, fromColumn, null);
            boolean passCheck = isKingInCheck(board, piece.getColor());

            board.setPieceAt(fromRow, fromColumn, piece);
            board.setPieceAt(fromRow, passColumn, null);

            if (passCheck) {
                return true;
            }
        }

        board.setPieceAt(toRow, toColumn, piece);
        board.setPieceAt(fromRow, fromColumn, null);

        boolean inCheck = isKingInCheck(board, piece.getColor());

        board.setPieceAt(fromRow, fromColumn, piece);
        board.setPieceAt(toRow, toColumn, tempTarget);

        if (enPassantCapturedPiece != null) {
            board.setPieceAt(epRow, epCol, enPassantCapturedPiece);
        }

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

    // --- НОВІ МЕТОДИ ДЛЯ ПЕРЕВІРКИ СТАНУ ГРИ ---

    // Метод перевіряє, чи є у гравця хоча б один легальний хід
    public boolean hasLegalMoves(Board board, Color color) {
        boolean isWhiteTurn = color == Color.WHITE;
        Piece[][] grid = board.getGrid();
        for (int fromRow = 0; fromRow < 8; fromRow++) {
            for (int fromColumn = 0; fromColumn < 8; fromColumn++) {
                Piece piece = grid[fromRow][fromColumn];
                if (piece != null && piece.getColor() == color) {
                    for (int toRow = 0; toRow < 8; toRow++) {
                        for (int toColumn = 0; toColumn < 8; toColumn++) {
                            if (isValidMove(board, fromRow, fromColumn, toRow, toColumn, isWhiteTurn, false)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    // Метод перевіряє, чи достатньо матеріалу на дошці для того, щоб поставити мат
    public boolean isInsufficientMaterial(Board board) {
        Piece[][] grid = board.getGrid();
        int whiteKnights = 0, whiteBishops = 0, blackKnights = 0, blackBishops = 0;
        int otherPieces = 0;

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p != null) {
                    if (p instanceof King) continue;
                    if (p instanceof Knight) {
                        if (p.getColor() == Color.WHITE) whiteKnights++; else blackKnights++;
                    } else if (p instanceof Bishop) {
                        if (p.getColor() == Color.WHITE) whiteBishops++; else blackBishops++;
                    } else {
                        // Пішаки, Тури та Ферзі - це достатній матеріал
                        otherPieces++;
                    }
                }
            }
        }

        if (otherPieces > 0) return false;

        int totalWhiteMinors = whiteKnights + whiteBishops;
        int totalBlackMinors = blackKnights + blackBishops;

        // Тільки Король проти Короля
        if (totalWhiteMinors == 0 && totalBlackMinors == 0) return true;

        // Король + один Слон/Кінь проти Короля
        if ((totalWhiteMinors == 1 && totalBlackMinors == 0) || (totalWhiteMinors == 0 && totalBlackMinors == 1)) return true;

        return false;
    }

    @Override
    public boolean isGameOver(Board board, Color color) {
        // Мат ставиться ТІЛЬКИ якщо король ПІД ШАХОМ і у нього НЕМАЄ ХОДІВ
        return isKingInCheck(board, color) && !hasLegalMoves(board, color);
    }
    // ---------------------------------------------

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
        piece.setPosition(toRow, toColumn);
        updatePieceMoveStatus(piece);
    }

    void performCastling(Board board, int fromRow, int fromColumn, int toRow, int toColumn, King king) {
        boolean isKingside = toColumn > fromColumn;
        int rookFromColumn = isKingside ? 7 : 0;
        int rookToColumn = isKingside ? (fromColumn == 3 ? 4 : 5) : (fromColumn == 3 ? 2 : 3);
        Rook rook = (Rook) board.getPieceAt(fromRow, rookFromColumn);

        board.setPieceAt(toRow, toColumn, king);
        board.setPieceAt(fromRow, fromColumn, null);
        king.setPosition(toRow, toColumn);

        board.setPieceAt(toRow, rookToColumn, rook);
        board.setPieceAt(fromRow, rookFromColumn, null);
        rook.setPosition(toRow, rookToColumn);

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
        MoveContext context = new MoveContext(board.getGrid(), coordinator.getEnPassantTargetRow(), coordinator.getEnPassantTargetColumn(), coordinator.isEnPassantPossible());

        for (int row = 0; row < 8; row++) {
            for (int column = 0; column < 8; column++) {
                Piece attackingPiece = board.getPieceAt(row, column);
                if (attackingPiece != null && attackingPiece.getColor() == opponentColor) {
                    if (attackingPiece instanceof Pawn) {
                        int direction = attackingPiece.getColor() == Color.WHITE ? -1 : 1;
                        if (targetRow == row + direction && (targetColumn == column - 1 || targetColumn == column + 1)) {
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