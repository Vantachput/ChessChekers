package org.sillylabs;

import org.sillylabs.pieces.*;

import java.util.List;

public class Board {
    private Piece[][] grid;
    private Game game;
    private GameMode gameMode;
    private static final int BOARD_SIZE = 8;
    private MoveValidator moveValidator;

    public Board() {
        grid = new Piece[BOARD_SIZE][BOARD_SIZE];
    }

    public void setGame(Game game) {
        this.game = game;
    }

    public void setMoveValidator(MoveValidator validator) {
        this.moveValidator = validator;
    }

    public void setupBoard(GameMode mode) {
        this.gameMode = mode;
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
        return moveValidator.isValidMove(this, game, fromRow, fromColumn, toRow, toColumn, isWhiteTurn, isMultiJump);
    }

    public void movePiece(int fromRow, int fromColumn, int toRow, int toColumn, boolean isMultiJump, int capturedPawnRow, int capturedPawnColumn) {
        Piece piece = grid[fromRow][fromColumn];
        System.out.println("Moving piece: " + piece.getType() + " from (" + fromRow + ", " + fromColumn + ") to (" + toRow + ", " + toColumn + ")");

        if (capturedPawnRow != -1 && capturedPawnColumn != -1) {
            grid[capturedPawnRow][capturedPawnColumn] = null;
            System.out.println(piece.getColor() + " Pawn captures en passant at (" + capturedPawnRow + ", " + capturedPawnColumn + ")");
        }

        if (piece instanceof CheckersPiece && Math.abs(toRow - fromRow) >= 2) {
            executeCheckersCapture(fromRow, fromColumn, toRow, toColumn, piece);
        } else if (piece instanceof King && Math.abs(toColumn - fromColumn) == 2 && fromRow == toRow) {
            performCastling(fromRow, fromColumn, toRow, toColumn, (King) piece);
            return;
        }

        if (grid[toRow][toColumn] != null) {
            System.out.println(piece.getColor() + " " + piece.getType() + " captures " + grid[toRow][toColumn].getColor() + " " + grid[toRow][toColumn].getType());
        }
        grid[toRow][toColumn] = piece;
        grid[fromRow][fromColumn] = null;
        piece.setPosition(toRow, toColumn);

        handlePawnPromotion(piece, toRow, toColumn);
        updatePieceMoveStatus(piece);
    }

    private void executeCheckersCapture(int fromRow, int fromColumn, int toRow, int toColumn, Piece piece) {
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
        grid[toRow][toColumn] = piece;
        grid[fromRow][fromColumn] = null;
        piece.setPosition(toRow, toColumn);
    }

    private void performCastling(int fromRow, int fromColumn, int toRow, int toColumn, King king) {
        boolean isKingside = toColumn > fromColumn;
        int rookFromColumn = isKingside ? 7 : 0;
        int rookToColumn = isKingside ? (fromColumn == 3 ? 4 : 5) : (fromColumn == 3 ? 2 : 3);
        Rook rook = (Rook) grid[fromRow][rookFromColumn];

        grid[toRow][toColumn] = king;
        grid[fromRow][fromColumn] = null;
        king.setPosition(toRow, toColumn);
        king.setHasMoved(true);

        grid[toRow][rookToColumn] = rook;
        grid[fromRow][rookFromColumn] = null;
        rook.setPosition(toRow, rookToColumn);
        rook.setHasMoved(true);

        System.out.println("Performed " + (isKingside ? "Kingside" : "Queenside") + " castling.");
    }

    private void handlePawnPromotion(Piece piece, int toRow, int toColumn) {
        if (piece instanceof Pawn) {
            boolean shouldPromote = (piece.getColor() == Color.WHITE && toRow == 7) || (piece.getColor() == Color.BLACK && toRow == 0);
            if (shouldPromote && game != null) {
                game.requestPawnPromotion(toRow, toColumn, piece.getColor());
            }
        }
    }

    private void updatePieceMoveStatus(Piece piece) {
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
        System.out.println("Pawn promoted to " + pieceType + " at (" + row + ", " + column + ")");
    }

    public Piece getPiece(int row, int column) {
        return grid[row][column];
    }

    public Piece[][] getGrid() {
        return grid;
    }

    public GameMode getGameMode() {
        return gameMode;
    }
}