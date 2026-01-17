package com.chess.rules;

import com.chess.model.Position;
import com.chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

/**
 * King movement logic.
 * Kings move one square in any direction.
 */
public class KingRules {

    public static boolean isValidMove(Position from, Position to, String team, List<Piece> boardState) {
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());
        
        // King moves 1 square in any direction
        if (dx > 1 || dy > 1 || (dx == 0 && dy == 0)) {
            return false;
        }
        
        return GeneralRules.tileIsEmptyOrOccupiedByOpponent(to, boardState, team);
    }

    public static List<Position> getPossibleMoves(Position from, String team, List<Piece> boardState) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {
            {1, 0}, {-1, 0}, {0, 1}, {0, -1},
            {1, 1}, {1, -1}, {-1, 1}, {-1, -1}
        };
        
        for (int[] dir : directions) {
            int x = from.getX() + dir[0];
            int y = from.getY() + dir[1];
            
            if (x >= 0 && x <= 7 && y >= 0 && y <= 7) {
                Position dest = new Position(x, y);
                if (!GeneralRules.tileIsOccupied(dest, boardState) || 
                    GeneralRules.tileIsOccupiedByOpponent(dest, boardState, team)) {
                    moves.add(dest);
                }
            }
        }
        
        return moves;
    }
}
