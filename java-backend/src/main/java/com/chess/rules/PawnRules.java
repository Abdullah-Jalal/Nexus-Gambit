package com.chess.rules;

import com.chess.model.Position;
import com.chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

public class PawnRules {

    public static boolean isValidMove(Position from, Position to, String team, List<Piece> boardState) {
        int direction = team.toLowerCase().startsWith("w") ? 1 : -1;
        int dy = to.getY() - from.getY();
        int dx = Math.abs(to.getX() - from.getX());

        // --- 1. FORWARD MOVES ---
        if (dx == 0) {
            // A. Single Step
            if (dy == direction) {
                return !GeneralRules.tileIsOccupied(to, boardState);
            }
            // B. Double Step (First move only)
            if (dy == 2 * direction) {
                Position pathSquare = new Position(from.getX(), from.getY() + direction);
                // Path must be clear AND Dest must be clear
                if (GeneralRules.tileIsOccupied(pathSquare, boardState)) return false;
                if (GeneralRules.tileIsOccupied(to, boardState)) return false;
                
                Piece pawn = getPieceAt(from, boardState);
                return pawn != null && !pawn.isHasMoved();
            }
        }

        // --- 2. DIAGONAL CAPTURE ---
        if (dx == 1 && dy == direction) {
            return GeneralRules.tileIsOccupiedByOpponent(to, boardState, team);
        }

        return false;
    }

    // Helper for CheckmateDetector: Can this pawn ATTACK this square?
    public static boolean canCapture(Position from, Position to, String team, List<Piece> boardState) {
        int direction = team.toLowerCase().startsWith("w") ? 1 : -1;
        int dx = Math.abs(to.getX() - from.getX());
        int dy = to.getY() - from.getY();
        return (dx == 1 && dy == direction);
    }

    // Helper for UI Green Dots
    public static List<Position> getPossibleMoves(Position from, String team, List<Piece> boardState) {
        List<Position> moves = new ArrayList<>();
        int direction = team.toLowerCase().startsWith("w") ? 1 : -1;

        // Forward 1
        Position f1 = new Position(from.getX(), from.getY() + direction);
        if (isValidMove(from, f1, team, boardState)) moves.add(f1);

        // Forward 2
        Position f2 = new Position(from.getX(), from.getY() + 2 * direction);
        if (isValidMove(from, f2, team, boardState)) moves.add(f2);

        // Captures
        int[] offsets = {-1, 1};
        for (int o : offsets) {
            Position cap = new Position(from.getX() + o, from.getY() + direction);
            if (isValidMove(from, cap, team, boardState)) moves.add(cap);
        }
        return moves;
    }
    
    private static Piece getPieceAt(Position pos, List<Piece> list) {
        return list.stream().filter(p -> p.getPosition().samePosition(pos)).findFirst().orElse(null);
    }
}