package com.chess.rules;

import com.chess.model.Position;
import com.chess.model.Piece;
import java.util.List;

/**
 * General helper functions used by all piece rules.
 */
public class GeneralRules {

    /**
     * Check if a tile has any piece on it
     */
    public static boolean tileIsOccupied(Position position, List<Piece> boardState) {
        return boardState.stream().anyMatch(p -> p.getPosition().samePosition(position));
    }

    /**
     * Check if a tile has an opponent's piece
     */
    public static boolean tileIsOccupiedByOpponent(Position position, List<Piece> boardState, String team) {
        return boardState.stream()
                .anyMatch(p -> p.getPosition().samePosition(position) && !p.getTeam().equals(team));
    }

    /**
     * Check if a tile is empty OR has an opponent's piece (valid move destination)
     */
    public static boolean tileIsEmptyOrOccupiedByOpponent(Position position, List<Piece> boardState, String team) {
        return !tileIsOccupied(position, boardState) || tileIsOccupiedByOpponent(position, boardState, team);
    }
}
