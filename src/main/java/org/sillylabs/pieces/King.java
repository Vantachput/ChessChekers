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

        // Звичайний хід короля (на 1 клітинку)
        if (dRow <= 1 && dColumn <= 1) {
            Piece target = grid[toRow][toColumn];
            return target == null || target.getColor() != color;
        }

        // СПЕЦІАЛЬНИЙ ХІД: Рокировка (Король стрибає на 2 клітинки по горизонталі)
        // Умова: Король ще жодного разу не ходив
        if (dRow == 0 && dColumn == 2 && !this.getHasMoved()) {

            // Коротка рокировка (вправо)
            if (toColumn == column + 2) {
                Piece rook = grid[row][column + 3];
                // Перевіряємо, чи стоїть на своєму місці тура, чи не ходила вона, і чи ПУСТІ 2 поля між ними
                if (rook instanceof Rook && !((Rook) rook).getHasMoved() &&
                        grid[row][column + 1] == null &&
                        grid[row][column + 2] == null) {
                    return true;
                }
            }
            // Довга рокировка (вліво)
            else if (toColumn == column - 2) {
                Piece rook = grid[row][column - 4];
                // Перевіряємо туру і ПУСТІ 3 поля між королем та турою
                if (rook instanceof Rook && !((Rook) rook).getHasMoved() &&
                        grid[row][column - 1] == null &&
                        grid[row][column - 2] == null &&
                        grid[row][column - 3] == null) {
                    return true;
                }
            }
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