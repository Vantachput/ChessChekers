package org.sillylabs.pieces;

import org.sillylabs.GameMode;

import java.util.ArrayList;
import java.util.List;

public class CheckersMan extends CheckersPiece {
    public CheckersMan(Color color, int row, int column) {
        super("CheckersMan", color, row, column);
    }

    @Override
    public boolean isValidMove(int toRow, int toColumn, MoveContext context) {
        Piece[][] grid = context.grid;
        if (toRow < 0 || toRow >= 8 || toColumn < 0 || toColumn >= 8 || grid[toRow][toColumn] != null) {
            return false;
        }

        int dRow = toRow - row;
        int dColumn = toColumn - column;
        int absDRow = Math.abs(dRow);
        int absDColumn = Math.abs(dColumn);

        if (absDRow != absDColumn) {
            return false;
        }

        int direction = color == Color.WHITE ? -1 : 1;
        int stepRow = dRow > 0 ? 1 : -1;
        int stepColumn = dColumn > 0 ? 1 : -1;

        List<int[]> captureMoves = getCaptureMoves(row, column, grid, GameMode.CHECKERS);
        if (!captureMoves.isEmpty()) {
            if (absDRow < 2) {
                System.out.println("Invalid: Must capture when possible");
                return false;
            }
            return isCapture(toRow, toColumn, grid);
        }

        if (!isKing) {
            return absDRow == 1 && dRow == direction;
        } else {
            if (absDRow == 1) {
                return true;
            }
            for (int i = 1; i < absDRow; i++) {
                int checkRow = row + i * stepRow;
                int checkColumn = column + i * stepColumn;
                if (checkRow < 0 || checkRow >= 8 || checkColumn < 0 || checkColumn >= 8 || grid[checkRow][checkColumn] != null) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public boolean isValidMoveWithMultiJump(int toRow, int toColumn, Piece[][] grid, boolean isMultiJump) {
        if (toRow < 0 || toRow >= 8 || toColumn < 0 || toColumn >= 8 || grid[toRow][toColumn] != null) {
            return false;
        }

        int dRow = toRow - row;
        int dColumn = toColumn - column;
        int absDRow = Math.abs(dRow);
        int absDColumn = Math.abs(dColumn);

        if (absDRow != absDColumn) {
            return false;
        }

        if (isMultiJump) {
            boolean isCapture = isCapture(toRow, toColumn, grid);
            System.out.println("MultiJump mode active - capture move: " + isCapture);
            return isCapture;
        }

        MoveContext context = new MoveContext(grid, -1, -1, false);
        return isValidMove(toRow, toColumn, context);
    }

    protected boolean isCapture(int toRow, int toColumn, Piece[][] grid) {
        int dRow = toRow - row;
        int dColumn = toColumn - column;
        int absDRow = Math.abs(dRow);
        int absDColumn = Math.abs(dColumn);
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
            boolean foundOpponent = false;
            int opponentCount = 0;
            for (int i = 1; i < absDRow; i++) {
                int checkRow = row + i * stepRow;
                int checkColumn = column + i * stepColumn;
                if (checkRow < 0 || checkRow >= 8 || checkColumn < 0 || checkColumn >= 8) {
                    break;
                }
                if (grid[checkRow][checkColumn] != null) {
                    if (grid[checkRow][checkColumn].getColor() == color) {
                        break;
                    }
                    opponentCount++;
                    foundOpponent = true;
                }
            }
            return foundOpponent && opponentCount == 1;
        }
        return false;
    }

    @Override
    public List<int[]> getCaptureMoves(int fromRow, int fromColumn, Piece[][] grid, GameMode gameMode) {
        List<int[]> captureMoves = new ArrayList<>();
        int direction = color == Color.WHITE ? -1 : 1;

        if (!isKing) {
            int[][] directions = {
                    {direction, -1}, {direction, 1}
            };
            for (int[] dir : directions) {
                int midRow = fromRow + dir[0];
                int midColumn = fromColumn + dir[1];
                int toRow = fromRow + 2 * dir[0];
                int toColumn = fromColumn + 2 * dir[1];
                if (toRow >= 0 && toRow < 8 && toColumn >= 0 && toColumn < 8 && grid[toRow][toColumn] == null &&
                        midRow >= 0 && midColumn >= 0 && midColumn < 8 && grid[midRow][midColumn] != null &&
                        grid[midRow][midColumn].getColor() != color) {
                    captureMoves.add(new int[]{toRow, toColumn});
                }
            }
        } else {
            int[][] directions = {{-1, -1}, {-1, 1}, {1, -1}, {1, 1}};
            for (int[] dir : directions) {
                for (int i = 1; i < 7; i++) {
                    int midRow = fromRow + i * dir[0];
                    int midColumn = fromColumn + i * dir[1];
                    int toRow = fromRow + (i + 1) * dir[0];
                    int toColumn = fromColumn + (i + 1) * dir[1];
                    if (toRow < 0 || toRow >= 8 || toColumn < 0 || toColumn >= 8) break;
                    if (grid[toRow][toColumn] != null) break;
                    if (midRow >= 0 && midRow < 8 && midColumn >= 0 && midColumn < 8 &&
                            grid[midRow][midColumn] != null && grid[midRow][midColumn].getColor() != color) {
                        boolean valid = true;
                        for (int j = 1; j < i; j++) {
                            int checkRow = fromRow + j * dir[0];
                            int checkColumn = fromColumn + j * dir[1];
                            if (grid[checkRow][checkColumn] != null) {
                                valid = false;
                                break;
                            }
                        }
                        if (valid) {
                            captureMoves.add(new int[]{toRow, toColumn});
                        }
                    }
                }
            }
        }
        return captureMoves;
    }

    private boolean isValidCaptureTarget(Piece piece, GameMode gameMode) {
        return true; // All pieces are valid capture targets in CHECKERS mode
    }
}