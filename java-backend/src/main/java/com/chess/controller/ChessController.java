package com.chess.controller;

import com.chess.model.Position;
import com.chess.model.Piece;
import com.chess.rules.*;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;

@RestController
@RequestMapping("/api/chess")
@CrossOrigin(origins = "*")
public class ChessController {

    @PostMapping("/analyze")
    public Map<String, Object> analyzeMove(@RequestBody Map<String, Object> request) {
        try {
            // --- 1. PARSE INPUT ---
            Map<String, Integer> fromPos = (Map<String, Integer>) request.get("from");
            Map<String, Integer> toPos = (Map<String, Integer>) request.get("to");
            Map<String, Object> pieceData = (Map<String, Object>) request.get("piece");
            List<Map<String, Object>> boardStateData = (List<Map<String, Object>>) request.get("boardState");
            
            int totalMoves = ((Number) request.getOrDefault("totalMoves", 0)).intValue();

            Position from = new Position(fromPos.get("x"), fromPos.get("y"));
            Position to = new Position(toPos.get("x"), toPos.get("y"));
            String pieceType = (String) pieceData.get("type");
            String team = (String) pieceData.get("team");

            List<Piece> pieces = convertBoardState(boardStateData);

            // --- 2. VALIDATION PHASE (Actual Move Security) ---
            // This ensures you CANNOT kill the King or commit suicide.
            
            boolean isValid = true;
            String errorReason = null;

            // A. Check Direct King Capture
            Piece targetPiece = getPieceAt(to, pieces);
            if (targetPiece != null && targetPiece.getType().equalsIgnoreCase("king")) {
                isValid = false;
                errorReason = "Cannot capture King";
            }

            // B. Check Geometry
            if (isValid && !validateMove(from, to, pieceType, team, pieces)) {
                isValid = false;
                errorReason = "Invalid move geometry";
            }

            // C. Check Suicide (King Safety)
            if (isValid) {
                List<Piece> boardAfterMove = simulateMove(pieces, from, to, pieceType, team);
                if (CheckmateDetector.isKingInCheck(team, boardAfterMove)) {
                    isValid = false;
                    errorReason = "Move puts King in check";
                }
            }

            // If move is invalid, we still want to calculate dots below (for UI refresh)
            // But we will mark the response as valid=false

            // --- 3. PREDICTION PHASE (Green Dots) ---
            // We get ALL geometric moves. We do NOT filter them for safety here.
            // This ensures the dots ALWAYS show up.
            
            List<Position> possibleMoves = getPossibleMoves(from, pieceType, team, pieces);
            
            // --- 4. GAME STATUS PHASE ---
            List<Piece> nextBoard = isValid ? simulateMove(pieces, from, to, pieceType, team) : pieces;
            String opponentTeam = team.equals("w") ? "b" : "w";
            
            boolean isCheckmate = CheckmateDetector.isCheckmate(opponentTeam, nextBoard, totalMoves);
            boolean isStalemate = CheckmateDetector.isStalemate(opponentTeam, nextBoard, totalMoves);
            boolean isCheck = CheckmateDetector.isKingInCheck(opponentTeam, nextBoard);

            String winningTeam = isCheckmate ? team : null;

            Map<String, Object> response = new HashMap<>();
            response.put("valid", isValid);
            response.put("possibleMoves", possibleMoves); // SEND ALL DOTS
            response.put("isCheckmate", isCheckmate);
            response.put("isStalemate", isStalemate);
            response.put("isCheck", isCheck);
            response.put("winningTeam", winningTeam);
            
            if (errorReason != null) {
                System.out.println("Invalid Move: " + errorReason);
            }

            return response;

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("valid", false, "possibleMoves", new ArrayList<>(), "error", e.getMessage());
        }
    }

    // --- HELPERS ---

    private boolean validateMove(Position from, Position to, String pieceType, String team, List<Piece> pieces) {
        if (from.samePosition(to)) return false;
        switch (pieceType.toLowerCase()) {
            case "pawn": return PawnRules.isValidMove(from, to, team, pieces);
            case "rook": return RookRules.isValidMove(from, to, team, pieces);
            case "knight": return KnightRules.isValidMove(from, to, team, pieces);
            case "bishop": return BishopRules.isValidMove(from, to, team, pieces);
            case "queen": return QueenRules.isValidMove(from, to, team, pieces);
            case "king": return KingRules.isValidMove(from, to, team, pieces);
            default: return false;
        }
    }

    private List<Position> getPossibleMoves(Position from, String pieceType, String team, List<Piece> pieces) {
        switch (pieceType.toLowerCase()) {
            case "pawn": return PawnRules.getPossibleMoves(from, team, pieces);
            case "rook": return RookRules.getPossibleMoves(from, team, pieces);
            case "knight": return KnightRules.getPossibleMoves(from, team, pieces);
            case "bishop": return BishopRules.getPossibleMoves(from, team, pieces);
            case "queen": return QueenRules.getPossibleMoves(from, team, pieces);
            case "king": return KingRules.getPossibleMoves(from, team, pieces);
            default: return new ArrayList<>();
        }
    }

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
            simulated.add(new Piece(new Position(p.getPosition().getX(), p.getPosition().getY()), p.getType(), p.getTeam(), p.isHasMoved()));
        }
        simulated.removeIf(p -> p.getPosition().getX() == to.getX() && p.getPosition().getY() == to.getY());
        for (Piece p : simulated) {
            if (p.getPosition().getX() == from.getX() && p.getPosition().getY() == from.getY()) {
                p.setPosition(new Position(to.getX(), to.getY()));
                p.setHasMoved(true);
                break;
            }
        }
        return simulated;
    }
    
    private Piece getPieceAt(Position pos, List<Piece> pieces) {
        for (Piece p : pieces) {
            if (p.getPosition().samePosition(pos)) return p;
        }
        return null;
    }
}