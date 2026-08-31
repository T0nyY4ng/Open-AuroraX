package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.events.EventTravel;
import espada.spacex.aurora.events.JumpEvent;
import espada.spacex.aurora.events.KeyboardInputEvent;
import espada.spacex.aurora.events.UpdateVelocityEvent;
import espada.spacex.aurora.managers.Managers;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.systems.modules.render.Freecam;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.MathHelper;

public class MoveFix extends Module {
   public static MoveFix INSTANCE;
   public static float fixRotation;
   public static float fixPitch;
   public static boolean shouldRotate;
   private float prevYaw;
   private float prevPitch;

   public MoveFix() {
      super(Aurora.MovementPlus, "MoveFix", "");
      INSTANCE = this;
   }

   public static boolean isOpen() {
      return INSTANCE != null && INSTANCE.isActive();
   }

   public String getInfoString() {
      return shouldRotate ? "fixing" : "";
   }

   @EventHandler
   public void travel(EventTravel e) {
      Managers.ROTATION.updateNext();
      if (shouldRotate) {
         if (!this.mc.player.isRiding()) {
            if (e.isPre()) {
               this.prevYaw = this.mc.player.getYaw();
               this.prevPitch = this.mc.player.getPitch();
               this.mc.player.setYaw(fixRotation);
               this.mc.player.setPitch(fixPitch);
            } else {
               this.mc.player.setYaw(this.prevYaw);
               this.mc.player.setPitch(this.prevPitch);
            }

         }
      }
   }

   @EventHandler
   public void onJump(JumpEvent e) {
      Managers.ROTATION.updateNext();
      if (shouldRotate) {
         if (!this.mc.player.isRiding()) {
            if (e.isPre()) {
               this.prevYaw = this.mc.player.getYaw();
               this.prevPitch = this.mc.player.getPitch();
               this.mc.player.setYaw(fixRotation);
               this.mc.player.setPitch(fixPitch);
            } else {
               this.mc.player.setYaw(this.prevYaw);
               this.mc.player.setPitch(this.prevPitch);
            }

         }
      }
   }

   @EventHandler
   public void onPlayerMove(UpdateVelocityEvent event) {
      Managers.ROTATION.updateNext();
      if (shouldRotate) {
         if (!this.mc.player.isRiding()) {
            event.cancel();
            event.setVelocity(movementInputToVelocity(event.getMovementInput(), event.getSpeed(), fixRotation));
         }
      }
   }

   @EventHandler(
      priority = -999
   )
   public void onKeyInput(KeyboardInputEvent e) {
      Managers.ROTATION.updateNext();
      if (shouldRotate) {
         if (!((HoleSnap)Modules.get().get(HoleSnap.class)).isActive()) {
            if (!this.mc.player.isRiding() && !Modules.get().isActive(Freecam.class)) {
               float mF = this.mc.player.input.movementForward;
               float mS = this.mc.player.input.movementSideways;
               float delta = (this.mc.player.getYaw() - fixRotation) * ((float)Math.PI / 180F);
               float cos = MathHelper.cos(delta);
               float sin = MathHelper.sin(delta);
               this.mc.player.input.movementSideways = (float)Math.round(mS * cos - mF * sin);
               this.mc.player.input.movementForward = (float)Math.round(mF * cos + mS * sin);
            }
         }
      }
   }

   private static Vec3d movementInputToVelocity(Vec3d movementInput, float speed, float yaw) {
      double d = movementInput.lengthSquared();
      if (d < 1.0E-7) {
         return Vec3d.ZERO;
      } else {
         Vec3d vec3d = (d > (double)1.0F ? movementInput.normalize() : movementInput).multiply((double)speed);
         float f = MathHelper.sin(yaw * ((float)Math.PI / 180F));
         float g = MathHelper.cos(yaw * ((float)Math.PI / 180F));
         return new Vec3d(vec3d.x * (double)g - vec3d.z * (double)f, vec3d.y, vec3d.z * (double)g + vec3d.x * (double)f);
      }
   }
}
