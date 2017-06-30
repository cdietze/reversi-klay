package reversi.core

import klay.core.Platform
import klay.core.Surface
import klay.scene.Layer
import klay.scene.SceneGame

class Reversi(plat: Platform) : SceneGame(plat) {
    val boardSize = 8

    init {
        // figure out how big the game view is
        val size = plat.graphics.viewSize

        // create a layer that just draws a grey background
        rootLayer.add(object : Layer() {
            override fun paintImpl(surf: Surface) {
                surf.setFillColor(0xFFCCCCCC.toInt()).fillRect(0f, 0f, size.width, size.height);
            }
        })

        // create and add a board view
        rootLayer.addCenterAt(BoardView(this, size), size.width / 2, size.height / 2)
    }
}

data class Coord(val x: Int, val y: Int) {
    init {
        assert(x >= 0)
        assert(y >= 0)
    }
}