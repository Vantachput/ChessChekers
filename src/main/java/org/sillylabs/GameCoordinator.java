package org.sillylabs;

import org.sillylabs.pieces.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameCoordinator implements GameStateView {
    private final Board board;
    private TurnManager turnManager;
    private SpecialMoveHandler specialMoveHandler;
    private PromotionHandler promotionHandler;
    private GameMode gameMode;
    private GameRules gameRules;
    private final List<String> moveHistory;
    private final List<GameObserver> observers;

    // Історія позицій для правила трикратного повторення
    private final Map<String, Integer> positionCounts;

    public GameCoordinator() {
        board = new Board();
        turnManager = new TurnManager();
        specialMoveHandler = new SpecialMoveHandler();
        promotionHandler = new PromotionHandler();
        moveHistory = new ArrayList<>();
        observers = new ArrayList<>();
        positionCounts = new HashMap<>();
    }

    public void addObserver(GameObserver observer) {
        observers.add(observer);
    }

    public void removeObserver(GameObserver observer) {
        observers.remove(observer);
    }

    public void start(GameMode mode) {
        this.gameMode = mode;

        this.turnManager = new TurnManager();
        this.specialMoveHandler = new SpecialMoveHandler();
        this.promotionHandler = new PromotionHandler();

        board.setupBoard(mode);
        moveHistory.clear();
        positionCounts.clear(); // Очищуємо історію позицій при старті

        gameRules = switch (mode) {
            case CHESS -> new ChessRules();
            case CHECKERS -> new CheckersRules();
        };
        gameRules.setGameCoordinator(this);

        // Записуємо першу позицію
        recordPosition(false);

        notifyBoardChanged();
        notifyStatus("");
    }

    public String generateFEN(Piece[][] board, boolean isWhiteTurn) {
        StringBuilder fen = new StringBuilder();
        for (int row = 0; row < 8; row++) {
            int emptySquares = 0;
            for (int col = 0; col < 8; col++) {
                Piece p = board[row][col];
                if (p == null) {
                    emptySquares++;
                } else {
                    if (emptySquares > 0) {
                        fen.append(emptySquares);
                        emptySquares = 0;
                    }
                    char pieceChar = getPieceChar(p);
                    // Білі фігури з великої літери, чорні - з маленької
                    if (p.getColor() == org.sillylabs.pieces.Color.WHITE) {
                        fen.append(Character.toUpperCase(pieceChar));
                    } else {
                        fen.append(Character.toLowerCase(pieceChar));
                    }
                }
            }
            if (emptySquares > 0) {
                fen.append(emptySquares);
            }
            if (row < 7) {
                fen.append("/");
            }
        }

        // Додаємо чий хід. Для базової версії опускаємо рокіровку та взяття на проході (додаємо " - - 0 1")
        fen.append(isWhiteTurn ? " w" : " b").append(" - - 0 1");
        return fen.toString();
    }

    private char getPieceChar(Piece p) {
        String name = p.getClass().getSimpleName();
        switch (name) {
            case "Pawn": return 'p';
            case "Knight": return 'n';
            case "Bishop": return 'b';
            case "Rook": return 'r';
            case "Queen": return 'q';
            case "King": return 'k';
            default: return 'p';
        }
    }

    public boolean isValidMove(int fromRow, int fromColumn, int toRow, int toColumn) {
        return gameRules.isValidMove(board, fromRow, fromColumn, toRow, toColumn, turnManager.isWhiteTurn(), specialMoveHandler.isMultiJump());
    }

    public boolean makeMove(int fromRow, int fromColumn, int toRow, int toColumn) {
        if (promotionHandler.isWaitingForPromotion()) {
            notifyStatus("Спочатку виберіть фігуру для перетворення пішака!");
            return false;
        }

        if (specialMoveHandler.isMultiJump() && (fromRow != specialMoveHandler.getMultiJumpFromRow() || fromColumn != specialMoveHandler.getMultiJumpFromColumn())) {
            notifyStatus("Завершіть серію взяттів або підтвердіть закінчення ходу!");
            return false;
        }

        if (specialMoveHandler.completeMultiJump(fromRow, fromColumn, toRow, toColumn)) {
            turnManager.switchTurn();
            notifyStatus("Хід завершено.");
            notifyBoardChanged();
            return true;
        }

        if (!isValidMove(fromRow, fromColumn, toRow, toColumn)) {
            notifyStatus("Неправильний хід");
            return false;
        }

        // Перевіряємо, чи є хід "незворотним" (хід пішаком або взяття) для скидання лічильника позицій
        boolean irreversibleMove = false;
        Piece pieceToMove = board.getPieceAt(fromRow, fromColumn);

        if (gameMode == GameMode.CHESS) {
            // У шахах незворотний хід: хід пішаком або взяття фігури
            if (pieceToMove instanceof Pawn || board.getPieceAt(toRow, toColumn) != null) {
                irreversibleMove = true;
            }
        } else if (gameMode == GameMode.CHECKERS) {
            // У шашках незворотний хід: взяття (стрибок через клітинку) або рух простої шашки (бо вона йде тільки вперед)
            if (Math.abs(toRow - fromRow) >= 2) {
                irreversibleMove = true; // Це було взяття
            } else if (pieceToMove instanceof CheckersPiece && !((CheckersPiece) pieceToMove).isKing()) {
                irreversibleMove = true; // Це хід простою шашкою
            }
        }

        long moveEndTime = System.currentTimeMillis();
        double timeTaken = (moveEndTime - turnManager.getTurnStartTime()) / 1000.0;

        String moveNotation = getMoveNotation(fromRow, fromColumn, toRow, toColumn);
        String moveRecord = String.format("%s (%.2f сек)", moveNotation, timeTaken);
        moveHistory.add(moveRecord);

        int capturedPawnRow = -1, capturedPawnColumn = -1;
        boolean isEnPassant = specialMoveHandler.isEnPassantMove(board, fromRow, fromColumn, toRow, toColumn);
        if (isEnPassant) {
            irreversibleMove = true; // Взяття на проході - це теж незворотний хід
            int direction = board.getPieceAt(fromRow, fromColumn).getColor() == Color.WHITE ? -1 : 1;
            capturedPawnRow = toRow - direction;
            capturedPawnColumn = toColumn;
        }

        gameRules.movePiece(board, fromRow, fromColumn, toRow, toColumn, specialMoveHandler.isMultiJump(), capturedPawnRow, capturedPawnColumn);
        specialMoveHandler.updateEnPassant(board, fromRow, toRow, toColumn);
        Piece movedPiece = board.getPieceAt(toRow, toColumn);
        specialMoveHandler.updateMultiJump(board, movedPiece, fromRow, fromColumn, toRow, toColumn, gameMode, this);
        if (!specialMoveHandler.isMultiJump()) {
            promotionHandler.requestPromotion(board, toRow, toColumn, turnManager.getCurrentPlayerColor(), this);
        }

        if (!promotionHandler.isWaitingForPromotion() && !specialMoveHandler.isMultiJump()) {
            turnManager.switchTurn();
            notifyBoardChanged();

            Color currentPlayerColor = turnManager.getCurrentPlayerColor();
            Color previousPlayerColor = currentPlayerColor == Color.WHITE ? Color.BLACK : Color.WHITE;
            String prevPlayerStr = previousPlayerColor == Color.WHITE ? "Білі" : "Чорні";
            String currPlayerStr = currentPlayerColor == Color.WHITE ? "Білий" : "Чорний";

            checkGameOverAndDraws(currentPlayerColor, previousPlayerColor, currPlayerStr, prevPlayerStr, isEnPassant, irreversibleMove);

        } else if (specialMoveHandler.isMultiJump()) {
            notifyStatus("Продовжуйте взяття або підтвердіть закінчення ходу!");
        }
        return true;
    }

    // --- НОВІ МЕТОДИ ЛОГІКИ ЗАВЕРШЕННЯ ТА НІЧИЇ ---

    private void checkGameOverAndDraws(Color currentPlayerColor, Color previousPlayerColor, String currPlayerStr, String prevPlayerStr, boolean isEnPassant, boolean irreversibleMove) {
        // Записуємо позицію
        recordPosition(irreversibleMove);

        // Перевірка на трикратне повторення (Працює і для Шахів, і для Шашок)
        if (isThreefoldRepetition()) {
            notifyStatus("Нічия! Трикратне повторення позиції.");
            notifyGameOver(true, null);
            return;
        }

        if (gameMode == GameMode.CHESS && gameRules instanceof ChessRules chessRules) {
            // Перевірка на нестачу матеріалу
            if (chessRules.isInsufficientMaterial(board)) {
                notifyStatus("Нічия! Недостатньо матеріалу для мату.");
                notifyGameOver(true, null);
                return;
            }

            // Перевірка чи є доступні ходи
            if (!chessRules.hasLegalMoves(board, currentPlayerColor)) {
                if (chessRules.isKingInCheck(board, currentPlayerColor)) {
                    // Якщо немає ходів і під шахом - це Мат
                    notifyStatus("Шах і мат! Перемога " + prevPlayerStr + "!");
                    notifyGameOver(true, previousPlayerColor);
                } else {
                    // Якщо немає ходів і НЕ під шахом - це Пат (Нічия)
                    notifyStatus("Нічия! Пат на дошці.");
                    notifyGameOver(true, null);
                }
                return;
            } else if (chessRules.isKingInCheck(board, currentPlayerColor)) {
                notifyStatus(currPlayerStr + " король під шахом!");
                return;
            }
        } else if (gameMode == GameMode.CHECKERS) {
            // Логіка перемоги для Шашок
            if (gameRules.isGameOver(board, currentPlayerColor)) {
                notifyStatus("Гра закінчена! Перемога " + prevPlayerStr + "!");
                notifyGameOver(true, previousPlayerColor);
                return;
            }
        }

        // Якщо нічого з вище переліченого не спрацювало, просто оновлюємо статус
        if (isEnPassant) {
            notifyStatus("Взяття на проході!");
        } else {
            notifyStatus("");
        }
    }

    private void recordPosition(boolean irreversible) {
        // Якщо хід незворотний (пішак або взяття), очищуємо історію, бо повторення вже неможливе
        if (irreversible) {
            positionCounts.clear();
        }
        String state = generateBoardState(board, turnManager.getCurrentPlayerColor());
        positionCounts.put(state, positionCounts.getOrDefault(state, 0) + 1);
    }

    private boolean isThreefoldRepetition() {
        for (int count : positionCounts.values()) {
            if (count >= 3) return true;
        }
        return false;
    }

    private String generateBoardState(Board board, Color turnColor) {
        StringBuilder sb = new StringBuilder();
        Piece[][] grid = board.getGrid();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = grid[r][c];
                if (p == null) {
                    sb.append("1");
                } else {
                    char type = p.getType().charAt(0);
                    if (p.getType().equals("Knight")) type = 'N';

                    // НОВИЙ РЯДОК: Якщо це шашка і вона дамка, позначаємо її як 'K' (King)
                    if (p instanceof CheckersPiece && ((CheckersPiece) p).isKing()) {
                        type = 'K';
                    }

                    if (p.getColor() == Color.WHITE) type = Character.toUpperCase(type);
                    else type = Character.toLowerCase(type);
                    sb.append(type);
                }
            }
        }
        sb.append(turnColor == Color.WHITE ? "w" : "b");
        return sb.toString();
    }
    // ----------------------------------------------

    private String getMoveNotation(int fromRow, int fromColumn, int toRow, int toColumn) {
        Piece piece = board.getPieceAt(fromRow, fromColumn);
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
                board.getPieceAt(toRow, toColumn) != null ||
                (piece instanceof Pawn && Math.abs(toColumn - fromColumn) == 1 && board.getPieceAt(toRow, toColumn) == null &&
                        toRow == specialMoveHandler.getEnPassantTargetRow() && toColumn == specialMoveHandler.getEnPassantTargetColumn()) ? "x" : "";
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

    public void completePawnPromotion(String pieceType) {
        promotionHandler.completePromotion(board, pieceType, this);
        if (!promotionHandler.isWaitingForPromotion()) {
            turnManager.switchTurn();
            Color currentPlayerColor = turnManager.getCurrentPlayerColor();
            Color previousPlayerColor = currentPlayerColor == Color.WHITE ? Color.BLACK : Color.WHITE;
            String prevPlayerStr = previousPlayerColor == Color.WHITE ? "Білі" : "Чорні";
            String currPlayerStr = currentPlayerColor == Color.WHITE ? "Білий" : "Чорний";

            // Перетворення пішака - це незворотний хід
            checkGameOverAndDraws(currentPlayerColor, previousPlayerColor, currPlayerStr, prevPlayerStr, false, true);
        }
    }

    public void notifyStatus(String message) {
        for (GameObserver observer : observers) {
            observer.onStatusUpdate(message);
        }
    }

    public void notifyBoardChanged() {
        for (GameObserver observer : observers) {
            observer.onBoardChanged();
        }
    }

    public void notifyPromotionRequested(int row, int column, Color color) {
        for (GameObserver observer : observers) {
            observer.onPromotionRequested(row, column, color);
        }
    }

    public void notifyGameOver(boolean isGameOver, Color winner) {
        for (GameObserver observer : observers) {
            observer.onGameOver(isGameOver, winner);
        }
    }

    @Override
    public Piece[][] getBoardState() {
        return board.getGrid();
    }

    @Override
    public boolean isWhiteTurn() {
        return turnManager.isWhiteTurn();
    }

    @Override
    public GameMode getGameMode() {
        return gameMode;
    }

    @Override
    public boolean isMultiJump() {
        return specialMoveHandler.isMultiJump();
    }

    @Override
    public int getMultiJumpFromRow() {
        return specialMoveHandler.getMultiJumpFromRow();
    }

    @Override
    public int getMultiJumpFromColumn() {
        return specialMoveHandler.getMultiJumpFromColumn();
    }

    @Override
    public boolean isWaitingForPromotion() {
        return promotionHandler.isWaitingForPromotion();
    }

    @Override
    public boolean isGameOver() {
        // Використовуємо наш новий метод для перевірки легальних ходів
        if (gameMode == GameMode.CHESS && gameRules instanceof ChessRules) {
            return ((ChessRules) gameRules).isGameOver(board, turnManager.getCurrentPlayerColor());
        }
        return gameRules.isGameOver(board, turnManager.getCurrentPlayerColor());
    }

    public int getEnPassantTargetRow() {
        return specialMoveHandler.getEnPassantTargetRow();
    }

    public int getEnPassantTargetColumn() {
        return specialMoveHandler.getEnPassantTargetColumn();
    }

    public boolean isEnPassantPossible() {
        return specialMoveHandler.isEnPassantPossible();
    }

    public List<String> getMoveHistory() {
        return new ArrayList<>(moveHistory);
    }
}