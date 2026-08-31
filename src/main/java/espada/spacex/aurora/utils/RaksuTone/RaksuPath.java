package espada.spacex.aurora.utils.RaksuTone;

import espada.spacex.aurora.utils.OLEPOSSUtils;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixin.AbstractBlockAccessor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;

public class RaksuPath {
   public final List<Movement> path = new ArrayList();
   public final int step = 2;
   public final int reverseStep = 3;
   public double speed = 0.2873;
   public final int fallDist = 150;
   private List<Direction> dirs = null;

   public void calculate(int blocks, BlockPos target, boolean opposite) {
      BlockPos pos = MeteorClient.mc.player.getBlockPos().toImmutable();
      if (!this.is(pos.down()) && !OLEPOSSUtils.inside(MeteorClient.mc.player, MeteorClient.mc.player.getBoundingBox().offset((double)0.0F, -0.2, (double)0.0F))) {
         for(int i = 0; i < 150; ++i) {
            if (this.is(pos.down(i + 1))) {
               this.path.add(new Movement(true, pos.down(i), RaksuPath.MovementType.Fall));
               break;
            }
         }
      }

      for(int i = 0; i < blocks; ++i) {
         Movement m = this.nextPos(pos, target, true, opposite);
         if (m == null || !m.valid) {
            return;
         }

         if (pos.equals(m.pos)) {
            return;
         }

         pos = m.pos;
         this.path.add(m);
      }

   }

   private Movement nextPos(BlockPos pos, BlockPos target, boolean stuckCheck, boolean reversed) {
      this.closestDir(pos, target, reversed);
      Iterator var5 = this.dirs.iterator();

      Movement m;
      while(true) {
         if (!var5.hasNext()) {
            return null;
         }

         Direction dir = (Direction)var5.next();
         m = this.getMovement(pos, dir);
         if (m.valid()) {
            if (!stuckCheck) {
               break;
            }

            Movement m1 = this.nextPos(m.pos, target, false, reversed);
            if (m1 == null || !m1.valid || !m1.pos.equals(pos)) {
               break;
            }
         }
      }

      return m;
   }

   private Movement getMovement(BlockPos pos, Direction dir) {
      if (this.canWalkTrough(pos, dir)) {
         if (this.is(pos.offset(dir).down())) {
            return new Movement(true, pos.offset(dir), RaksuPath.MovementType.Move);
         } else {
            Movement m = this.getFall(pos, dir);
            return m.valid ? m : new Movement(false, (BlockPos)null, (MovementType)null);
         }
      } else {
         Movement m = this.getStep(pos, dir);
         return m.valid ? m : new Movement(false, (BlockPos)null, (MovementType)null);
      }
   }

   private Movement getStep(BlockPos pos, Direction dir) {
      for(int i = 1; i <= 2; ++i) {
         if (this.is(pos.up(i + 1))) {
            return new Movement(false, (BlockPos)null, (MovementType)null);
         }

         if (this.is(pos.offset(dir).up(i - 1)) && !this.is(pos.offset(dir).up(i)) && !this.is(pos.offset(dir).up(i + 1))) {
            return new Movement(true, pos.offset(dir).up(i), RaksuPath.MovementType.Step);
         }
      }

      return new Movement(false, (BlockPos)null, (MovementType)null);
   }

   private Movement getFall(BlockPos pos, Direction dir) {
      for(int i = 0; i < 150; ++i) {
         if (this.is(pos.offset(dir).down(i + 1))) {
            if (i < 3) {
               return new Movement(true, pos.offset(dir).down(i), RaksuPath.MovementType.Reverse);
            }

            return new Movement(true, pos.offset(dir).down(i), RaksuPath.MovementType.Fall);
         }
      }

      return new Movement(false, (BlockPos)null, (MovementType)null);
   }

   private boolean canWalkTrough(BlockPos pos, Direction dir) {
      return !this.is(pos.offset(dir)) && !this.is(pos.offset(dir).up());
   }

   private void closestDir(BlockPos from, BlockPos target, boolean reversed) {
      if (reversed) {
         Comparator<Direction> c = Comparator.comparingDouble((i) -> from.offset(i).toCenterPos().distanceTo(target.toCenterPos()));
         this.dirs = Arrays.stream(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}).sorted(c.reversed()).toList();
      } else {
         this.dirs = Arrays.stream(new Direction[]{Direction.EAST, Direction.WEST, Direction.NORTH, Direction.SOUTH}).sorted(Comparator.comparingDouble((i) -> from.offset(i).toCenterPos().distanceTo(target.toCenterPos()))).toList();
      }

   }

   private boolean is(BlockPos pos) {
      return ((AbstractBlockAccessor)MeteorClient.mc.world.getBlockState(pos).getBlock()).isCollidable();
   }

   public static record Movement(boolean valid, BlockPos pos, MovementType type) {
   }

   public static enum MovementType {
      Step,
      Reverse,
      Fall,
      Move;

      // $FF: synthetic method
      private static MovementType[] $values() {
         return new MovementType[]{Step, Reverse, Fall, Move};
      }
   }
}
