package espada.spacex.aurora.utils;

import espada.spacex.aurora.enums.SwingState;
import espada.spacex.aurora.enums.SwingType;
import espada.spacex.aurora.mixins.IBlockSettings;
import espada.spacex.aurora.timers.TimerList;
import java.util.Objects;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.utils.player.Rotations;
import meteordevelopment.meteorclient.utils.world.BlockUtils;
import net.minecraft.util.Hand;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.enchantment.Enchantments;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Blocks;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.network.packet.Packet;
import net.minecraft.block.BlockState;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.registry.tag.FluidTags;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.client.network.PendingUpdateManager;
import net.minecraft.client.network.SequencedPacketCreator;
import espada.spacex.aurora.mixins.IClientPlayerInteractionManager;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.block.AbstractBlock.Settings;

public class BOBlockUtil {
   private static final TimerList placed = new TimerList();

   public static boolean replaceable(BlockPos block) {
      return ((IBlockSettings)Settings.copy(MeteorClient.mc.world.getBlockState(block).getBlock())).replaceable();
   }

   public static boolean solid(BlockPos block) {
      Block b = MeteorClient.mc.world.getBlockState(block).getBlock();
      return !(b instanceof AbstractFireBlock) && !(b instanceof FluidBlock) && !(b instanceof AirBlock);
   }

   public static boolean solid2(BlockPos block) {
      return MeteorClient.mc.world.getBlockState(block).isSolid();
   }

   public static boolean isAir(BlockPos block) {
      return MeteorClient.mc.world.getBlockState(block).isAir() || getBlock(block) == Blocks.FIRE;
   }

   public static Block getBlock(BlockPos block) {
      return MeteorClient.mc.world.getBlockState(block).getBlock();
   }

   public static boolean isAir(Vec3d vec3d) {
      return Util.mc.world.getBlockState(vec3toBlockPos(vec3d)).getBlock().equals(Blocks.AIR);
   }

   public static BlockPos vec3toBlockPos(Vec3d vec3d) {
      return new BlockPos((int)Math.floor(vec3d.x), (int)Math.round(vec3d.y), (int)Math.floor(vec3d.z));
   }

   public static double getPushDistance(PlayerEntity player, double x, double z) {
      double d0 = player.getX() - x;
      double d2 = player.getZ() - z;
      return Math.sqrt(d0 * d0 + d2 * d2);
   }

   public static BlockState getState(BlockPos pos) {
      return MeteorClient.mc.world.getBlockState(pos);
   }

   public static boolean fakeBBoxCheckFeet(PlayerEntity player, Vec3d offset) {
      Vec3d futurePos = player.getPos().add(offset);
      return isAir(futurePos.add(0.3, (double)0.0F, 0.3)) && isAir(futurePos.add(-0.3, (double)0.0F, 0.3)) && isAir(futurePos.add(0.3, (double)0.0F, -0.3)) && isAir(futurePos.add(-0.3, (double)0.0F, (double)0.0F)) && isAir(futurePos.add((double)0.0F, (double)0.0F, 0.3)) && isAir(futurePos.add(0.3, (double)0.0F, (double)0.0F)) && isAir(futurePos.add((double)0.0F, (double)0.0F, -0.3));
   }

   public static BlockPos getFlooredPosition(Entity entity) {
      return new BlockPos((int)Math.floor(entity.getX()), (int)Math.round(entity.getY()), (int)Math.floor(entity.getZ()));
   }

   public static boolean cantBlockPlace(BlockPos blockPos) {
      if (MeteorClient.mc.world.getBlockState(blockPos.add(0, 0, 1)).getBlock() == Blocks.AIR && MeteorClient.mc.world.getBlockState(blockPos.add(0, 0, -1)).getBlock() == Blocks.AIR && MeteorClient.mc.world.getBlockState(blockPos.add(1, 0, 0)).getBlock() == Blocks.AIR && MeteorClient.mc.world.getBlockState(blockPos.add(-1, 0, 0)).getBlock() == Blocks.AIR && MeteorClient.mc.world.getBlockState(blockPos.add(0, 1, 0)).getBlock() == Blocks.AIR && MeteorClient.mc.world.getBlockState(blockPos.add(0, -1, 0)).getBlock() == Blocks.AIR) {
         return true;
      } else {
         return !MeteorClient.mc.world.getBlockState(blockPos).isAir() && getBlock(blockPos) != Blocks.FIRE;
      }
   }

   public static void placeBlock(BlockPos pos, Hand hand, boolean rotate, int priority) {
      if (getBlock(pos) == Blocks.AIR) {
         TimerList var10001 = placed;
         Objects.requireNonNull(var10001);
         PlaceData data = SettingUtils.getPlaceDataOR(pos, var10001::contains);
         if (data != null && data.valid()) {
            Vec3d hitPos = Vec3d.ofCenter(pos);
            Direction side = BlockUtils.getPlaceSide(pos);
            if (side != null) {
               hitPos = hitPos.add((double)side.getOffsetX() * (double)0.5F, (double)side.getOffsetY() * (double)0.5F, (double)side.getOffsetZ() * (double)0.5F);
            }

            if (rotate) {
               Rotations.rotate(Rotations.getYaw(hitPos), Rotations.getPitch(hitPos), priority);
            }

            place(data.pos(), hand, data.pos().toCenterPos(), data.dir());
         }
      }

   }

   public static void sendSequenced(SequencedPacketCreator packetCreator) {
      if (MeteorClient.mc.interactionManager != null && MeteorClient.mc.world != null && MeteorClient.mc.getNetworkHandler() != null) {
         ((IClientPlayerInteractionManager)MeteorClient.mc.interactionManager).aurora$sendSequencedPacket(MeteorClient.mc.world, packetCreator);
      }
   }

   private static void place(BlockPos pos, Hand hand, Vec3d blockHitVec, Direction blockDirection) {
      Vec3d eyes = MeteorClient.mc.player.getEyePos();
      boolean inside = eyes.x > (double)pos.getX() && eyes.x < (double)pos.getX() && eyes.y > (double)pos.getY() && eyes.y < (double)pos.getY() && eyes.z > (double)pos.getZ() && eyes.z < (double)pos.getZ();
      SettingUtils.swing(SwingState.Pre, SwingType.Placing, hand);
      sendSequenced((s) -> new PlayerInteractBlockC2SPacket(hand, new BlockHitResult(blockHitVec, blockDirection, pos, inside), s));
      SettingUtils.swing(SwingState.Post, SwingType.Placing, hand);
   }

   public static int getBlockBreakingSpeed(BlockState block, BlockPos pos, int slot) {
      PlayerEntity player = MeteorClient.mc.player;
      ItemStack stack = player.getInventory().getStack(slot);
      float f = stack.getMiningSpeedMultiplier(block);
      if (f > 1.0F) {
         int i = EnchantmentHelper.getLevel((RegistryEntry)MeteorClient.mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.EFFICIENCY).get(), stack);
         if (i > 0 && !stack.isEmpty()) {
            f += (float)(i * i + 1);
         }
      }

      if (StatusEffectUtil.hasHaste(player)) {
         f *= 1.0F + (float)(StatusEffectUtil.getHasteAmplifier(player) + 1) * 0.2F;
      }

      if (player.hasStatusEffect(StatusEffects.MINING_FATIGUE)) {
         float var10000;
         switch (player.getStatusEffect(StatusEffects.MINING_FATIGUE).getAmplifier()) {
            case 0 -> var10000 = 0.3F;
            case 1 -> var10000 = 0.09F;
            case 2 -> var10000 = 0.0027F;
            default -> var10000 = 8.1E-4F;
         }

         float k = var10000;
         f *= k;
      }

      if (player.isSubmergedIn(FluidTags.WATER) && EnchantmentHelper.getEquipmentLevel((RegistryEntry)MeteorClient.mc.world.getRegistryManager().get(RegistryKeys.ENCHANTMENT).getEntry(Enchantments.AQUA_AFFINITY).get(), player) == 0) {
         f /= 5.0F;
      }

      if (!player.isOnGround()) {
         f /= 5.0F;
      }

      float t = block.getHardness(MeteorClient.mc.world, pos);
      return t == -1.0F ? 0 : (int)Math.ceil((double)(1.0F / (f / t / 30.0F)));
   }
}
