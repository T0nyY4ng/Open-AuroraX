package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
import meteordevelopment.meteorclient.events.entity.LivingEntityMoveEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.movement.Anchor;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.math.Vec3d;

public class BurrowMove extends Modules {
   private final SettingGroup sgGeneral;
   public final Setting<Double> speed;
   public final Setting<Double> AnchorSpeed;
   public final Setting<Double> webspeed;
   public final Setting<Double> effectspeed;
   private final Setting<Boolean> pEndChest;

   public BurrowMove() {
      super(Aurora.MovementPlus, "Burrow Move", "Allow you move in burrow.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.speed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Speed")).description("The speed in blocks per second.")).defaultValue(0.3).range((double)0.0F, (double)1.0F).sliderRange((double)0.0F, (double)1.0F).build());
      this.AnchorSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("AnchorSpeed")).description("The speed in blocks per second.")).defaultValue(0.3).range((double)0.0F, (double)1.0F).sliderRange((double)0.0F, (double)1.0F).build());
      this.webspeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("WebSpeed")).description("Test.")).defaultValue(0.3).range((double)0.0F, (double)10.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.effectspeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("EffectSpeed")).description("Test.")).defaultValue(0.3).range((double)0.0F, (double)10.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.pEndChest = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("PauseInEndChest")).description("Pause ec player.")).defaultValue(false)).build());
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      if (BOEntityUtils.isBurrowed(this.mc.player, !(Boolean)this.pEndChest.get())) {
         Vec3d vel = PlayerUtils.getHorizontalVelocity(BOEntityUtils.isWebbed(this.mc.player) ? (Double)this.webspeed.get() : (BOEntityUtils.isAnchor(this.mc.player) ? (Double)this.AnchorSpeed.get() : (this.mc.player.hasStatusEffect(StatusEffects.SPEED) ? (Double)this.effectspeed.get() : (Double)this.speed.get())));
         double velX = vel.getX();
         double velZ = vel.getZ();
         Anchor anchor = (Anchor)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Anchor.class);
         if (anchor.isActive() && anchor.controlMovement) {
            velX = anchor.deltaX;
            velZ = anchor.deltaZ;
         }

         ((IVec3d)event.movement).set(velX, event.movement.y, velZ);
      }

   }

   @EventHandler
   public void onLivingEntityMove(LivingEntityMoveEvent event) {
      if (event.entity == this.mc.player) {
         if (BOEntityUtils.isBurrowed(this.mc.player, !(Boolean)this.pEndChest.get())) {
            Vec3d vel = PlayerUtils.getHorizontalVelocity(BOEntityUtils.isWebbed(this.mc.player) ? (Double)this.webspeed.get() : (this.mc.player.hasStatusEffect(StatusEffects.SPEED) ? (Double)this.effectspeed.get() : (Double)this.speed.get()));
            double velX = vel.getX();
            double velZ = vel.getZ();
            Anchor anchor = (Anchor)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Anchor.class);
            if (anchor.isActive() && anchor.controlMovement) {
               velX = anchor.deltaX;
               velZ = anchor.deltaZ;
            }

            ((IVec3d)event.movement).set(velX, event.movement.y, velZ);
         }

      }
   }
}
