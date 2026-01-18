package com.chess.rules;

import com.chess.model.Piece;
import com.chess.model.Position;
import java.util.List;
import java.util.ArrayList;

public class CheckmateDetector {

    // --- 1. IS KING IN CHECK? ---
    public static boolean isKingInCheck(String team, List<Piece> boardState) {
        Position kingPos = findKingPosition(team, boardState);
        if (kingPos == null) return false;

        String opponentTeam = team.toLowerCase().startsWith("w") ? "b" : "w";
        
        for (Piece p : boardState) {
            if (GeneralRules.isSameTeam(p.getTeam(), opponentTeam)) {
                // Special check for Pawns (capture logic only)
                if (p.getType().equalsIgnoreCase("pawn")) {
                    if (PawnRules.canCapture(p.getPosition(), kingPos, p.getTeam(), boardState)) return true;
                } else {
                    // Standard rules for other pieces
                    if (canPieceAttackSquare(p, kingPos, boardState)) return true;
                }
            }
        }
        return false;
    }

    // --- 2. IS CHECKMATE? (Check + No Escape) ---
    public static boolean isCheckmate(String team, List<Piece> boardState, int totalMoves) {
        if (!isKingInCheck(team, boardState)) return false;
        return !hasLegalMoves(team, boardState);
    }

    // --- 3. IS STALEMATE? (No Check + No Moves) ---
    public static boolean isStalemate(String team, List<Piece> boardState, int totalMoves) {
        if (isKingInCheck(team, boardState)) return false;
        return !hasLegalMoves(team, boardState);
    }

    // --- HELPERS ---
    
    private static boolean hasLegalMoves(String team, List<Piece> boardState) {
        for (Piece p : boardState) {
            if (GeneralRules.isSameTeam(p.getTeam(), team)) {
                // Try every possible geometric move
                List<Position> candidates = getAllPotentialMoves(p, boardState);
                for (Position dest : candidates) {
                    // Simulate move
                    List<Piece> simBoard = simulateMove(boardState, p.getPosition(), dest);
                    // If King is SAFE after simulation, we have at least 1 legal move
                    if (!isKingInCheck(team, simBoard)) return true;
                }
            }
        }
        return false;
    }

    private static Position findKingPosition(String team, List<Piece> boardState) {
        for (Piece p : boardState) {
            if (p.getType().equalsIgnoreCase("king") && GeneralRules.isSameTeam(p.getTeam(), team)) {
                return p.getPosition();
            }
        }
        return null;
    }

    private static boolean canPieceAttackSquare(Piece attacker, Position target, List<Piece> boardState) {
        Position from = attacker.getPosition();
        String team = attacker.getTeam();
        switch (attacker.getType().toLowerCase()) {
            case "rook": return RookRules.isValidMove(from, target, team, boardState);
            case "knight": return KnightRules.isValidMove(from, target, team, boardState);
            case "bishop": return BishopRules.isValidMove(from, target, team, boardState);
            case "queen": return QueenRules.isValidMove(from, target, team, boardState);
            case "king": return KingRules.isValidMove(from, target, team, boardState);
            default: return false;
        }
    }

    private static List<Position> getAllPotentialMoves(Piece p, List<Piece> boardState) {
        Position from = p.getPosition();
        String team = p.getTeam();
        switch (p.getType().toLowerCase()) {
            case "pawn": return PawnRules.getPossibleMoves(from, team, boardState);
            case "rook": return RookRules.getPossibleMoves(from, team, boardState);
            case "knight": return KnightRules.getPossibleMoves(from, team, boardState);
            case "bishop": return BishopRules.getPossibleMoves(from, team, boardState);
            case "queen": return QueenRules.getPossibleMoves(from, team, boardState);
            case "king": return KingRules.getPossibleMoves(from, team, boardState);
            default: return new ArrayList<>();
        }
    }

    private static List<Piece> simulateMove(List<Piece> original, Position from, Position to) {
        List<Piece> sim = new ArrayList<>();
        for (Piece p : original) {
            sim.add(new Piece(new Position(p.getPosition().getX(), p.getPosition().getY()), 
                              p.getType(), p.getTeam(), p.isHasMoved()));
        }
        sim.removeIf(p -> p.getPosition().getX() == to.getX() && p.getPosition().getY() == to.getY());
        for (Piece p : sim) {
            if (p.getPosition().getX() == from.getX() && p.getPosition().getY() == from.getY()) {
                p.setPosition(new Position(to.getX(), to.getY()));
                break;
            }
        }
        return sim;
    }
}