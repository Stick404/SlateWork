package org.sophia.slate_work.blocks

import at.petrak.hexcasting.api.casting.circles.ICircleComponent.ControlFlow
import at.petrak.hexcasting.api.casting.eval.env.CircleCastEnv
import at.petrak.hexcasting.api.casting.eval.vm.CastingImage
import at.petrak.hexcasting.api.casting.iota.Iota
import at.petrak.hexcasting.api.casting.iota.Vec3Iota
import at.petrak.hexcasting.api.casting.mishaps.MishapInvalidIota
import at.petrak.hexcasting.common.blocks.circles.BlockSlate
import at.petrak.hexcasting.common.lib.HexSounds
import at.petrak.hexcasting.interop.HexInterop
import com.mojang.datafixers.util.Pair
import net.fabricmc.fabric.api.event.player.UseBlockCallback
import net.fabricmc.fabric.api.event.player.UseEntityCallback
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.block.Block
import net.minecraft.block.BlockState
import net.minecraft.block.ShapeContext
import net.minecraft.command.argument.EntityAnchorArgumentType
import net.minecraft.entity.Entity
import net.minecraft.entity.ExperienceOrbEntity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.passive.VillagerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemUsageContext
import net.minecraft.nbt.NbtHelper
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.state.StateManager
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.util.shape.VoxelShape
import net.minecraft.util.shape.VoxelShapes
import net.minecraft.world.BlockView
import net.minecraft.world.RaycastContext
import net.minecraft.world.World
import org.sophia.slate_work.Slate_work.IS_LEFT_CLICKING
import org.sophia.slate_work.Slate_work.IS_OPTIONAL_VECTOR
import org.sophia.slate_work.blocks.entities.HotbarLociEntity
import org.sophia.slate_work.casting.mishap.MishapNoHotbarLoci
import org.sophia.slate_work.casting.mishap.MishapSpellCircleInvalidIota.Companion.ofType
import org.sophia.slate_work.casting.mishap.MishapSpellCircleNotEnoughArgs
import org.sophia.slate_work.compat.SlateWorksTrinkets
import org.sophia.slate_work.storage.SlateFakePlayer
import java.util.stream.Collectors
import java.util.stream.Stream
import kotlin.math.roundToInt
import kotlin.math.sign


class FakePlayerLoci : AbstractSlate {
    val hasTrinkets: Boolean = FabricLoader.getInstance().isModLoaded(HexInterop.Fabric.TRINKETS_API_ID)

    constructor(settings: Settings) : super(settings) {
        this.defaultState = this.stateManager.getDefaultState().with(IS_OPTIONAL_VECTOR, false).with(ENERGIZED, false).with(WATERLOGGED, false).with(IS_LEFT_CLICKING, true)
    }

    override fun appendProperties(builder: StateManager.Builder<Block?, BlockState?>) {
        super.appendProperties(builder)
        builder.add(IS_OPTIONAL_VECTOR).add(IS_LEFT_CLICKING)
    }

    override fun onUse(
        state: BlockState,
        world: World,
        pos: BlockPos?,
        player: PlayerEntity,
        hand: Hand?,
        hit: BlockHitResult?
    ): ActionResult {
        if (player.isSneaky) {
            val sta = state.get(IS_OPTIONAL_VECTOR).not()
            world.setBlockState(pos, state.with(IS_OPTIONAL_VECTOR, sta))
            world.playSound(player, pos, HexSounds.ABACUS_SHAKE, SoundCategory.BLOCKS, 1.0f, 1f)
            return ActionResult.CONSUME
        } else {
            val sta = state.get(IS_LEFT_CLICKING).not()
            world.setBlockState(pos, state.with(IS_LEFT_CLICKING, sta))
            world.playSound(player, pos, HexSounds.FLIGHT_FINISH, SoundCategory.BLOCKS, 1.0f, 1f)
            return ActionResult.CONSUME
        }
    }

    val UP_AB: VoxelShape? = VoxelShapes.union(
        BlockSlate.AABB_FLOOR,
        createCuboidShape(1.0, 1.0, 1.0, 15.0, 6.0, 15.0),
        createCuboidShape(3.0, 6.0, 3.0, 13.0, 10.0, 13.0)
    )

    val DOWN_AB: VoxelShape? = VoxelShapes.union(
        BlockSlate.AABB_CEILING,
        createCuboidShape(1.0, (16 - 6).toDouble(), 1.0, 15.0, (16 - 1).toDouble(), 15.0),
        createCuboidShape(3.0, (16 - 10).toDouble(), 3.0, 13.0, (16 - 6).toDouble(), 13.0)
    )

    val EAST_AB: VoxelShape? = VoxelShapes.union(
        BlockSlate.AABB_EAST_WALL,
        createCuboidShape(1.0, 1.0, 1.0, 6.0, 15.0, 15.0),
        createCuboidShape(3.0, 3.0, 3.0, 10.0, 13.0, 13.0)
    )

    val WEST_AB: VoxelShape? = VoxelShapes.union(
        BlockSlate.AABB_WEST_WALL,
        createCuboidShape((16 - 6).toDouble(), 1.0, 1.0, (16 - 1).toDouble(), 15.0, 15.0),
        createCuboidShape((16 - 10).toDouble(), 3.0, 3.0, (16 - 3).toDouble(), 16.0-3, 13.0)
    )

    val SOUTH_AB: VoxelShape? = VoxelShapes.union(
        BlockSlate.AABB_SOUTH_WALL,
        createCuboidShape(1.0, 1.0, 1.0, 15.0, 15.0, 6.0),
        createCuboidShape(3.0, 3.0, 3.0, 13.0, 13.0, 10.0)
    )

    val NORTH_AB: VoxelShape? = VoxelShapes.union(
        BlockSlate.AABB_NORTH_WALL,
        createCuboidShape(1.0, 1.0, (16 - 6).toDouble(), 15.0, 15.0, (16 - 1).toDouble()),
        createCuboidShape(3.0, 3.0, (16 - 10).toDouble(), 13.0, 13.0, (16 - 3).toDouble())
    )

    override fun getOutlineShape(
        state: BlockState,
        world: BlockView?,
        pos: BlockPos?,
        context: ShapeContext?
    ): VoxelShape? {
        return when (state.get(FACING)) {
            Direction.DOWN -> DOWN_AB
            Direction.UP -> UP_AB
            Direction.NORTH -> NORTH_AB
            Direction.SOUTH -> SOUTH_AB
            Direction.WEST -> WEST_AB
            Direction.EAST -> EAST_AB
        }
    }

    override fun acceptControlFlow(
        imageIn: CastingImage,
        env: CircleCastEnv,
        enterDir: Direction,
        pos: BlockPos,
        bs: BlockState,
        world: ServerWorld
    ): ControlFlow {
        val hotbar = world.getBlockEntity(NbtHelper.toBlockPos(imageIn.userData.getCompound("hotbar_loci")))
        if (hotbar !is HotbarLociEntity) {
            this.fakeThrowMishap(
                pos, bs, imageIn, env,
                MishapNoHotbarLoci(pos)
            )
            return ControlFlow.Stop()
        }
        val hexStack: ArrayList<Iota> = ArrayList(imageIn.stack)
        val direction: Direction
        // Either sets direction to where the block is facing, or the provided vector
        if (bs.get(IS_OPTIONAL_VECTOR)) {
            if (hexStack.isEmpty()) {
                this.fakeThrowMishap(
                    pos, bs, imageIn, env,
                    MishapSpellCircleNotEnoughArgs(1, 0, pos)
                )
                return ControlFlow.Stop()
            }
            val last: Iota = hexStack.get(hexStack.size - 1)
            hexStack.removeAt(hexStack.size - 1)
            if (last !is Vec3Iota) {
                this.fakeThrowMishap(
                    pos, bs, imageIn, env,
                    ofType(last, 0, "vector", pos)
                )
                return ControlFlow.Stop()
            }
            val offset = last.vec3
            val temp = Direction.fromVector(offset.x.toInt().sign, offset.y.toInt().sign, offset.z.toInt().sign)
            if (temp == null) {
                this.fakeThrowMishap(
                    pos, bs, imageIn, env,
                    MishapInvalidIota.of(last, 0, "axis_vector_sw")
                )
                return ControlFlow.Stop()
            }
            direction = temp

        } else {
            direction = bs.get(FACING)
        }

        // Pre-calcs the exit dirs, since we may exit in a few different places
        val exitDirsSet = this.possibleExitDirections(pos, bs, world)
        exitDirsSet.remove(enterDir.opposite)
        val exitDirs: Stream<Pair<BlockPos?, Direction?>?>? = exitDirsSet.stream()
            .map<Pair<BlockPos?, Direction?>?> { dir: Direction? -> this.exitPositionFromDirection(pos, dir) }

        val fake = SlateFakePlayer(world, hotbar)
        val stack = hotbar.getSlotStack(hotbar.slot)

        val hit = world.raycast(
            RaycastContext(
                pos.toCenterPos().add(direction.offsetX.toDouble(), direction.offsetY.toDouble(),
                    direction.offsetZ.toDouble()
                ),
                pos.toCenterPos().add(direction.offsetX.toDouble()*10, direction.offsetY.toDouble()*10,
                    direction.offsetZ.toDouble()*10),
                RaycastContext.ShapeType.OUTLINE,
                RaycastContext.FluidHandling.NONE,
                fake
            )
        )
        val hitPos = hit.blockPos.toCenterPos()
        val startPos = pos.toCenterPos()
        val offset = hitPos.subtract(startPos).normalize()
        val missed = hit.type == HitResult.Type.MISS
        val pos = pos.toCenterPos()
        // A ton of different values that can be used to help later on
        // Offsets the Fake so things like snowballs can be tossed
        val fakePos = pos.subtract(0.0, 2.0, 0.0).add(offset)
        fake.setPos(fakePos.x, fakePos.y, fakePos.z)
        fake.lookAt(EntityAnchorArgumentType.EntityAnchor.FEET, hit.pos)
        val width = 0.4

        val entities: MutableList<Entity?>? = world.getEntitiesByClass(Entity::class.java,
                Box(pos, hit.pos).expand(width)) { true }
            .stream()
            .collect(Collectors.toList())
        // If we find any entities...
        if (!entities!!.isEmpty()) {
            // Get a random entity

            entities.sortWith(Comparator { entity, entity1 -> (entity!!.squaredDistanceTo(fakePos) - entity1!!.squaredDistanceTo(fakePos)).roundToInt() })
            val entity: Entity? = entities[0] //world.random.nextInt(entities.size)
            if (!bs.get(IS_LEFT_CLICKING)) {
                // If we are attacking, thwack em
                for (attempt in entities) {
                    if (attempt!!.isAttackable){
                        fake.attack(attempt)
                        break
                    }
                }
                end(fake)
                return ControlFlow.Continue(imageIn, exitDirs!!.toList())
            }
            // Else, attempt to click on them
            val cancelResult: ActionResult? =
                UseEntityCallback.EVENT.invoker().interact(fake, world, Hand.MAIN_HAND, entity, EntityHitResult(entity))
            if (cancelResult == null || cancelResult === ActionResult.PASS) {
                if (entity!!.interact(fake, Hand.MAIN_HAND) == ActionResult.CONSUME) {
                    if (entity is VillagerEntity) {
                        if (entity.customer is SlateFakePlayer) entity.customer = null
                    }
                    end(fake)
                    return ControlFlow.Continue(imageIn, exitDirs!!.toList())
                } else if (entity is LivingEntity && stack.useOnEntity(fake, entity, Hand.MAIN_HAND) == ActionResult.CONSUME) {
                    end(fake)
                    return ControlFlow.Continue(imageIn, exitDirs!!.toList())
                }
            }
        } else if (!bs.get(IS_LEFT_CLICKING)) {
            // If we wanted to punch, but couldn't find an entity, stop
            end(fake)
            return ControlFlow.Continue(imageIn, exitDirs!!.toList())
        }

        // Go down the interaction list to poll and test each of them
        val z = UseBlockCallback.EVENT.invoker().interact(fake, world, fake.activeHand, hit)
        if (z == ActionResult.PASS) {
            if (missed || world.getBlockState(hit.blockPos).onUse(world, fake, fake.activeHand, hit) == ActionResult.PASS) {
                if (missed || fake.activeItem.useOnBlock(ItemUsageContext(fake, fake.activeHand, hit)) == ActionResult.PASS) {
                    fake.setStackInHand(Hand.MAIN_HAND, fake.activeItem.use(world, fake, fake.activeHand).value)
                }
            }
        }

        end(fake)
        return ControlFlow.Continue(imageIn, exitDirs!!.toList())
    }

    // Cleans up the Fake Player just in case it is a form of a leak. And drops all the items for safety
    private fun end(fake: SlateFakePlayer){
        if (hasTrinkets) {
            SlateWorksTrinkets.makeFakeDropTrinkets(fake)
        }
        fake.stopUsingItem()
        fake.getInventory().dropAll()
        fake.discard()
    }
}