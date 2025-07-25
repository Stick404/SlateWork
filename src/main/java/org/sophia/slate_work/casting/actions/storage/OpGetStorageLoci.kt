package org.sophia.slate_work.casting.actions.storage

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.ListIota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtHelper

object OpGetStorageLoci : Action {
    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        if (env !is CircleCastEnv)
            throw MishapNoSpellCircle()
        val stack = image.stack.toMutableList()
        val list: ArrayList<Vec3Iota> = ArrayList()
        val nbt = env.circleState().currentImage.userData.getList("storage_loci", NbtElement.COMPOUND_TYPE.toInt())

        for (itemTemp in nbt){
            val z = itemTemp as NbtCompound
            val x = NbtHelper.toBlockPos(z).toCenterPos()
            list.add(Vec3Iota(x))
        }

        stack.add(ListIota(list as List<Iota>))
        val image2 = image.withUsedOp().copy(stack = stack)

        return OperationResult(image2, listOf(),continuation, HexEvalSounds.NORMAL_EXECUTE)
    }
}