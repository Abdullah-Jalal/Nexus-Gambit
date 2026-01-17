package com.chess.controller;

import com.chess.model.Position;
import com.chess.model.Piece;
import com.chess.rules.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Chess API Controller.
 * Single unified endpoint: /api/chess/analyze
 * Returns both valid moves AND possible moves in one call for speed.
 */
@RestController
@RequestMapping("/api/chess")
@CrossOrigin(origins = "*")
public class ChessController {

    /**
     * UNIFIED ENDPOINT: Validates a move AND returns all possible moves
     * This single call is faster than making 2 separate API calls
     * 
     * Request body: {
     *   from: {x, y},
     *   to: {x, y},
     *   piece: {position, type, team},
     *   boardState: [...],
     *   totalMoves: number
     * }
     */
    @PostMapping("/analyze")
    public Map<String, Object> analyzeMove(@RequestBody Map<String, Object> request) {
        try {
            // Parse position and move
            Map<String, Integer> fromPos = (Map<String, Integer>) request.get("from");
            Map<String, Integer> toPos = (Map<String, Integer>) request.get("to");
            Map<String, Object> pieceData = (Map<String, Object>) request.get("piece");
            List<Map<String, Object>> boardStateData = (List<Map<String, Object>>) request.get("boardState");
            
            int totalMoves = ((Number) request.getOrDefault("totalMoves", 0)).intValue();

            Position from = new Position(fromPos.get("x"), fromPos.get("y"));
            Position to = new Position(toPos.get("x"), toPos.get("y"));
            String pieceType = (String) pieceData.get("type");
            String team = (String) pieceData.get("team");

            // Convert board state
            List<Piece> pieces = convertBoardState(boardStateData);

            // VALIDATE MOVE
            boolean isValid = validateMove(from, to, pieceType, team, pieces);

            // GET ALL POSSIBLE MOVES for this piece
            List<Position> possibleMoves = getPossibleMoves(from, pieceType, team, pieces);

            // Simulate the move to check if opponent is in checkmate after this move
            List<Piece> boardAfterMove = simulateMove(pieces, from, to, pieceType, team);
            
            String opponentTeam = team.equals("w") ? "b" : "w";
            
            boolean isCheckmate = CheckmateDetector.isCheckmate(opponentTeam, boardAfterMove, totalMoves);
            boolean isStalemate = CheckmateDetector.isStalemate(opponentTeam, boardAfterMove, totalMoves);
            boolean isCheck = CheckmateDetector.isKingInCheck(opponentTeam, boardAfterMove);

            System.out.println("[DEBUG] Move: " + team + " from (" + from.getX() + "," + from.getY() + ") to (" + to.getX() + "," + to.getY() + ")");
            System.out.println("[DEBUG] Total moves: " + totalMoves + ", Opponent team: " + opponentTeam + ", isCheckmate: " + isCheckmate + ", isCheck: " + isCheck);
            System.out.println("[DEBUG] Board pieces after move: " + boardAfterMove.size());
            
            String winningTeam = isCheckmate ? team : null;

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("possibleMoves", possibleMoves);
            response.put("from", Map.of("x", from.getX(), "y", from.getY()));
            response.put("to", Map.of("x", to.getX(), "y", to.getY()));
            response.put("piece", Map.of("type", pieceType, "team", team));
            response.put("isCheckmate", isCheckmate);
            response.put("isStalemate", isStalemate);
            response.put("isCheck", isCheck);
            response.put("winningTeam", winningTeam);

            return response;

        } catch (Exception e) {
            return Map.of(
                    "valid", false,
                    "possibleMoves", List.of(),
                    "error", e.getMessage()
            );
        }
    }

    /**
     * Validates if a move is legal for a specific piece
     */
    private boolean validateMove(Position from, Position to, String pieceType, String team, List<Piece> pieces) {
        // Can't move to same position
        if (from.samePosition(to)) {
            return false;
        }

        // Route to appropriate piece rules
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

    /**
     * Gets all possible moves for a piece
     */
    private List<Position> getPossibleMoves(Position from, String pieceType, String team, List<Piece> pieces) {
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
                return new ArrayList<>();
        }
    }

    /**
     * Helper: Convert board state from JSON to Piece objects
     */
    private List<Piece> convertBoardState(List<Map<String, Object>> boardStateData) {
        List<Piece> pieces = new ArrayList<>();
        for (Map<String, Object> p : boardStateData) {
            Map<String, Integer> pos = (Map<String, Integer>) p.get("position");
            Position position = new Position(pos.get("x"), pos.get("y"));
            String type = (String) p.get("type");
            String team = (String) p.get("team");
            boolean hasMoved = (Boolean) p.getOrDefault("hasMoved", false);

            pieces.add(new Piece(position, type, team, hasMoved));
        }
        return pieces;
    }

    private List<Piece> simulateMove(List<Piece> originalPieces, Position from, Position to, String pieceType, String team) {
        List<Piece> simulated = new ArrayList<>();
        
        for (Piece p : originalPieces) {
            Piece copy = new Piece(
                new Position(p.getPosition().getX(), p.getPosition().getY()),
                p.getType(),
                p.getTeam(),
                p.isHasMoved()
            );
            simulated.add(copy);
        }

        // Remove piece at destination if exists (capture)
        simulated.removeIf(p -> p.getPosition().getX() == to.getX() && p.getPosition().getY() == to.getY());

        // Move the piece
        for (Piece p : simulated) {
            if (p.getPosition().getX() == from.getX() && p.getPosition().getY() == from.getY()) {
                p.setPosition(new Position(to.getX(), to.getY()));
                p.setHasMoved(true);
                break;
            }
        }

        return simulated;
    }
}
