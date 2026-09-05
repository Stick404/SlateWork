package org.sophia.slate_work.casting.mishap

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.utils.styledWith
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos

class MishapNoStorageLoci(
    val pos: BlockPos?
) : Mishap() {
    override fun accentColor(env: CastingEnvironment, errorCtx: Context) = dyeColor(DyeColor.GRAY)

    override fun errorMessage(env: CastingEnvironment, errorCtx: Context): Text {
        if (env is CircleCastEnv && pos != null)
            return error("circle.no_storage_loci_ran", Text.literal("(").append(pos.toShortString()).append(")").styledWith(Formatting.RED))
        return error("no_storage_loci_ran")
    }

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {}
}