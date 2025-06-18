package org.sillylabs;

public class Game {
    private Board board;
    private boolean isWhiteTurn;
    private GameGUI gui;
    private String gameMode; // "Chess", "Checkers", "Hybrid", "Unified"

    public Game() {
        board = new Board();
        isWhiteTurn = true;
    }

    public void start(String mode) {
        this.gameMode = mode;
        board.setupBoard(mode);
        if (gui != null) {
            gui.updateDisplay();
        }
    }

    public boolean makeMove(int fromX, int fromY, int toX, int toY) {
        if (board.isValidMove(fromX, fromY, toX, toY, isWhiteTurn, gameMode)) {
            board.movePiece(fromX, fromY, toX, toY);
            isWhiteTurn = !isWhiteTurn;
            gui.updateDisplay();
            return true;
        }
        return false;
    }

    public boolean isGameOver() {
        if (gameMode.equals("Chess") || gameMode.equals("Hybrid") || gameMode.equals("Unified")) {
            return board.isCheckmate(isWhiteTurn ? "White" : "Black");
        }
        return false; // Checkers ends via resign
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
}