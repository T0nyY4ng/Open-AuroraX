package espada.spacex.aurora.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.block.FluidBlock;
import net.minecraft.util.math.Vec3d;
import net.minecraft.block.StairsBlock;
import net.minecraft.block.BlockState;

public class NCPRaytracer {
   public static boolean raytrace(Vec3d from, Vec3d to, Box box) {
      int lx = 0;
      int ly = 0;
      int lz = 0;

      for(float i = 0.0F; i < 1.0F; i = (float)((double)i + 0.001)) {
         double x = lerp(from.x, to.x, (double)i);
         double y = lerp(from.y, to.y, (double)i);
         double z = lerp(from.z, to.z, (double)i);
         if (box.contains(x, y, z)) {
            return true;
         }

         int ix = (int)Math.floor(x);
         int iy = (int)Math.floor(y);
         int iz = (int)Math.floor(z);
         if (lx != ix || ly != iy || lz != iz) {
            BlockPos pos = new BlockPos(ix, iy, iz);
            if (validForCheck(pos, MeteorClient.mc.world.getBlockState(pos))) {
               return false;
            }
         }

         lx = ix;
         ly = iy;
         lz = iz;
      }

      return false;
   }

   private static double lerp(double from, double to, double delta) {
      return from + (to - from) * delta;
   }

   public static boolean validForCheck(BlockPos pos, BlockState state) {
      if (state.isSolid()) {
         return true;
      } else if (state.getBlock() instanceof FluidBlock) {
         return false;
      } else if (state.getBlock() instanceof StairsBlock) {
         return false;
      } else {
         return state.hasBlockEntity() ? false : state.isFullCube(MeteorClient.mc.world, pos);
      }
   }
}
