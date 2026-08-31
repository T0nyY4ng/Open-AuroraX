package espada.spacex.aurora.utils;

import espada.spacex.aurora.enums.HoleType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;

public class HoleUtils {
   public static Hole getHole(BlockPos pos) {
      return getHole(pos, true, true, true, 3, true);
   }

   public static Hole getHole(BlockPos pos, int depth) {
      return getHole(pos, depth, true);
   }

   public static Hole getHole(BlockPos pos, int depth, boolean floor) {
      return getHole(pos, true, true, true, depth, floor);
   }

   public static Hole getHole(BlockPos pos, boolean s, boolean d, boolean q, int depth, boolean floor) {
      if (!isHole(pos, depth, floor)) {
         return new Hole(pos, HoleType.NotHole);
      } else if (isBlock(pos.west()) && isBlock(pos.north())) {
         boolean x = isHole(pos.east(), depth, floor) && isBlock(pos.east().north()) && isBlock(pos.east(2));
         boolean z = isHole(pos.south(), depth, floor) && isBlock(pos.south().west()) && isBlock(pos.south(2));
         if (s && !x && !z && isBlock(pos.east()) && isBlock(pos.south())) {
            return new Hole(pos, HoleType.Single);
         } else if (q && x && z && isHole(pos.south().east(), depth, floor) && isBlock(pos.east().east().south()) && isBlock(pos.south().south().east())) {
            return new Hole(pos, HoleType.Quad);
         } else if (!d) {
            return new Hole(pos, HoleType.NotHole);
         } else if (x && !z && isBlock(pos.south()) && isBlock(pos.south().east())) {
            return new Hole(pos, HoleType.DoubleX);
         } else {
            return z && !x && isBlock(pos.east()) && isBlock(pos.south().east()) ? new Hole(pos, HoleType.DoubleZ) : new Hole(pos, HoleType.NotHole);
         }
      } else {
         return new Hole(pos, HoleType.NotHole);
      }
   }

   static boolean isBlock(BlockPos pos) {
      return OLEPOSSUtils.collidable(pos);
   }

   static boolean isHole(BlockPos pos, int depth, boolean floor) {
      if (floor && !isBlock(pos.down())) {
         return false;
      } else {
         for(int i = 0; i < depth; ++i) {
            if (isBlock(pos.up(i))) {
               return false;
            }
         }

         return true;
      }
   }

   public static boolean inHole(PlayerEntity player) {
      BlockPos pos = player.getBlockPos();
      if (getHole(pos, 1).type == HoleType.Single) {
         return true;
      } else if (getHole(pos, 1).type != HoleType.DoubleX && getHole(pos.add(-1, 0, 0), 1).type != HoleType.DoubleX) {
         if (getHole(pos, 1).type != HoleType.DoubleZ && getHole(pos.add(0, 0, -1), 1).type != HoleType.DoubleZ) {
            return getHole(pos, 1).type == HoleType.Quad || getHole(pos.add(-1, 0, -1), 1).type == HoleType.Quad || getHole(pos.add(-1, 0, 0), 1).type == HoleType.Quad || getHole(pos.add(0, 0, -1), 1).type == HoleType.Quad;
         } else {
            return true;
         }
      } else {
         return true;
      }
   }
}
