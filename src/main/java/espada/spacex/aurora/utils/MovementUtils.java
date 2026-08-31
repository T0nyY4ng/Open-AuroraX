package espada.spacex.aurora.utils;

import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

public class MovementUtils {
   public static double xMovement(double speed, double yaw) {
      return Math.cos(Math.toRadians(yaw + (double)90.0F)) * speed;
   }

   public static double zMovement(double speed, double yaw) {
      return Math.sin(Math.toRadians(yaw + (double)90.0F)) * speed;
   }

   public static double getSpeed(double baseSpeed) {
      if (MeteorClient.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
         baseSpeed *= 1.2 + (double)MeteorClient.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() * 0.2;
      }

      if (MeteorClient.mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
         baseSpeed /= 1.2 + (double)MeteorClient.mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() * 0.2;
      }

      if (MeteorClient.mc.player.isSneaking()) {
         baseSpeed *= 0.3;
      }

      return baseSpeed;
   }

   public static void moveTowards(Vec3d movement, double baseSpeed, Vec3d vec, int step, int reverseStep) {
      double speed = getSpeed(baseSpeed);
      double yaw = RotationUtils.getYaw(MeteorClient.mc.player.getPos(), vec);
      double xm = xMovement(speed, yaw);
      double zm = zMovement(speed, yaw);
      double xd = vec.x - MeteorClient.mc.player.getX();
      double zd = vec.z - MeteorClient.mc.player.getZ();
      double x = Math.abs(xm) <= Math.abs(xd) ? xm : xd;
      double z = Math.abs(zm) <= Math.abs(zd) ? zm : zd;
      y(movement, x, z, step, reverseStep);
      ((IVec3d)movement).setXZ(x, z);
   }

   private static void y(Vec3d movement, double x, double z, int step, int rev) {
      if (MeteorClient.mc.player.isOnGround() && !OLEPOSSUtils.inside(MeteorClient.mc.player, MeteorClient.mc.player.getBoundingBox()) && OLEPOSSUtils.inside(MeteorClient.mc.player, MeteorClient.mc.player.getBoundingBox().offset(x, (double)0.0F, z))) {
         double s = getStep(MeteorClient.mc.player.getBoundingBox().offset(x, (double)0.0F, z), step);
         if (s > (double)0.0F) {
            ((IVec3d)movement).setY(s);
            MeteorClient.mc.player.setVelocity(MeteorClient.mc.player.getVelocity().x, (double)0.0F, MeteorClient.mc.player.getVelocity().z);
         }

      } else {
         if (MeteorClient.mc.player.isOnGround() && !OLEPOSSUtils.inside(MeteorClient.mc.player, MeteorClient.mc.player.getBoundingBox().offset(x, -0.04, z))) {
            double s = getReverse(MeteorClient.mc.player.getBoundingBox(), rev);
            if (s > (double)0.0F) {
               ((IVec3d)movement).setY(-s);
               MeteorClient.mc.player.setVelocity(MeteorClient.mc.player.getVelocity().x, (double)0.0F, MeteorClient.mc.player.getVelocity().z);
            }
         }

      }
   }

   private static double getStep(Box box, int step) {
      for(double i = (double)0.0F; i <= (double)step + (double)0.125F; i += (double)0.125F) {
         if (!OLEPOSSUtils.inside(MeteorClient.mc.player, box.offset((double)0.0F, i, (double)0.0F))) {
            return i;
         }
      }

      return (double)0.0F;
   }

   private static double getReverse(Box box, int reverse) {
      for(double i = (double)0.0F; i <= (double)reverse; i += (double)0.125F) {
         if (OLEPOSSUtils.inside(MeteorClient.mc.player, box.offset((double)0.0F, -i - (double)0.125F, (double)0.0F))) {
            return i;
         }
      }

      return (double)0.0F;
   }
}
