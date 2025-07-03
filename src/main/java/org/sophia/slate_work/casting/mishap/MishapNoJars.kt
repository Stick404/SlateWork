package org.sophia.slate_work.casting.mishap

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import net.minecraft.text.Text
import net.minecraft.util.DyeColor

class MishapNoJars : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context) = dyeColor(DyeColor.GRAY)

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Text? =
        error("circle.no_jars_ran")

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {} // TODO: Make it uhhh, do something
}