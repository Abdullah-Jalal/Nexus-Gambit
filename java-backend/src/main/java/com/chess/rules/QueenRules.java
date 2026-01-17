package com.chess.rules;

import com.chess.model.Position;
import com.chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

/**
 * Queen movement logic.
 * Queens move like rooks (straight) or bishops (diagonal).
 */
public class QueenRules {

    public static boolean isValidMove(Position from, Position to, String team, List<Piece> boardState) {
        // Queen can move like a rook or bishop
        return RookRules.isValidMove(from, to, team, boardState) || 
               BishopRules.isValidMove(from, to, team, boardState);
    }

    public static List<Position> getPossibleMoves(Position from, String team, List<Piece> boardState) {
        List<Position> moves = new ArrayList<>();
        moves.addAll(RookRules.getPossibleMoves(from, team, boardState));
        moves.addAll(BishopRules.getPossibleMoves(from, team, boardState));
        return moves;
    }
}
