package org.sophia.slate_work.misc;

import at.petrak.hexcasting.api.casting.eval.vm.CastingImage;

public interface ICircleSpeedValue {
    // Makes the next return of `CircleExecutionState#getTickSpeed` return its true speed
    int slate_work$getTickSpeed();
    void slate_work$getRealValue();
    void slate_work$setImage(CastingImage image);
}
