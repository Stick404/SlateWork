package org.sophia.slate_work.mixins;

import at.petrak.hexcasting.api.casting.eval.CastingEnvironment;
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.item.ItemStack;
import org.sophia.slate_work.misc.CircleHelper;
import org.spongepowered.asm.mixin.Mixin;

import java.util.ArrayList;
import java.util.function.Predicate;

@Mixin(CastingEnvironment.class)
abstract public class MixinCastEnv extends Object {

    // look away! i feel ashamed!!
    @WrapMethod(method = "withdrawItem", remap = false)
    private boolean slate_work$withdrawItem(Predicate<ItemStack> stackOk, int count, boolean actuallyRemove, Operation<Boolean> original) {
        if (!((Object) this instanceof CircleCastEnv)) return original.call(stackOk, count, actuallyRemove);

        // reimplement withdrawItem with storage loci itemslots instead
        var storages = CircleHelper.INSTANCE.getStorage((CircleCastEnv) (Object) this);
        if (storages.isEmpty()) return original.call(stackOk, count, actuallyRemove);

        var hashMap = CircleHelper.INSTANCE.getLists(storages);

        long presentCount = 0;
        var matches = new ArrayList<CircleHelper.ItemSlot>();
        for (CircleHelper.ItemSlot item : hashMap.values()) {
            if (stackOk.test(item.getItem().toStack((int) item.getCount()))) {
                presentCount += item.getCount();
                matches.add(item);

                if (presentCount >= count) break;
            }
        }
        if (presentCount < count) return false;
        if (!actuallyRemove) return true;

        // i don't actually know if a transaction is necessary here; copying from DumbDumbHexIsStupid
        long remaining = count;
        var trans = Transaction.openOuter();
        for (CircleHelper.ItemSlot item : matches) {
            var extracted = item.getStorageLociEntity().extract(item.getItem(), remaining, trans);
            remaining -= extracted;

            if (remaining <= 0) break;
        }
        trans.commit();

        return true;
    }
}
