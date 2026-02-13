package org.sillylabs;

import org.sillylabs.pieces.*;

public class PromotionHandler {
    private boolean waitingForPromotion;
    private int promotionRow;
    private int promotionColumn;
    private Color promotionColor;

    public PromotionHandler() {
        waitingForPromotion = false;
        promotionRow = -1;
        promotionColumn = -1;
        promotionColor = null;
    }

    public boolean isWaitingForPromotion() {
        return waitingForPromotion;
    }

    public void requestPromotion(Board board, int row, int column, Color color, GameCoordinator coordinator) {
        Piece piece = board.getPieceAt(row, column);
        if (piece instanceof Pawn) {
            boolean shouldPromote = (piece.getColor() == Color.WHITE && row == 7) || (piece.getColor() == Color.BLACK && row == 0);
            if (shouldPromote) {
                waitingForPromotion = true;
                promotionRow = row;
                promotionColumn = column;
                promotionColor = color;
                coordinator.notifyPromotionRequested(row, column, color);
            }
        } else if (piece instanceof CheckersPiece checkersPiece && !checkersPiece.isKing() &&
                ((piece.getColor() == Color.WHITE && row == 0) || (piece.getColor() == Color.BLACK && row == 7))) {
            checkersPiece.setKing(true);
            coordinator.notifyStatus("Шашка превращена в дамку!");
        }
    }

    public void completePromotion(Board board, String pieceType, GameCoordinator coordinator) {
        if (waitingForPromotion) {
            Piece newPiece = switch (pieceType) {
                case "Queen" -> new Queen(promotionColor, promotionRow, promotionColumn);
                case "Rook" -> new Rook(promotionColor, promotionRow, promotionColumn);
                case "Bishop" -> new Bishop(promotionColor, promotionRow, promotionColumn);
                case "Knight" -> new Knight(promotionColor, promotionRow, promotionColumn);
                default -> new Queen(promotionColor, promotionRow, promotionColumn);
            };
            board.setPieceAt(promotionRow, promotionColumn, newPiece);
            System.out.println("Pawn promoted to " + pieceType + " at (" + promotionRow + ", " + promotionColumn + ")");
            waitingForPromotion = false;
            coordinator.notifyStatus("Пешка превращена в " + getPieceNameInRussian(pieceType) + "!");
            coordinator.notifyBoardChanged();
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
}