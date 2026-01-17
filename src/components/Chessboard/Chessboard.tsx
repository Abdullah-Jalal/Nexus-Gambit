"use client"

import type React from "react"
import { useRef, useState } from "react"
import "./Chessboard.css"
import Tile from "../Tile/Tile"
import { VERTICAL_AXIS, HORIZONTAL_AXIS, GRID_SIZE } from "../../Constants"
import { type Piece, Position } from "../../models"
import { analyzeMove } from "../../api/chessApi"



interface Props {
  playMove: (piece: Piece, position: Position) => Promise<boolean>
  pieces: Piece[]
}

export default function Chessboard({ playMove, pieces }: Props) {

  const [activePiece, setActivePiece] = useState<HTMLElement | null>(null)
  const [grabPosition, setGrabPosition] = useState<Position>(new Position(-1, -1))
  const [possibleMoves, setPossibleMoves] = useState<Position[]>([])
  const chessboardRef = useRef<HTMLDivElement>(null)

  async function grabPiece(e: React.MouseEvent) {
    const element = e.target as HTMLElement
    const chessboard = chessboardRef.current
    e.preventDefault()

    if (element.classList.contains("chess-piece") && chessboard) {
      const grabX = Math.floor((e.clientX - chessboard.offsetLeft) / GRID_SIZE)
      const grabY = Math.abs(Math.ceil((e.clientY - chessboard.offsetTop - 800) / GRID_SIZE))
      const position = new Position(grabX, grabY)

      setGrabPosition(position)

      const currentPiece = pieces.find((p) => p.samePosition(position))
      if (currentPiece) {
        console.log("[v0] Grabbed piece:", {
          position: { x: currentPiece.position.x, y: currentPiece.position.y },
          type: currentPiece.type,
          team: currentPiece.team,
        })

        const response = await analyzeMove({
          from: { x: currentPiece.position.x, y: currentPiece.position.y },
          to: { x: currentPiece.position.x, y: currentPiece.position.y },
          piece: {
            position: { x: currentPiece.position.x, y: currentPiece.position.y },
            type: currentPiece.type,
            team: currentPiece.team,
          },
          boardState: pieces.map((p) => ({
            position: { x: p.position.x, y: p.position.y },
            type: p.type,
            team: p.team,
            hasMoved: p.hasMoved,
          })),
          totalMoves: 0
        })

        console.log("[v0] Possible moves received:", response.possibleMoves)
        setPossibleMoves(response.possibleMoves.map((m) => new Position(m.x, m.y)))
      }

      const x = e.clientX - GRID_SIZE / 2
      const y = e.clientY - GRID_SIZE / 2
      element.style.position = "absolute"
      element.style.left = `${x}px`
      element.style.top = `${y}px`

      setActivePiece(element)
    }
  }

  function movePiece(e: React.MouseEvent) {
    const chessboard = chessboardRef.current
    if (activePiece && chessboard) {
      const minX = chessboard.offsetLeft - 25
      const minY = chessboard.offsetTop - 25
      const maxX = chessboard.offsetLeft + chessboard.clientWidth - 75
      const maxY = chessboard.offsetTop + chessboard.clientHeight - 75
      const x = e.clientX - 50
      const y = e.clientY - 50
      activePiece.style.position = "absolute"

      if (x < minX) activePiece.style.left = `${minX}px`
      else if (x > maxX) activePiece.style.left = `${maxX}px`
      else activePiece.style.left = `${x}px`

      if (y < minY) activePiece.style.top = `${minY}px`
      else if (y > maxY) activePiece.style.top = `${maxY}px`
      else activePiece.style.top = `${y}px`
    }
  }

  async function dropPiece(e: React.MouseEvent) {
    const chessboard = chessboardRef.current
    if (activePiece && chessboard) {
      const x = Math.floor((e.clientX - chessboard.offsetLeft) / GRID_SIZE)
      const y = Math.abs(Math.ceil((e.clientY - chessboard.offsetTop - 800) / GRID_SIZE))

      const currentPiece = pieces.find((p) => p.samePosition(grabPosition))

      if (currentPiece) {
        const success = await playMove(currentPiece.clone(), new Position(x, y))
        if (!success) {
          activePiece.style.position = "relative"
          activePiece.style.removeProperty("top")
          activePiece.style.removeProperty("left")
        }
      }
      setPossibleMoves([])
      setActivePiece(null)
    }
  }

  const tiles = []

  for (let j = VERTICAL_AXIS.length - 1; j >= 0; j--) {
    for (let i = 0; i < HORIZONTAL_AXIS.length; i++) {
      const number = j + i + 2
      const piece = pieces.find((p) => p.samePosition(new Position(i, j)))
      const image = piece ? piece.image : undefined

      const currentPiece = activePiece ? pieces.find((p) => p.samePosition(grabPosition)) : undefined
      const highlight = possibleMoves.length > 0 ? possibleMoves.some((p) => p.samePosition(new Position(i, j))) : false

      tiles.push(<Tile key={`${j},${i}`} image={image} number={number} highlight={highlight} />)
    }
  }

  return (
    <div onMouseMove={movePiece} onMouseDown={grabPiece} onMouseUp={dropPiece} id="chessboard" ref={chessboardRef}>
      {tiles}
    </div>
  )
}
