package org.sillylabs;

import java.util.ArrayList;
import java.util.List;

public class Game {
    private Board board;
    private boolean isWhiteTurn;
    private GameGUI gui;
    private String gameMode;
    private boolean waitingForPromotion = false;
    private int promotionX, promotionY;
    private String promotionColor;
    private long turnStartTime;
    private List<String> moveHistory;

    public Game() {
        board = new Board();
        board.setGame(this);
        isWhiteTurn = true;
        moveHistory = new ArrayList<>();
        turnStartTime = System.currentTimeMillis();
    }

    public void start(String mode) {
        this.gameMode = mode;
        board.setupBoard(mode);
        moveHistory.clear();
        isWhiteTurn = true;
        turnStartTime = System.currentTimeMillis();
        if (gui != null) {
            gui.updateDisplay();
        }
    }

    public boolean makeMove(int fromX, int fromY, int toX, int toY) {
        if (waitingForPromotion) {
            gui.setStatusMessage("Сначала выберите фигуру для превращения пешки!");
            return false;
        }

        if (board.isValidMove(fromX, fromY, toX, toY, isWhiteTurn, gameMode)) {
            long moveEndTime = System.currentTimeMillis();
            double timeTaken = (moveEndTime - turnStartTime) / 1000.0;

            String moveNotation = getMoveNotation(fromX, fromY, toX, toY);
            String moveRecord = String.format("%s (%.2f сек)", moveNotation, timeTaken);
            moveHistory.add(moveRecord);

            board.movePiece(fromX, fromY, toX, toY);

            if (!waitingForPromotion) {
                isWhiteTurn = !isWhiteTurn;
                turnStartTime = System.currentTimeMillis();
                gui.updateDisplay();

                String currentPlayerColor = isWhiteTurn ? "White" : "Black";
                String previousPlayerColor = isWhiteTurn ? "Black" : "White";

                if (board.isKingInCheck(currentPlayerColor)) {
                    if (board.isCheckmate(currentPlayerColor, gameMode)) {
                        gui.setStatusMessage("Шах и мат! " + previousPlayerColor + " победили!");
                        gui.setGameOver(true);
                    } else {
                        gui.setStatusMessage(currentPlayerColor + " король под шахом!");
                    }
                } else {
                    gui.setStatusMessage("");
                }
            }
            return true;
        }
        gui.setStatusMessage("Неверный ход");
        return false;
    }

    private String getMoveNotation(int fromX, int fromY, int toX, int toY) {
        Piece piece = board.getPiece(fromX, fromY);
        // Handle castling
        if (piece instanceof King && fromY == 4 && toX == fromX) {
            if (toY == 6) {
                return "O-O"; // Kingside castling
            } else if (toY == 2) {
                return "O-O-O"; // Queenside castling
            }
        }
        char fromFile = (char) ('a' + fromY);
        int fromRank = 8 - fromX;
        char toFile = (char) ('a' + toY);
        int toRank = 8 - toX;
        String pieceSymbol = piece instanceof CheckersPiece ? "" : getPieceSymbol(piece.getType());
        String capture = board.getPiece(toX, toY) != null ? "x" : "";
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

    public void requestPawnPromotion(int x, int y, String color) {
        waitingForPromotion = true;
        promotionX = x;
        promotionY = y;
        promotionColor = color;
        gui.showPromotionDialog(color);
    }

    public void completePawnPromotion(String pieceType) {
        if (waitingForPromotion) {
            board.promotePawn(promotionX, promotionY, pieceType, promotionColor);
            waitingForPromotion = false;

            isWhiteTurn = !isWhiteTurn;
            turnStartTime = System.currentTimeMillis();
            gui.updateDisplay();

            String currentPlayerColor = isWhiteTurn ? "White" : "Black";
            String previousPlayerColor = isWhiteTurn ? "Black" : "White";

            if (board.isKingInCheck(currentPlayerColor)) {
                if (board.isCheckmate(currentPlayerColor, gameMode)) {
                    gui.setStatusMessage("Шах и мат! " + previousPlayerColor + " победили!");
                    gui.setGameOver(true);
                } else {
                    gui.setStatusMessage(currentPlayerColor + " король под шахом!");
                }
            } else {
                gui.setStatusMessage("Пешка превращена в " + getPieceNameInRussian(pieceType) + "!");
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
        if (gameMode.equals("Chess") || gameMode.equals("Hybrid") || gameMode.equals("Unified")) {
            return board.isCheckmate(isWhiteTurn ? "White" : "Black", gameMode);
        }
        return false;
    }

    public Board getBoard() {
        return board;
    }

    public void setGUI(GameGUI gui) {
        this.gui = gui;
    }

    public String getGameMode() {
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
}