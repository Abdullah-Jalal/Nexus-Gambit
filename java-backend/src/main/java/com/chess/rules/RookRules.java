package com.chess.rules;

import com.chess.model.Position;
import com.chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

public class RookRules {

    public static boolean isValidMove(Position from, Position to, String team, List<Piece> boardState) {
        if (from.getX() != to.getX() && from.getY() != to.getY()) return false;
        if (isPathBlocked(from, to, boardState)) return false;
        return GeneralRules.tileIsEmptyOrOccupiedByOpponent(to, boardState, team);
    }

    public static List<Position> getPossibleMoves(Position from, String team, List<Piece> boardState) {
        // Reuse sliding logic from BishopRules to save code, or implement manually
        return BishopRules.getSlidingMoves(from, team, boardState, new int[][]{{0,1}, {0,-1}, {1,0}, {-1,0}});
    }

    private static boolean isPathBlocked(Position from, Position to, List<Piece> boardState) {
        int dx = Integer.compare(to.getX(), from.getX());
        int dy = Integer.compare(to.getY(), from.getY());
        int currX = from.getX() + dx;
        int currY = from.getY() + dy;

        while (currX != to.getX() || currY != to.getY()) {
            if (GeneralRules.tileIsOccupied(new Position(currX, currY), boardState)) return true;
            currX += dx;
            currY += dy;
        }
        return false;
    }
}