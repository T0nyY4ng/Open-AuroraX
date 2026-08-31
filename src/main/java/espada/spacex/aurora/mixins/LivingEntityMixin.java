package espada.spacex.aurora.mixins;

import espada.spacex.aurora.events.EventTravel;
import meteordevelopment.meteorclient.MeteorClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MovementType;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({LivingEntity.class})
public class LivingEntityMixin {
   @Inject(
      method = {"travel"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void travelHook(Vec3d movementInput, CallbackInfo ci) {
      if ((Object)this == MeteorClient.mc.player) {
         EventTravel event = new EventTravel(movementInput, true);
         MeteorClient.EVENT_BUS.post(event);
         if (event.isCancelled()) {
            MeteorClient.mc.player.move(MovementType.SELF, MeteorClient.mc.player.getVelocity());
            ci.cancel();
         }

      }
   }

   @Inject(
      method = {"travel"},
      at = {@At("RETURN")},
      cancellable = true
   )
   public void travelPostHook(Vec3d movementInput, CallbackInfo ci) {
      if ((Object)this == MeteorClient.mc.player) {
         EventTravel event = new EventTravel(movementInput, false);
         MeteorClient.EVENT_BUS.post(event);
         if (event.isCancelled()) {
            MeteorClient.mc.player.move(MovementType.SELF, MeteorClient.mc.player.getVelocity());
            ci.cancel();
         }

      }
   }
}
