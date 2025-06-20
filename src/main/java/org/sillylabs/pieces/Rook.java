package org.sillylabs.pieces;

public class Rook extends ChessPiece {
    private boolean hasMoved;

    public Rook(Color color, int row, int column) {
        super("Rook", color, row, column);
        this.hasMoved = false;
    }

    public boolean getHasMoved() {
        return hasMoved;
    }

    public void setHasMoved(boolean hasMoved) {
        this.hasMoved = hasMoved;
    }

    @Override
    public boolean isValidMove(int toRow, int toColumn, MoveContext context) {
        Piece[][] grid = context.grid;
        if (toRow != row && toColumn != column) return false;

        int start, end;
        if (toRow == row) {
            start = Math.min(column, toColumn);
            end = Math.max(column, toColumn);
            for (int i = start + 1; i < end; i++) {
                if (grid[toRow][i] != null) return false;
            }
        } else {
            start = Math.min(row, toRow);
            end = Math.max(row, toRow);
            for (int i = start + 1; i < end; i++) {
                if (grid[i][toColumn] != null) return false;
            }
        }
        return grid[toRow][toColumn] == null || grid[toRow][toColumn].getColor() != color;
    }
}
