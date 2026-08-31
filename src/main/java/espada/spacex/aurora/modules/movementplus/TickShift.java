package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.KeybindSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;

public class TickShift extends Modules {
   private final SettingGroup sgGeneral;
   public final Setting<SmoothMode> smooth;
   public final Setting<Integer> packets;
   private final Setting<Double> timer;
   public final Setting<Keybind> Key;
   public int unSent;
   private boolean lastTimer;
   private boolean lastMoving;
   private final Timer timerModule;

   public TickShift() {
      super(Aurora.MovementPlus, "Tick Shift", "Stores packets when standing still and uses them when you start moving.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.smooth = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Smoothness")).description(".")).defaultValue(TickShift.SmoothMode.Exponent)).build());
      this.packets = this.sgGeneral.add(((IntSetting.Builder)((IntSetting.Builder)((IntSetting.Builder)(new IntSetting.Builder()).name("Packets")).description("How many packets to store for later use.")).defaultValue(50)).min(0).sliderRange(0, 100).build());
      this.timer = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("Timer")).description("How many packets to send every movement tick.")).defaultValue((double)2.0F).min((double)1.0F).sliderRange((double)0.0F, (double)10.0F).build());
      this.Key = this.sgGeneral.add(((KeybindSetting.Builder)((KeybindSetting.Builder)((KeybindSetting.Builder)(new KeybindSetting.Builder()).name("TickShiftKey")).description(".")).defaultValue(Keybind.none())).build());
      this.unSent = 0;
      this.lastTimer = false;
      this.lastMoving = false;
      this.timerModule = (Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class);
   }

   public void onActivate() {
      super.onActivate();
      this.unSent = 0;
   }

   public void onDeactivate() {
      super.onDeactivate();
      if (this.lastTimer) {
         this.lastTimer = false;
         this.timerModule.setOverride((double)1.0F);
      }

   }

   public String getInfoString() {
      Object[] var10001 = new Object[]{(double)this.unSent / (double)(Integer)this.packets.get() * (double)100.0F};
      return String.format("%.0f", var10001) + "%";
   }

   @EventHandler
   private void onTick(TickEvent.Pre e) {
      if (this.unSent > 0 && this.lastMoving && ((Keybind)this.Key.get()).isPressed()) {
         this.lastMoving = false;
         this.lastTimer = true;
         this.timerModule.setOverride(this.getTimer());
      } else if (this.lastTimer) {
         this.lastTimer = false;
         this.timerModule.setOverride((double)1.0F);
      }

   }

   @EventHandler
   private void onMove(PlayerMoveEvent e) {
      if (e.movement.length() > (double)0.0F && (!(e.movement.length() > 0.0784) || !(e.movement.length() < 0.0785))) {
         if (!((Keybind)this.Key.get()).isPressed()) {
            return;
         }

         this.unSent = Math.max(0, this.unSent - 1);
         this.lastMoving = true;
      }

   }

   private double getTimer() {
      if (this.smooth.get() == TickShift.SmoothMode.Disabled) {
         return (Double)this.timer.get();
      } else {
         double progress = (double)(1.0F - (float)this.unSent / (float)(Integer)this.packets.get());
         if (this.smooth.get() == TickShift.SmoothMode.Exponent) {
            progress *= progress * progress * progress * progress;
         }

         return (double)1.0F + ((Double)this.timer.get() - (double)1.0F) * ((double)1.0F - progress);
      }
   }

   public static enum SmoothMode {
      Disabled,
      Normal,
      Exponent;

      // $FF: synthetic method
      private static SmoothMode[] $values() {
         return new SmoothMode[]{Disabled, Normal, Exponent};
      }
   }
}
