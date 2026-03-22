package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;
import at.petrak.hexcasting.api.pigment.FrozenPigment;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mixin(CircleExecutionState.class)
public interface INewCustomCircleExec {
    @Invoker("<init>")
    static CircleExecutionState newState(BlockPos impetusPos, Direction impetusDir, Set<BlockPos> knownPositions,
                         List<BlockPos> reachedPositions, BlockPos currentPos, Direction enteredFrom,
                         CastingImage currentImage, @Nullable UUID caster, @Nullable FrozenPigment casterPigment){
        throw new AssertionError();
    }
}
