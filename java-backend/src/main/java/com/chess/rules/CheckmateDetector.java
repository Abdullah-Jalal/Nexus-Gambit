package com.chess.rules;

import com.chess.model.Position;
import com.chess.model.Piece;

import java.util.ArrayList;
import java.util.List;

/**
 * Detects if a side is in checkmate, stalemate, or check.
 * Checkmate = King is in check AND has no legal moves
 * Stalemate = King is NOT in check BUT has no legal moves
 */
public class CheckmateDetector {

    /**
     * Checks if the given team is in checkmate
     * IMPORTANT: Checkmate requires MINIMUM 4 moves (fool's mate)
     */
    public static boolean isCheckmate(String team, List<Piece> pieces, int totalMoves) {
        if (totalMoves < 4) {
            return false;
        }
        return isKingInCheck(team, pieces) && hasNoLegalMoves(team, pieces);
    }

    /**
     * Checks if the given team is in stalemate
     */
    public static boolean isStalemate(String team, List<Piece> pieces, int totalMoves) {
        if (totalMoves < 4) {
            return false;
        }
        return !isKingInCheck(team, pieces) && hasNoLegalMoves(team, pieces);
    }

    /**
     * Fixed: Now properly checks if opponent pieces can attack the king, including pawn captures
     */
    public static boolean isKingInCheck(String team, List<Piece> pieces) {
        // Find the king of this team
        Piece king = pieces.stream()
                .filter(p -> p.getType().equalsIgnoreCase("KING") && p.getTeam().equals(team))
                .findFirst()
                .orElse(null);

        if (king == null) {
            return false;
        }

        // Check if any opponent piece can attack the king
        String opponentTeam = team.equals("w") ? "b" : "w";
        Position kingPos = king.getPosition();
        
        for (Piece piece : pieces) {
            if (!piece.getTeam().equals(opponentTeam)) {
                continue;
            }

            // Special handling for pawns - they have unique capture logic
            if (piece.getType().equalsIgnoreCase("PAWN")) {
                if (PawnRules.canCapture(piece.getPosition(), kingPos, opponentTeam, pieces)) {
                    return true;
                }
            } else {
                // For other pieces, check if they can move to the king's position
                if (isValidPieceMove(piece.getPosition(), kingPos, piece.getType(), opponentTeam, pieces)) {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * Checks if the given team has no legal moves
     * FIXED: Now properly validates that moves don't leave king in check
     */
    private static boolean hasNoLegalMoves(String team, List<Piece> pieces) {
        for (Piece piece : pieces) {
            if (!piece.getTeam().equals(team)) continue;

            List<Position> moves = getPossibleMovesForPiece(piece.getPosition(), piece.getType(), team, pieces);

            for (Position move : moves) {
                List<Piece> simulated = simulateMoveForCheck(pieces, piece.getPosition(), move);

                if (!isKingInCheck(team, simulated)) {
                    return false; // legal move exists
                }
            }
        }
        return true; // no legal moves found
    }

    private static List<Piece> simulateMoveForCheck(
            List<Piece> original,
            Position from,
            Position to) {

        List<Piece> copy = new ArrayList<>();

        for (Piece p : original) {
            copy.add(new Piece(
                new Position(p.getPosition().getX(),
                             p.getPosition().getY()),
                p.getType(),
                p.getTeam(),
                p.isHasMoved()
            ));
        }

        // Remove piece at destination if exists (capture)
        copy.removeIf(p ->
            p.getPosition().getX() == to.getX() &&
            p.getPosition().getY() == to.getY()
        );

        // Move the piece
        for (Piece p : copy) {
            if (p.getPosition().samePosition(from)) {
                p.setPosition(to);
                break;
            }
        }

        return copy;
    }

    /**
     * Helper: Get possible moves for a specific piece
     */
    private static List<Position> getPossibleMovesForPiece(Position from, String pieceType, String team, List<Piece> pieces) {
        switch (pieceType.toLowerCase()) {
            case "pawn":
                return PawnRules.getPossibleMoves(from, team, pieces);
            case "rook":
                return RookRules.getPossibleMoves(from, team, pieces);
            case "knight":
                return KnightRules.getPossibleMoves(from, team, pieces);
            case "bishop":
                return BishopRules.getPossibleMoves(from, team, pieces);
            case "queen":
                return QueenRules.getPossibleMoves(from, team, pieces);
            case "king":
                return KingRules.getPossibleMoves(from, team, pieces);
            default:
                return List.of();
        }
    }

    /**
     * Helper: Check if a specific move is valid
     */
    private static boolean isValidPieceMove(Position from, Position to, String pieceType, String team, List<Piece> pieces) {
        switch (pieceType.toLowerCase()) {
            case "pawn":
                return PawnRules.isValidMove(from, to, team, pieces);
            case "rook":
                return RookRules.isValidMove(from, to, team, pieces);
            case "knight":
                return KnightRules.isValidMove(from, to, team, pieces);
            case "bishop":
                return BishopRules.isValidMove(from, to, team, pieces);
            case "queen":
                return QueenRules.isValidMove(from, to, team, pieces);
            case "king":
                return KingRules.isValidMove(from, to, team, pieces);
            default:
                return false;
        }
    }
}
