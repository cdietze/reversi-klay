package reversi.core

import klay.core.GL20
import klay.core.TriangleBatch
import tripleklay.shaders.ShaderUtil

class FlipBatch(gl: GL20, zScale: Float) : TriangleBatch(gl, object : Source() {
    override fun vertex(): String = FlipBatch.vertex(zScale)
}) {
    /** The angle of rotation.  */
    var angle: Float = 0.toFloat()

    /** The current "eye" position in screen coordinates.  */
    var eyeX: Float = 0.toFloat()
    var eyeY: Float = 0.toFloat()

    private val uAngle: Int = program.getUniformLocation("u_Angle")
    private val uEye: Int = program.getUniformLocation("u_Eye")

    override fun begin(fbufWidth: Float, fbufHeight: Float, flip: Boolean) {
        super.begin(fbufWidth, fbufHeight, flip)
        program.activate()
        gl.glUniform1f(uAngle, angle)
        gl.glUniform2f(uEye, eyeX, eyeY)
    }

    companion object {

        private fun vertex(zScale: Float): String {
            return TriangleBatch.Source.VERT_UNIFS +
                    "uniform float u_Angle;\n" +
                    "uniform vec2 u_Eye;\n" +
                    TriangleBatch.Source.VERT_ATTRS +
                    TriangleBatch.Source.PER_VERT_ATTRS +
                    TriangleBatch.Source.VERT_VARS +

                    "void main(void) {\n" +
                    // Transform the vertex per the normal screen transform
                    "  mat4 transform = mat4(\n" +
                    "    a_Matrix[0],      a_Matrix[1],      0, 0,\n" +
                    "    a_Matrix[2],      a_Matrix[3],      0, 0,\n" +
                    "    0,                0,                1, 0,\n" +
                    "    a_Translation[0], a_Translation[1], 0, 1);\n" +
                    "  vec4 pos = transform * vec4(a_Position, 0, 1);\n" +

                    // Rotate the vertex per our 3D rotation
                    "  float cosa = cos(u_Angle);\n" +
                    "  float sina = sin(u_Angle);\n" +
                    "  mat4 rotmat = mat4(\n" +
                    "    cosa, 0, sina, 0,\n" +
                    "    0,    1, 0,    0,\n" +
                    "   -sina, 0, cosa, 0,\n" +
                    "    0,    0, 0,    1);\n" +
                    "  pos = rotmat * vec4(pos.x - u_Eye.x,\n" +
                    "                      pos.y - u_Eye.y,\n" +
                    "                      0, 1);\n" +

                    // Perspective project the vertex back into the plane
                    "  mat4 persp = mat4(\n" +
                    "    1, 0, 0, 0,\n" +
                    "    0, 1, 0, 0,\n" +
                    "    0, 0, 1, -1.0/2000.0,\n" +
                    "    0, 0, 0, 1);\n" +
                    "  pos = persp * pos;\n" +
                    "  pos += vec4(u_Eye.x,\n" +
                    "              u_Eye.y, 0, 0);\n" +

                    // Finally convert the coordinates into OpenGL space
                    "  pos.xy /= u_HScreenSize.xy;\n" +
                    "  pos.z  /= (u_HScreenSize.x * " + ShaderUtil.format(zScale) + ");\n" +
                    "  pos.xy -= 1.0;\n" +
                    // z may already be rotated into negative space so we don't shift it
                    "  pos.y  *= u_Flip;\n" +
                    "  gl_Position = pos;\n" +

                    TriangleBatch.Source.VERT_SETTEX +
                    TriangleBatch.Source.VERT_SETCOLOR +
                    "}"
        }
    }
}
