package org.sillylabs;

import org.sillylabs.pieces.*;

public class Board {
    private final Piece[][] grid;
    private static final int BOARD_SIZE = 8;

    public Board() {
        grid = new Piece[BOARD_SIZE][BOARD_SIZE];
    }

    public void setupBoard(GameMode mode) {
        for (int row = 0; row < BOARD_SIZE; row++) {
            for (int col = 0; col < BOARD_SIZE; col++) {
                grid[row][col] = null;
            }
        }
        switch (mode) {
            case CHESS:
                setupChess();
                break;
            case CHECKERS:
                setupCheckers();
                break;
        }
    }

    private void setupChess() {
        // Чорні зверху (рядок 0 - фігури, рядок 1 - пішаки)
        grid[0][0] = new Rook(Color.BLACK, 0, 0);
        grid[0][1] = new Knight(Color.BLACK, 0, 1);
        grid[0][2] = new Bishop(Color.BLACK, 0, 2);
        grid[0][3] = new Queen(Color.BLACK, 0, 3);
        grid[0][4] = new King(Color.BLACK, 0, 4);
        grid[0][5] = new Bishop(Color.BLACK, 0, 5);
        grid[0][6] = new Knight(Color.BLACK, 0, 6);
        grid[0][7] = new Rook(Color.BLACK, 0, 7);
        for (int i = 0; i < BOARD_SIZE; i++) {
            grid[1][i] = new Pawn(Color.BLACK, 1, i);
        }

        // Білі знизу (рядок 7 - фігури, рядок 6 - пішаки)
        grid[7][0] = new Rook(Color.WHITE, 7, 0);
        grid[7][1] = new Knight(Color.WHITE, 7, 1);
        grid[7][2] = new Bishop(Color.WHITE, 7, 2);
        grid[7][3] = new Queen(Color.WHITE, 7, 3);
        grid[7][4] = new King(Color.WHITE, 7, 4);
        grid[7][5] = new Bishop(Color.WHITE, 7, 5);
        grid[7][6] = new Knight(Color.WHITE, 7, 6);
        grid[7][7] = new Rook(Color.WHITE, 7, 7);
        for (int i = 0; i < BOARD_SIZE; i++) {
            grid[6][i] = new Pawn(Color.WHITE, 6, i);
        }
    }

    private void setupCheckers() {
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                if ((row + column) % 2 == 1) {
                    grid[row][column] = new CheckersMan(Color.BLACK, row, column);
                }
            }
        }
        for (int row = 5; row < BOARD_SIZE; row++) {
            for (int column = 0; column < BOARD_SIZE; column++) {
                if ((row + column) % 2 == 1) {
                    grid[row][column] = new CheckersMan(Color.WHITE, row, column);
                }
            }
        }
    }

    public Piece getPieceAt(int row, int column) {
        return grid[row][column];
    }

    public void setPieceAt(int row, int column, Piece piece) {
        grid[row][column] = piece;
        if (piece != null) {
            piece.setPosition(row, column);
        }
    }

    public Piece[][] getGrid() {
        Piece[][] copy = new Piece[BOARD_SIZE][BOARD_SIZE];
        for (int i = 0; i < BOARD_SIZE; i++) {
            System.arraycopy(grid[i], 0, copy[i], 0, BOARD_SIZE);
        }
        return copy;
    }
}