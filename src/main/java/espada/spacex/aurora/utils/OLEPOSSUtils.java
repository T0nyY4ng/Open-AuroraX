package espada.spacex.aurora.utils;

import espada.spacex.aurora.mixins.IBlockSettings;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.AbstractBlockAccessor;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Box;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.AbstractFireBlock;
import net.minecraft.block.AbstractBlock.Settings;

public class OLEPOSSUtils {
   public static Vec3d getMiddle(Box box) {
      return new Vec3d((box.minX + box.maxX) / (double)2.0F, (box.minY + box.maxY) / (double)2.0F, (box.minZ + box.maxZ) / (double)2.0F);
   }

   public static boolean inside(PlayerEntity en, Box bb) {
      return MeteorClient.mc.world != null && MeteorClient.mc.world.getBlockCollisions(en, bb).iterator().hasNext();
   }

   public static int closerToZero(int x) {
      return (int)((float)x - Math.signum((float)x));
   }

   public static Vec3d getClosest(Vec3d pPos, Vec3d middle, double width, double height) {
      return new Vec3d(Math.min(Math.max(pPos.x, middle.x - width / (double)2.0F), middle.x + width / (double)2.0F), Math.min(Math.max(pPos.y, middle.y), middle.y + height), Math.min(Math.max(pPos.z, middle.z - width / (double)2.0F), middle.z + width / (double)2.0F));
   }

   public static boolean strictDir(BlockPos pos, Direction dir) {
      boolean var10000;
      switch (dir) {
         case DOWN -> var10000 = MeteorClient.mc.player.getEyePos().y <= (double)pos.getY() + (double)0.5F;
         case UP -> var10000 = MeteorClient.mc.player.getEyePos().y >= (double)pos.getY() + (double)0.5F;
         case NORTH -> var10000 = MeteorClient.mc.player.getZ() < (double)pos.getZ();
         case SOUTH -> var10000 = MeteorClient.mc.player.getZ() >= (double)(pos.getZ() + 1);
         case WEST -> var10000 = MeteorClient.mc.player.getX() < (double)pos.getX();
         case EAST -> var10000 = MeteorClient.mc.player.getX() >= (double)(pos.getX() + 1);
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000;
   }

   public static Box getCrystalBox(BlockPos pos) {
      return new Box((double)pos.getX() - (double)0.5F, (double)pos.getY(), (double)pos.getZ() - (double)0.5F, (double)pos.getX() + (double)1.5F, (double)(pos.getY() + 2), (double)pos.getZ() + (double)1.5F);
   }

   public static Box getCrystalBox(Vec3d pos) {
      return new Box(pos.getX() - (double)1.0F, pos.getY(), pos.getZ() - (double)1.0F, pos.getX() + (double)1.0F, pos.getY() + (double)2.0F, pos.getZ() + (double)1.0F);
   }

   public static boolean replaceable(BlockPos block) {
      return ((IBlockSettings)Settings.copy(MeteorClient.mc.world.getBlockState(block).getBlock())).replaceable();
   }

   public static boolean solid2(BlockPos block) {
      return MeteorClient.mc.world.getBlockState(block).isSolid();
   }

   public static boolean solid(BlockPos block) {
      Block b = MeteorClient.mc.world.getBlockState(block).getBlock();
      return !(b instanceof AbstractFireBlock) && !(b instanceof FluidBlock) && !(b instanceof AirBlock);
   }

   public static boolean isGapple(Item item) {
      return item == Items.GOLDEN_APPLE || item == Items.ENCHANTED_GOLDEN_APPLE;
   }

   public static boolean isGapple(ItemStack stack) {
      return isGapple(stack.getItem());
   }

   public static boolean collidable(BlockPos block) {
      return ((AbstractBlockAccessor)MeteorClient.mc.world.getBlockState(block).getBlock()).isCollidable();
   }
}
