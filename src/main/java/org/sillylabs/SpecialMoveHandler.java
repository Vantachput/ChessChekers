package org.sillylabs;

import org.sillylabs.pieces.*;

import java.util.List;

public class SpecialMoveHandler {
    private boolean isMultiJump;
    private int multiJumpFromRow;
    private int multiJumpFromColumn;
    private int enPassantTargetRow;
    private int enPassantTargetColumn;
    private boolean enPassantPossible;

    public SpecialMoveHandler() {
        isMultiJump = false;
        multiJumpFromRow = -1;
        multiJumpFromColumn = -1;
        enPassantTargetRow = -1;
        enPassantTargetColumn = -1;
        enPassantPossible = false;
    }

    public boolean isMultiJump() {
        return isMultiJump;
    }

    public int getMultiJumpFromRow() {
        return multiJumpFromRow;
    }

    public int getMultiJumpFromColumn() {
        return multiJumpFromColumn;
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

    public boolean isEnPassantMove(Board board, int fromRow, int fromColumn, int toRow, int toColumn) {
        Piece piece = board.getPieceAt(fromRow, fromColumn);
        return piece instanceof Pawn && Math.abs(toColumn - fromColumn) == 1 &&
                toRow == enPassantTargetRow && toColumn == enPassantTargetColumn && enPassantPossible;
    }

    public void updateEnPassant(Board board, int fromRow, int toRow, int toColumn) {
        Piece piece = board.getPieceAt(fromRow, toColumn);
        enPassantPossible = false;
        enPassantTargetRow = -1;
        enPassantTargetColumn = -1;

        if (piece instanceof Pawn && Math.abs(toRow - fromRow) == 2) {
            enPassantPossible = true;
            enPassantTargetRow = (fromRow + toRow) / 2;
            enPassantTargetColumn = toColumn;
        }
    }

    public void updateMultiJump(Board board, Piece piece, int toRow, int toColumn, GameMode gameMode, GameCoordinator coordinator) {
        if (gameMode == GameMode.CHECKERS && piece instanceof CheckersPiece checkersPiece) {
            boolean isCaptureMove = Math.abs(toRow - piece.getRow()) >= 2 && Math.abs(toColumn - piece.getColumn()) >= 2;
            if (isCaptureMove) {
                List<int[]> furtherCaptures = checkersPiece.getCaptureMoves(toRow, toColumn, board.getGrid(), gameMode);
                if (!furtherCaptures.isEmpty()) {
                    isMultiJump = true;
                    multiJumpFromRow = toRow;
                    multiJumpFromColumn = toColumn;
                    coordinator.notifyStatus("Доступны дополнительные взятия! Выберите ход или подтвердите окончание.");
                    coordinator.notifyBoardChanged();
                } else {
                    resetMultiJump();
                }
            } else {
                resetMultiJump();
            }
        } else {
            resetMultiJump();
        }
    }

    public boolean completeMultiJump(int fromRow, int fromColumn, int toRow, int toColumn) {
        if (isMultiJump && fromRow == toRow && fromColumn == toColumn) {
            resetMultiJump();
            return true;
        }
        return false;
    }

    private void resetMultiJump() {
        isMultiJump = false;
        multiJumpFromRow = -1;
        multiJumpFromColumn = -1;
    }
}