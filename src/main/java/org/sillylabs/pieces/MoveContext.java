package org.sillylabs.pieces;

public class MoveContext {
    public final Piece[][] grid;
    public final int enPassantTargetRow;
    public final int enPassantTargetColumn;
    public final boolean isEnPassantPossible;

    public MoveContext(Piece[][] grid, int enPassantTargetRow, int enPassantTargetColumn, boolean isEnPassantPossible) {
        this.grid = grid;
        this.enPassantTargetRow = enPassantTargetRow;
        this.enPassantTargetColumn = enPassantTargetColumn;
        this.isEnPassantPossible = isEnPassantPossible;
    }
}