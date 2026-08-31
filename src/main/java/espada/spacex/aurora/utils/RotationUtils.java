package espada.spacex.aurora.utils;

import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

public class RotationUtils {
   public static float nextYaw(double current, double target, double step) {
      double i = yawAngle(current, target);
      return step >= Math.abs(i) ? (float)(current + i) : (float)(current + (double)(i < (double)0.0F ? -1 : 1) * step);
   }

   public static double yawAngle(double current, double target) {
      double c = MathHelper.wrapDegrees(current) + (double)180.0F;
      double t = MathHelper.wrapDegrees(target) + (double)180.0F;
      if (c > t) {
         return t + (double)360.0F - c < Math.abs(c - t) ? (double)360.0F - c + t : t - c;
      } else {
         return (double)360.0F - t + c < Math.abs(c - t) ? -((double)360.0F - t + c) : t - c;
      }
   }

   public static float nextPitch(double current, double target, double step) {
      double i = target - current;
      return (float)(Math.abs(i) <= step ? target : (i >= (double)0.0F ? current + step : current - step));
   }

   public static double radAngle(Vec2f vec1, Vec2f vec2) {
      double p = (double)(vec1.x * vec2.x + vec1.y * vec2.y);
      p /= Math.sqrt((double)(vec1.x * vec1.x + vec1.y * vec1.y));
      p /= Math.sqrt((double)(vec2.x * vec2.x + vec2.y * vec2.y));
      return Math.acos(p);
   }

   public static double getYaw(Vec3d start, Vec3d target) {
      return (double)(MeteorClient.mc.player.getYaw() + MathHelper.wrapDegrees((float)Math.toDegrees(Math.atan2(target.getZ() - start.getZ(), target.getX() - start.getX())) - 90.0F - MeteorClient.mc.player.getYaw()));
   }

   public static double getPitch(Vec3d start, Vec3d target) {
      double diffX = target.getX() - start.getX();
      double diffY = target.getY() - start.getY();
      double diffZ = target.getZ() - start.getZ();
      double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
      return (double)(MeteorClient.mc.player.getPitch() + MathHelper.wrapDegrees((float)(-Math.toDegrees(Math.atan2(diffY, diffXZ))) - MeteorClient.mc.player.getPitch()));
   }
}
