package com.chess.rules;

import com.chess.model.Position;
import com.chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

public class QueenRules {
    public static boolean isValidMove(Position from, Position to, String team, List<Piece> boardState) {
        boolean straight = (from.getX() == to.getX() || from.getY() == to.getY());
        boolean diagonal = (Math.abs(to.getX() - from.getX()) == Math.abs(to.getY() - from.getY()));

        if (!straight && !diagonal) return false;
        if (straight) return RookRules.isValidMove(from, to, team, boardState);
        return BishopRules.isValidMove(from, to, team, boardState);
    }

    public static List<Position> getPossibleMoves(Position from, String team, List<Piece> boardState) {
        List<Position> moves = new ArrayList<>();
        moves.addAll(RookRules.getPossibleMoves(from, team, boardState));
        moves.addAll(BishopRules.getPossibleMoves(from, team, boardState));
        return moves;
    }
}