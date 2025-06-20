package org.sillylabs;

import org.sillylabs.pieces.*;

import java.util.List;

public class Board {
    private Piece[][] grid;
    private Game game;
    private static final int BOARD_SIZE = 8;

    public Board() {
        grid = new Piece[BOARD_SIZE][BOARD_SIZE];
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setupBoard(GameMode mode) {
        grid = new Piece[BOARD_SIZE][BOARD_SIZE];
        switch (mode) {
            case CHESS:
                setupChess();
                break;
            case CHECKERS:
                setupCheckers();
                break;
            case HYBRID:
            case UNIFIED:
                setupHybrid();
                break;
        }
    }

    private void setupChess() {
        grid[0][0] = new Rook(Color.WHITE, 0, 0);
        grid[0][1] = new Knight(Color.WHITE, 0, 1);
        grid[0][2] = new Bishop(Color.WHITE, 0, 2);
        grid[0][3] = new King(Color.WHITE, 0, 3);
        grid[0][4] = new Queen(Color.WHITE, 0, 4);
        grid[0][5] = new Bishop(Color.WHITE, 0, 5);
        grid[0][6] = new Knight(Color.WHITE, 0, 6);
        grid[0][7] = new Rook(Color.WHITE, 0, 7);
        for (int i = 0; i < BOARD_SIZE; i++) {
            grid[1][i] = new Pawn(Color.WHITE, 1, i);
        }
        grid[7][0] = new Rook(Color.BLACK, 7, 0);
        grid[7][1] = new Knight(Color.BLACK, 7, 1);
        grid[7][2] = new Bishop(Color.BLACK, 7, 2);
        grid[7][3] = new King(Color.BLACK, 7, 3);
        grid[7][4] = new Queen(Color.BLACK, 7, 4);
        grid[7][5] = new Bishop(Color.BLACK, 7, 5);
        grid[7][6] = new Knight(Color.BLACK, 7, 6);
        grid[7][7] = new Rook(Color.BLACK, 7, 7);
        for (int i = 0; i < BOARD_SIZE; i++) {
            grid[6][i] = new Pawn(Color.BLACK, 6, i);
        }
    }

    private void setupCheckers() {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                if ((row + column) % 2 == 1) {
                    grid[row][column] = new CheckersMan(Color.BLACK, row, column);
                }
            }
        }
        for (int row = 5; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                if ((row + column) % 2 == 1) {
                    grid[row][column] = new CheckersMan(Color.WHITE, row, column);
                }
            }
        }
    }

    private void setupHybrid() {
        grid[0][0] = new Rook(Color.WHITE, 0, 0);
        grid[0][1] = new Knight(Color.WHITE, 0, 1);
        grid[0][2] = new Bishop(Color.WHITE, 0, 2);
        grid[0][3] = new Queen(Color.WHITE, 0, 3);
        grid[0][4] = new King(Color.WHITE, 0, 4);
        grid[0][5] = new Bishop(Color.WHITE, 0, 5);
        grid[0][6] = new Knight(Color.WHITE, 0, 6);
        grid[0][7] = new Rook(Color.WHITE, 0, 7);
        for (int i = 0; i < BOARD_SIZE; i++) {
            grid[1][i] = new Pawn(Color.WHITE, 1, i);
        }
        grid[7][0] = new Rook(Color.BLACK, 7, 0);
        grid[7][1] = new Knight(Color.BLACK, 7, 1);
        grid[7][2] = new Bishop(Color.BLACK, 7, 2);
        grid[7][3] = new Queen(Color.BLACK, 7, 3);
        grid[7][4] = new King(Color.BLACK, 7, 4);
        grid[7][5] = new Bishop(Color.BLACK, 7, 5);
        grid[7][6] = new Knight(Color.BLACK, 7, 6);
        grid[7][7] = new Rook(Color.BLACK, 7, 7);
        for (int i = 0; i < BOARD_SIZE; i++) {
            grid[6][i] = new Pawn(Color.BLACK, 6, i);
        }
        for (int row = 2; row < 4; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                if ((row + column) % 2 == 1) {
                    grid[row][column] = new CheckersMan(Color.BLACK, row, column);
                }
            }
        }
        for (int row = 4; row < 6; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                if ((row + column) % 2 == 1) {
                    grid[row][column] = new CheckersMan(Color.WHITE, row, column);
                }
            }
        }
    }

    public boolean isSquareAttacked(int targetRow, int targetColumn, Color friendlyKingColor) {
        Color opponentColor = friendlyKingColor == Color.WHITE ? Color.BLACK : Color.WHITE;

        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                Piece attackingPiece = grid[row][column];

                if (attackingPiece != null && attackingPiece.getColor() == opponentColor) {
                    Piece[][] tempGrid = new Piece[BOARD_SIZE][BOARD_SIZE];
                    for (int i = 0; i < BOARD_SIZE; i++) {
                        System.arraycopy(grid[i], 0, tempGrid[i], 0, BOARD_SIZE);
                    }
                    tempGrid[targetRow][targetColumn] = null;

                    MoveContext context = new MoveContext(tempGrid, game.getEnPassantTargetRow(), game.getEnPassantTargetColumn(), game.isEnPassantPossible());

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

    public boolean hasAvailableCaptures(boolean isWhiteTurn, GameMode gameMode) {
        Color color = isWhiteTurn ? Color.WHITE : Color.BLACK;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                Piece piece = grid[row][column];
                if (piece != null && piece.getColor() == color && piece instanceof CheckersPiece) {
                    List<int[]> captureMoves = ((CheckersPiece) piece).getCaptureMoves(row, column, grid, gameMode);
                    if (!captureMoves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isValidMove(int fromRow, int fromColumn, int toRow, int toColumn, boolean isWhiteTurn, GameMode gameMode, boolean isMultiJump) {
        System.out.println("Validating move: from (" + fromRow + ", " + fromColumn + ") to (" + toRow + ", " + toColumn + ")");
        Piece pieceToMove = grid[fromRow][fromColumn];
        if (pieceToMove == null) {
            System.out.println("Invalid: No piece at (" + fromRow + ", " + fromColumn + ")");
            return false;
        }
        if (pieceToMove.getColor() != (isWhiteTurn ? Color.WHITE : Color.BLACK)) {
            System.out.println("Invalid: Wrong color. Piece is " + pieceToMove.getColor() + ", turn is " + (isWhiteTurn ? "White" : "Black"));
            return false;
        }

        if (grid[toRow][toColumn] instanceof King) {
            System.out.println("Invalid: Cannot capture the King directly.");
            return false;
        }
        if (pieceToMove instanceof CheckersPiece checkersPiece) {
            System.out.println("Checking CheckersPiece move with multiJump: " + isMultiJump);
            return checkersPiece.isValidMoveWithMultiJump(toRow, toColumn, grid, isMultiJump);
        }
        if (pieceToMove instanceof King king && (gameMode == GameMode.CHESS || gameMode == GameMode.HYBRID || gameMode == GameMode.UNIFIED)) {
            if (!king.getHasMoved() && fromRow == toRow) {
                if ((fromColumn == 3 && toColumn == 5) || (fromColumn == 4 && toColumn == 6)) {
                    int rookColumn = 7;
                    Piece rookPiece = grid[fromRow][rookColumn];
                    if (rookPiece instanceof Rook kingsideRook) {
                        if (!kingsideRook.getHasMoved() && kingsideRook.getColor() == king.getColor()) {
                            boolean pathClear = true;
                            for (int column = fromColumn + 1; column < rookColumn; column++) {
                                if (grid[fromRow][column] != null) {
                                    pathClear = false;
                                    break;
                                }
                            }

                            if (pathClear) {
                                if (!isKingInCheck(king.getColor())) {
                                    boolean squaresSafe = true;
                                    for (int column = fromColumn; column <= toColumn; column++) {
                                        if (isSquareAttacked(fromRow, column, king.getColor())) {
                                            squaresSafe = false;
                                            break;
                                        }
                                    }

                                    if (squaresSafe) {
                                        System.out.println("Valid: Kingside castling");
                                        return true;
                                    } else {
                                        System.out.println("Invalid: Kingside castling - squares attacked");
                                        return false;
                                    }
                                } else {
                                    System.out.println("Invalid: Kingside castling - King is in check");
                                    return false;
                                }
                            } else {
                                System.out.println("Invalid: Kingside castling - squares between King and Rook are not empty");
                                return false;
                            }
                        } else {
                            System.out.println("Invalid: Kingside castling - Rook not found or already moved");
                            return false;
                        }
                    } else {
                        System.out.println("Invalid: Kingside castling - No rook at expected position");
                        return false;
                    }
                }
                else if ((fromColumn == 3 && toColumn == 1) || (fromColumn == 4 && toColumn == 2)) {
                    int rookColumn = 0;
                    Piece rookPiece = grid[fromRow][rookColumn];
                    if (rookPiece instanceof Rook queensideRook) {
                        if (!queensideRook.getHasMoved() && queensideRook.getColor() == king.getColor()) {
                            boolean pathClear = true;
                            for (int column = rookColumn + 1; column < fromColumn; column++) {
                                if (grid[fromRow][column] != null) {
                                    pathClear = false;
                                    break;
                                }
                            }

                            if (pathClear) {
                                if (!isKingInCheck(king.getColor())) {
                                    boolean squaresSafe = true;
                                    for (int column = toColumn; column <= fromColumn; column++) {
                                        if (isSquareAttacked(fromRow, column, king.getColor())) {
                                            squaresSafe = false;
                                            break;
                                        }
                                    }

                                    if (squaresSafe) {
                                        System.out.println("Valid: Queenside castling");
                                        return true;
                                    } else {
                                        System.out.println("Invalid: Queenside castling - squares attacked");
                                        return false;
                                    }
                                } else {
                                    System.out.println("Invalid: Queenside castling - King is in check");
                                    return false;
                                }
                            } else {
                                System.out.println("Invalid: Queenside castling - squares between King and Rook are not empty");
                                return false;
                            }
                        } else {
                            System.out.println("Invalid: Queenside castling - Rook not found or already moved");
                            return false;
                        }
                    } else {
                        System.out.println("Invalid: Queenside castling - No rook at expected position");
                        return false;
                    }
                }
            }
        }

        // ALWAYS FALSE??? Probably broken
        if (gameMode == GameMode.CHECKERS && pieceToMove instanceof CheckersPiece && !isMultiJump) {
            boolean hasCaptures = hasAvailableCaptures(isWhiteTurn, gameMode);
            boolean isCaptureMove = Math.abs(toRow - fromRow) >= 2 && Math.abs(toColumn - fromColumn) >= 2;
            if (hasCaptures && !isCaptureMove) {
                System.out.println("Invalid: Must capture when possible");
                return false;
            }
        }

        MoveContext context = new MoveContext(grid, game.getEnPassantTargetRow(), game.getEnPassantTargetColumn(), game.isEnPassantPossible());
        boolean isValid = pieceToMove.isValidMove(toRow, toColumn, context);
        if (!isValid) {
            System.out.println("Invalid: Piece move invalid for " + pieceToMove.getType());
            return false;
        }

        Piece target = grid[toRow][toColumn];
        if (target != null) {
            // Second statement is always false?? Probably broken
            if (gameMode == GameMode.HYBRID && !((pieceToMove instanceof ChessPiece && target instanceof ChessPiece) ||
                    (pieceToMove instanceof CheckersPiece && target instanceof CheckersPiece))) {
                System.out.println("Invalid: Hybrid mode capture restriction");
                return false;
            }
        }

        Piece tempTarget = grid[toRow][toColumn];
        int originalPieceRow = pieceToMove.getRow();
        int originalPieceColumn = pieceToMove.getColumn();

        grid[toRow][toColumn] = pieceToMove;
        grid[fromRow][fromColumn] = null;
        pieceToMove.setPosition(toRow, toColumn);

        King currentPlayerKing = null;
        Color currentPlayerColor = isWhiteTurn ? Color.WHITE : Color.BLACK;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                if (grid[row][column] instanceof King && grid[row][column].getColor() == currentPlayerColor) {
                    currentPlayerKing = (King) grid[row][column];
                    break;
                }
            }
            if (currentPlayerKing != null) break;
        }

        boolean kingInCheckAfterMove = false;
        if (currentPlayerKing != null) {
            kingInCheckAfterMove = currentPlayerKing.isInCheck(grid);
        }

        pieceToMove.setPosition(originalPieceRow, originalPieceColumn);
        grid[fromRow][fromColumn] = pieceToMove;
        grid[toRow][toColumn] = tempTarget;

        if (kingInCheckAfterMove) {
            System.out.println("Invalid: Move would put or leave King in check.");
            return false;
        }

        System.out.println("Move valid");
        return true;
    }

    public void movePiece(int fromRow, int fromColumn, int toRow, int toColumn, boolean isMultiJump, int capturedPawnRow, int capturedPawnColumn) {
        Piece piece = grid[fromRow][fromColumn];
        System.out.println("Moving piece: " + piece.getType() + " from (" + fromRow + ", " + fromColumn + ") to (" + toRow + ", " + toColumn + ")");

        if (capturedPawnRow != -1 && capturedPawnColumn != -1) {
            System.out.println("Before en passant capture: grid[" + capturedPawnRow + "][" + capturedPawnColumn + "] = " +
                    (grid[capturedPawnRow][capturedPawnColumn] != null ? grid[capturedPawnRow][capturedPawnColumn].getType() : "null"));
            grid[capturedPawnRow][capturedPawnColumn] = null;
            System.out.println(piece.getColor() + " Pawn captures en passant at (" + capturedPawnRow + ", " + capturedPawnColumn + ")");
            System.out.println("After en passant capture: grid[" + capturedPawnRow + "][" + capturedPawnColumn + "] = " +
                    (grid[capturedPawnRow][capturedPawnColumn] != null ? grid[capturedPawnRow][capturedPawnColumn].getType() : "null"));
        }

        if (piece instanceof CheckersPiece && Math.abs(toRow - fromRow) >= 2 && Math.abs(toColumn - fromColumn) >= 2) {
            int dRow = (toRow - fromRow) > 0 ? 1 : -1;
            int dColumn = (toColumn - fromColumn) > 0 ? 1 : -1;
            int steps = Math.abs(toRow - fromRow);
            for (int i = 1; i < steps; i++) {
                int midRow = fromRow + i * dRow;
                int midColumn = fromColumn + i * dColumn;
                if (grid[midRow][midColumn] != null && grid[midRow][midColumn].getColor() != piece.getColor()) {
                    System.out.println(piece.getColor() + " CheckersMan captures " + grid[midRow][midColumn].getColor() + " piece at (" + midRow + ", " + midColumn + ")");
                    grid[midRow][midColumn] = null;
                }
            }
        }

        if (piece instanceof King && !((King) piece).getHasMoved() && fromRow == toRow) {
            if ((fromColumn == 3 && toColumn == 5) || (fromColumn == 4 && toColumn == 6)) {
                grid[toRow][toColumn] = piece;
                grid[fromRow][fromColumn] = null;
                piece.setPosition(toRow, toColumn);
                Rook kingsideRook = (Rook) grid[fromRow][7];
                int rookNewColumn = fromColumn == 3 ? 4 : 5;
                grid[toRow][rookNewColumn] = kingsideRook;
                grid[fromRow][7] = null;
                kingsideRook.setPosition(toRow, rookNewColumn);
                ((King) piece).setHasMoved(true);
                kingsideRook.setHasMoved(true);
                System.out.println("Выполнена короткая рокировка (королевский фланг).");
                return;
            } else if ((fromColumn == 3 && toColumn == 1) || (fromColumn == 4 && toColumn == 2)) {
                grid[toRow][toColumn] = piece;
                grid[fromRow][fromColumn] = null;
                piece.setPosition(toRow, toColumn);
                Rook queensideRook = (Rook) grid[fromRow][0];
                int rookNewColumn = fromColumn == 3 ? 2 : 3;
                grid[toRow][rookNewColumn] = queensideRook;
                grid[fromRow][0] = null;
                queensideRook.setPosition(toRow, rookNewColumn);
                ((King) piece).setHasMoved(true);
                queensideRook.setHasMoved(true);
                System.out.println("Выполнена длинная рокировка (ферзевый фланг).");
                return;
            }
        }

        if (grid[toRow][toColumn] != null) {
            System.out.println(piece.getColor() + " " + piece.getType() + " captures " + grid[toRow][toColumn].getColor() + " " + grid[toRow][toColumn].getType());
        }
        grid[toRow][toColumn] = piece;
        grid[fromRow][fromColumn] = null;
        piece.setPosition(toRow, toColumn);

        if (piece instanceof Pawn) {
            boolean shouldPromote = false;
            if (piece.getColor() == Color.WHITE && toRow == 7) {
                shouldPromote = true;
            } else if (piece.getColor() == Color.BLACK && toRow == 0) {
                shouldPromote = true;
            }
            if (shouldPromote) {
                if (game != null) {
                    game.requestPawnPromotion(toRow, toColumn, piece.getColor());
                }
                return;
            }
        }

        if (piece instanceof King) {
            ((King) piece).setHasMoved(true);
        } else if (piece instanceof Rook) {
            ((Rook) piece).setHasMoved(true);
        }
    }

    public void promotePawn(int row, int column, String pieceType, Color color) {
        Piece newPiece = switch (pieceType) {
            case "Queen" -> new Queen(color, row, column);
            case "Rook" -> new Rook(color, row, column);
            case "Bishop" -> new Bishop(color, row, column);
            case "Knight" -> new Knight(color, row, column);
            default -> new Queen(color, row, column);
        };
        grid[row][column] = newPiece;
        System.out.println("Пешка превращена в " + pieceType + " на позиции (" + row + ", " + column + ")");
    }

    public Piece getPiece(int row, int column) {
        return grid[row][column];
    }

    public boolean isKingInCheck(Color color) {
        King king = null;
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                if (grid[row][column] instanceof King && grid[row][column].getColor() == color) {
                    king = (King) grid[row][column];
                    break;
                }
            }
            if (king != null) break;
        }
        return king != null && king.isInCheck(grid);
    }

    public boolean isCheckmate(Color color, GameMode gameMode) {
        if (!isKingInCheck(color)) {
            return false;
        }

        boolean isWhiteTurn = color == Color.WHITE;

        for (int fromRow = 0; fromRow < BOARD_SIZE; fromRow++) {
            for (int fromColumn = 0; fromColumn < BOARD_SIZE; fromColumn++) {
                Piece piece = grid[fromRow][fromColumn];

                if (piece != null && piece.getColor() == color) {
                    for (int toRow = 0; toRow < BOARD_SIZE; toRow++) {
                        for (int toColumn = 0; toColumn < BOARD_SIZE; toColumn++) {
                            if (isValidMove(fromRow, fromColumn, toRow, toColumn, isWhiteTurn, gameMode, false)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private String getPieceTextStyle(Color pieceColor) {
        return "-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " +
                (pieceColor == Color.WHITE ? "white" : "black") + "; -fx-alignment: center;";
    }

    public Piece[][] getGrid() {
        return grid;
    }
}
