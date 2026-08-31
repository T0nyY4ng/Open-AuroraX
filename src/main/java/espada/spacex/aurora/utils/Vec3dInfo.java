package espada.spacex.aurora.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

public class Vec3dInfo {
   public static boolean isInRange(Vec3d vec3d, double radius) {
      return vec3d.isInRange(vec3d, radius);
   }

   public static boolean isWithinRange(Vec3d vec3d, double range) {
      return MeteorClient.mc.player.getBlockPos().isWithinDistance(vec3d, range);
   }

   public static Vec3d add(Vec3d vec3d, Vec3d added) {
      return new Vec3d(vec3d.add(added).getX(), vec3d.add(added).getY(), vec3d.add(added).getZ());
   }

   public static Vec3d add(Vec3d vec3d, double x, double y, double z) {
      return new Vec3d(vec3d.add(x, y, z).getX(), vec3d.add(x, y, z).getY(), vec3d.add(x, y, z).getZ());
   }

   public static boolean notNull(Vec3d vec3d) {
      return vec3d != null;
   }

   public static Vec3d getEyeVec(PlayerEntity entity) {
      return entity.getPos().add((double)0.0F, (double)entity.getEyeHeight(entity.getPose()), (double)0.0F);
   }

   public static Vec3d closestVec3d(BlockPos blockPos) {
      if (blockPos == null) {
         return new Vec3d((double)0.0F, (double)0.0F, (double)0.0F);
      } else {
         double x = MathHelper.clamp(MeteorClient.mc.player.getX() - (double)blockPos.getX(), (double)0.0F, (double)1.0F);
         double y = MathHelper.clamp(MeteorClient.mc.player.getY() - (double)blockPos.getY(), (double)0.0F, 0.6);
         double z = MathHelper.clamp(MeteorClient.mc.player.getZ() - (double)blockPos.getZ(), (double)0.0F, (double)1.0F);
         return new Vec3d((double)blockPos.getX() + x, (double)blockPos.getY() + y, (double)blockPos.getZ() + z);
      }
   }
}
