package com.chess.rules;

import com.chess.model.Position;
import com.chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

/**
 * Bishop movement logic.
 * Bishops move diagonally any number of squares.
 */
public class BishopRules {

    public static boolean isValidMove(Position from, Position to, String team, List<Piece> boardState) {
        // Bishop moves diagonally
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());

        if (dx != dy || dx == 0) return false;

        // Check path is clear
        int stepX = to.getX() > from.getX() ? 1 : -1;
        int stepY = to.getY() > from.getY() ? 1 : -1;

        int x = from.getX() + stepX;
        int y = from.getY() + stepY;

        while (x != to.getX()) {
            if (GeneralRules.tileIsOccupied(new Position(x, y), boardState)) {
                return false;
            }
            x += stepX;
            y += stepY;
        }

        return GeneralRules.tileIsEmptyOrOccupiedByOpponent(to, boardState, team);
    }

    public static List<Position> getPossibleMoves(Position from, String team, List<Piece> boardState) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{1, 1}, {1, -1}, {-1, -1}, {-1, 1}};

        for (int[] dir : directions) {
            for (int i = 1; i < 8; i++) {
                int x = from.getX() + dir[0] * i;
                int y = from.getY() + dir[1] * i;

                if (x < 0 || x > 7 || y < 0 || y > 7) break;

                Position dest = new Position(x, y);
                if (GeneralRules.tileIsOccupied(dest, boardState)) {
                    if (GeneralRules.tileIsOccupiedByOpponent(dest, boardState, team)) {
                        moves.add(dest);
                    }
                    break;
                }
                moves.add(dest);
            }
        }

        return moves;
    }
}
