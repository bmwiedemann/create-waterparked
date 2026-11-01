package net.omori_sunny.create_waterparked.client.compat.shaderpack

/**
 * Bliss (Chocapic13 edit, with its Colorwheel programs). Water is
 * `mc_Entity.x == 8.0` (BLOCK_WATER); Colorwheel renames mc_Entity to
 * clrwl_vertexEntity, so stamping 8 keeps the pack's own water handling
 * (e.g. clrwl_shadow drops BLOCK_WATER from the shadow map).
 *
 * The clrwl translucent vertex program is compiled with ENTITIES /
 * BLOCKENTITIES defined, so right after the water check it overwrites the
 * mask with the entity value (mat = 0.9) and the fragment program never sees
 * isWater (MATERIALS > 0.99). [injectWaterMat] re-asserts the water mask just
 * before the mask is written to normalMat.
 */
object BlissWaterAdapter : ShaderpackWaterAdapter {

    override val waterStampId: Int = 8

    override val injectsWaterMat: Boolean = true

    private const val MARKER = "WATERPARKED bliss water mask"

    private val MASK_WRITE = Regex("normalMat\\s*\\.\\s*(a|w)\\s*=\\s*mat\\s*;")

    override fun matches(packName: String): Boolean =
        packName.contains("bliss", ignoreCase = true)

    override fun injectWaterMat(source: String): String? {
        if (!source.contains("gl_Position")) return null
        if (!source.contains("clrwl_vertexEntity")) return null
        if (source.contains(MARKER)) return null
        val write = MASK_WRITE.find(source) ?: return null
        // float only comparison, compiles on every GLSL version
        val code = "// $MARKER\n" +
            "\tif (clrwl_vertexEntity.x > 7.5 && clrwl_vertexEntity.x < 8.5) mat = 1.0;\n\t"
        return source.substring(0, write.range.first) + code + source.substring(write.range.first)
    }
}
