package org.sillylabs;

public abstract class Piece {
    protected String type;
    protected String color;
    protected int x; // Координата строки
    protected int y; // Координата столбца

    public Piece(String type, String color, int x, int y) {
        this.type = type;
        this.color = color;
        this.x = x;
        this.y = y;
    }

    public String getType() {
        return type;
    }

    public String getColor() {
        return color;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public abstract boolean isValidMove(int toX, int toY, Piece[][] board);

    protected boolean isWithinBoard(int x, int y) {
        return x >= 0 && x < 8 && y >= 0 && y < 8;
    }
}