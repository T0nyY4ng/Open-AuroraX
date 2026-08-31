package espada.spacex.aurora.modules.globalsettings;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.OLEPOSSUtils;
import espada.spacex.aurora.utils.PlaceData;
import espada.spacex.aurora.utils.SettingUtils;
import java.util.function.Predicate;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import net.minecraft.block.AirBlock;
import net.minecraft.block.Block;
import net.minecraft.block.ButtonBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.state.property.Properties;
import net.minecraft.block.RespawnAnchorBlock;

public class FacingSettings extends Modules {
   private final SettingGroup sgGeneral;
   private static FacingSettings INSTANCE = new FacingSettings();
   public final Setting<Boolean> strictDir;
   public final Setting<Boolean> unblocked;
   public final Setting<Boolean> airPlace;
   public final Setting<MaxHeight> maxHeight;

   public FacingSettings() {
      super(Aurora.Settings, "Facing", "Global facing settings for every aurora module.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.strictDir = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Strict Direction")).description("Doesn't place on faces which aren't in your direction.")).defaultValue(false)).build());
      this.unblocked = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Unblocked")).description("Doesn't place on faces that have block on them.")).defaultValue(false)).build());
      this.airPlace = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Air Place")).description("Allows placing blocks in air.")).defaultValue(false)).build());
      this.maxHeight = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Max Height")).description("Doesn't place on top sides of blocks at max height. Old: 1.12, New: 1.17+")).defaultValue(FacingSettings.MaxHeight.New)).build());
   }

   public static FacingSettings getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new FacingSettings();
      }

      return INSTANCE;
   }

   public PlaceData getPlaceDataOR(BlockPos pos, Predicate predicate, boolean ignoreContainers) {
      if (pos == null) {
         return new PlaceData((BlockPos)null, (Direction)null, false);
      } else {
         Direction best = null;
         if (this.mc.world != null && this.mc.player != null) {
            if ((Boolean)this.airPlace.get()) {
               return new PlaceData(pos, Direction.UP, true);
            }

            double cDist = (double)-1.0F;

            for(Direction dir : Direction.values()) {
               if (!this.heightCheck(pos.offset(dir)) && (!ignoreContainers || !this.mc.world.getBlockState(pos.offset(dir)).hasBlockEntity()) && (OLEPOSSUtils.solid(pos.offset(dir)) || predicate == null || predicate.test(pos.offset(dir))) && (!(Boolean)this.strictDir.get() || OLEPOSSUtils.strictDir(pos.offset(dir), dir.getOpposite()))) {
                  double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                  if (dist >= (double)0.0F && (cDist < (double)0.0F || dist < cDist)) {
                     best = dir;
                     cDist = dist;
                  }
               }
            }
         }

         return best == null ? new PlaceData((BlockPos)null, (Direction)null, false) : new PlaceData(pos.offset(best), best.getOpposite(), true);
      }
   }

   public PlaceData getPlaceDataAND(BlockPos pos, Predicate<Direction> predicate, Predicate<BlockPos> predicatePos, boolean ignoreContainers) {
      if (pos == null) {
         return new PlaceData((BlockPos)null, (Direction)null, false);
      } else {
         Direction best = null;
         if (this.mc.world != null && this.mc.player != null) {
            if ((Boolean)this.airPlace.get()) {
               return new PlaceData(pos, Direction.UP, true);
            }

            double cDist = (double)-1.0F;

            for(Direction dir : Direction.values()) {
               if (!this.heightCheck(pos.offset(dir)) && (this.mc.player.isSneaking() || !ignoreContainers || !this.mc.world.getBlockState(pos.offset(dir)).hasBlockEntity()) && (this.mc.player.isSneaking() || !ignoreContainers || !(this.mc.world.getBlockState(pos.offset(dir)).getBlock() instanceof ButtonBlock)) && OLEPOSSUtils.solid(pos.offset(dir)) && (predicate == null || predicate.test(dir)) && (predicatePos == null || predicatePos.test(pos.offset(dir))) && (!(Boolean)this.strictDir.get() || OLEPOSSUtils.strictDir(pos.offset(dir), dir.getOpposite()))) {
                  double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                  if (dist >= (double)0.0F && (cDist < (double)0.0F || dist < cDist)) {
                     best = dir;
                     cDist = dist;
                  }
               }
            }
         }

         return best == null ? new PlaceData((BlockPos)null, (Direction)null, false) : new PlaceData(pos.offset(best), best.getOpposite(), true);
      }
   }

   public PlaceData getPlaceDataA(BlockPos pos, boolean ignoreContainers) {
      if (pos == null) {
         return new PlaceData((BlockPos)null, (Direction)null, false);
      } else {
         Direction best = null;
         if (this.mc.world != null && this.mc.player != null) {
            if ((Boolean)this.airPlace.get()) {
               return new PlaceData(pos, Direction.UP, true);
            }

            double cDist = (double)-1.0F;

            for(Direction dir : Direction.values()) {
               if (!this.heightCheck(pos.offset(dir)) && (this.mc.player.isSneaking() || !ignoreContainers || !this.mc.world.getBlockState(pos.offset(dir)).hasBlockEntity()) && OLEPOSSUtils.solid(pos.offset(dir)) && (!(Boolean)this.strictDir.get() || OLEPOSSUtils.strictDir(pos.offset(dir), dir.getOpposite()))) {
                  double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                  if (dist >= (double)0.0F && (cDist < (double)0.0F || dist < cDist)) {
                     best = dir;
                     cDist = dist;
                  }
               }
            }
         }

         return best == null ? new PlaceData((BlockPos)null, (Direction)null, false) : new PlaceData(pos.offset(best), best.getOpposite(), true);
      }
   }

   public PlaceData getPlaceData(BlockPos pos, boolean ignoreContainers) {
      if (pos == null) {
         return new PlaceData((BlockPos)null, (Direction)null, false);
      } else {
         Direction best = null;
         if (this.mc.world != null && this.mc.player != null) {
            if ((Boolean)this.airPlace.get()) {
               return new PlaceData(pos, Direction.UP, true);
            }

            double cDist = (double)-1.0F;

            for(Direction dir : Direction.values()) {
               if (!this.heightCheck(pos.offset(dir)) && (this.mc.player.isSneaking() || !ignoreContainers || !this.mc.world.getBlockState(pos.offset(dir)).hasBlockEntity()) && (this.mc.player.isSneaking() || !ignoreContainers || !(this.mc.world.getBlockState(pos.offset(dir)).getBlock() instanceof ButtonBlock)) && (this.mc.player.isSneaking() || !ignoreContainers || !(this.mc.world.getBlockState(pos.offset(dir)).getBlock() instanceof RespawnAnchorBlock) || (Integer)this.mc.world.getBlockState(pos.offset(dir)).get(Properties.CHARGES) < 1) && OLEPOSSUtils.solid(pos.offset(dir)) && (!(Boolean)this.strictDir.get() || OLEPOSSUtils.strictDir(pos.offset(dir), dir.getOpposite()))) {
                  double dist = SettingUtils.placeRangeTo(pos.offset(dir));
                  if (dist >= (double)0.0F && (cDist < (double)0.0F || dist < cDist)) {
                     best = dir;
                     cDist = dist;
                  }
               }
            }
         }

         return best == null ? new PlaceData((BlockPos)null, (Direction)null, false) : new PlaceData(pos.offset(best), best.getOpposite(), true);
      }
   }

   public Direction getPlaceOnDirection(BlockPos pos) {
      if (pos == null) {
         return null;
      } else {
         Direction best = null;
         if (this.mc.world != null && this.mc.player != null) {
            double cDist = (double)-1.0F;

            for(Direction dir : Direction.values()) {
               if (!this.heightCheck(pos.offset(dir)) && (!(Boolean)this.unblocked.get() || this.getBlock(pos.offset(dir)) instanceof AirBlock) && (!(Boolean)this.strictDir.get() || OLEPOSSUtils.strictDir(pos, dir))) {
                  double dist = this.dist(pos, dir);
                  if (dist >= (double)0.0F && (cDist < (double)0.0F || dist < cDist)) {
                     best = dir;
                     cDist = dist;
                  }
               }
            }
         }

         return best;
      }
   }

   private boolean heightCheck(BlockPos pos) {
      int var10000 = pos.getY();
      short var10001;
      switch (((MaxHeight)this.maxHeight.get()).ordinal()) {
         case 0 -> var10001 = 255;
         case 1 -> var10001 = 319;
         case 2 -> var10001 = 1000;
         default -> throw new MatchException((String)null, (Throwable)null);
      }

      return var10000 >= var10001;
   }

   private double dist(BlockPos pos, Direction dir) {
      if (this.mc.player == null) {
         return (double)0.0F;
      } else {
         Vec3d vec = new Vec3d((double)((float)pos.getX() + (float)dir.getOffsetX() / 2.0F), (double)((float)pos.getY() + (float)dir.getOffsetY() / 2.0F), (double)((float)pos.getZ() + (float)dir.getOffsetZ() / 2.0F));
         Vec3d dist = this.mc.player.getEyePos().add(-vec.x, -vec.y, -vec.z);
         return Math.sqrt(dist.x * dist.x + dist.y * dist.y + dist.z * dist.z);
      }
   }

   private Block getBlock(BlockPos pos) {
      return this.mc.world.getBlockState(pos).getBlock();
   }

   public static enum MaxHeight {
      Old,
      New,
      Disabled;

      // $FF: synthetic method
      private static MaxHeight[] $values() {
         return new MaxHeight[]{Old, New, Disabled};
      }
   }
}
