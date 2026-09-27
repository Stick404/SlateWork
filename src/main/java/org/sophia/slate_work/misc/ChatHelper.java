package org.sophia.slate_work.misc;

import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Pair;
import org.jetbrains.annotations.Nullable;
import org.sophia.slate_work.blocks.impetus.ListeningImpetusEntity;
import org.sophia.slate_work.compat.SlateWorksTrinkets;
import org.sophia.slate_work.registries.SlateWorksAttributeRegistry;
import org.sophia.slate_work.registries.SlateWorksBlockRegistry;
import org.sophia.slate_work.registries.SlateWorksComponents;
import org.sophia.slate_work.saving.Listeners;

import java.util.Optional;

public class ChatHelper {
    private static ChatHelper helper;
    private Boolean TRINKETS = false;
    public Pair<String, Long> LAST_CHECK_DATA = new Pair<>("", 0L);
    public ShouldRun LAST_CHECK = new ShouldRun(false, false, "", Optional.empty(), false, Optional.empty());

    public static ChatHelper getHelper() {
        if (helper == null){
            return new ChatHelper();
        }
        return helper;
    }

    private ChatHelper(){
        helper = this;
    }

    public void setTRINKETS(Boolean TRINKETS) {
        this.TRINKETS = TRINKETS;
    }

    public ShouldRun of(boolean blocked, boolean item, String string, Optional<ListeningImpetusEntity> entity, boolean failed, Optional<ItemStack> whispering){
        ShouldRun record = new ShouldRun(blocked, item, string, entity, failed, whispering);
        if (entity.isPresent() && entity.get().getWorld() != null) {
            this.LAST_CHECK = record;
            this.LAST_CHECK_DATA = new Pair<>(string, entity.get().getWorld().getTime());
        }
        return record;
    }

    public ChatHelper.ShouldRun willBlock(String compared, ServerPlayerEntity sender){
        if (compared.equals(LAST_CHECK_DATA.getLeft()) && sender.getWorld().getTime() == LAST_CHECK_DATA.getRight()){
            return LAST_CHECK;
        }
        ServerWorld world = sender.getServerWorld();
        if (sender.getAttributeValue(SlateWorksAttributeRegistry.WHISPERING) > 0) {
            var ref = new ItemHold();
            sender.getHandItems().iterator().forEachRemaining((item) -> {
                if (item.isOf(SlateWorksBlockRegistry.WHISPERING_STONE)) {
                    ref.stack = item;
                }
            });
            if (ref.stack == null) {
                sender.getArmorItems().forEach((item) -> {
                    if (item.isOf(SlateWorksBlockRegistry.WHISPERING_STONE)) {
                        ref.stack = item;
                    }
                });
            }
            if (ref.stack == null && TRINKETS) {
                SlateWorksTrinkets.finder(ref, sender);
            }

            if (ref.stack != null) {
                SlateWorksComponents.WhisperingStoneComponent component = ref.stack.getOrDefault(
                        SlateWorksComponents.WHISPERING_STONE_COMPONENT, null);

                if (component != null) {
                    if (world.getBlockEntity(component.pos()) instanceof ListeningImpetusEntity entity) {
                        if (compared.startsWith(entity.getString())) {
                            if (entity.isRunning()) {
                                return of(true, true, compared, Optional.empty(), false, Optional.of(ref.stack));
                            } else {
                                return of(true, true, compared, Optional.of(entity), false, Optional.of(ref.stack));
                            }
                        }
                    } else {
                        if (compared.startsWith(component.string())) {
                            return of(true, true, compared, Optional.empty(), true, Optional.of(ref.stack));
                        }
                    }

                }
            }
        }
        for (var listen : Listeners.getListenersAroundPos(sender.getServerWorld(), sender.getBlockPos())){
            if (world.getBlockEntity(listen) instanceof ListeningImpetusEntity entity){
                if (entity.isRunning()) continue;
                if (entity.getPos().getSquaredDistance(sender.getPos()) < 16*16 && compared.startsWith(entity.getString()))
                    return of(true, false, compared, Optional.of(entity), false, Optional.empty());
            }
        }
        return of(false, false, compared, Optional.empty(), false, Optional.empty());
    }


    /**
     * @param blocked is if the message should be blocked
     * @param item is if it was casted via the item
     * @param string is the message that will be sent to the Listening Impetus Entity
     * @param entity is the Listening Impetus Entity
     * @param failed is if the item "hard failed" (IE: if it should be cleared)
     * @param whispering is the Whispering Stone that was used
     *
     * **/
    public record ShouldRun(boolean blocked, boolean item, String string, Optional<ListeningImpetusEntity> entity, boolean failed, Optional<ItemStack> whispering){}
    public static class ItemHold {
        @Nullable ItemStack stack;

        public @Nullable ItemStack getStack() {
            return stack;
        }

        public void setStack(@Nullable ItemStack stack) {
            this.stack = stack;
        }
    }
}
