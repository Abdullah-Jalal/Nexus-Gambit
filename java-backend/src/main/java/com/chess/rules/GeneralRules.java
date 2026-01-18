package com.chess.rules;

import com.chess.model.Position;
import com.chess.model.Piece;
import java.util.List;

public class GeneralRules {

    // Check if a specific tile is occupied by ANY piece
    public static boolean tileIsOccupied(Position position, List<Piece> boardState) {
        return boardState.stream().anyMatch(p -> p.getPosition().samePosition(position));
    }

    // Check if a tile is occupied by an OPPONENT (Capture target)
    public static boolean tileIsOccupiedByOpponent(Position position, List<Piece> boardState, String currentTeam) {
        return boardState.stream()
                .anyMatch(p -> p.getPosition().samePosition(position) 
                            && !isSameTeam(p.getTeam(), currentTeam));
    }

    // Check if a tile is empty OR has an opponent (Valid move destination)
    public static boolean tileIsEmptyOrOccupiedByOpponent(Position position, List<Piece> boardState, String team) {
        return !tileIsOccupied(position, boardState) || tileIsOccupiedByOpponent(position, boardState, team);
    }

    // Robust Team Comparison (Handles "w" vs "White")
    public static boolean isSameTeam(String team1, String team2) {
        if (team1 == null || team2 == null) return false;
        if (team1.isEmpty() || team2.isEmpty()) return false;
        return team1.substring(0, 1).equalsIgnoreCase(team2.substring(0, 1));
    }
}