import type { Piece } from "./Piece"
import type { Position } from "./Position"
import { TeamType } from "../Types"
import { Move } from "./Move"

export interface BoardState {
  pieces: Piece[]
  totalTurns: number
  moves: Move[]
  enPassantMove?: any
  winningTeam?: any
  stalemate?: boolean // Added to interface
}

export class Board {
  pieces: Piece[]
  totalTurns: number
  moves: Move[] = []
  enPassantMove: any = {}
  winningTeam: any = undefined
  
  // FIX: Changed from getters to real variables
  stalemate: boolean = false; 
  draw: boolean = false;

  constructor(pieces: Piece[], totalTurns: number, moves: Move[], enPassantMove: any, winningTeam: any) {
    this.pieces = pieces
    this.totalTurns = totalTurns
    this.moves = moves
    this.enPassantMove = enPassantMove
    this.winningTeam = winningTeam
  }

  clone(): Board {
    const clonedBoard = new Board(
      this.pieces.map((p) => p.clone()),
      this.totalTurns,
      [...this.moves],
      this.enPassantMove,
      this.winningTeam,
    )
    
    // FIX: Copy the state variables
    clonedBoard.stalemate = this.stalemate;
    clonedBoard.draw = this.draw;
    
    return clonedBoard;
  }

  calculateAllMoves() {
    // No-op for now - API handles validation
  }

  playMove(enPassant: boolean, isFirstMove: boolean, piece: Piece, destination: Position): boolean {
    const pieceIndex = this.pieces.findIndex((p) => p.samePiecePosition(piece))
    if (pieceIndex === -1) return false

    const originalPosition = piece.position.clone()
    const movedPiece = this.pieces[pieceIndex]

    // First, remove any captured piece at destination (but NOT the piece we're moving)
    this.pieces = this.pieces.filter((p) => {
      // Keep the piece we're moving
      if (p.samePiecePosition(movedPiece)) return true
      // Remove any enemy piece on the destination square
      if (p.samePosition(destination)) return false
      return true
    })

    // Now update the moved piece's position
    const updatedPieceIndex = this.pieces.findIndex((p) => p.samePiecePosition(piece))
    if (updatedPieceIndex !== -1) {
      this.pieces[updatedPieceIndex].position = destination.clone()
      this.pieces[updatedPieceIndex].hasMoved = true
    }

    this.moves.push(new Move(originalPosition, destination, this.pieces[updatedPieceIndex]))

    // Handle en passant capture
    if (enPassant) {
      const pawnDirection = piece.team === TeamType.OUR ? 1 : -1
      const captureY = destination.y - pawnDirection
      this.pieces = this.pieces.filter((p) => !(p.position.x === destination.x && p.position.y === captureY))
    }

    return true
  }

  get currentTeam(): TeamType {
    return this.totalTurns % 2 === 0 ? TeamType.OUR : TeamType.OPPONENT
  }
}