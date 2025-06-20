package org.sillylabs.pieces;

public abstract class Piece {
    protected String type;
    protected Color color;
    protected int row;
    protected int column;

    public Piece(String type, Color color, int row, int column) {
        this.type = type;
        this.color = color;
        this.row = row;
        this.column = column;
    }

    public String getType() {
        return type;
    }

    public Color getColor() {
        return color;
    }

    public int getRow() {
        return row;
    }

    public int getColumn() {
        return column;
    }

    public void setPosition(int row, int column) {
        this.row = row;
        this.column = column;
    }

    public abstract boolean isValidMove(int toRow, int toColumn, Piece[][] board);

    protected boolean isWithinBoard(int row, int column) {
        return row >= 0 && row < 8 && column >= 0 && column < 8;
    }
}