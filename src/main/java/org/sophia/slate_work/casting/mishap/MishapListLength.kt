package org.sophia.slate_work.casting.mishap

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.pigment.FrozenPigment
import net.minecraft.text.Text
import net.minecraft.util.DyeColor

class MishapListLength(val needed: Int, val got: Int) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment
        = dyeColor(DyeColor.GRAY)

    override fun errorMessage(ctx: CastingEnvironment, errorCtx: Context): Text? =
        error("list_length",
            Text.literal(needed.toString()),
            Text.literal(got.toString()))


    override fun execute(env: CastingEnvironment, errorCtx: Context,stack: MutableList<Iota>) {}
}