package reversi.core

import klay.core.Sound
import klay.core.Texture
import klay.core.Tile
import klay.scene.GroupLayer
import klay.scene.ImageLayer
import klay.scene.Layer
import klay.scene.LayerUtil
import pythagoras.f.IDimension
import pythagoras.f.MathUtil
import react.RMap
import reversi.core.Reversi.Coord
import reversi.core.Reversi.Piece
import tripleklay.anim.Animation
import java.util.*

class GameView(val game: Reversi, viewSize: IDimension) : GroupLayer() {
    private val bview: BoardView = BoardView(game, viewSize)
    private val pgroup = GroupLayer()

    private val ptiles = arrayOfNulls<Tile>(Piece.values().size)
    private val pviews = HashMap<Coord, ImageLayer>()

    private val click: Sound = game.plat.assets.getSound("sounds/click")
    private val flip: FlipBatch = FlipBatch(game.plat.graphics.gl, 2f)

    init {
        addCenterAt(bview, viewSize.width / 2, viewSize.height / 2)
        addAt(pgroup, bview.tx(), bview.ty())

        // draw a black piece and white piece into a single canvas image
        val size = bview.cellSize - 2
        val hsize = size / 2
        val canvas = game.plat.graphics.createCanvas(2 * size, size)
        canvas.setFillColor(0xFF000000.toInt()).fillCircle(hsize, hsize, hsize).setStrokeColor(0xFFFFFFFF.toInt()).setStrokeWidth(2f).strokeCircle(hsize, hsize, hsize - 1)
        canvas.setFillColor(0xFFFFFFFF.toInt()).fillCircle(size + hsize, hsize, hsize).setStrokeColor(0xFF000000.toInt()).setStrokeWidth(2f).strokeCircle(size + hsize, hsize, hsize - 1)

        // convert the image to a texture and extract a texture region (tile) for each piece
        val ptex = canvas.toTexture(Texture.Config.UNMANAGED)

        ptiles[Piece.BLACK.ordinal] = ptex.tile(0f, 0f, size, size)
        ptiles[Piece.WHITE.ordinal] = ptex.tile(size, 0f, size, size)

        // dispose our pieces texture when this layer is disposed
        onDisposed(ptex.disposeSlot())

        game.pieces.connect(object : RMap.Listener<Coord, Piece> {
            override fun onPut(coord: Coord, piece: Piece) {
                setPiece(coord, piece)
            }

            override fun onRemove(coord: Coord) {
                clearPiece(coord)
            }
        })
    }

    override fun close() {
        super.close()
        flip.close()
    }

    fun showPlays(coords: List<Coord>, color: Piece) {
        val plays = ArrayList<ImageLayer>()
        for (coord in coords) {
            val pview = addPiece(coord, color)
            // fade the piece in
            pview.setVisible(false).setAlpha(0f)
            game.anim.setVisible(pview, true).then().tweenAlpha(pview).to(0.3f).`in`(300f)
            // when the player clicks on a potential play, commit that play as their move
            pview.events().connect(object : klay.scene.Pointer.Listener {
                override fun onStart(iact: klay.scene.Pointer.Interaction) {
                    // clear out the potential plays layers
                    for (play in plays) play.close()
                    // apply this play to the game state
                    game.logic.applyPlay(game.pieces, color, coord)
                    // and move to the next player's turn
                    game.turn.update(color.next)
                }
            })
            // when the player hovers over a potential play, highlight it
            pview.events().connect(object : klay.scene.Mouse.Listener {
                override fun onHover(event: klay.scene.Mouse.HoverEvent, iact: klay.scene.Mouse.Interaction) {
                    iact.hitLayer.setAlpha(if (event.inside) 0.6f else 0.3f)
                }
            })
            plays.add(pview)
        }
    }

    private fun addPiece(at: Coord, piece: Piece): ImageLayer {
        val pview = ImageLayer(ptiles[piece.ordinal]!!)
        pview.setOrigin(Layer.Origin.CENTER)
        pgroup.addAt(pview, bview.cell(at.x), bview.cell(at.y))
        return pview
    }

    private fun setPiece(at: Coord, piece: Piece) {
        var pview: ImageLayer? = pviews[at]
        if (pview == null) {
            pview = addPiece(at, piece)
            pviews.put(at, pview)
            // animate the piece view "falling" into place
            pview.setVisible(false).setScale(2f)
            game.anim.setVisible(pview, true).then().tweenScale(pview).to(1f).`in`(500f).bounceOut()
            game.anim.delay(250f).then().play(click)
            game.anim.addBarrier()

        } else {
            val fview = pview
            val tile = ptiles[piece.ordinal]
            val eye = LayerUtil.layerToScreen(pview, fview.width() / 2, fview.height() / 2)
            val flipAngle = object : Animation.Value {
                override fun initial(): Float {
                    return flip.angle
                }

                override fun set(value: Float) {
                    flip.angle = value
                }
            }
            game.anim
                    .action(Runnable {
                        flip.eyeX = eye.x
                        flip.eyeY = eye.y
                        fview.setBatch(flip)
                    }).then()
                    .tween(flipAngle).from(0f).to(MathUtil.PI / 2).`in`(150f).then()
                    .action(Runnable { fview.setTile(tile) }).then()
                    .tween(flipAngle).to(MathUtil.PI).`in`(150f).then()
                    .action(Runnable { fview.setBatch(null) })
            game.anim.addBarrier()
        }
    }

    private fun clearPiece(at: Coord) {
        val pview = pviews.remove(at)
        if (pview != null) pview.close()
    }
}
