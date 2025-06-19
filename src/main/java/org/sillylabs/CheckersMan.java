package org.sillylabs;

import java.util.ArrayList;
import java.util.List;

public class CheckersMan extends CheckersPiece {
    public CheckersMan(String color, int x, int y) {
        super("CheckersMan", color, x, y);
    }

    @Override
    public boolean isValidMove(int toX, int toY, Piece[][] grid) {
        if (toX < 0 || toX >= 8 || toY < 0 || toY >= 8 || grid[toX][toY] != null) {
            return false;
        }

        int dx = toX - x;
        int dy = toY - y;
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);

        if (absDx != absDy) {
            return false; // Must move diagonally
        }

        int direction = color.equals("White") ? -1 : 1;
        int stepX = dx > 0 ? 1 : -1;
        int stepY = dy > 0 ? 1 : -1;

        // Check for available captures first
        List<int[]> captureMoves = getCaptureMoves(x, y, grid, "Checkers");
        if (!captureMoves.isEmpty()) {
            // If captures are available, only allow capture moves
            if (absDx < 2) {
                System.out.println("Invalid: Must capture when possible");
                return false;
            }
            // Verify if this is a valid capture
            return isCapture(toX, toY, grid);
        }

        // No captures available, proceed with regular move validation
        if (!isKing) {
            // Non-kings: Move one square forward
            if (absDx == 1 && dx == direction) {
                return true; // Regular move
            }
            return false;
        } else {
            // Kings: Move any distance diagonally
            if (absDx == 1) {
                // Simple one-square move for kings
                return true;
            }
            // For multi-square non-capture moves, check path is clear
            for (int i = 1; i < absDx; i++) {
                int checkX = x + i * stepX;
                int checkY = y + i * stepY;
                if (checkX < 0 || checkX >= 8 || checkY < 0 || checkY >= 8 || grid[checkX][checkY] != null) {
                    return false;
                }
            }
            return true;
        }
    }

    @Override
    public boolean isValidMoveWithMultiJump(int toX, int toY, Piece[][] grid, boolean isMultiJump) {
        if (toX < 0 || toX >= 8 || toY < 0 || toY >= 8 || grid[toX][toY] != null) {
            return false;
        }

        int dx = toX - x;
        int dy = toY - y;
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);

        if (absDx != absDy) {
            return false; // Must move diagonally
        }

        // If in multi-jump mode, only allow capture moves
        if (isMultiJump) {
            boolean isCapture = isCapture(toX, toY, grid);
            System.out.println("MultiJump mode active - capture move: " + isCapture);
            return isCapture;
        }

        // Otherwise, use standard move validation (which checks for mandatory captures)
        return isValidMove(toX, toY, grid);
    }

    protected boolean isCapture(int toX, int toY, Piece[][] grid) {
        int dx = toX - x;
        int dy = toY - y;
        int absDx = Math.abs(dx);
        int absDy = Math.abs(dy);
        int stepX = dx > 0 ? 1 : -1;
        int stepY = dy > 0 ? 1 : -1;

        if (!isKing) {
            if (absDx == 2) {
                int midX = x + stepX;
                int midY = y + stepY;
                return midX >= 0 && midX < 8 && midY >= 0 && midY < 8 &&
                        grid[midX][midY] != null && !grid[midX][midY].getColor().equals(color);
            }
        } else {
            // Kings: Check capture over any distance
            boolean foundOpponent = false;
            int opponentCount = 0;
            for (int i = 1; i < absDx; i++) {
                int checkX = x + i * stepX;
                int checkY = y + i * stepY;
                if (checkX < 0 || checkX >= 8 || checkY < 0 || checkY >= 8) {
                    break;
                }
                if (grid[checkX][checkY] != null) {
                    if (grid[checkX][checkY].getColor().equals(color)) {
                        break; // Friendly piece blocks path
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
    public List<int[]> getCaptureMoves(int fromX, int fromY, Piece[][] grid, String gameMode) {
        List<int[]> captureMoves = new ArrayList<>();
        Piece[][] tempGrid = new Piece[8][8];
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                tempGrid[i][j] = grid[i][j];
            }
        }
        findCaptures(fromX, fromY, tempGrid, captureMoves, gameMode, new ArrayList<>());
        return captureMoves;
    }

    private void findCaptures(int fromX, int fromY, Piece[][] grid, List<int[]> captureMoves, String gameMode, List<int[]> path) {
        int[] directions = {1, -1};
        boolean isKingAtStart = isKing;
        // Check for promotion at current position
        if (!isKing && ((color.equals("White") && fromX == 0) || (color.equals("Black") && fromX == 7))) {
            isKing = true;
        }

        for (int dx : directions) {
            for (int dy : directions) {
                if (!isKing) {
                    // Non-kings: Capture forward or backward, two squares
                    int midX = fromX + dx;
                    int midY = fromY + dy;
                    int toX = fromX + 2 * dx;
                    int toY = fromY + 2 * dy;
                    if (toX >= 0 && toX < 8 && toY >= 0 && toY < 8 && grid[toX][toY] == null &&
                            midX >= 0 && midX < 8 && midY >= 0 && midY < 8 &&
                            grid[midX][midY] != null && !grid[midX][midY].getColor().equals(color) &&
                            isValidCaptureTarget(grid[midX][midY], gameMode)) {
                        // Simulate capture
                        Piece captured = grid[midX][midY];
                        grid[midX][midY] = null;
                        grid[toX][toY] = grid[fromX][fromY];
                        grid[fromX][fromY] = null;

                        List<int[]> newPath = new ArrayList<>(path);
                        newPath.add(new int[]{toX, toY});
                        captureMoves.add(new int[]{toX, toY});

                        // Check for promotion after this jump
                        boolean wasKing = isKing;
                        if ((color.equals("White") && toX == 0) || (color.equals("Black") && toX == 7)) {
                            isKing = true;
                        }
                        findCaptures(toX, toY, grid, captureMoves, gameMode, newPath);
                        isKing = wasKing;

                        // Undo simulation
                        grid[fromX][fromY] = grid[toX][toY];
                        grid[toX][toY] = null;
                        grid[midX][midY] = captured;
                    }
                } else {
                    // Kings: Capture any distance along a diagonal
                    for (int i = 1; i < 8; i++) {
                        int checkX = fromX + i * dx;
                        int checkY = fromY + i * dy;
                        if (checkX < 0 || checkX >= 8 || checkY < 0 || checkY >= 8) {
                            break;
                        }
                        if (grid[checkX][checkY] != null) {
                            if (grid[checkX][checkY].getColor().equals(color) || !isValidCaptureTarget(grid[checkX][checkY], gameMode)) {
                                break; // Friendly piece or invalid target
                            }
                            // Found an opponent piece to capture
                            for (int j = 1; j < 8; j++) {
                                int toX = checkX + j * dx;
                                int toY = checkY + j * dy;
                                if (toX < 0 || toX >= 8 || toY < 0 || toY >= 8 || grid[toX][toY] != null) {
                                    break; // Invalid or occupied landing square
                                }
                                // Check path between captured piece and landing square
                                boolean pathClear = true;
                                for (int k = 1; k < j; k++) {
                                    int pathX = checkX + k * dx;
                                    int pathY = checkY + k * dy;
                                    if (pathX < 0 || pathX >= 8 || pathY < 0 || pathY >= 8 || grid[pathX][pathY] != null) {
                                        pathClear = false;
                                        break;
                                    }
                                }
                                if (!pathClear) {
                                    continue;
                                }
                                // Simulate capture
                                Piece captured = grid[checkX][checkY];
                                grid[checkX][checkY] = null;
                                grid[toX][toY] = grid[fromX][fromY];
                                grid[fromX][fromY] = null;

                                List<int[]> newPath = new ArrayList<>(path);
                                newPath.add(new int[]{toX, toY});
                                captureMoves.add(new int[]{toX, toY});

                                // Recursively check for further captures
                                findCaptures(toX, toY, grid, captureMoves, gameMode, newPath);

                                // Undo simulation
                                grid[fromX][fromY] = grid[toX][toY];
                                grid[toX][toY] = null;
                                grid[checkX][checkY] = captured;
                            }
                            break; // Only one piece per jump
                        }
                    }
                }
            }
        }
        isKing = isKingAtStart; // Restore original king status
    }

    private boolean isValidCaptureTarget(Piece piece, String gameMode) {
        if (gameMode.equals("Hybrid") && !(piece instanceof CheckersPiece)) {
            return false; // In hybrid mode, checkers can only capture checkers
        }
        return true;
    }
}