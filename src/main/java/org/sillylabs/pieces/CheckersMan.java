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

        MoveContext context = new MoveContext(grid, -1, -1, false); // En passant not used for checkers
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
        Piece[][] tempGrid = new Piece[8][8];
        for (int i = 0; i < 8; i++) {
            System.arraycopy(grid[i], 0, tempGrid[i], 0, 8);
        }
        findCaptures(fromRow, fromColumn, tempGrid, captureMoves, gameMode, new ArrayList<>());
        return captureMoves;
    }

    private void findCaptures(int fromRow, int fromColumn, Piece[][] grid, List<int[]> captureMoves, GameMode gameMode, List<int[]> path) {
        int[] directions = {1, -1};
        boolean isKingAtStart = isKing;
        if (!isKing && ((color == Color.WHITE && fromRow == 0) || (color == Color.BLACK && fromRow == 7))) {
            isKing = true;
        }

        for (int dRow : directions) {
            for (int dColumn : directions) {
                if (!isKing) {
                    int midRow = fromRow + dRow;
                    int midColumn = fromColumn + dColumn;
                    int toRow = fromRow + 2 * dRow;
                    int toColumn = fromColumn + 2 * dColumn;
                    if (toRow >= 0 && toRow < 8 && toColumn >= 0 && toColumn < 8 && grid[toRow][toColumn] == null &&
                            midRow >= 0 && midRow < 8 && midColumn >= 0 && midColumn < 8 &&
                            grid[midRow][midColumn] != null && grid[midRow][midColumn].getColor() != color &&
                            isValidCaptureTarget(grid[midRow][midColumn], gameMode)) {
                        Piece captured = grid[midRow][midColumn];
                        grid[midRow][midColumn] = null;
                        grid[toRow][toColumn] = grid[fromRow][fromColumn];
                        grid[fromRow][fromColumn] = null;

                        List<int[]> newPath = new ArrayList<>(path);
                        newPath.add(new int[]{toRow, toColumn});
                        captureMoves.add(new int[]{toRow, toColumn});

                        boolean wasKing = isKing;
                        if ((color == Color.WHITE && toRow == 0) || (color == Color.BLACK && toRow == 7)) {
                            isKing = true;
                        }
                        findCaptures(toRow, toColumn, grid, captureMoves, gameMode, newPath);
                        isKing = wasKing;

                        grid[fromRow][fromColumn] = grid[toRow][toColumn];
                        grid[toRow][toColumn] = null;
                        grid[midRow][midColumn] = captured;
                    }
                } else {
                    for (int i = 1; i < 8; i++) {
                        int checkRow = fromRow + i * dRow;
                        int checkColumn = fromColumn + i * dColumn;
                        if (checkRow < 0 || checkRow >= 8 || checkColumn < 0 || checkColumn >= 8) {
                            break;
                        }
                        if (grid[checkRow][checkColumn] != null) {
                            if (grid[checkRow][checkColumn].getColor() == color || !isValidCaptureTarget(grid[checkRow][checkColumn], gameMode)) {
                                break;
                            }
                            for (int j = 1; j < 8; j++) {
                                int toRow = checkRow + j * dRow;
                                int toColumn = checkColumn + j * dColumn;
                                if (toRow < 0 || toRow >= 8 || toColumn < 0 || toColumn >= 8 || grid[toRow][toColumn] != null) {
                                    break;
                                }
                                boolean pathClear = true;
                                for (int k = 1; k < j; k++) {
                                    int pathRow = checkRow + k * dRow;
                                    int pathColumn = checkColumn + k * dColumn;
                                    if (pathRow < 0 || pathRow >= 8 || pathColumn < 0 || pathColumn >= 8 || grid[pathRow][pathColumn] != null) {
                                        pathClear = false;
                                        break;
                                    }
                                }
                                if (!pathClear) {
                                    continue;
                                }
                                Piece captured = grid[checkRow][checkColumn];
                                grid[checkRow][checkColumn] = null;
                                grid[toRow][toColumn] = grid[fromRow][fromColumn];
                                grid[fromRow][fromColumn] = null;

                                List<int[]> newPath = new ArrayList<>(path);
                                newPath.add(new int[]{toRow, toColumn});
                                captureMoves.add(new int[]{toRow, toColumn});

                                findCaptures(toRow, toColumn, grid, captureMoves, gameMode, newPath);

                                grid[fromRow][fromColumn] = grid[toRow][toColumn];
                                grid[toRow][toColumn] = null;
                                grid[checkRow][checkColumn] = captured;
                            }
                            break;
                        }
                    }
                }
            }
        }
        isKing = isKingAtStart;
    }

    private boolean isValidCaptureTarget(Piece piece, GameMode gameMode) {
        return gameMode != GameMode.HYBRID || piece instanceof CheckersPiece;
    }
}
