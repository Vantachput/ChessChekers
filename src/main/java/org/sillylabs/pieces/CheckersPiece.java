package org.sillylabs.pieces;

import org.sillylabs.GameMode;

import java.util.List;

public abstract class CheckersPiece extends Piece {
    protected boolean isKing;

    public CheckersPiece(String type, Color color, int row, int column) {
        super(type, color, row, column);
        isKing = false;
    }

    public boolean isKing() {
        return isKing;
    }

    public void setKing(boolean king) {
        isKing = king;
    }

    public abstract List<int[]> getCaptureMoves(int fromRow, int fromColumn, Piece[][] grid, GameMode gameMode);

    public boolean isValidMoveWithMultiJump(int toRow, int toColumn, Piece[][] grid, boolean isMultiJump) {
        if (!isMultiJump) {
            List<int[]> availableCaptures = getCaptureMoves(row, column, grid, GameMode.CHECKERS);
            if (!availableCaptures.isEmpty()) {
                return isCapture(toRow, toColumn, grid);
            }
        }
        MoveContext context = new MoveContext(grid, -1, -1, false);
        return isValidMove(toRow, toColumn, context);
    }

    protected boolean isCapture(int toRow, int toColumn, Piece[][] grid) {
        int dRow = toRow - row;
        int dColumn = toColumn - column;
        int absDRow = Math.abs(dRow);
        int absDColumn = Math.abs(dColumn);

        if (absDRow != absDColumn) {
            return false;
        }

        int stepRow = dRow > 0 ? 1 : -1;
        int stepColumn = dColumn > 0 ? 1 : -1;

        if (!isKing) {
            if (absDRow == 2) {
                int midRow = row + stepRow;
                int midColumn = column + stepColumn;
                return midRow >= 0 && midRow < 8 && midColumn >= 0 && midColumn < 8 &&
                        grid[midRow][midColumn] != null && grid[midRow][midColumn].getColor() != color;
            }
        } else {
            int opponentCount = 0;
            for (int i = 1; i < absDRow; i++) {
                int checkRow = row + i * stepRow;
                int checkColumn = column + i * stepColumn;
                Piece p = grid[checkRow][checkColumn];
                if (p != null) {
                    if (p.getColor() == color) return false;
                    opponentCount++;
                }
            }
            return opponentCount == 1;
        }
        return false;
    }

    @Override
    public abstract boolean isValidMove(int toRow, int toColumn, MoveContext context);
}