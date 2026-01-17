package com.chess.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Piece {
    private Position position;
    private String type;
    private String team;
    private boolean hasMoved;

    public Piece(Position position, String type, String team, boolean hasMoved) {
        this.position = position;
        this.type = type;
        this.team = team;
        this.hasMoved = hasMoved;
    }

    public Position getPosition() { return position; }
    public String getType() { return type; }
    public String getTeam() { return team; }
    public boolean isHasMoved() { return hasMoved; }

    public void setPosition(Position position) { this.position = position; }
    public void setHasMoved(boolean hasMoved) { this.hasMoved = hasMoved; }

    public Piece clone() {
        return new Piece(this.position.clone(), this.type, this.team, this.hasMoved);
    }
}
