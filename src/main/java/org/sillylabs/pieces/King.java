package org.sillylabs.pieces;

public class King extends ChessPiece {
    private boolean hasMoved;

    public King(Color color, int row, int column) {
        super("King", color, row, column);
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
        int dRow = Math.abs(toRow - row);
        int dColumn = Math.abs(toColumn - column);

        if (dRow <= 1 && dColumn <= 1) {
            return grid[toRow][toColumn] == null || grid[toRow][toColumn].getColor() != color;
        }

        if (!hasMoved && dRow == 0 && Math.abs(dColumn) == 2) {
            return (column == 3 && (toColumn == 1 || toColumn == 5)) || (column == 4 && (toColumn == 2 || toColumn == 6));
        }

        return false;
    }

    public boolean isInCheck(Piece[][] grid) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                Piece piece = grid[i][j];
                if (piece != null && piece.getColor() != color) {
                    MoveContext context = new MoveContext(grid, -1, -1, false); // En passant not needed for check
                    if (piece.isValidMove(row, column, context)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}