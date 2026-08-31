package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;

public class ElytraFlyPlus extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgSpeed;
   private final Setting<Mode> mode;
   private final Setting<Double> horizontal;
   private final Setting<Double> up;
   private final Setting<Double> speed;
   private final Setting<Double> upMultiplier;
   private final Setting<Double> down;
   private final Setting<Boolean> smartFall;
   private final Setting<Double> fallSpeed;
   private boolean moving;
   private float yaw;
   private float pitch;
   private float p;
   private double velocity;

   public ElytraFlyPlus() {
      super(Aurora.MovementPlus, "Elytra Fly+", "Better efly.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgSpeed = this.settings.createGroup("Speed");
      this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mode")).description(".")).defaultValue(ElytraFlyPlus.Mode.Wasp)).build());
      this.horizontal = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Horizontal Speed")).description("How many blocks to move each tick horizontally.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)5.0F).visible(() -> this.mode.get() == ElytraFlyPlus.Mode.Wasp)).build());
      this.up = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Up Speed")).description("How many blocks to move up each tick.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)5.0F).visible(() -> this.mode.get() == ElytraFlyPlus.Mode.Wasp)).build());
      this.speed = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Speed")).description("How many blocks to move each tick.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)5.0F).visible(() -> this.mode.get() == ElytraFlyPlus.Mode.Control)).build());
      this.upMultiplier = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Up Multiplier")).description("How many times faster should we fly up.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)5.0F).visible(() -> this.mode.get() == ElytraFlyPlus.Mode.Control)).build());
      this.down = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Down Speed")).description("How many blocks to move down each tick.")).defaultValue((double)1.0F).min((double)0.0F).sliderRange((double)0.0F, (double)5.0F).build());
      this.smartFall = this.sgSpeed.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Smart Fall")).description("Only falls down when looking down.")).defaultValue(true)).visible(() -> this.mode.get() == ElytraFlyPlus.Mode.Wasp)).build());
      this.fallSpeed = this.sgSpeed.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Fall Speed")).description("How many blocks to fall down each tick.")).defaultValue(0.01).min((double)0.0F).sliderRange((double)0.0F, (double)1.0F).build());
   }

   @EventHandler(
      priority = 200
   )
   private void onMove(PlayerMoveEvent event) {
      switch (((Mode)this.mode.get()).ordinal()) {
         case 0 -> this.waspTick(event);
         case 1 -> this.controlTick(event);
      }

   }

   private void waspTick(PlayerMoveEvent event) {
      if (this.mc.player.isFallFlying()) {
         this.updateWaspMovement();
         this.pitch = this.mc.player.getPitch();
         double cos = Math.cos(Math.toRadians((double)(this.yaw + 90.0F)));
         double sin = Math.sin(Math.toRadians((double)(this.yaw + 90.0F)));
         double x = this.moving ? cos * (Double)this.horizontal.get() : (double)0.0F;
         double y = -(Double)this.fallSpeed.get();
         double z = this.moving ? sin * (Double)this.horizontal.get() : (double)0.0F;
         if ((Boolean)this.smartFall.get()) {
            y *= Math.abs(Math.sin(Math.toRadians((double)this.pitch)));
         }

         if (this.mc.options.sneakKey.isPressed() && !this.mc.options.jumpKey.isPressed()) {
            y = -(Double)this.down.get();
         }

         if (!this.mc.options.sneakKey.isPressed() && this.mc.options.jumpKey.isPressed()) {
            y = (Double)this.up.get();
         }

         ((IVec3d)event.movement).set(x, y, z);
         this.mc.player.setVelocity((double)0.0F, (double)0.0F, (double)0.0F);
      }
   }

   private void updateWaspMovement() {
      float yaw = this.mc.player.getYaw();
      float f = this.mc.player.input.movementForward;
      float s = this.mc.player.input.movementSideways;
      if (f > 0.0F) {
         this.moving = true;
         yaw += s > 0.0F ? -45.0F : (s < 0.0F ? 45.0F : 0.0F);
      } else if (f < 0.0F) {
         this.moving = true;
         yaw += s > 0.0F ? -135.0F : (s < 0.0F ? 135.0F : 180.0F);
      } else {
         this.moving = s != 0.0F;
         yaw += s > 0.0F ? -90.0F : (s < 0.0F ? 90.0F : 0.0F);
      }

      this.yaw = yaw;
   }

   private void controlTick(PlayerMoveEvent event) {
      if (this.mc.player.isFallFlying()) {
         this.updateControlMovement();
         this.pitch = 0.0F;
         boolean movingUp = false;
         if (!this.mc.options.sneakKey.isPressed() && this.mc.options.jumpKey.isPressed() && this.velocity > (Double)this.speed.get() * 0.4) {
            this.p = (float)Math.min((double)this.p + 0.1 * (double)(1.0F - this.p) * (double)(1.0F - this.p) * (double)(1.0F - this.p), (double)1.0F);
            this.pitch = Math.max(Math.max(this.p, 0.0F) * -90.0F, -90.0F);
            movingUp = true;
            this.moving = false;
         } else {
            this.velocity = (Double)this.speed.get();
            this.p = -0.2F;
         }

         this.velocity = this.moving ? (Double)this.speed.get() : Math.min(this.velocity + Math.sin(Math.toRadians((double)this.pitch)) * 0.08, (Double)this.speed.get());
         double cos = Math.cos(Math.toRadians((double)(this.yaw + 90.0F)));
         double sin = Math.sin(Math.toRadians((double)(this.yaw + 90.0F)));
         double x = this.moving && !movingUp ? cos * (Double)this.speed.get() : (movingUp ? this.velocity * Math.cos(Math.toRadians((double)this.pitch)) * cos : (double)0.0F);
         double y = this.pitch < 0.0F ? this.velocity * (Double)this.upMultiplier.get() * -Math.sin(Math.toRadians((double)this.pitch)) * this.velocity : -(Double)this.fallSpeed.get();
         double z = this.moving && !movingUp ? sin * (Double)this.speed.get() : (movingUp ? this.velocity * Math.cos(Math.toRadians((double)this.pitch)) * sin : (double)0.0F);
         y *= Math.abs(Math.sin(Math.toRadians(movingUp ? (double)this.pitch : (double)this.mc.player.getPitch())));
         if (this.mc.options.sneakKey.isPressed() && !this.mc.options.jumpKey.isPressed()) {
            y = -(Double)this.down.get();
         }

         ((IVec3d)event.movement).set(x, y, z);
         this.mc.player.setVelocity((double)0.0F, (double)0.0F, (double)0.0F);
      }
   }

   private void updateControlMovement() {
      float yaw = this.mc.player.getYaw();
      float f = this.mc.player.input.movementForward;
      float s = this.mc.player.input.movementSideways;
      if (f > 0.0F) {
         this.moving = true;
         yaw += s > 0.0F ? -45.0F : (s < 0.0F ? 45.0F : 0.0F);
      } else if (f < 0.0F) {
         this.moving = true;
         yaw += s > 0.0F ? -135.0F : (s < 0.0F ? 135.0F : 180.0F);
      } else {
         this.moving = s != 0.0F;
         yaw += s > 0.0F ? -90.0F : (s < 0.0F ? 90.0F : 0.0F);
      }

      this.yaw = yaw;
   }

   public static enum Mode {
      Wasp,
      Control;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{Wasp, Control};
      }
   }
}
