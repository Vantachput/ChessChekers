package org.sillylabs;

public abstract class CheckersPiece extends Piece {
    protected boolean isKing;

    public CheckersPiece(String type, String color, int x, int y) {
        super(type, color, x, y);
        isKing = false;
    }

    public boolean isKing() {
        return isKing;
    }

    public void setKing(boolean king) {
        isKing = king;
    }
}
