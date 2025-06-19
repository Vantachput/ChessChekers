package org.sillylabs;

import java.util.ArrayList;
import java.util.List;

public class Board {
    private Piece[][] grid;
    private Game game;

    public Board() {
        grid = new Piece[8][8];
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setupBoard(String mode) {
        grid = new Piece[8][8];
        if (mode.equals("Chess")) {
            setupChess();
        } else if (mode.equals("Checkers")) {
            setupCheckers();
        } else if (mode.equals("Hybrid") || mode.equals("Unified")) {
            setupHybrid();
        }
    }

    private void setupChess() {
        grid[0][0] = new Rook("White", 0, 0);
        grid[0][1] = new Knight("White", 0, 1);
        grid[0][2] = new Bishop("White", 0, 2);
        grid[0][3] = new King("White", 0, 3);
        grid[0][4] = new Queen("White", 0, 4);
        grid[0][5] = new Bishop("White", 0, 5);
        grid[0][6] = new Knight("White", 0, 6);
        grid[0][7] = new Rook("White", 0, 7);
        for (int i = 0; i < 8; i++) {
            grid[1][i] = new Pawn("White", 1, i, game);
        }
        grid[7][0] = new Rook("Black", 7, 0);
        grid[7][1] = new Knight("Black", 7, 1);
        grid[7][2] = new Bishop("Black", 7, 2);
        grid[7][3] = new King("Black", 7, 3);
        grid[7][4] = new Queen("Black", 7, 4);
        grid[7][5] = new Bishop("Black", 7, 5);
        grid[7][6] = new Knight("Black", 7, 6);
        grid[7][7] = new Rook("Black", 7, 7);
        for (int i = 0; i < 8; i++) {
            grid[6][i] = new Pawn("Black", 6, i, game);
        }
    }

    private void setupCheckers() {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 8; x++) {
                if ((x + y) % 2 == 1) {
                    grid[y][x] = new CheckersMan("Black", y, x);
                }
            }
        }
        for (int y = 5; y < 8; y++) {
            for (int x = 0; x < 8; x++) {
                if ((x + y) % 2 == 1) {
                    grid[y][x] = new CheckersMan("White", y, x);
                }
            }
        }
    }

    private void setupHybrid() {
        grid[0][0] = new Rook("White", 0, 0);
        grid[0][1] = new Knight("White", 0, 1);
        grid[0][2] = new Bishop("White", 0, 2);
        grid[0][3] = new Queen("White", 0, 3);
        grid[0][4] = new King("White", 0, 4);
        grid[0][5] = new Bishop("White", 0, 5);
        grid[0][6] = new Knight("White", 0, 6);
        grid[0][7] = new Rook("White", 0, 7);
        for (int i = 0; i < 8; i++) {
            grid[1][i] = new Pawn("White", 1, i, game);
        }
        grid[7][0] = new Rook("Black", 7, 0);
        grid[7][1] = new Knight("Black", 7, 1);
        grid[7][2] = new Bishop("Black", 7, 2);
        grid[7][3] = new Queen("Black", 7, 3);
        grid[7][4] = new King("Black", 7, 4);
        grid[7][5] = new Bishop("Black", 7, 5);
        grid[7][6] = new Knight("Black", 7, 6);
        grid[7][7] = new Rook("Black", 7, 7);
        for (int i = 0; i < 8; i++) {
            grid[6][i] = new Pawn("Black", 6, i, game);
        }
        for (int y = 2; y < 4; y++) {
            for (int x = 0; x < 8; x++) {
                if ((x + y) % 2 == 1) {
                    grid[y][x] = new CheckersMan("Black", y, x);
                }
            }
        }
        for (int y = 4; y < 6; y++) {
            for (int x = 0; x < 8; x++) {
                if ((x + y) % 2 == 1) {
                    grid[y][x] = new CheckersMan("White", y, x);
                }
            }
        }
    }

    public boolean isSquareAttacked(int targetX, int targetY, String friendlyKingColor) {
        String opponentColor = friendlyKingColor.equals("White") ? "Black" : "White";

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece attackingPiece = grid[r][c];

                if (attackingPiece != null && attackingPiece.getColor().equals(opponentColor)) {
                    Piece[][] tempGrid = new Piece[8][8];
                    for (int i = 0; i < 8; i++) {
                        for (int j = 0; j < 8; j++) {
                            tempGrid[i][j] = grid[i][j];
                        }
                    }
                    tempGrid[targetX][targetY] = null;

                    if (attackingPiece instanceof Pawn) {
                        int pawnRow = attackingPiece.getX();
                        int pawnCol = attackingPiece.getY();
                        int direction = attackingPiece.getColor().equals("White") ? 1 : -1;

                        if (targetX == pawnRow + direction && (targetY == pawnCol - 1 || targetY == pawnCol + 1)) {
                            return true;
                        }
                    } else {
                        if (attackingPiece.isValidMove(targetX, targetY, tempGrid)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    public boolean hasAvailableCaptures(boolean isWhiteTurn, String gameMode) {
        String color = isWhiteTurn ? "White" : "Black";
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = grid[x][y];
                if (piece != null && piece.getColor().equals(color) && piece instanceof CheckersPiece) {
                    List<int[]> captureMoves = ((CheckersPiece) piece).getCaptureMoves(x, y, grid, gameMode);
                    if (!captureMoves.isEmpty()) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isValidMove(int fromX, int fromY, int toX, int toY, boolean isWhiteTurn, String gameMode, boolean isMultiJump) {
        System.out.println("Validating move: from (" + fromX + ", " + fromY + ") to (" + toX + ", " + toY + ")");
        Piece pieceToMove = grid[fromX][fromY];
        if (pieceToMove == null) {
            System.out.println("Invalid: No piece at (" + fromX + ", " + fromY + ")");
            return false;
        }
        if (!pieceToMove.getColor().equals(isWhiteTurn ? "White" : "Black")) {
            System.out.println("Invalid: Wrong color. Piece is " + pieceToMove.getColor() + ", turn is " + (isWhiteTurn ? "White" : "Black"));
            return false;
        }

        if (grid[toX][toY] instanceof King) {
            System.out.println("Invalid: Cannot capture the King directly.");
            return false;
        }
        if (pieceToMove instanceof CheckersPiece) {
            CheckersPiece checkersPiece = (CheckersPiece) pieceToMove;
            System.out.println("Checking CheckersPiece move with multiJump: " + isMultiJump);
            return checkersPiece.isValidMoveWithMultiJump(toX, toY, grid, isMultiJump);
        }
        if (pieceToMove instanceof King && (gameMode.equals("Chess") || gameMode.equals("Hybrid") || gameMode.equals("Unified"))) {
            King king = (King) pieceToMove;
            if (!king.getHasMoved() && fromX == toX) {
                // Короткая рокировка (в сторону h-файла)
                if ((fromY == 3 && toY == 5) || (fromY == 4 && toY == 6)) {
                    int rookY = 7; // Ладья на h-файле
                    Piece rookPiece = grid[fromX][rookY];
                    if (rookPiece instanceof Rook) {
                        Rook kingsideRook = (Rook) rookPiece;
                        if (!kingsideRook.getHasMoved() && kingsideRook.getColor().equals(king.getColor())) {
                            boolean pathClear = true;
                            for (int y = fromY + 1; y < rookY; y++) {
                                if (grid[fromX][y] != null) {
                                    pathClear = false;
                                    break;
                                }
                            }

                            if (pathClear) {
                                if (!isKingInCheck(king.getColor())) {
                                    boolean squaresSafe = true;
                                    for (int y = fromY; y <= toY; y++) {
                                        if (isSquareAttacked(fromX, y, king.getColor())) {
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
                // Длинная рокировка (в сторону a-файла)
                else if ((fromY == 3 && toY == 1) || (fromY == 4 && toY == 2)) {
                    int rookY = 0; // Ладья на a-файле
                    Piece rookPiece = grid[fromX][rookY];
                    if (rookPiece instanceof Rook) {
                        Rook queensideRook = (Rook) rookPiece;
                        if (!queensideRook.getHasMoved() && queensideRook.getColor().equals(king.getColor())) {
                            boolean pathClear = true;
                            for (int y = rookY + 1; y < fromY; y++) {
                                if (grid[fromX][y] != null) {
                                    pathClear = false;
                                    break;
                                }
                            }

                            if (pathClear) {
                                if (!isKingInCheck(king.getColor())) {
                                    boolean squaresSafe = true;
                                    for (int y = toY; y <= fromY; y++) {
                                        if (isSquareAttacked(fromX, y, king.getColor())) {
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

        if (gameMode.equals("Checkers") && pieceToMove instanceof CheckersPiece && !isMultiJump) {
            boolean hasCaptures = hasAvailableCaptures(isWhiteTurn, gameMode);
            boolean isCaptureMove = Math.abs(toX - fromX) >= 2 && Math.abs(toY - fromY) >= 2;
            if (hasCaptures && !isCaptureMove) {
                System.out.println("Invalid: Must capture when possible");
                return false;
            }
        }

        boolean isValid = pieceToMove.isValidMove(toX, toY, grid);
        if (!isValid) {
            System.out.println("Invalid: Piece move invalid for " + pieceToMove.getType());
            return false;
        }

        Piece target = grid[toX][toY];
        if (target != null) {
            if (gameMode.equals("Hybrid") && !((pieceToMove instanceof ChessPiece && target instanceof ChessPiece) ||
                    (pieceToMove instanceof CheckersPiece && target instanceof CheckersPiece))) {
                System.out.println("Invalid: Hybrid mode capture restriction");
                return false;
            }
        }

        Piece tempTarget = grid[toX][toY];
        int originalPieceX = pieceToMove.getX();
        int originalPieceY = pieceToMove.getY();

        // Simulate move
        grid[toX][toY] = pieceToMove;
        grid[fromX][fromY] = null;
        pieceToMove.setPosition(toX, toY);

        King currentPlayerKing = null;
        String currentPlayerColor = isWhiteTurn ? "White" : "Black";
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (grid[r][c] instanceof King && grid[r][c].getColor().equals(currentPlayerColor)) {
                    currentPlayerKing = (King) grid[r][c];
                    break;
                }
            }
            if (currentPlayerKing != null) break;
        }

        boolean kingInCheckAfterMove = false;
        if (currentPlayerKing != null) {
            kingInCheckAfterMove = currentPlayerKing.isInCheck(grid);
        }

        // Undo move
        pieceToMove.setPosition(originalPieceX, originalPieceY);
        grid[fromX][fromY] = pieceToMove;
        grid[toX][toY] = tempTarget;

        if (kingInCheckAfterMove) {
            System.out.println("Invalid: Move would put or leave King in check.");
            return false;
        }

        System.out.println("Move valid");
        return true;
    }

    public void movePiece(int fromX, int fromY, int toX, int toY, boolean isMultiJump, int capturedPawnX, int capturedPawnY) {
        Piece piece = grid[fromX][fromY];
        System.out.println("Moving piece: " + piece.getType() + " from (" + fromX + ", " + fromY + ") to (" + toX + ", " + toY + ")");

        // Handle en passant capture
        if (capturedPawnX != -1 && capturedPawnY != -1) {
            System.out.println("Before en passant capture: grid[" + capturedPawnX + "][" + capturedPawnY + "] = " +
                    (grid[capturedPawnX][capturedPawnY] != null ? grid[capturedPawnX][capturedPawnY].getType() : "null"));
            grid[capturedPawnX][capturedPawnY] = null;
            System.out.println(piece.getColor() + " Pawn captures en passant at (" + capturedPawnX + ", " + capturedPawnY + ")");
            System.out.println("After en passant capture: grid[" + capturedPawnX + "][" + capturedPawnY + "] = " +
                    (grid[capturedPawnX][capturedPawnY] != null ? grid[capturedPawnX][capturedPawnY].getType() : "null"));
        }

        // Handle checkers capture
        if (piece instanceof CheckersPiece && Math.abs(toX - fromX) >= 2 && Math.abs(toY - fromY) >= 2) {
            int dx = (toX - fromX) > 0 ? 1 : -1;
            int dy = (toY - fromY) > 0 ? 1 : -1;
            int steps = Math.abs(toX - fromX);
            for (int i = 1; i < steps; i++) {
                int midX = fromX + i * dx;
                int midY = fromY + i * dy;
                if (grid[midX][midY] != null && !grid[midX][midY].getColor().equals(piece.getColor())) {
                    System.out.println(piece.getColor() + " CheckersMan captures " + grid[midX][midY].getColor() + " piece at (" + midX + ", " + midY + ")");
                    grid[midX][midY] = null;
                }
            }
        }

        // Handle castling
        if (piece instanceof King && !((King) piece).getHasMoved() && fromX == toX) {
            if ((fromY == 3 && toY == 5) || (fromY == 4 && toY == 6)) {
                grid[toX][toY] = piece;
                grid[fromX][fromY] = null;
                piece.setPosition(toX, toY);
                Rook kingsideRook = (Rook) grid[fromX][7];
                int rookNewY = fromY == 3 ? 4 : 5;
                grid[toX][rookNewY] = kingsideRook;
                grid[fromX][7] = null;
                kingsideRook.setPosition(toX, rookNewY);
                ((King) piece).setHasMoved(true);
                kingsideRook.setHasMoved(true);
                System.out.println("Выполнена короткая рокировка (королевский фланг).");
                return;
            } else if ((fromY == 3 && toY == 1) || (fromY == 4 && toY == 2)) {
                grid[toX][toY] = piece;
                grid[fromX][fromY] = null;
                piece.setPosition(toX, toY);
                Rook queensideRook = (Rook) grid[fromX][0];
                int rookNewY = fromY == 3 ? 2 : 3;
                grid[toX][rookNewY] = queensideRook;
                grid[fromX][0] = null;
                queensideRook.setPosition(toX, rookNewY);
                ((King) piece).setHasMoved(true);
                queensideRook.setHasMoved(true);
                System.out.println("Выполнена длинная рокировка (ферзевый фланг).");
                return;
            }
        }

        // Generic move or capture
        if (grid[toX][toY] != null) {
            System.out.println(piece.getColor() + " " + piece.getType() + " captures " + grid[toX][toY].getColor() + " " + grid[toX][toY].getType());
        }
        grid[toX][toY] = piece;
        grid[fromX][fromY] = null;
        piece.setPosition(toX, toY);

        // Handle pawn promotion
        if (piece instanceof Pawn) {
            boolean shouldPromote = false;
            if (piece.getColor().equals("White") && toX == 7) {
                shouldPromote = true;
            } else if (piece.getColor().equals("Black") && toX == 0) {
                shouldPromote = true;
            }
            if (shouldPromote) {
                if (game != null) {
                    game.requestPawnPromotion(toX, toY, piece.getColor());
                }
                return;
            }
        }

        // Update hasMoved for King or Rook
        if (piece instanceof King) {
            ((King) piece).setHasMoved(true);
        } else if (piece instanceof Rook) {
            ((Rook) piece).setHasMoved(true);
        }
    }

    public void promotePawn(int x, int y, String pieceType, String color) {
        Piece newPiece = switch (pieceType) {
            case "Queen" -> new Queen(color, x, y);
            case "Rook" -> new Rook(color, x, y);
            case "Bishop" -> new Bishop(color, x, y);
            case "Knight" -> new Knight(color, x, y);
            default -> new Queen(color, x, y);
        };
        grid[x][y] = newPiece;
        System.out.println("Пешка превращена в " + pieceType + " на позиции (" + x + ", " + y + ")");
    }

    public Piece getPiece(int x, int y) {
        return grid[x][y];
    }

    public boolean isKingInCheck(String color) {
        King king = null;
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                if (grid[r][c] instanceof King && grid[r][c].getColor().equals(color)) {
                    king = (King) grid[r][c];
                    break;
                }
            }
            if (king != null) break;
        }
        return king != null && king.isInCheck(grid);
    }

    public boolean isCheckmate(String color, String gameMode) {
        if (!isKingInCheck(color)) {
            return false;
        }

        boolean isWhiteTurn = color.equals("White");

        for (int fromX = 0; fromX < 8; fromX++) {
            for (int fromY = 0; fromY < 8; fromY++) {
                Piece piece = grid[fromX][fromY];

                if (piece != null && piece.getColor().equals(color)) {
                    for (int toX = 0; toX < 8; toX++) {
                        for (int toY = 0; toY < 8; toY++) {
                            if (isValidMove(fromX, fromY, toX, toY, isWhiteTurn, gameMode, false)) {
                                return false;
                            }
                        }
                    }
                }
            }
        }
        return true;
    }

    private String getPieceTextStyle(String pieceColor) {
        return "-fx-font-size: 36px; -fx-font-weight: bold; -fx-text-fill: " +
                (pieceColor.equals("White") ? "white" : "black") + "; -fx-alignment: center;";
    }

    public Piece[][] getGrid() {
        return grid;
    }
}