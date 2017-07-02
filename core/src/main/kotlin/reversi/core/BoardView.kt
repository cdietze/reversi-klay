package reversi.core

import klay.core.Surface
import klay.scene.Layer
import pythagoras.f.IDimension

class BoardView(private val game: Reversi, viewSize: IDimension) : Layer() {

    val cellSize: Float

    init {
        val maxBoardSize = Math.min(viewSize.width, viewSize.height) - 20
        this.cellSize = Math.floor((maxBoardSize / game.boardSize).toDouble()).toFloat()
    }

    /** Returns the offset to the center of cell `cc` (in x or y).  */
    fun cell(cc: Int): Float {
        // cc*cellSize is upper left corner, then cellSize/2 to center,
        // then 1 to account for our 2 pixel line width
        return cc * cellSize + cellSize / 2 + 1f
    }

    // we want two extra pixels in width/height to account for the grid lines
    override fun width(): Float = cellSize * game.boardSize + LINE_WIDTH

    override fun height(): Float = width() // width == height

    override fun paintImpl(surf: Surface) {
        surf.setFillColor(0xFF000000.toInt()) // black with full alpha
        val top = 0f
        val bot = height()
        val left = 0f
        val right = width()

        // draw lines from top to bottom for each vertical grid line
        for (yy in 0..game.boardSize) {
            val ypos = yy * cellSize + 1
            surf.drawLine(left, ypos, right, ypos, LINE_WIDTH)
        }

        // draw lines from left to right for each horizontal grid line
        for (xx in 0..game.boardSize) {
            val xpos = xx * cellSize + 1
            surf.drawLine(xpos, top, xpos, bot, LINE_WIDTH)
        }
    }

    companion object {
        private val LINE_WIDTH = 2f
    }
}
