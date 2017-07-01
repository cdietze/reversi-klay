package reversi.core

import reversi.core.Reversi.Coord
import reversi.core.Reversi.Piece
import java.util.*

/** "Does the math" for Reversi.  */
class Logic(val boardSize: Int) {

    /** Returns true if the specified player can play a piece at the specified coordinate.  */
    fun isLegalPlay(board: Map<Coord, Piece>, color: Piece, coord: Coord): Boolean {
        if (!inBounds(coord.x, coord.y) || board.containsKey(coord)) return false

        // look in each direction from this piece; if we see the other piece color and then one of our
        // own, then this is a legal move
        for (ii in DX.indices) {
            var sawOther = false
            var x = coord.x
            var y = coord.y
            for (dd in 0..boardSize - 1) {
                x += DX[ii]
                y += DY[ii]
                if (!inBounds(x, y)) break // stop when we end up off the board
                val piece = board[Coord(x, y)]
                if (piece == null)
                    break
                else if (piece != color)
                    sawOther = true
                else if (sawOther)
                    return true
                else
                    break
            }
        }

        return false
    }

    /** Applies the specified play (caller must have already checked its legality).
     * Flips pieces as appropriate.  */
    fun applyPlay(board: MutableMap<Coord, Piece>, color: Piece, coord: Coord) {
        val toFlip = ArrayList<Coord>()
        // place this piece into the game state
        board.put(coord, color)
        // determine where this piece captures other pieces
        for (ii in DX.indices) {
            // look in this direction for captured pieces
            var x = coord.x
            var y = coord.y
            for (dd in 0..boardSize - 1) {
                x += DX[ii]
                y += DY[ii]
                if (!inBounds(x, y)) break // stop when we end up off the board
                val fc = Coord(x, y)
                val piece = board[fc]
                if (piece == null)
                    break
                else if (piece != color)
                    toFlip.add(fc)
                else { // piece == color
                    for (tf in toFlip) board.put(tf, color) // flip it!
                    break
                }
            }
            toFlip.clear()
        }
    }

    /** Returns all legal plays for the player with the specified color.  */
    fun legalPlays(board: Map<Coord, Piece>, color: Piece): List<Coord> {
        val plays = ArrayList<Coord>()
        // search every board position for a legal move; the force, it's so brute!
        for (yy in 0..boardSize - 1) {
            for (xx in 0..boardSize - 1) {
                val coord = Coord(xx, yy)
                if (board.containsKey(coord)) continue
                if (isLegalPlay(board, color, coord)) plays.add(coord)
            }
        }
        return plays
    }

    private fun inBounds(x: Int, y: Int): Boolean {
        return x >= 0 && x < boardSize && y >= 0 && y < boardSize
    }

    companion object {

        protected val DX = intArrayOf(-1, 0, 1, -1, 1, -1, 0, 1)
        protected val DY = intArrayOf(-1, -1, -1, 0, 0, 1, 1, 1)
    }
}
