package com.chess.rules;

import com.chess.model.Position;
import com.chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

/**
 * Knight movement logic.
 * Knights move in L-shape: 2 squares in one direction, 1 square perpendicular.
 * Knights jump over pieces.
 */
public class KnightRules {

    public static boolean isValidMove(Position from, Position to, String team, List<Piece> boardState) {
        int dx = Math.abs(to.getX() - from.getX());
        int dy = Math.abs(to.getY() - from.getY());
        
        // Valid knight move: (2,1) or (1,2)
        boolean validL = (dx == 2 && dy == 1) || (dx == 1 && dy == 2);
        
        if (!validL) {
            return false;
        }
        
        return GeneralRules.tileIsEmptyOrOccupiedByOpponent(to, boardState, team);
    }

    public static List<Position> getPossibleMoves(Position from, String team, List<Piece> boardState) {
        List<Position> moves = new ArrayList<>();
        int[][] knightMoves = {
            {2, 1}, {2, -1}, {-2, 1}, {-2, -1},
            {1, 2}, {1, -2}, {-1, 2}, {-1, -2}
        };
        
        for (int[] move : knightMoves) {
            int x = from.getX() + move[0];
            int y = from.getY() + move[1];
            
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
