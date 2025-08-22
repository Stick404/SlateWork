package org.sophia.slate_work.blocks;

import at.petrak.hexcasting.api.casting.circles.ICircleComponent;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.casting.iota.NullIota;
import at.petrak.hexcasting.api.casting.iota.PatternIota;
import at.petrak.hexcasting.common.blocks.akashic.AkashicFloodfiller;
import at.petrak.hexcasting.common.blocks.akashic.BlockAkashicRecord;
import at.petrak.hexcasting.common.blocks.akashic.BlockEntityAkashicBookshelf;
import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleInvalidIota;
import org.sophia.slate_work.casting.mishap.MishapSpellCircleNotEnoughArgs;

import java.util.ArrayList;

/**
 * Future note for what this does: <br>
 *  Checks if the top of the stack is a pattern iota, and returns the bound iota (or null if there is none). <br> <br>
 *  If the top is not a pattern iota, it checks if the iota below is a pattern iota. If so, tries to store that into the Record. As well, if the top iota is Null, it clears the bound key
 **/
public class AkashicRecordLoci {
    public static ICircleComponent.ControlFlow acceptControlFlow(CastingImage imageIn, CircleCastEnv env, Direction enterDir, BlockPos pos, BlockState bs, ServerWorld world, BlockAkashicRecord recordBlock) {
        var stack = new ArrayList<>(imageIn.getStack());
        var recordI = ((ICircleComponent)recordBlock);
        if (stack.isEmpty()) { // If the stack is empty, fail
            recordI.fakeThrowMishap(
                    pos, bs, imageIn, env,
                    new MishapSpellCircleNotEnoughArgs(1,0, pos)
            );
            return new ICircleComponent.ControlFlow.Stop();
        }

        var top = stack.get(stack.size() -1);
        stack.remove(stack.size() -1);
        if ((top instanceof PatternIota pattern)) { // If the top pattern is just a pattern, look for it
            var iota = recordBlock.lookupPattern(pos, pattern.getPattern(), world);
            if (iota == null) iota = new NullIota(); // Rude
            stack.add(iota);
        } else { // If it is not a pattern...
            if (stack.isEmpty()) { // If there is nothing else on the stack, fail
                recordI.fakeThrowMishap(
                        pos, bs, imageIn, env,
                        new MishapSpellCircleNotEnoughArgs(2,1, pos)
                );
                return new ICircleComponent.ControlFlow.Stop();
            }
            var second = stack.get(stack.size() -1);
            stack.remove(stack.size() -1);
            if (!(second instanceof PatternIota)){ // If the 2nd iota isn't a Pattern Iota, fail
                recordI.fakeThrowMishap(
                    pos, bs, imageIn, env,
                    MishapSpellCircleInvalidIota.ofType(second, 1, "pattern", pos)
                );
                return new ICircleComponent.ControlFlow.Stop();
            }
            var pattern = ((PatternIota) second).getPattern();
            if (top instanceof NullIota){ // If the top iota is a NullIota, clear the shelf
                var foundPos = AkashicFloodfiller.floodFillFor(pos, world,
                        (pos1, bs1, world1) ->
                                world1.getBlockEntity(pos1) instanceof BlockEntityAkashicBookshelf tile
                                        && tile.getPattern() != null && tile.getPattern().sigsEqual(pattern));
                if (foundPos != null && world.getBlockEntity(foundPos) instanceof BlockEntityAkashicBookshelf tile){
                    tile.clearIota();
                }
            } else { // else, add it
                recordBlock.addNewDatum(pos, world, pattern, top);
            }
        }

        var exitDirsSet = recordI.possibleExitDirections(pos, bs, world);
        exitDirsSet.remove(enterDir.getOpposite());
        var exits = exitDirsSet.stream().map((dir) -> recordI.exitPositionFromDirection(pos, dir)).toList();
        return new ICircleComponent.ControlFlow.Continue(imageIn.copy(stack, imageIn.getParenCount(),
                imageIn.getParenthesized(), imageIn.getEscapeNext(), imageIn.getOpsConsumed(), imageIn.getUserData()), exits);
    }
}
