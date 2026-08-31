package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import java.util.Objects;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.registry.tag.FluidTags;

public class SpeedPlus extends Modules {
   private final SettingGroup sgGeneral;
   private final SettingGroup sgPause;
   private final Setting<SpeedMode> mode;
   private final Setting<Double> accelerationAmount;
   private final Setting<Boolean> rbReset;
   private final Setting<Boolean> airStrafe;
   private final Setting<Boolean> onlyPressed;
   private final Setting<Keybind> strafeBind;
   private final Setting<Double> speed;
   private final Setting<Boolean> knockBack;
   private final Setting<Double> kbFactor;
   private final Setting<Boolean> pauseSneak;
   private final Setting<Boolean> burslow;
   private final Setting<Double> burspeed;
   private final Setting<Boolean> pauseElytra;
   private final Setting<Boolean> pauseFly;
   private final Setting<LiquidMode> pauseWater;
   private final Setting<LiquidMode> pauseLava;
   private boolean move;
   public double velocity;
   private double acceleration;
   private double ax;
   private double az;
   private int jumpPhase;

   public SpeedPlus() {
      super(Aurora.MovementPlus, "Speed+", "Speeeeeeeed.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sgPause = this.settings.createGroup("Pause");
      this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mode")).description("Mode for speed.")).defaultValue(SpeedPlus.SpeedMode.Instant)).build());
      this.accelerationAmount = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Acceleration")).description("How much should the speed increase every movement tick.")).defaultValue(0.3).min((double)0.0F).sliderMax((double)10.0F).visible(() -> this.mode.get() == SpeedPlus.SpeedMode.Accelerate)).build());
      this.rbReset = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Reset On Rubberband")).description("Resets speed when rubberbanding.")).defaultValue(false)).visible(() -> this.mode.get() == SpeedPlus.SpeedMode.Accelerate)).build());
      this.airStrafe = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Air Strafe")).description("Lets you move fast in air too.")).defaultValue(false)).visible(() -> this.mode.get() == SpeedPlus.SpeedMode.Accelerate)).build());
      this.onlyPressed = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Only Pressed")).description("Uses instant mode when you arent pressing jump key.")).defaultValue(false)).visible(() -> this.mode.get() == SpeedPlus.SpeedMode.CCStrafe)).build());
      this.strafeBind = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)(new KeybindSetting.Builder()).name("Strafe Bind")).description("Strafes when this key is pressed.")).defaultValue(Keybind.fromKey(-1))).visible(() -> this.mode.get() == SpeedPlus.SpeedMode.CCStrafe && (Boolean)this.onlyPressed.get())).build());
      this.speed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Speed")).description("How many blocks to move every movement tick")).defaultValue(0.287).min((double)0.0F).sliderMax((double)10.0F).build());
      this.knockBack = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Damage Boost")).description("Turns knockback into velocity.")).defaultValue(false)).build());
      SettingGroup var10001 = this.sgGeneral;
      DoubleSetting.Builder var10002 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Damage Boost Factor")).description("Knockback multiplier")).defaultValue((double)1.0F).min((double)0.0F).sliderMax((double)10.0F);
      Setting<Boolean> var10003 = this.knockBack;
      Objects.requireNonNull(var10003);
      this.kbFactor = var10001.add(((DoubleSetting.Builder)var10002.visible(var10003::get)).build());
      this.pauseSneak = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause Sneak")).description("Doesn't modify movement while sneaking.")).defaultValue(true)).build());
      this.burslow = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("BurrowSlow")).description("")).defaultValue(false)).build());
      var10001 = this.sgGeneral;
      var10002 = ((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("BurrowSpeed")).description("")).defaultValue((double)0.01F).min((double)0.001F).sliderRange((double)0.0F, (double)1.0F);
      var10003 = this.burslow;
      Objects.requireNonNull(var10003);
      this.burspeed = var10001.add(((DoubleSetting.Builder)var10002.visible(var10003::get)).build());
      this.pauseElytra = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause Elytra")).description("Doesn't modify movement while flying with elytra.")).defaultValue(true)).build());
      this.pauseFly = this.sgPause.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Pause Fly")).description("Doesn't modify movement while flying.")).defaultValue(true)).build());
      this.pauseWater = this.sgPause.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Pause Water")).description("Doesn't modify movement when in water.")).defaultValue(SpeedPlus.LiquidMode.Submerged)).build());
      this.pauseLava = this.sgPause.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Pause Lava")).description("Doesn't modify movement when in lava.")).defaultValue(SpeedPlus.LiquidMode.Both)).build());
      this.move = false;
      this.acceleration = (double)0.0F;
      this.ax = (double)0.0F;
      this.az = (double)0.0F;
      this.jumpPhase = 1;
   }

   public void onActivate() {
      super.onActivate();
   }

   public void onDeactivate() {
      super.onDeactivate();
      ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((double)1.0F);
   }

   @EventHandler
   private void onKB(PacketEvent.Receive event) {
      if (this.mc.player != null && this.mc.world != null) {
         if ((Boolean)this.knockBack.get() && event.packet instanceof EntityVelocityUpdateS2CPacket) {
            EntityVelocityUpdateS2CPacket packet = (EntityVelocityUpdateS2CPacket)event.packet;
            if (packet.getEntityId() == this.mc.player.getId()) {
               double x = packet.getVelocityX() / (double)8000.0F;
               double z = packet.getVelocityZ() / (double)8000.0F;
               this.velocity = Math.max(this.velocity, Math.sqrt(x * x + z * z) * (Double)this.kbFactor.get());
            }
         }

         if ((Boolean)this.rbReset.get() && event.packet instanceof PlayerPositionLookS2CPacket) {
            this.acceleration = (double)0.0F;
         }
      }

   }

   @EventHandler(
      priority = 200
   )
   public void onMove(PlayerMoveEvent event) {
      if (!this.mc.player.isOnGround()) {
         if (this.mc.player != null && this.mc.world != null) {
            if (((HoleSnap)meteordevelopment.meteorclient.systems.modules.Modules.get().get(HoleSnap.class)).isActive()) {
               return;
            }

            if ((Boolean)this.pauseSneak.get() && this.mc.player.isSneaking()) {
               return;
            }

            if ((Boolean)this.pauseElytra.get() && this.mc.player.isFallFlying()) {
               return;
            }

            if ((Boolean)this.pauseFly.get() && this.mc.player.getAbilities().flying) {
               return;
            }

            switch (((LiquidMode)this.pauseWater.get()).ordinal()) {
               case 1:
                  if (this.mc.player.isSubmergedIn(FluidTags.WATER)) {
                     return;
                  }
                  break;
               case 2:
                  if (this.mc.player.isTouchingWater()) {
                     return;
                  }
                  break;
               case 3:
                  if (this.mc.player.isTouchingWater() || this.mc.player.isSubmergedIn(FluidTags.WATER)) {
                     return;
                  }
            }

            switch (((LiquidMode)this.pauseLava.get()).ordinal()) {
               case 1:
                  if (this.mc.player.isSubmergedIn(FluidTags.LAVA)) {
                     return;
                  }
                  break;
               case 2:
                  if (this.mc.player.isInLava()) {
                     return;
                  }
                  break;
               case 3:
                  if (this.mc.player.isInLava() || this.mc.player.isSubmergedIn(FluidTags.LAVA)) {
                     return;
                  }
            }

            double forward = (double)this.mc.player.input.movementForward;
            double sideways = (double)this.mc.player.input.movementSideways;
            double yaw = this.getYaw(forward, sideways);
            if (this.mode.get() == SpeedPlus.SpeedMode.CCStrafe && (!(Boolean)this.onlyPressed.get() || ((Keybind)this.strafeBind.get()).isPressed())) {
               if (this.jumpPhase == 4) {
                  this.velocity *= 0.9888888889;
                  if (this.mc.player.isOnGround()) {
                     this.jumpPhase = 1;
                  }
               }

               if (this.jumpPhase == 3) {
                  this.velocity += (0.2873 - this.velocity) * 0.6;
                  this.jumpPhase = 4;
               }

               if (this.jumpPhase == 2) {
                  ((IVec3d)event.movement).setY(0.4);
                  this.velocity *= 1.85;
                  this.jumpPhase = 3;
               }

               if (this.jumpPhase == 1 && this.mc.player.isOnGround() && this.move) {
                  this.velocity = 0.2873;
                  this.jumpPhase = 2;
               }

               this.velocity = Math.max(this.velocity, 0.2873);
            } else {
               this.velocity = Math.max((Double)this.speed.get(), this.velocity * 0.98);
            }

            if (this.mc.world.getBlockState(new BlockPos(this.mc.player.getBlockPos())).getBlock() != Blocks.OBSIDIAN && (Boolean)this.burslow.get()) {
               this.velocity = Math.max((Double)this.speed.get(), this.velocity * (Double)this.burspeed.get());
            }

            double motion = this.velocity;
            if (this.velocity < 0.01) {
               motion = (double)0.0F;
            }

            if (this.mc.player.hasStatusEffect(StatusEffects.SPEED)) {
               motion *= 1.2 + (double)this.mc.player.getStatusEffect(StatusEffects.SPEED).getAmplifier() * 0.2;
            }

            if (this.mc.player.hasStatusEffect(StatusEffects.SLOWNESS)) {
               motion /= 1.2 + (double)this.mc.player.getStatusEffect(StatusEffects.SLOWNESS).getAmplifier() * 0.2;
            }

            if (this.mc.world.getBlockState(new BlockPos(this.mc.player.getBlockPos())).getBlock() == Blocks.OBSIDIAN && (Boolean)this.burslow.get()) {
               motion = (Double)this.burspeed.get();
            }

            double x = Math.cos(Math.toRadians(yaw + (double)90.0F));
            double y = this.mc.player.getVelocity().getY();
            double z = Math.sin(Math.toRadians(yaw + (double)90.0F));
            switch (((SpeedMode)this.mode.get()).ordinal()) {
               case 0:
               case 1:
                  if (this.move) {
                     ((IVec3d)event.movement).set(motion * x, y, motion * z);
                  } else {
                     ((IVec3d)event.movement).set((double)0.0F, y, (double)0.0F);
                  }
                  break;
               case 2:
                  this.acceleration = Math.min((double)1.0F, (!this.move ? this.acceleration : this.acceleration + (!this.mc.player.isOnGround() && !(Boolean)this.airStrafe.get() ? 0.02 : (Double)this.accelerationAmount.get() / (double)10.0F)) * this.slipperiness(this.move));
                  if (this.move && this.mc.player.isOnGround() || (Boolean)this.airStrafe.get()) {
                     this.ax = x;
                     this.az = z;
                  }

                  ((IVec3d)event.movement).setXZ((Double)this.speed.get() * this.ax * this.acceleration, (Double)this.speed.get() * this.az * this.acceleration);
            }
         }

      }
   }

   private double slipperiness(boolean moving) {
      if (moving) {
         return (double)1.0F;
      } else {
         return this.mc.player.isOnGround() ? (double)this.mc.world.getBlockState(new BlockPos((int)this.mc.player.getX(), (int)Math.ceil(this.mc.player.getY() - (double)1.0F), (int)this.mc.player.getZ())).getBlock().getSlipperiness() : 0.98;
      }
   }

   private double getYaw(double f, double s) {
      double yaw = (double)this.mc.player.getYaw();
      if (f > (double)0.0F) {
         this.move = true;
         yaw += s > (double)0.0F ? (double)-45.0F : (s < (double)0.0F ? (double)45.0F : (double)0.0F);
      } else if (f < (double)0.0F) {
         this.move = true;
         yaw += s > (double)0.0F ? (double)-135.0F : (s < (double)0.0F ? (double)135.0F : (double)180.0F);
      } else {
         this.move = s != (double)0.0F;
         yaw += s > (double)0.0F ? (double)-90.0F : (s < (double)0.0F ? (double)90.0F : (double)0.0F);
      }

      return yaw;
   }

   public static enum SpeedMode {
      CCStrafe,
      Instant,
      Accelerate;

      // $FF: synthetic method
      private static SpeedMode[] $values() {
         return new SpeedMode[]{CCStrafe, Instant, Accelerate};
      }
   }

   public static enum LiquidMode {
      Disabled,
      Submerged,
      Touching,
      Both;

      // $FF: synthetic method
      private static LiquidMode[] $values() {
         return new LiquidMode[]{Disabled, Submerged, Touching, Both};
      }
   }
}
