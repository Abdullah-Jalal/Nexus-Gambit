import { PieceType, TeamType } from "../Types"

const API_BASE_URL = "http://localhost:8080/api/chess"

export interface AnalyzeMoveRequest {
  from: { x: number; y: number }
  to: { x: number; y: number }
  piece: {
  position: { x: number; y: number }
  type: PieceType
  team: TeamType
}

boardState: {
  position: { x: number; y: number }
  type: PieceType
  team: TeamType
  hasMoved: boolean
}[]

  totalMoves: number 
}

export interface AnalyzeMoveResponse {
  valid: boolean
  possibleMoves: { x: number; y: number }[]
  from: { x: number; y: number }
  to: { x: number; y: number }
  piece: { type: string; team: string }
  isCheckmate?: boolean
  isStalemate?: boolean
  isCheck?: boolean
  winningTeam?: string
  error?: string
}

/**
 * Single unified endpoint that returns both move validation AND possible moves.
 * Much faster than making 2 separate API calls.
 */
export async function analyzeMove(request: AnalyzeMoveRequest): Promise<AnalyzeMoveResponse> {
  try {
    console.log("[v0] Calling API:", `${API_BASE_URL}/analyze`)
    const response = await fetch(`${API_BASE_URL}/analyze`, {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(request),
    })

    if (!response.ok) {
      console.error("[v0] API error:", response.status, response.statusText)
      throw new Error(`HTTP error! status: ${response.status}`)
    }

    const result = await response.json()
    console.log("[v0] API response:", result)
    return result
  } catch (error) {
    console.error("[v0] Error analyzing move:", error)
    return {
      valid: false,
      possibleMoves: [],
      from: request.from,
      to: request.to,
      piece: request.piece,
      error: "API call failed",
    }
  }
}
