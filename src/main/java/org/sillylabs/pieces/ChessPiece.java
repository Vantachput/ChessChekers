package org.sillylabs.pieces;

public abstract class ChessPiece extends Piece {
    public ChessPiece(String type, Color color, int row, int column) {
        super(type, color, row, column);
    }
}