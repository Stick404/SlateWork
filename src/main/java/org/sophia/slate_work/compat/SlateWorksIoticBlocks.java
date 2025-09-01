package org.sophia.slate_work.compat;

import gay.object.ioticblocks.api.IoticBlocksAPI;
import net.minecraft.util.Identifier;
import org.sophia.slate_work.blocks.impetus.ListeningImpetusEntity;

import static org.sophia.slate_work.Slate_work.MOD_ID;

public class SlateWorksIoticBlocks {
    public static void init(){
        IoticBlocksAPI.INSTANCE.registerIotaHolderProvider(new Identifier(MOD_ID, "listening_impetus"), (z, x) -> {
            var entity = z.getBlockEntity(x);
            if (entity instanceof ListeningImpetusEntity listener) return listener;
            return null;
        });
    }
}
