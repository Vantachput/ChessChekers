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
    private boolean isMultiJump;
    private int multiJumpFromX, multiJumpFromY;
    private int enPassantTargetX = -1; // Row of en passant target square
    private int enPassantTargetY = -1; // Column of en passant target square
    private boolean enPassantPossible = false; // Flag for en passant availability

    public Game() {
        board = new Board();
        board.setGame(this);
        isWhiteTurn = true;
        moveHistory = new ArrayList<>();
        turnStartTime = System.currentTimeMillis();
        isMultiJump = false;
        multiJumpFromX = -1;
        multiJumpFromY = -1;
    }

    public void start(String mode) {
        this.gameMode = mode;
        board.setupBoard(mode);
        moveHistory.clear();
        isWhiteTurn = true;
        turnStartTime = System.currentTimeMillis();
        isMultiJump = false;
        multiJumpFromX = -1;
        multiJumpFromY = -1;
        enPassantTargetX = -1;
        enPassantTargetY = -1;
        enPassantPossible = false;
        if (gui != null) {
            gui.updateDisplay();
        }
    }

    public boolean makeMove(int fromX, int fromY, int toX, int toY) {
        if (waitingForPromotion) {
            gui.setStatusMessage("Сначала выберите фигуру для превращения пешки!");
            return false;
        }

        if (isMultiJump && (fromX != multiJumpFromX || fromY != multiJumpFromY)) {
            gui.setStatusMessage("Завершайте серию взятий или подтвердите окончание хода!");
            return false;
        }

        Piece piece = board.getPiece(fromX, fromY);
        boolean isEnPassantMove = piece instanceof Pawn && Math.abs(toY - fromY) == 1 &&
                toX == enPassantTargetX && toY == enPassantTargetY && enPassantPossible;

        // Allow ending multi-jump by selecting the same square
        if (isMultiJump && fromX == toX && fromY == toY) {
            isMultiJump = false;
            multiJumpFromX = -1;
            multiJumpFromY = -1;
            isWhiteTurn = !isWhiteTurn;
            turnStartTime = System.currentTimeMillis();
            gui.setStatusMessage("Ход завершен.");
            gui.updateDisplay();
            return true;
        }

        if (board.isValidMove(fromX, fromY, toX, toY, isWhiteTurn, gameMode, isMultiJump)) {
            long moveEndTime = System.currentTimeMillis();
            double timeTaken = (moveEndTime - turnStartTime) / 1000.0;

            String moveNotation = getMoveNotation(fromX, fromY, toX, toY);
            String moveRecord = String.format("%s (%.2f сек)", moveNotation, timeTaken);
            moveHistory.add(moveRecord);

            boolean isCaptureMove = piece instanceof CheckersPiece && Math.abs(toX - fromX) >= 2 && Math.abs(toY - fromY) >= 2;

            // Store en passant capture coordinates if applicable
            int capturedPawnX = -1, capturedPawnY = -1;
            if (isEnPassantMove) {
                int direction = piece.getColor().equals("White") ? 1 : -1;
                capturedPawnX = toX - direction;
                capturedPawnY = toY;
            }

            // Reset en passant state after storing capture info
            enPassantPossible = false;
            enPassantTargetX = -1;
            enPassantTargetY = -1;

            // Check for en passant eligibility
            if (piece instanceof Pawn && Math.abs(toX - fromX) == 2) {
                enPassantPossible = true;
                enPassantTargetX = (fromX + toX) / 2; // Square passed over
                enPassantTargetY = toY;
            }

            board.movePiece(fromX, fromY, toX, toY, isMultiJump, capturedPawnX, capturedPawnY);

            if (gameMode.equals("Checkers") || gameMode.equals("Hybrid") || gameMode.equals("Unified")) {
                if (piece instanceof CheckersPiece) {
                    CheckersPiece checkersPiece = (CheckersPiece) piece;
                    // Check for promotion, including during multi-jumps
                    if (!checkersPiece.isKing() &&
                            ((checkersPiece.getColor().equals("White") && toX == 0) ||
                                    (checkersPiece.getColor().equals("Black") && toX == 7))) {
                        checkersPiece.setKing(true);
                        gui.setStatusMessage("Шашка превращена в дамку!");
                    }
                    // Handle multi-jump captures
                    if (isCaptureMove) {
                        List<int[]> furtherCaptures = checkersPiece.getCaptureMoves(toX, toY, board.getGrid(), gameMode);
                        if (!furtherCaptures.isEmpty()) {
                            isMultiJump = true;
                            multiJumpFromX = toX;
                            multiJumpFromY = toY;
                            gui.setStatusMessage("Доступны дополнительные взятия! Выберите ход или подтвердите окончание.");
                            gui.updateDisplay();
                            return true;
                        } else {
                            isMultiJump = false;
                            multiJumpFromX = -1;
                            multiJumpFromY = -1;
                        }
                    }
                }
            }

            if (!waitingForPromotion) {
                isWhiteTurn = !isWhiteTurn;
                turnStartTime = System.currentTimeMillis();
                gui.updateDisplay();

                String currentPlayerColor = isWhiteTurn ? "White" : "Black";
                String previousPlayerColor = isWhiteTurn ? "Black" : "White";

                if (isEnPassantMove) {
                    gui.setStatusMessage("Взято на проходе!");
                } else if (board.isKingInCheck(currentPlayerColor)) {
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
        if (piece instanceof King && fromY == 4 && toX == fromX) {
            if (toY == 6) {
                return "O-O";
            } else if (toY == 2) {
                return "O-O-O";
            }
        }
        char fromFile = (char) ('a' + fromY);
        int fromRank = 8 - fromX;
        char toFile = (char) ('a' + toY);
        int toRank = 8 - toX;
        String pieceSymbol = piece instanceof CheckersPiece ? "" : getPieceSymbol(piece.getType());
        String capture = (piece instanceof CheckersPiece && Math.abs(toX - fromX) >= 2 && Math.abs(toY - fromY) >= 2) ||
                board.getPiece(toX, toY) != null ||
                (piece instanceof Pawn && Math.abs(toY - fromY) == 1 && board.getPiece(toX, toY) == null && toX == enPassantTargetX && toY == enPassantTargetY) ? "x" : "";
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

    public Piece[][] getGrid() {
        return board.getGrid();
    }

    public int getEnPassantTargetX() {
        return enPassantTargetX;
    }

    public int getEnPassantTargetY() {
        return enPassantTargetY;
    }

    public boolean isEnPassantPossible() {
        return enPassantPossible;
    }

    public boolean isMultiJump() {
        return isMultiJump;
    }
}