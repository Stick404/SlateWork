package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import net.minecraft.util.math.BlockPos;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.List;

@Mixin(CircleExecutionState.class)
public class MixinDirtyDirtyHack {
    @Redirect(method = "tick",
            at = @At(value = "FIELD",
                    target = "Lat/petrak/hexcasting/api/casting/circles/CircleExecutionState;reachedPositions:Ljava/util/List;",
                    opcode = Opcodes.AASTORE),
            remap = false
    )
    private List<BlockPos> Slate_work$DirtyPatch(CircleExecutionState instance){
        var list = instance.reachedPositions;
        if (!list.contains(instance.currentPos)){
            list.add(instance.currentPos);
        }
        return list;
    }
}
