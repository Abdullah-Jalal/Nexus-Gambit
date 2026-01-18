"use client"

import { useRef, useState, useEffect } from "react"
import { initialBoard } from "../../Constants"
import { Piece } from "../../models"
import Chessboard from "../Chessboard/Chessboard"
import "./Refree.css"
import { analyzeMove } from "../../api/chessApi"
import type { Position } from "../../models/Position"
import type { Board } from "../../models/Board"
import { PieceType, TeamType } from "../../Types"

const formatTime = (seconds: number) => {
  const mins = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${mins}:${secs < 10 ? "0" : ""}${secs}`
}

export default function Refree() {
  const [board, setBoard] = useState(initialBoard.clone())
  const [promotionPawn, setPromotionPawn] = useState<Piece | undefined>(undefined)
  const [modalMessage, setModalMessage] = useState("")
  
  const [whiteTime, setWhiteTime] = useState(600)
  const [blackTime, setBlackTime] = useState(600)

  const modalRef = useRef<HTMLDivElement | null>(null)
  const endgameModalRef = useRef<HTMLDivElement | null>(null)

  const currentPlayer = board.totalTurns % 2 === 0 ? "white" : "black"
  const totalPiecesOnBoard = board.pieces.length
  const capturedPiecesCount = 32 - totalPiecesOnBoard

  useEffect(() => {
    // FIX: Removed casting because stalemate is now a boolean property
    if (board.winningTeam !== undefined || board.draw || board.stalemate) return

    const timer = setInterval(() => {
      if (currentPlayer === "white") {
        setWhiteTime((prev) => {
          if (prev <= 1) {
            clearInterval(timer)
            handleTimeOut(TeamType.OPPONENT)
            return 0
          }
          return prev - 1
        })
      } else {
        setBlackTime((prev) => {
          if (prev <= 1) {
            clearInterval(timer)
            handleTimeOut(TeamType.OUR)
            return 0
          }
          return prev - 1
        })
      }
    }, 1000)

    return () => clearInterval(timer)
  }, [currentPlayer, board.winningTeam, board.draw, board.stalemate])

  function handleTimeOut(winningTeam: TeamType) {
    const winnerName = winningTeam === TeamType.OUR ? "White" : "Black"
    setModalMessage(`${winnerName} Wins by Timeout!`)
    endgameModalRef.current?.classList.remove("hidden")
  }

  async function playMove(playedPiece: Piece, destination: Position) {
    if (playedPiece.possibleMoves === undefined) return false

    if (playedPiece.team === TeamType.OUR && board.totalTurns % 2 !== 0) return false
    if (playedPiece.team === TeamType.OPPONENT && board.totalTurns % 2 !== 1) return false

    let playedMoveIsValid = false

    const boardState = board.pieces.map((p) => ({
      position: { x: p.position.x, y: p.position.y },
      team: p.team,
      type: p.type,
      hasMoved: p.hasMoved,
    }))

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

      playedMoveIsValid = clonedBoard.playMove(enPassantMove, true, playedPiece, destination)

      if (analyzeResponse.isCheckmate) {
        const winner = (analyzeResponse.winningTeam === "w" || analyzeResponse.winningTeam === "white")
          ? TeamType.OUR 
          : TeamType.OPPONENT
        clonedBoard.winningTeam = winner
      } else {
        clonedBoard.winningTeam = undefined
      }
      
      // FIX: Assign directly to the new boolean property
      if (analyzeResponse.isStalemate) {
        clonedBoard.stalemate = true
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
        if (piece) return true
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
    setWhiteTime(600)
    setBlackTime(600)
  }

  function checkForEndGame(board: Board) {
    if (board.winningTeam !== undefined) {
      const winner = board.winningTeam === TeamType.OUR ? "White" : "Black"
      setModalMessage(`${winner} Wins by Checkmate!`)
      endgameModalRef.current?.classList.remove("hidden")
    } 
    // FIX: Direct access
    else if (board.stalemate) {
      setModalMessage("Draw by Stalemate!")
      endgameModalRef.current?.classList.remove("hidden")
    }
  }

  // Helper to safely get move message
  const getMoveMessage = (move: any) => {
      if (!move) return "";
      // If move is just a string or object, handle accordingly
      // Assuming simple display for now or existing method
      return move.toMessage ? move.toMessage() : "Move";
  }

  const movesDisplay = board.moves?.map((move: any, index: number) => (
    <div key={index} className="move-item">
      <span className="move-number">{Math.floor(index / 2) + 1}.</span>
      {/* Safe check for toMessage function */}
      <span className={`move-text ${index % 2 === 0 ? "white-move" : "black-move"}`}>
         {move.toMessage ? move.toMessage() : `${move.from} -> ${move.to}`}
      </span>
    </div>
  )) || [];

  return (
    <main>
      <section className="information">
        <div className="player-status">
          <div className={`player-info ${currentPlayer === "white" ? "active" : ""}`}>
            <div className="player-avatar white">W</div>
            <div className="player-details">
              <h3>Team White</h3>
              <div className="timer">{formatTime(whiteTime)}</div>
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
              <div className="timer">{formatTime(blackTime)}</div>
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
          <p>Game Over</p>
          <button className="restart-btn" onClick={restartGame}>
            New Game
          </button>
        </div>
      </div>
    </main>
  )
}