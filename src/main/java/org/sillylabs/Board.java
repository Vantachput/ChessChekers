package org.sillylabs;

public class Board {
    private Piece[][] grid;

    public Board() {
        grid = new Piece[8][8];
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
        // White pieces
        grid[0][0] = new Rook("White", 0, 0);
        grid[0][1] = new Knight("White", 0, 1);
        grid[0][2] = new Bishop("White", 0, 2);
        grid[0][3] = new Queen("White", 0, 3);
        grid[0][4] = new King("White", 0, 4);
        grid[0][5] = new Bishop("White", 0, 5);
        grid[0][6] = new Knight("White", 0, 6);
        grid[0][7] = new Rook("White", 0, 7);
        for (int i = 0; i < 8; i++) {
            grid[1][i] = new Pawn("White", 1, i);
        }
        // Black pieces
        grid[7][0] = new Rook("Black", 7, 0);
        grid[7][1] = new Knight("Black", 7, 1);
        grid[7][2] = new Bishop("Black", 7, 2);
        grid[7][3] = new Queen("Black", 7, 3);
        grid[7][4] = new King("Black", 7, 4);
        grid[7][5] = new Bishop("Black", 7, 5);
        grid[7][6] = new Knight("Black", 7, 6);
        grid[7][7] = new Rook("Black", 7, 7);
        for (int i = 0; i < 8; i++) {
            grid[6][i] = new Pawn("Black", 6, i);
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
        // Chess pieces in rows 0-1, 6-7
        grid[0][0] = new Rook("White", 0, 0);
        grid[0][1] = new Knight("White", 0, 1);
        grid[0][2] = new Bishop("White", 0, 2);
        grid[0][3] = new Queen("White", 0, 3);
        grid[0][4] = new King("White", 0, 4);
        grid[0][5] = new Bishop("White", 0, 5);
        grid[0][6] = new Knight("White", 0, 6);
        grid[0][7] = new Rook("White", 0, 7);
        for (int i = 0; i < 8; i++) {
            grid[1][i] = new Pawn("White", 1, i);
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
            grid[6][i] = new Pawn("Black", 6, i);
        }
        // Checkers pieces in rows 2-3, 4-5
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

    public boolean isValidMove(int fromX, int fromY, int toX, int toY, boolean isWhiteTurn, String gameMode) {
        Piece piece = grid[fromX][fromY];
        if (piece == null || !piece.getColor().equals(isWhiteTurn ? "White" : "Black")) {
            return false;
        }
        boolean isValid = piece.isValidMove(toX, toY, grid);
        if (!isValid) return false;

        Piece target = grid[toX][toY];
        if (target != null) {
            if (gameMode.equals("Hybrid") && !((piece instanceof ChessPiece && target instanceof ChessPiece) ||
                    (piece instanceof CheckersPiece && target instanceof CheckersPiece))) {
                return false;
            }
        }
        return true;
    }

    public void movePiece(int fromX, int fromY, int toX, int toY) {
        Piece piece = grid[fromX][fromY];
        grid[toX][toY] = piece;
        grid[fromX][fromY] = null;
        piece.setPosition(toX, toY);
        if (piece instanceof CheckersPiece) {
            if ((piece.getColor().equals("White") && toX == 0) || (piece.getColor().equals("Black") && toX == 7)) {
                ((CheckersPiece) piece).setKing(true);
            }
        }
    }

    public Piece getPiece(int x, int y) {
        return grid[x][y];
    }

    public boolean isCheckmate(String color) {
        // Simplified: check if king is in check and no moves escape
        for (int x = 0; x < 8; x++) {
            for (int y = 0; y < 8; y++) {
                Piece piece = grid[x][y];
                if (piece instanceof King && piece.getColor().equals(color)) {
                    return ((King) piece).isInCheckmate(grid);
                }
            }
        }
        return false;
    }
}