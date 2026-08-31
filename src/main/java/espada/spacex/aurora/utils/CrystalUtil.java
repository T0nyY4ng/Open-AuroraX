package espada.spacex.aurora.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.entity.Entity;
import net.minecraft.block.AirBlock;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class CrystalUtil {
   public static Vec3d getMotionVec(Entity entity, int ticks, boolean collision) {
      double dX = entity.getX() - entity.prevX;
      double dZ = entity.getZ() - entity.prevZ;
      double entityMotionPosX = (double)0.0F;
      double entityMotionPosZ = (double)0.0F;
      if (collision) {
         for(int i = 1; i <= ticks && MeteorClient.mc.world.getBlockState(new BlockPos((int)((double)entity.getBlockX() + dX * (double)i), entity.getBlockX(), (int)((double)entity.getBlockZ() + dZ * (double)i))).getBlock() instanceof AirBlock; ++i) {
            entityMotionPosX = dX * (double)i;
            entityMotionPosZ = dZ * (double)i;
         }
      } else {
         entityMotionPosX = dX * (double)ticks;
         entityMotionPosZ = dZ * (double)ticks;
      }

      return new Vec3d(entityMotionPosX, (double)0.0F, entityMotionPosZ);
   }
}
