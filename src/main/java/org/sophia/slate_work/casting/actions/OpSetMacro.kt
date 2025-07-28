package org.sophia.slate_work.casting.actions

import at.petrak.hexcasting.api.casting.castables.ConstMediaAction
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.getBlockPos
import at.petrak.hexcasting.api.casting.getPattern
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.misc.MediaConstants
import org.sophia.slate_work.blocks.entities.MacroLociEntity
import org.sophia.slate_work.casting.mishap.MishapNeededFocus
import org.sophia.slate_work.casting.mishap.MishapWrongBlock
import org.sophia.slate_work.registries.BlockRegistry

object OpSetMacro : ConstMediaAction {
    override val argc: Int
        get() = 3

    override fun execute(args: List<Iota>, env: CastingEnvironment): List<Iota> {
        val target = args.getBlockPos(0, argc)
        val iota = args[1]
        val pattern = args.getPattern(2, argc)

        val entity = env.world.getBlockEntity(target)
        if (entity !is MacroLociEntity) {
            throw MishapWrongBlock(target,
                BlockRegistry.MACRO_LOCI,
                env.world.getBlockState(target).block)
        }
        if (entity.isEmpty)
            throw MishapNeededFocus(target)

        entity.setFocusContents(iota, env)
        entity.setPattern(pattern)


        return listOf()
    }

    override val mediaCost: Long
        get() = MediaConstants.DUST_UNIT / 100
}