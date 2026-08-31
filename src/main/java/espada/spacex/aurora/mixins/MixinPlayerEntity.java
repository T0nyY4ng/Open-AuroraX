package espada.spacex.aurora.mixins;

import espada.spacex.aurora.events.Event;
import espada.spacex.aurora.events.JumpEvent;
import espada.spacex.aurora.modules.miscplus.SoundModifier;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.systems.modules.Modules;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundCategory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({PlayerEntity.class})
public abstract class MixinPlayerEntity {
   @Unique
   Entity attackEntity = null;

   @Inject(
      method = {"jump"},
      at = {@At("HEAD")}
   )
   private void onJumpPre(CallbackInfo ci) {
      MeteorClient.EVENT_BUS.post(new JumpEvent(Event.Stage.Pre));
   }

   @Inject(
      method = {"jump"},
      at = {@At("RETURN")}
   )
   private void onJumpPost(CallbackInfo ci) {
      MeteorClient.EVENT_BUS.post(new JumpEvent(Event.Stage.Post));
   }

   @Inject(
      method = {"attack"},
      at = {@At("HEAD")}
   )
   private void inject(Entity target, CallbackInfo ci) {
      this.attackEntity = target;
   }

   @Redirect(
      method = {"attack"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/world/World;playSound(Lnet/minecraft/entity/player/PlayerEntity;DDDLnet/minecraft/sound/SoundEvent;Lnet/minecraft/sound/SoundCategory;FF)V"
)
   )
   private void poseNotCollide(World instance, PlayerEntity except, double x, double y, double z, SoundEvent sound, SoundCategory category, float volume, float pitch) {
      SoundModifier m = (SoundModifier)Modules.get().get(SoundModifier.class);
      if (m.isActive()) {
         if ((Boolean)m.crystalHits.get()) {
            instance.playSound(except, x, y, z, sound, category, (float)((double)volume * (Double)m.crystalHitVolume.get()), (float)((double)pitch * (Double)m.crystalHitPitch.get()));
         }

      } else {
         instance.playSound(except, x, y, z, sound, category, volume, pitch);
      }
   }
}
