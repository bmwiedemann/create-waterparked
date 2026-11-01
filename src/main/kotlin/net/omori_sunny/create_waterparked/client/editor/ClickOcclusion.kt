package net.omori_sunny.create_waterparked.client.editor

import net.minecraft.client.Minecraft
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import net.neoforged.api.distmarker.Dist
import net.neoforged.api.distmarker.OnlyIn

// shared gate for the slide pickers (supports, ghost blocks, walls): a custom
// pick may only own a click when no real block sits in front of it on the ray
@OnlyIn(Dist.CLIENT)
object ClickOcclusion {

    private const val EPS = 1.0e-3

    // eye -> vanilla block hit distance; +inf when the crosshair is not on a block
    @JvmStatic
    fun realHitDistance(mc: Minecraft): Double {
        val player = mc.player ?: return Double.POSITIVE_INFINITY
        val hit = mc.hitResult ?: return Double.POSITIVE_INFINITY
        if (hit.type != HitResult.Type.BLOCK) return Double.POSITIVE_INFINITY
        return player.eyePosition.distanceTo(hit.location)
    }

    // true when a real block is hit before the custom geometry at distance d
    @JvmStatic
    fun occluded(mc: Minecraft, d: Double): Boolean = d > realHitDistance(mc) + EPS

    // along-ray distance to a box, or null when the ray misses it
    @JvmStatic
    fun rayDistanceTo(box: AABB, eye: Vec3, view: Vec3, range: Double): Double? {
        if (box.contains(eye)) return 0.0
        val hit = box.clip(eye, eye.add(view.scale(range)))
        return if (hit.isPresent) eye.distanceTo(hit.get()) else null
    }
}
