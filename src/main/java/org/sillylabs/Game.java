package org.sillylabs;

import org.sillylabs.pieces.*;
import java.util.ArrayList;
import java.util.List;

public class Game {
    private final Board board;
    private boolean isWhiteTurn;
    private GameMode gameMode;
    private boolean waitingForPromotion = false;
    private int promotionRow, promotionColumn;
    private Color promotionColor;
    private long turnStartTime;
    private final List<String> moveHistory;
    private boolean isMultiJump;
    private int multiJumpFromRow, multiJumpFromColumn;
    private int enPassantTargetRow = -1;
    private int enPassantTargetColumn = -1;
    private boolean enPassantPossible = false;
    private final List<GameObserver> observers = new ArrayList<>();

    public Game() {
        board = new Board();
        board.setGame(this);
        isWhiteTurn = true;
        moveHistory = new ArrayList<>();
        turnStartTime = System.currentTimeMillis();
        isMultiJump = false;
        multiJumpFromRow = -1;
        multiJumpFromColumn = -1;
    }

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    public void start(GameMode mode) {
        this.gameMode = mode;
        board.setupBoard(mode);
        moveHistory.clear();
        isWhiteTurn = true;
        turnStartTime = System.currentTimeMillis();
        isMultiJump = false;
        multiJumpFromRow = -1;
        multiJumpFromColumn = -1;
        enPassantTargetRow = -1;
        enPassantTargetColumn = -1;
        enPassantPossible = false;
        notifyBoardChanged();
        notifyStatusUpdated("");
    }

    public boolean makeMove(int fromRow, int fromColumn, int toRow, int toColumn) {
        if (waitingForPromotion) {
            notifyStatusUpdated("Сначала выберите фигуру для превращения пешки!");
            return false;
        }

        if (isMultiJump && (fromRow != multiJumpFromRow || fromColumn != multiJumpFromColumn)) {
            notifyStatusUpdated("Завершайте серию взятий или подтвердите окончание хода!");
            return false;
        }

        Piece piece = board.getPiece(fromRow, fromColumn);
        boolean isEnPassantMove = piece instanceof Pawn && Math.abs(toColumn - fromColumn) == 1 &&
                toRow == enPassantTargetRow && toColumn == enPassantTargetColumn && enPassantPossible;

        if (isMultiJump && fromRow == toRow && fromColumn == toColumn) {
            isMultiJump = false;
            multiJumpFromRow = -1;
            multiJumpFromColumn = -1;
            isWhiteTurn = !isWhiteTurn;
            turnStartTime = System.currentTimeMillis();
            notifyStatusUpdated("Ход завершен.");
            notifyBoardChanged();
            return true;
        }

        if (board.isValidMove(fromRow, fromColumn, toRow, toColumn, isWhiteTurn, gameMode, isMultiJump)) {
            long moveEndTime = System.currentTimeMillis();
            double timeTaken = (moveEndTime - turnStartTime) / 1000.0;

            String moveNotation = getMoveNotation(fromRow, fromColumn, toRow, toColumn);
            String moveRecord = String.format("%s (%.2f сек)", moveNotation, timeTaken);
            moveHistory.add(moveRecord);

            boolean isCaptureMove = piece instanceof CheckersPiece && Math.abs(toRow - fromRow) >= 2 && Math.abs(toColumn - fromColumn) >= 2;

            int capturedPawnRow = -1, capturedPawnColumn = -1;
            if (isEnPassantMove) {
                int direction = piece.getColor() == Color.WHITE ? 1 : -1;
                capturedPawnRow = toRow - direction;
                capturedPawnColumn = toColumn;
            }

            enPassantPossible = false;
            enPassantTargetRow = -1;
            enPassantTargetColumn = -1;

            if (piece instanceof Pawn && Math.abs(toRow - fromRow) == 2) {
                enPassantPossible = true;
                enPassantTargetRow = (fromRow + toRow) / 2;
                enPassantTargetColumn = toColumn;
            }

            board.movePiece(fromRow, fromColumn, toRow, toColumn, isMultiJump, capturedPawnRow, capturedPawnColumn);

            if (gameMode == GameMode.CHECKERS || gameMode == GameMode.HYBRID || gameMode == GameMode.UNIFIED) {
                if (piece instanceof CheckersPiece checkersPiece) {
                    if (!checkersPiece.isKing() &&
                            ((checkersPiece.getColor() == Color.WHITE && toRow == 0) ||
                                    (checkersPiece.getColor() == Color.BLACK && toRow == 7))) {
                        checkersPiece.setKing(true);
                        notifyStatusUpdated("Шашка превращена в дамку!");
                    }
                    if (isCaptureMove) {
                        List<int[]> furtherCaptures = checkersPiece.getCaptureMoves(toRow, toColumn, board.getGrid(), gameMode);
                        if (!furtherCaptures.isEmpty()) {
                            isMultiJump = true;
                            multiJumpFromRow = toRow;
                            multiJumpFromColumn = toColumn;
                            notifyStatusUpdated("Доступны дополнительные взятия! Выберите ход или подтвердите окончание.");
                            notifyBoardChanged();
                            return true;
                        } else {
                            isMultiJump = false;
                            multiJumpFromRow = -1;
                            multiJumpFromColumn = -1;
                        }
                    }
                }
            }

            if (!waitingForPromotion) {
                isWhiteTurn = !isWhiteTurn;
                turnStartTime = System.currentTimeMillis();
                notifyBoardChanged();

                Color currentPlayerColor = isWhiteTurn ? Color.WHITE : Color.BLACK;
                Color previousPlayerColor = isWhiteTurn ? Color.BLACK : Color.WHITE;

                if (isEnPassantMove) {
                    notifyStatusUpdated("Взято на проходе!");
                } else if (board.isKingInCheck(currentPlayerColor)) {
                    if (board.isCheckmate(currentPlayerColor, gameMode)) {
                        notifyStatusUpdated("Шах и мат! " + previousPlayerColor + " победили!");
                        notifyGameOver(true, previousPlayerColor);
                    } else {
                        notifyStatusUpdated(currentPlayerColor + " король под шахом!");
                    }
                } else {
                    notifyStatusUpdated("");
                }
            }
            return true;
        }
        notifyStatusUpdated("Неверный ход");
        return false;
    }

    private String getMoveNotation(int fromRow, int fromColumn, int toRow, int toColumn) {
        Piece piece = board.getPiece(fromRow, fromColumn);
        if (piece instanceof King && fromColumn == 4 && toRow == fromRow) {
            if (toColumn == 6) {
                return "O-O";
            } else if (toColumn == 2) {
                return "O-O-O";
            }
        }
        char fromFile = (char) ('a' + fromColumn);
        int fromRank = 8 - fromRow;
        char toFile = (char) ('a' + toColumn);
        int toRank = 8 - toRow;
        String pieceSymbol = piece instanceof CheckersPiece ? "" : getPieceSymbol(piece.getType());
        String capture = (piece instanceof CheckersPiece && Math.abs(toRow - fromRow) >= 2 && Math.abs(toColumn - fromColumn) >= 2) ||
                board.getPiece(toRow, toColumn) != null ||
                (piece instanceof Pawn && Math.abs(toColumn - fromColumn) == 1 && board.getPiece(toRow, toColumn) == null && toRow == enPassantTargetRow && toColumn == enPassantTargetColumn) ? "x" : "";
        return String.format("%s%s%s%s%d", pieceSymbol, fromFile + "" + fromRank, capture, toFile, toRank);
    }

    private String getPieceSymbol(String pieceType) {
        return switch (pieceType) {
            case "King" -> "K";
            case "Queen" -> "Q";
            case "Rook" -> "R";
            case "Bishop" -> "B";
            case "Knight" -> "N";
            case "Pawn" -> "";
            default -> "";
        };
    }

    public void requestPawnPromotion(int row, int column, Color color) {
        waitingForPromotion = true;
        promotionRow = row;
        promotionColumn = column;
        promotionColor = color;
        notifyPromotionRequested(row, column, color);
    }

    public void completePawnPromotion(String pieceType) {
        if (waitingForPromotion) {
            board.promotePawn(promotionRow, promotionColumn, pieceType, promotionColor);
            waitingForPromotion = false;

            isWhiteTurn = !isWhiteTurn;
            turnStartTime = System.currentTimeMillis();
            notifyBoardChanged();

            Color currentPlayerColor = isWhiteTurn ? Color.WHITE : Color.BLACK;
            Color previousPlayerColor = isWhiteTurn ? Color.BLACK : Color.WHITE;

            if (board.isKingInCheck(currentPlayerColor)) {
                if (board.isCheckmate(currentPlayerColor, gameMode)) {
                    notifyStatusUpdated("Шах и мат! " + previousPlayerColor + " победили!");
                    notifyGameOver(true, previousPlayerColor);
                } else {
                    notifyStatusUpdated(currentPlayerColor + " король под шахом!");
                }
            } else {
                notifyStatusUpdated("Пешка превращена в " + getPieceNameInRussian(pieceType) + "!");
            }
        }
    }

    private String getPieceNameInRussian(String pieceType) {
        return switch (pieceType) {
            case "Queen" -> "ферзя";
            case "Rook" -> "ладью";
            case "Bishop" -> "слона";
            case "Knight" -> "коня";
            default -> "фигуру";
        };
    }

    public boolean isGameOver() {
        if (gameMode == GameMode.CHECKERS || gameMode == GameMode.HYBRID || gameMode == GameMode.UNIFIED) {
            return board.isCheckmate(isWhiteTurn ? Color.WHITE : Color.BLACK, gameMode);
        }
        return false;
    }

    public Board getBoard() {
        return board;
    }

    public GameMode getGameMode() {
        return gameMode;
    }

    public boolean isWhiteTurn() {
        return isWhiteTurn;
    }

    public boolean isWaitingForPromotion() {
        return waitingForPromotion;
    }

    public List<String> getMoveHistory() {
        return moveHistory;
    }

    public Piece[][] getGrid() {
        return board.getGrid();
    }

    public int getEnPassantTargetRow() {
        return enPassantTargetRow;
    }

    public int getEnPassantTargetColumn() {
        return enPassantTargetColumn;
    }

    public boolean isEnPassantPossible() {
        return enPassantPossible;
    }

    public boolean isMultiJump() {
        return isMultiJump;
    }

    private void notifyBoardChanged() {
        for (GameObserver observer : observers) {
            observer.onBoardChanged();
        }
    }

    private void notifyStatusUpdated(String message) {
        for (GameObserver observer : observers) {
            observer.onStatusUpdate(message);
        }
    }

    private void notifyPromotionRequested(int row, int column, Color color) {
        for (GameObserver observer : observers) {
            observer.onPromotionRequested(row, column, color);
        }
    }

    private void notifyGameOver(boolean isGameOver, Color winner) {
        for (GameObserver observer : observers) {
            observer.onGameOver(isGameOver, winner);
        }
    }

    public int getMultiJumpFromRow() {
        return multiJumpFromRow;
    }

    public int getMultiJumpFromColumn() {
        return multiJumpFromColumn;
    }
}