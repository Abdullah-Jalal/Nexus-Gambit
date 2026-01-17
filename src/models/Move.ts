import type { Position } from "./Position"
import type { Piece } from "./Piece"

export class Move {
  from: Position
  to: Position
  piece: Piece

  constructor(from: Position, to: Position, piece: Piece) {
    this.from = from
    this.to = to
    this.piece = piece
  }

  toMessage(): string {
    const pieceType = this.piece.type.charAt(0).toUpperCase() + this.piece.type.slice(1)
    const fromNotation = `${String.fromCharCode(97 + this.from.x)}${8 - this.from.y}`
    const toNotation = `${String.fromCharCode(97 + this.to.x)}${8 - this.to.y}`
    return `${pieceType}: ${fromNotation}-${toNotation}`
  }
}
