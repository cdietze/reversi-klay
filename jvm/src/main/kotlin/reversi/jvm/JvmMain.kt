package reversi.jvm

import klay.jvm.JavaPlatform
import klay.jvm.LWJGLPlatform
import reversi.core.Reversi

object JvmMain {
    @JvmStatic
    fun main(args: Array<String>) {
        val config = JavaPlatform.Config()
        config.width = 800
        config.height = 600
        config.appName = "Reversi"
        val plat = LWJGLPlatform(config)
        Reversi(plat)
        plat.start()
    }
}