package com.chess.rules;

import com.chess.model.Position;
import com.chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

public class BishopRules {

    public static boolean isValidMove(Position from, Position to, String team, List<Piece> boardState) {
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());
        if (dx != dy) return false;
        if (isPathBlocked(from, to, boardState)) return false;
        return GeneralRules.tileIsEmptyOrOccupiedByOpponent(to, boardState, team);
    }

    public static List<Position> getPossibleMoves(Position from, String team, List<Piece> boardState) {
        return getSlidingMoves(from, team, boardState, new int[][]{{1,1}, {1,-1}, {-1,1}, {-1,-1}});
    }

    // Shared Helper for Sliding Pieces
    protected static List<Position> getSlidingMoves(Position from, String team, List<Piece> boardState, int[][] directions) {
        List<Position> moves = new ArrayList<>();
        for (int[] dir : directions) {
            for (int i = 1; i < 8; i++) {
                int x = from.getX() + (dir[0] * i);
                int y = from.getY() + (dir[1] * i);
                if (x < 0 || x > 7 || y < 0 || y > 7) break;
                
                Position dest = new Position(x, y);
                if (GeneralRules.tileIsOccupied(dest, boardState)) {
                    if (GeneralRules.tileIsOccupiedByOpponent(dest, boardState, team)) moves.add(dest);
                    break;
                }
                moves.add(dest);
            }
        }
        return moves;
    }

    private static boolean isPathBlocked(Position from, Position to, List<Piece> boardState) {
        int dx = Integer.compare(to.getX(), from.getX());
        int dy = Integer.compare(to.getY(), from.getY());
        int currX = from.getX() + dx;
        int currY = from.getY() + dy;
        while (currX != to.getX()) {
            if (GeneralRules.tileIsOccupied(new Position(currX, currY), boardState)) return true;
            currX += dx;
            currY += dy;
        }
        return false;
    }
}