package com.chess.rules;

import com.chess.model.Position;
import com.chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

public class KingRules {

    public static boolean isValidMove(Position from, Position to, String team, List<Piece> boardState) {
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());

        // 1. Geometric Check (1 square radius)
        if (dx > 1 || dy > 1) return false;
        if (dx == 0 && dy == 0) return false;

        // 2. King Separation (Cannot touch enemy king)
        if (isNearEnemyKing(to, team, boardState)) return false;

        // 3. Occupancy Check
        return GeneralRules.tileIsEmptyOrOccupiedByOpponent(to, boardState, team);
    }

    public static List<Position> getPossibleMoves(Position from, String team, List<Piece> boardState) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {
            {0, 1}, {0, -1}, {1, 0}, {-1, 0}, 
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };

        for (int[] dir : directions) {
            Position dest = new Position(from.getX() + dir[0], from.getY() + dir[1]);
            if (dest.getX() >= 0 && dest.getX() <= 7 && dest.getY() >= 0 && dest.getY() <= 7) {
                if (isValidMove(from, dest, team, boardState)) {
                    moves.add(dest);
                }
            }
        }
        return moves;
    }

    private static boolean isNearEnemyKing(Position target, String myTeam, List<Piece> boardState) {
        for (Piece p : boardState) {
            if (p.getType().equalsIgnoreCase("king") && !GeneralRules.isSameTeam(p.getTeam(), myTeam)) {
                int dx = Math.abs(p.getPosition().getX() - target.getX());
                int dy = Math.abs(p.getPosition().getY() - target.getY());
                if (dx <= 1 && dy <= 1) return true;
            }
        }
        return false;
    }
}