package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import java.util.Objects;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.orbit.EventHandler;

public class FlightPlus extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<FlightMode> flyMode;
   private final Setting<Boolean> useTimer;
   private final Setting<Double> timer;
   private final Setting<Double> speed;
   private final Setting<Double> ySpeed;
   private final Setting<Double> antiKickDelay;
   private final Setting<Double> antiKickAmount;
   private final Setting<Boolean> keepY;
   private final Setting<Double> glideAmount;
   private double startY;
   private int tick;

   public FlightPlus() {
      super(Aurora.MovementPlus, "Flight+", "KasumsSoft Flight.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.flyMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Flight Mode")).description("Method of flying.")).defaultValue(FlightPlus.FlightMode.Momentum)).build());
      this.useTimer = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Use Timer")).description("Should we use timer.")).defaultValue(false)).build());
      SettingGroup var10001 = this.sgGeneral;
      DoubleSetting.Builder var10002 = new DoubleSetting.Builder();
      Setting<Boolean> var10003 = this.useTimer;
      Objects.requireNonNull(var10003);
      var10002 = ((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)var10002.visible(var10003::get)).name("Timer")).description("How many times more packets should be sent.")).defaultValue(1.088).min((double)0.0F).sliderMax((double)10.0F);
      var10003 = this.useTimer;
      Objects.requireNonNull(var10003);
      this.timer = var10001.add(((DoubleSetting.Builder)var10002.visible(var10003::get)).build());
      this.speed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Speed")).description("How many blocks should be moved each tick.")).defaultValue(0.6).min((double)0.0F).sliderMax((double)10.0F).visible(() -> this.flyMode.get() == FlightPlus.FlightMode.Momentum)).build());
      this.ySpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Y Speed")).description("DA Y SPEEDOS.")).defaultValue((double)0.5F).min((double)0.0F).sliderMax((double)10.0F).visible(() -> this.flyMode.get() == FlightPlus.FlightMode.Momentum)).build());
      this.antiKickDelay = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Anti-Kick Delay")).description("How many ticks should be waited between antikick packets.")).defaultValue((double)10.0F).min((double)0.0F).sliderMax((double)100.0F).visible(() -> this.flyMode.get() == FlightPlus.FlightMode.Momentum)).build());
      this.antiKickAmount = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Anti-Kick Amount")).description("How much to move down.")).defaultValue((double)1.0F).min((double)0.0F).sliderMax((double)10.0F).visible(() -> this.flyMode.get() == FlightPlus.FlightMode.Momentum)).build());
      this.keepY = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("KeepY")).description("Should we try to keep the same y level when jump flying.")).defaultValue(true)).visible(() -> this.flyMode.get() == FlightPlus.FlightMode.Jump)).build());
      this.glideAmount = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Glide amount")).description("How much to glide down.")).defaultValue(0.2).min((double)0.0F).sliderMax((double)1.0F).visible(() -> this.flyMode.get() == FlightPlus.FlightMode.Glide)).build());
      this.startY = (double)0.0F;
      this.tick = 0;
   }

   public void onActivate() {
      if (this.mc.player != null && this.mc.world != null) {
         this.startY = this.mc.player.getY();
         ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((Double)this.timer.get());
      }

   }

   @EventHandler
   private void onMove(PlayerMoveEvent event) {
      if (this.mc.player != null && this.mc.world != null) {
         double[] result = this.getYaw((double)this.mc.player.input.movementForward, (double)this.mc.player.input.movementSideways);
         float yaw = (float)result[0] + 90.0F;
         double x = (double)0.0F;
         double y = (double)this.tick % (Double)this.antiKickDelay.get() == (double)0.0F ? (Double)this.antiKickAmount.get() * -0.04 : (double)0.0F;
         double z = (double)0.0F;
         if (((FlightMode)this.flyMode.get()).equals(FlightPlus.FlightMode.Momentum)) {
            if (this.mc.options.jumpKey.isPressed() && y == (double)0.0F) {
               y = (Double)this.ySpeed.get();
            } else if (this.mc.options.sneakKey.isPressed()) {
               y = -(Double)this.ySpeed.get();
            }

            if (result[1] == (double)1.0F) {
               x = Math.cos(Math.toRadians((double)yaw)) * (Double)this.speed.get();
               z = Math.sin(Math.toRadians((double)yaw)) * (Double)this.speed.get();
            }

            ((IVec3d)event.movement).set(x, y, z);
         }

         if (((FlightMode)this.flyMode.get()).equals(FlightPlus.FlightMode.Jump)) {
            if (this.mc.options.jumpKey.wasPressed()) {
               this.mc.player.jump();
               this.startY += 0.4;
            }

            if (this.mc.options.sneakKey.wasPressed() && !this.mc.options.sneakKey.isPressed()) {
               this.startY = this.mc.player.getY();
            }

            if ((Boolean)this.keepY.get() && this.mc.player.getY() <= this.startY && !this.mc.options.sneakKey.isPressed()) {
               this.mc.player.jump();
            }

            if (result[1] == (double)1.0F) {
               x = Math.cos(Math.toRadians((double)yaw)) * (Double)this.speed.get();
               z = Math.sin(Math.toRadians((double)yaw)) * (Double)this.speed.get();
            }

            ((IVec3d)event.movement).setXZ(x, z);
         }

         if (((FlightMode)this.flyMode.get()).equals(FlightPlus.FlightMode.Glide) && !this.mc.player.isOnGround()) {
            ((IVec3d)event.movement).setY(-(Double)this.glideAmount.get());
         }
      }

   }

   @EventHandler
   private void onTick(TickEvent.Pre event) {
      ++this.tick;
   }

   public void onDeactivate() {
      if (this.mc.player != null && this.mc.world != null) {
         ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((double)1.0F);
      }

   }

   private double[] getYaw(double f, double s) {
      double yaw = (double)this.mc.player.getYaw();
      double move;
      if (f > (double)0.0F) {
         move = (double)1.0F;
         yaw += s > (double)0.0F ? (double)-45.0F : (s < (double)0.0F ? (double)45.0F : (double)0.0F);
      } else if (f < (double)0.0F) {
         move = (double)1.0F;
         yaw += s > (double)0.0F ? (double)-135.0F : (s < (double)0.0F ? (double)135.0F : (double)180.0F);
      } else {
         move = s != (double)0.0F ? (double)1.0F : (double)0.0F;
         yaw += s > (double)0.0F ? (double)-90.0F : (s < (double)0.0F ? (double)90.0F : (double)0.0F);
      }

      return new double[]{yaw, move};
   }

   public static enum FlightMode {
      Momentum,
      Jump,
      Glide;

      // $FF: synthetic method
      private static FlightMode[] $values() {
         return new FlightMode[]{Momentum, Jump, Glide};
      }
   }
}
