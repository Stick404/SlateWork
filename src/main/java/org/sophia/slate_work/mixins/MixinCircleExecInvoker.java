package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.circles.CircleExecutionState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(CircleExecutionState.class)
public interface MixinCircleExecInvoker {

    @Invoker(value = "getTickSpeed", remap = false)
    int slate_work$getTickSpeed();
}
