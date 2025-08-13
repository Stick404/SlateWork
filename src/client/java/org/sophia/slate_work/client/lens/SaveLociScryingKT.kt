package org.sophia.slate_work.client.lens

import at.petrak.hexcasting.api.HexAPI
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage.Companion.TAG_OPS_CONSUMED
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage.ParenthesizedIota.Companion.TAG_ESCAPED
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage.ParenthesizedIota.Companion.TAG_IOTAS
import at.petrak.hexcasting.api.casting.iota.IotaType
import at.petrak.hexcasting.api.casting.iota.NullIota
import at.petrak.hexcasting.api.client.ScryingLensOverlayRegistry.OverlayBuilder
import at.petrak.hexcasting.api.utils.downcast
import at.petrak.hexcasting.api.utils.getCompound
import at.petrak.hexcasting.api.utils.getList
import at.petrak.hexcasting.api.utils.zipWithDefault
import com.mojang.datafixers.util.Pair
import net.minecraft.block.BlockState
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
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
        val data: NbtCompound = entity.save
        val userData: NbtCompound = data.getCompound(CastingImage.TAG_USERDATA)
        lines.add(Pair(ItemStack.EMPTY, Text.translatable("slate_work.scrying.save.stack")
            .append(stackIotas(data))))
        //TODO: Display the ravenmind too
        lines.add(Pair(ItemStack.EMPTY, Text.translatable("slate_work.scrying.save.ravenmind")
                .append(if (userData.contains(HexAPI.RAVENMIND_USERDATA))
                    IotaType.getDisplay(userData.getCompound(HexAPI.RAVENMIND_USERDATA))
                    else NullIota.DISPLAY)))
        lines.add(Pair(ItemStack.EMPTY, Text.translatable("slate_work.scrying.save.ops")
                .append(Text.literal(data.getLong(TAG_OPS_CONSUMED).toString()).formatted(Formatting.GREEN))))
        lines.add(Pair(ItemStack.EMPTY, Text.translatable("slate_work.scrying.save.escaping")
            .append(
                if (data.getBoolean(CastingImage.TAG_ESCAPE_NEXT)) Text.translatable("hexcasting.tooltip.boolean_true").formatted(Formatting.DARK_GREEN)
                else Text.translatable("hexcasting.tooltip.boolean_false").formatted(Formatting.DARK_RED))))
        lines.add(Pair(ItemStack.EMPTY, Text.translatable("slate_work.scrying.save.escaped")
            .append(escapedIotas(data))))
        lines.add(Pair(ItemStack.EMPTY, Text.translatable("slate_work.scrying.save.paren")
                .append(Text.literal((data.getInt(CastingImage.TAG_PAREN_COUNT)).toString()).formatted(Formatting.GREEN))))
    }

    fun escapedIotas(data: NbtCompound): Text {
        val parenIotasTag = data.getCompound(CastingImage.TAG_PARENTHESIZED).getList(TAG_IOTAS,NbtElement.COMPOUND_TYPE)
        val text: MutableText = Text.empty()

        for (subtag: NbtElement in parenIotasTag.toList()) {
            text.append(IotaType.getDisplay(subtag.downcast(NbtCompound.TYPE)))
        }
        return text
    }
    fun stackIotas(data: NbtCompound): Text {
        val text: MutableText = Text.empty()
        val stack = data.getList(CastingImage.TAG_STACK,NbtElement.COMPOUND_TYPE)
        for (subtag: NbtElement in stack.toList()) {
            text.append(IotaType.getDisplay(subtag.downcast(NbtCompound.TYPE)))
        }
        return text
    }
}