package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

/**
 * To any poor, poor code divers read things, this is a janky-hacky mixin; though we do believe this is clear.
 * What this mixin does though, is recreate <a href="https://github.com/FallingColors/HexMod/pull/908">this</a> PR for Spell Circles.
 * I am not proud of this mixin, frankly, I hate it. But, it works well enough, and it stops Spell Circles from being a Memory/NBT Leak disaster
 */
@Mixin(CircleExecutionState.class)
public abstract class MixinDirtyDirtyHack {
    @WrapOperation(method = "tick",
            at = @At(value = "FIELD",
                    target = "Lat/petrak/hexcasting/api/casting/circles/CircleExecutionState;reachedPositions:Ljava/util/List;",
                    opcode = Opcodes.AASTORE),
            remap = false
    )
    private List<BlockPos> Slate_work$DirtyPatch(CircleExecutionState instance, Operation<List<BlockPos>> original){
        var list = instance.reachedPositions;
        var pos = list.indexOf(instance.currentPos);

        // OHH FUCK THIS IS JANKY
        if (pos != -1 && list.size() > 32) {
            list.remove(pos);
        }

        return list;
    }
}
