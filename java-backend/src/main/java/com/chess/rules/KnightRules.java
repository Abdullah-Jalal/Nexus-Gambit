package com.chess.rules;

import com.chess.model.Position;
import com.chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

public class KnightRules {

    public static boolean isValidMove(Position from, Position to, String team, List<Piece> boardState) {
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());
        
        // Check L-Shape: (2,1) or (1,2)
        if (!((dx == 2 && dy == 1) || (dx == 1 && dy == 2))) return false;

        return GeneralRules.tileIsEmptyOrOccupiedByOpponent(to, boardState, team);
    }

    public static List<Position> getPossibleMoves(Position from, String team, List<Piece> boardState) {
        List<Position> moves = new ArrayList<>();
        int[][] jumps = {{2, 1}, {2, -1}, {-2, 1}, {-2, -1}, {1, 2}, {1, -2}, {-1, 2}, {-1, -2}};

        for (int[] jump : jumps) {
            Position dest = new Position(from.getX() + jump[0], from.getY() + jump[1]);
            if (dest.getX() >= 0 && dest.getX() <= 7 && dest.getY() >= 0 && dest.getY() <= 7) {
                if (isValidMove(from, dest, team, boardState)) moves.add(dest);
            }
        }
        return moves;
    }
}