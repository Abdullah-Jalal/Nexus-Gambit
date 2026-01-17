"use client"

import { useRef, useState } from "react"
import { initialBoard } from "../../Constants"
import { Piece } from "../../models"
import Chessboard from "../Chessboard/Chessboard"
import "./Refree.css"
import { analyzeMove } from "../../api/chessApi"
import type { Position } from "../../models/Position"
import type { Board } from "../../models/Board"
import { PieceType, TeamType } from "../../Types"

export default function Refree() {
  const [board, setBoard] = useState(initialBoard.clone())
  const [promotionPawn, setPromotionPawn] = useState<Piece | undefined>(undefined)
  const [modalMessage, setModalMessage] = useState("")
  const modalRef = useRef<HTMLDivElement | null>(null)
  const endgameModalRef = useRef<HTMLDivElement | null>(null)

  // --- Captured Count Logic ---
  const totalPiecesOnBoard = board.pieces.length
  const capturedPiecesCount = 32 - totalPiecesOnBoard

  async function playMove(playedPiece: Piece, destination: Position) {
    if (playedPiece.possibleMoves === undefined) return false

    // Enforce Turn Order
    if (playedPiece.team === TeamType.OUR && board.totalTurns % 2 !== 0) return false
    if (playedPiece.team === TeamType.OPPONENT && board.totalTurns % 2 !== 1) return false

    let playedMoveIsValid = false

    const boardState = board.pieces.map((p) => ({
      position: { x: p.position.x, y: p.position.y },
      team: p.team,
      type: p.type,
      hasMoved: p.hasMoved,
    }))

    // Call API
    const analyzeResponse = await analyzeMove({
      from: { x: playedPiece.position.x, y: playedPiece.position.y },
      to: { x: destination.x, y: destination.y },
      piece: {
        position: { x: playedPiece.position.x, y: playedPiece.position.y },
        type: playedPiece.type,
        team: playedPiece.team,
      },
      boardState: boardState,
      totalMoves: board.totalTurns,
    })

    if (!analyzeResponse.valid) return false

    const enPassantMove = isEnPassantMove(playedPiece.position, destination, playedPiece.type, playedPiece.team)

    setBoard((prevBoard) => {
      const clonedBoard = prevBoard.clone()
      const nextTurn = clonedBoard.totalTurns + 1

      // Execute move locally
      playedMoveIsValid = clonedBoard.playMove(enPassantMove, true, playedPiece, destination)

      // --- CRASH FIX: Removed the lines that tried to write to read-only .draw/.stalemate ---
      
      // We only update the winningTeam if the game has progressed enough (Turn 4+)
      // AND if the backend explicitly says there is a winner.
      if (nextTurn >= 4) {
        if (analyzeResponse.isCheckmate && analyzeResponse.winningTeam) {
          const winner =
            analyzeResponse.winningTeam === "w" || analyzeResponse.winningTeam === "white"
              ? TeamType.OUR
              : TeamType.OPPONENT
          clonedBoard.winningTeam = winner
        }
      } else {
        // If turn < 4, we ensure winningTeam is undefined to prevent early wins
        clonedBoard.winningTeam = undefined;
      }

      clonedBoard.totalTurns = nextTurn
      checkForEndGame(clonedBoard)
      return clonedBoard
    })

    const promotionRow = playedPiece.team === TeamType.OUR ? 7 : 0

    if (destination.y === promotionRow && playedPiece.isPawn) {
      modalRef.current?.classList.remove("hidden")
      setPromotionPawn((previousPromotionPawn) => {
        const clonedPlayedPiece = playedPiece.clone()
        clonedPlayedPiece.position = destination.clone()
        return clonedPlayedPiece
      })
    }

    return playedMoveIsValid
  }

  function isEnPassantMove(initialPosition: Position, desiredPosition: Position, type: PieceType, team: TeamType) {
    const pawnDirection = team === TeamType.OUR ? 1 : -1

    if (type === PieceType.PAWN) {
      if (
        (desiredPosition.x - initialPosition.x === -1 || desiredPosition.x - initialPosition.x === 1) &&
        desiredPosition.y - initialPosition.y === pawnDirection
      ) {
        const piece = board.pieces.find(
          (p) =>
            p.position.x === desiredPosition.x &&
            p.position.y === desiredPosition.y - pawnDirection &&
            p.isPawn &&
            (p as any).enPassant,
        )
        if (piece) {
          return true
        }
      }
    }

    return false
  }

  function promotePawn(pieceType: PieceType) {
    if (promotionPawn === undefined) return

    setBoard((previousBoard) => {
      const clonedBoard = board.clone()
      clonedBoard.pieces = clonedBoard.pieces.reduce((results: Piece[], piece) => {
        if (piece.samePiecePosition(promotionPawn)) {
          results.push(new Piece(piece.position.clone(), pieceType, piece.team, true))
        } else {
          results.push(piece)
        }
        return results
      }, [] as Piece[])

      checkForEndGame(clonedBoard)
      return clonedBoard
    })

    modalRef.current?.classList.add("hidden")
  }

  function restartGame() {
    endgameModalRef.current?.classList.add("hidden")
    setBoard(initialBoard.clone())
  }

  function checkForEndGame(board: Board) {
    // --- 🛡️ UI SAFETY LOCK ---
    // This is the most important line. 
    // It prevents the "Game Over" modal from appearing if the game just started (< 4 turns).
    // This suppresses any "fake" win/draw signals from the backend or local board state.
    if (board.totalTurns < 4) return

    if (board.winningTeam !== undefined) {
      setModalMessage(`The winning team is ${board.winningTeam === TeamType.OUR ? "white" : "black"}!`)
      endgameModalRef.current?.classList.remove("hidden")
    } else if (board.draw) {
      setModalMessage("It's a draw!")
      endgameModalRef.current?.classList.remove("hidden")
    } else if (board.stalemate) {
      setModalMessage("It's a stalemate!")
      endgameModalRef.current?.classList.remove("hidden")
    }
  }

  const currentPlayer = board.totalTurns % 2 === 0 ? "white" : "black"
  const movesDisplay = board.moves.map((move, index) => (
    <div key={index} className="move-item">
      <span className="move-number">{Math.floor(index / 2) + 1}.</span>
      <span className={`move-text ${index % 2 === 0 ? "white-move" : "black-move"}`}>{move.toMessage()}</span>
    </div>
  ))

  return (
    <main>
      <section className="information">
        <div className="player-status">
          <div className={`player-info ${currentPlayer === "white" ? "active" : ""}`}>
            <div className="player-avatar white">W</div>
            <div className="player-details">
              <h3>Team White</h3>
            </div>
          </div>
          <div className="turn-indicator">
            <div className={`turn-badge ${currentPlayer === "white" ? "white" : "black"}`}>
              {currentPlayer.toUpperCase()}'S TURN
            </div>
          </div>
          <div className={`player-info ${currentPlayer === "black" ? "active" : ""}`}>
            <div className="player-avatar black">B</div>
            <div className="player-details">
              <h3>Team Black</h3>
            </div>
          </div>
        </div>

        <div className="game-stats">
          <div className="stat">
            <span className="stat-label">Total Moves</span>
            <span className="stat-value">{board.totalTurns}</span>
          </div>
          <div className="stat">
            <span className="stat-label">Captured</span>
            <span className="stat-value">{capturedPiecesCount}/32</span>
          </div>
        </div>

        <div className="moves-header">
          <h4>Move History</h4>
        </div>

        <div className="moves">
          {movesDisplay.length > 0 ? movesDisplay : <p style={{ textAlign: "center", color: "#888" }}>No moves yet</p>}
        </div>
      </section>

      <Chessboard playMove={playMove} pieces={board.pieces} />

      <div ref={modalRef} className="modal hidden">
        <div className="modal-content">
          <h2>Promote your pawn</h2>
          <div className="promotion-options">
            <button onClick={() => promotePawn(PieceType.ROOK)}>Rook</button>
            <button onClick={() => promotePawn(PieceType.KNIGHT)}>Knight</button>
            <button onClick={() => promotePawn(PieceType.BISHOP)}>Bishop</button>
            <button onClick={() => promotePawn(PieceType.QUEEN)}>Queen</button>
          </div>
        </div>
      </div>

      <div ref={endgameModalRef} className="modal hidden">
        <div className="modal-content endgame">
          <h2>{modalMessage}</h2>
          <p>Play Again?</p>
          <button className="restart-btn" onClick={restartGame}>
            New Game
          </button>
        </div>
      </div>
    </main>
  )
}