package org.sophia.slate_work.client.lens

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry.OverlayBuilder
import com.mojang.datafixers.util.Pair
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import org.sophia.slate_work.blocks.SaveLoci.TOP_PART
import org.sophia.slate_work.blocks.entities.SaveLociEntity

class SaveLociScryingKT : OverlayBuilder {
    override fun addLines(
        lines: MutableList<Pair<ItemStack, Text>>, state: BlockState, pos: BlockPos, observer: PlayerEntity, world: World, hitFace: Direction) {
        var offset: Int = 0
        if (state.get(TOP_PART)) offset = -1;
        val entity = world.getBlockEntity(pos.up(offset))
        if (entity !is SaveLociEntity)
            return
        val data = entity.save

        //lines.add(Pair(ItemStack.EMPTY, Text.of("Unfinished!")))
        val userData: NbtCompound = data.userData
        lines.add(Pair(ItemStack.EMPTY, Text.translatable("slate_work.scrying.save.stack")
            .append(stackIotas(data))))
        //TODO: Display the ravenmind too
        lines.add(Pair(ItemStack.EMPTY, Text.translatable("slate_work.scrying.save.ravenmind")
                .append(if (userData.contains(HexAPI.RAVENMIND_USERDATA)) {
                    val ravenmind = userData.getCompound(HexAPI.RAVENMIND_USERDATA)
                    val iota = IotaType.TYPED_CODEC.decode(NbtOps.INSTANCE, ravenmind)
                    if (iota.isError) {
                        NullIota.DISPLAY
                    } else {
                        iota.orThrow.first.display()
                    }
                } else NullIota.DISPLAY)))
        lines.add(Pair(ItemStack.EMPTY, Text.translatable("slate_work.scrying.save.ops")
                .append(Text.literal(data.opsConsumed.toString()).formatted(Formatting.GREEN))))
        lines.add(Pair(ItemStack.EMPTY, Text.translatable("slate_work.scrying.save.escaping")
            .append(
                if (data.escapeNext) Text.translatable("hexcasting.tooltip.boolean_true").formatted(Formatting.DARK_GREEN)
                else Text.translatable("hexcasting.tooltip.boolean_false").formatted(Formatting.DARK_RED))))
        lines.add(Pair(ItemStack.EMPTY, Text.translatable("slate_work.scrying.save.escaped")
            .append(escapedIotas(data))))
        lines.add(Pair(ItemStack.EMPTY, Text.translatable("slate_work.scrying.save.paren")
                .append(Text.literal((data.parenCount).toString()).formatted(Formatting.GREEN))))

    }




    fun escapedIotas(data: CastingImage): Text {
        val text: MutableText = Text.empty()

        for (iota: CastingImage.ParenthesizedIota in data.parenthesized) {
            text.append(iota.iota.display()).append(" ")
        }
        return text
    }

    fun stackIotas(data: CastingImage): Text {
        val text: MutableText = Text.empty()
        for (iota: Iota in data.stack) {
            text.append(iota.display()).append(", ")
        }
        return text
    }
}