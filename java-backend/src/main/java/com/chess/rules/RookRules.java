package com.chess.rules;

import com.chess.model.Position;
import com.chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

/**
 * Rook movement logic.
 * Rooks move horizontally or vertically any number of squares.
 */
public class RookRules {

    public static boolean isValidMove(Position from, Position to, String team, List<Piece> boardState) {
        // Rook moves in straight lines (same row or same column)
        boolean sameRow = from.getY() == to.getY();
        boolean sameCol = from.getX() == to.getX();
        
        if (!sameRow && !sameCol) {
            return false;
        }
        
        if (sameRow && sameCol) {
            return false;  // Can't move to same position
        }
        
        // Check if path is clear
        return isPathClear(from, to, boardState) && 
               GeneralRules.tileIsEmptyOrOccupiedByOpponent(to, boardState, team);
    }

    public static List<Position> getPossibleMoves(Position from, String team, List<Piece> boardState) {
        List<Position> moves = new ArrayList<>();
        int[][] directions = {{1, 0}, {-1, 0}, {0, 1}, {0, -1}};  // Right, Left, Down, Up
        
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
    
    private static boolean isPathClear(Position from, Position to, List<Piece> boardState) {
        int stepX = Integer.compare(to.getX(), from.getX());
        int stepY = Integer.compare(to.getY(), from.getY());
        
        int x = from.getX() + stepX;
        int y = from.getY() + stepY;
        
        while (x != to.getX() || y != to.getY()) {
            if (GeneralRules.tileIsOccupied(new Position(x, y), boardState)) {
                return false;
            }
            x += stepX;
            y += stepY;
        }
        
        return true;
    }
}
