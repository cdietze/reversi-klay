package reversi.core

import klay.core.*
import klay.scene.*
import klay.scene.Mouse
import klay.scene.Pointer
import react.RMap
import react.Value
import tripleklay.anim.Animator
import java.util.*

class Reversi(plat: Platform) : SceneGame(plat, 33) {

    enum class Piece {
        BLACK {
            override val next: Piece get() = WHITE
        },
        WHITE {
            override val next: Piece get() = BLACK
        };

        abstract val next: Piece
    }

    data class Coord(val x: Int, val y: Int) {

        init {
            assert(x >= 0 && y >= 0)
        }

        override fun toString(): String {
            return "+$x+$y"
        }
    }

    val boardSize = 8
    val pieces: RMap<Coord, Piece> = RMap.create()
    val turn: Value<Piece> = Value(Piece.BLACK)
    val logic = Logic(boardSize)
    val pointer: Pointer = Pointer(plat, rootLayer, true)
    val anim: Animator = Animator(paint) // create an animator for some zip zing

    init {
        // wire up pointer and mouse event dispatch
        plat.input.mouseEvents.connect(Mouse.Dispatcher(rootLayer, false))

        // figure out how big the game view is
        val size = plat.graphics.viewSize

        // create a layer that just draws a grey background
        rootLayer.add(object : Layer() {
            override fun paintImpl(surf: Surface) {
                surf.setFillColor(0xFFCCCCCC.toInt()).fillRect(0f, 0f, size.width, size.height)
            }
        })

        // create and add a game view
        val gview = GameView(this, size)
        rootLayer.add(gview)

        // wire up a turn handler
        var lastPlayerPassed = false
        turn.connect({ color: Piece ->
            val plays = logic.legalPlays(pieces, color)
            if (!plays.isEmpty()) {
                lastPlayerPassed = false
                gview.showPlays(plays, color)
            } else if (lastPlayerPassed) {
                endGame()
            } else {
                lastPlayerPassed = true
                turn.update(color.next)
            }
        })

        // start the game
        reset()
    }// update our "simulation" 33ms (30 times per second)

    /** Clears the board and sets the 2x2 set of starting pieces in the middle.  */
    private fun reset() {
        pieces.clear()
        val half = boardSize / 2
        pieces.put(Coord(half - 1, half - 1), Piece.WHITE)
        pieces.put(Coord(half, half - 1), Piece.BLACK)
        pieces.put(Coord(half - 1, half), Piece.BLACK)
        pieces.put(Coord(half, half), Piece.WHITE)
        turn.updateForce(Piece.BLACK)
    }

    private fun endGame() {
        // count up the pieces for each color
        val ps = Piece.values()
        val count = IntArray(ps.size)
        for (p in pieces.values) count[p.ordinal]++

        // figure out who won
        val winners = ArrayList<Piece>()
        var highScore = 0
        for (ii in count.indices) {
            val score = count[ii]
            if (score == highScore)
                winners.add(ps[ii])
            else if (score > highScore) {
                winners.clear()
                winners.add(ps[ii])
                highScore = score
            }
        }

        // if we have only one winner, they win; otherwise it's a tie
        val msg = StringBuilder()
        if (winners.size == 1)
            msg.append(winners[0]).append(" wins!")
        else {
            for (p in winners) {
                if (msg.isNotEmpty()) msg.append(" and ")
                msg.append(p)
            }
            msg.append(" tie.")
        }
        msg.append("\nClick to play again.")

        // render the game over message and display it in a layer
        val viewSize = plat.graphics.viewSize
        val block = TextBlock(plat.graphics.layoutText(
                msg.toString(), TextFormat(Font("Helvetica", Font.Style.BOLD, 48f)),
                TextWrap(viewSize.width - 20)))
        val canvas = plat.graphics.createCanvas(block.bounds.width + 4, block.bounds.height + 4)
        canvas.setFillColor(0xFF0000FF.toInt()).setStrokeColor(0xFFFFFFFF.toInt()).setStrokeWidth(4f)
        block.stroke(canvas, TextBlock.Align.CENTER, 2f, 2f)
        block.fill(canvas, TextBlock.Align.CENTER, 2f, 2f)
        val layer = ImageLayer(canvas.toTexture())
        rootLayer.addFloorAt(layer, (viewSize.width - canvas.width) / 2, (viewSize.height - canvas.height) / 2)

        // when the player clicks anywhere, restart the game
        pointer.events.connect(object : (klay.core.Pointer.Event) -> Unit {
            override fun invoke(event: klay.core.Pointer.Event) {
                if (event.kind.isStart) {
                    layer.close()
                    reset()
                    pointer.events.disconnect(this)
                }
            }
        })
    }
}
