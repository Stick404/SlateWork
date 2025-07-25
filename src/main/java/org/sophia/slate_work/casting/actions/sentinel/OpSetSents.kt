package org.sophia.slate_work.casting.actions.sentinel

import at.petrak.hexcasting.api.casting.castables.Action
import at.petrak.hexcasting.api.casting.eval.CastingEnvironment
import at.petrak.hexcasting.api.casting.eval.OperationResult
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.SpellContinuation
import at.petrak.hexcasting.api.casting.getList
import at.petrak.hexcasting.api.casting.getVec3
import at.petrak.hexcasting.api.casting.mishaps.MishapNotEnoughMedia
import at.petrak.hexcasting.api.casting.mishaps.circle.MishapNoSpellCircle
import at.petrak.hexcasting.api.misc.MediaConstants
import at.petrak.hexcasting.api.utils.putList
import at.petrak.hexcasting.common.lib.hex.HexEvalSounds
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.math.Vec3d
import org.sophia.slate_work.blocks.entities.SentinelLociEntity
import org.sophia.slate_work.casting.mishap.MishapListLength
import org.sophia.slate_work.casting.mishap.MishapNoSentinelLoci
import org.sophia.slate_work.misc.CircleHelper

object OpSetSents : Action {

    override fun operate(
        env: CastingEnvironment,
        image: CastingImage,
        continuation: SpellContinuation
    ): OperationResult {
        if (env !is CircleCastEnv){
            throw MishapNoSpellCircle()
        }
        val args = image.stack.toMutableList()
        val inputList = args.getList(0,1)
        args.removeLast() // I think?
        val realList = mutableListOf<Vec3d>()
        var i = 0

        for (z in inputList){
            val vec = inputList.toList().getVec3(i)
            env.assertVecInRange(vec)
            realList.add(vec)
            i++
        }

        val data = image.userData.copy()
        val sentList = data.getList("sentinel_loci", NbtCompound.COMPOUND_TYPE.toInt())
        val sentTime = data.getLong("sentinel_time")

        val loci = CircleHelper.getSentLoci(env)
        if (loci.isEmpty()) {
            throw MishapNoSentinelLoci()
        }

        if (realList.size > loci.size){ // If there are too many items in the list, mishap
            throw MishapListLength(loci.size, realList.size)
        }
        i = 0
        val list = mutableListOf<Pair<Vec3d, SentinelLociEntity>>()
        var cost = MediaConstants.DUST_UNIT

        for (z in realList){

            val nbt = (sentList[i] as NbtCompound)
            if (sentTime != env.world.time){
                nbt.putLong("count", 0) // Clears the current "count" if its not the world time
            }

            if (loci[i].sentPos != realList[i]){
                nbt.putLong("count", nbt.getLong("count")+1)
                sentList[i] = nbt
                list.add(Pair(realList[i], loci[i]))
            }

            cost += (nbt.getLong("count") -1)* (MediaConstants.DUST_UNIT/8)
            i++
        }

        val media = env.extractMedia(cost, false)
        if (media != 0L) {
            throw MishapNotEnoughMedia(media)
        } // Ok cool, we have enough Media if we get past this point

        for (z in list){
            z.second.sentPos = z.first
        }


        data.putList("sentinel_loci",sentList)
        data.putLong("sentinel_time", env.world.time)

        return OperationResult(
            image.copy(stack = args, userData = data),
            listOf(),
            continuation,
            HexEvalSounds.NORMAL_EXECUTE
            )
    }
}