package com.chess.rules;

import com.chess.model.Position;
import com.chess.model.Piece;
import java.util.ArrayList;
import java.util.List;

public class PawnRules {

    public static boolean isValidMove(Position from, Position to, String team, List<Piece> boardState) {
        // 1. Determine Direction
        // White moves UP (+1), Black moves DOWN (-1)
        int direction = team.equalsIgnoreCase("w") ? 1 : -1;
        
        int dy = to.getY() - from.getY();
        int dx = Math.abs(to.getX() - from.getX());
        
        // Quick check: Cannot move backwards or sideways only
        if (dy * direction <= 0) return false;

        // --- SCENARIO 1: FORWARD MOVE (Non-Capture) ---
        if (dx == 0) {
            // Move 1 Square Forward
            if (dy == direction) {
                // Rule: Destination must be empty
                return !isSquareOccupied(to, boardState);
            }

            // Move 2 Squares Forward (First move only)
            if (dy == 2 * direction) {
                // Rule: Path and Destination must be empty
                Position pathSquare = new Position(from.getX(), from.getY() + direction);
                if (isSquareOccupied(pathSquare, boardState)) return false;
                if (isSquareOccupied(to, boardState)) return false;

                // Rule: Piece must not have moved yet
                Piece pawn = getPieceAt(from, boardState);
                return pawn != null && !pawn.isHasMoved();
            }
        }

        // --- SCENARIO 2: DIAGONAL CAPTURE ---
        if (dx == 1 && dy == direction) {
            // Rule: Must be capturing an opponent
            return isSquareOccupiedByOpponent(to, boardState, team);
        }

        return false;
    }

    /**
     * Checks if a pawn CAN capture a specific square (used for Checkmate detection).
     * This checks if the move is a valid diagonal attack, regardless of whose turn it is.
     */
    public static boolean canCapture(Position from, Position to, String team, List<Piece> boardState) {
        int direction = team.equalsIgnoreCase("w") ? 1 : -1;
        int dx = Math.abs(to.getX() - from.getX());
        int dy = to.getY() - from.getY();

        // A pawn "can capture" if the target is 1 step diagonally forward
        // and occupied by an opponent (e.g., the King).
        if (dx == 1 && dy == direction) {
            return isSquareOccupiedByOpponent(to, boardState, team);
        }
        return false;
    }

    public static List<Position> getPossibleMoves(Position from, String team, List<Piece> boardState) {
        List<Position> moves = new ArrayList<>();
        int direction = team.equalsIgnoreCase("w") ? 1 : -1;

        // 1. Move Forward 1
        Position forward1 = new Position(from.getX(), from.getY() + direction);
        if (isValidMove(from, forward1, team, boardState)) {
            moves.add(forward1);
        }

        // 2. Move Forward 2
        Position forward2 = new Position(from.getX(), from.getY() + 2 * direction);
        if (isValidMove(from, forward2, team, boardState)) {
            moves.add(forward2);
        }

        // 3. Capture Diagonals (Left & Right)
        int[] captureOffsets = {-1, 1};
        for (int offset : captureOffsets) {
            Position capturePos = new Position(from.getX() + offset, from.getY() + direction);
            if (isValidMove(from, capturePos, team, boardState)) {
                moves.add(capturePos);
            }
        }

        return moves;
    }

    // --- HELPER METHODS ---

    private static boolean isSquareOccupied(Position pos, List<Piece> boardState) {
        return boardState.stream().anyMatch(p -> p.getPosition().samePosition(pos));
    }

    private static boolean isSquareOccupiedByOpponent(Position pos, List<Piece> boardState, String currentTeam) {
        Piece p = getPieceAt(pos, boardState);
        if (p == null) return false;
        // Check if teams are different (handling "w" vs "white" safely)
        // We compare the first letter to cover "w", "white", "W", "White"
        String pTeam = p.getTeam().toLowerCase().substring(0, 1);
        String myTeam = currentTeam.toLowerCase().substring(0, 1);
        return !pTeam.equals(myTeam);
    }

    private static Piece getPieceAt(Position pos, List<Piece> boardState) {
        return boardState.stream()
                .filter(p -> p.getPosition().samePosition(pos))
                .findFirst()
                .orElse(null);
    }
}