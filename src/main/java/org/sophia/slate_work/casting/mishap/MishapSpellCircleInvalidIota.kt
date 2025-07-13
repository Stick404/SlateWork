package org.sophia.slate_work.casting.mishap

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.iota.GarbageIota
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.mishaps.Mishap
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.api.pigment.FrozenPigment
import at.petrak.hexcasting.api.utils.asTranslatedComponent
import at.petrak.hexcasting.api.utils.styledWith
import net.minecraft.text.Text
import net.minecraft.util.DyeColor
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import kotlin.text.append

class MishapSpellCircleInvalidIota(
    val perpetrator: Iota,
    val reverseIdx: Int,
    val expected: Text,
    val pos: BlockPos
) : Mishap() {
    override fun accentColor(ctx: CastingEnvironment, errorCtx: Context): FrozenPigment =
        dyeColor(DyeColor.GRAY)

    override fun errorMessage(ctx: CastingEnvironment,errorCtx: Context): Text? =
        error("circle.invalid_value",expected, reverseIdx,
            Text.literal("(").append(pos.toShortString()).append(")").styledWith(Formatting.RED),
            perpetrator.display()
        )

    override fun execute(env: CastingEnvironment, errorCtx: Context, stack: MutableList<Iota>) {
        stack[stack.size - 1 - reverseIdx] = GarbageIota();
    }

    companion object {
        @JvmStatic
        fun ofType(perpetrator: Iota, reverseIdx: Int, name: String, pos: BlockPos): MishapSpellCircleInvalidIota {
            return of(perpetrator, reverseIdx, "class.$name", pos = pos)
        }

        @JvmStatic
        fun of(perpetrator: Iota, reverseIdx: Int, name: String, vararg translations: Any, pos: BlockPos): MishapSpellCircleInvalidIota {
            val key = "hexcasting.mishap.invalid_value.$name"
            return MishapSpellCircleInvalidIota(perpetrator, reverseIdx, key.asTranslatedComponent(*translations), pos)
        }
    }
}