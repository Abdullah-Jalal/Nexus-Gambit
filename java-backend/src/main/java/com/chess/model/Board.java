package com.chess.model;

import java.util.List;
import java.util.ArrayList;

/**
 * Represents the chess board state with all pieces.
 * This is the core model for managing board state.
 */
public class Board {
    private List<Piece> pieces;
    
    public Board(List<Piece> pieces) {
        this.pieces = new ArrayList<>(pieces);
    }
    
    public List<Piece> getPieces() {
        return pieces;
    }
    
    public void setPieces(List<Piece> pieces) {
        this.pieces = pieces;
    }
    
    public Board clone() {
        return new Board(new ArrayList<>(pieces.stream().map(Piece::clone).toList()));
    }
}
