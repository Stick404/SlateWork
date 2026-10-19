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
import net.minecraft.util.math.Vec3d
import org.sophia.slate_work.blocks.entities.SentinelLociEntity
import org.sophia.slate_work.casting.mishap.MishapListLength
import org.sophia.slate_work.casting.mishap.MishapNoSentinelLoci
import org.sophia.slate_work.misc.CircleHelper
import org.sophia.slate_work.registries.SlateWorksImageComponents

object OpSetSents : Action {

    override fun operate(env: CastingEnvironment, image: CastingImage, continuation: SpellContinuation): OperationResult {
        if (env !is CircleCastEnv){
            throw MishapNoSpellCircle()
        }

        var args = image.stack
        val inputList = args.getList(args.lastIndex,1)
        args = args.init()
        val realList = mutableListOf<Vec3d>()
        val toMove = mutableListOf<Pair<SentinelLociEntity, Vec3d>>()
        var i = 0

        for (z in inputList){
            val vec = inputList.toList().getVec3(i)
            realList.add(vec)
            i++
        }

        val sentList = image.getComponent(SlateWorksImageComponents.SENTINEL_LOCI).orEmpty()
        var sentCount = image.getComponent(SlateWorksImageComponents.SENTINEL_COUNT) ?: -1
        val hasMoved = sentCount != -1L

        val loci = CircleHelper.getSentLoci(env)
        if (loci.isEmpty()) {
            throw MishapNoSentinelLoci()
        }

        if (realList.size > loci.size){ // If there are too many items in the list, mishap
            throw MishapListLength(loci.size, realList.size)
        }

        env.extractMedia(MediaConstants.DUST_UNIT, false).let {
            if (it != 0L) throw MishapNotEnoughMedia(it)
        }

        i = 0
        val radius = 4
        for (z in realList){ // I am not using z because I do not trust it to match with `loci`
            var found = false
            for (check in loci){
                if (realList[i].squaredDistanceTo(check.sentPos) < radius * radius +0.00000000001) {
                    // Ambit checks can be *really* laggy, so we optimize it with this little loop
                    found = true
                    break;
                }
            }
            if (!found){
                env.assertVecInRange(realList[i])
            }

            // If it is moved, we increment count of how many have been moved
            if (hasMoved && loci[i].sentPos != realList[i]){
                sentCount +=1
            }

            toMove.add(Pair(loci[i], realList[i]))
            i++
        }

        env.extractMedia((sentCount) * (MediaConstants.DUST_UNIT/8), false).let {
            if (it != 0L) throw MishapNotEnoughMedia(it)
        }

        return OperationResult(
            image.copy(stack = args)
                .withComponent(SlateWorksImageComponents.SENTINEL_COUNT, if (!hasMoved) 0 else sentCount),
            listOf(),
            continuation,
            HexEvalSounds.NORMAL_EXECUTE.get()
            )
    }
}