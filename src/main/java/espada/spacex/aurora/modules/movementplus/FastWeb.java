package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.modules.movementplus.timer.TimerPlus;
import espada.spacex.aurora.utils.meteor.BOEntityUtils;
import meteordevelopment.meteorclient.events.entity.LivingEntityMoveEvent;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.mixininterface.IVec3d;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.DoubleSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.world.Timer;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.orbit.EventHandler;

public class FastWeb extends Modules {
   private final SettingGroup sgGeneral;
   private final Setting<Boolean> onlySneak;
   private final Setting<Mode> mode;
   private final Setting<Double> fastSpeed;
   public static final double OFF = (double)1.0F;
   private double override;

   public FastWeb() {
      super(Aurora.MovementPlus, "FastWeb", "Test");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.onlySneak = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("OnlySneak")).defaultValue(true)).build());
      this.mode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mode")).defaultValue(FastWeb.Mode.FAST)).build());
      this.fastSpeed = this.sgGeneral.add(((DoubleSetting.Builder)((DoubleSetting.Builder)(new DoubleSetting.Builder()).name("FastSpeed")).defaultValue((double)3.0F).range((double)0.0F, (double)5.0F).sliderRange((double)0.0F, (double)5.0F).visible(() -> this.mode.get() == FastWeb.Mode.FAST)).build());
      this.override = (double)1.0F;
   }

   @EventHandler
   public void onPlayerMove(PlayerMoveEvent event) {
      if (BOEntityUtils.isWebbed(this.mc.player)) {
         if ((this.mode.get() != FastWeb.Mode.FAST || !this.mc.options.sneakKey.isPressed()) && (Boolean)this.onlySneak.get()) {
            if ((this.mode.get() != FastWeb.Mode.STRICT || this.mc.player.isOnGround() || !this.mc.options.sneakKey.isPressed()) && (Boolean)this.onlySneak.get()) {
               if (!((Keybind)((TickShift)meteordevelopment.meteorclient.systems.modules.Modules.get().get(TickShift.class)).Key.get()).isPressed() && !((TimerPlus)meteordevelopment.meteorclient.systems.modules.Modules.get().get(TimerPlus.class)).isActive()) {
                  ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((double)1.0F);
               }
            } else {
               ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((double)8.0F);
            }
         } else {
            if (!((Keybind)((TickShift)meteordevelopment.meteorclient.systems.modules.Modules.get().get(TickShift.class)).Key.get()).isPressed() && !((TimerPlus)meteordevelopment.meteorclient.systems.modules.Modules.get().get(TimerPlus.class)).isActive()) {
               ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((double)1.0F);
            }

            ((IVec3d)event.movement).set(event.movement.x, event.movement.y - (Double)this.fastSpeed.get(), event.movement.z);
         }
      } else if (!((Keybind)((TickShift)meteordevelopment.meteorclient.systems.modules.Modules.get().get(TickShift.class)).Key.get()).isPressed() && !((TimerPlus)meteordevelopment.meteorclient.systems.modules.Modules.get().get(TimerPlus.class)).isActive()) {
         ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((double)1.0F);
      }

   }

   @EventHandler
   public void onLivingEntityMove(LivingEntityMoveEvent event) {
      if (event.entity == this.mc.player) {
         if (BOEntityUtils.isWebbed(this.mc.player)) {
            if ((this.mode.get() != FastWeb.Mode.FAST || !this.mc.options.sneakKey.isPressed()) && (Boolean)this.onlySneak.get()) {
               if ((this.mode.get() != FastWeb.Mode.STRICT || this.mc.player.isOnGround() || !this.mc.options.sneakKey.isPressed()) && (Boolean)this.onlySneak.get()) {
                  if (!((Keybind)((TickShift)meteordevelopment.meteorclient.systems.modules.Modules.get().get(TickShift.class)).Key.get()).isPressed() && !((TimerPlus)meteordevelopment.meteorclient.systems.modules.Modules.get().get(TimerPlus.class)).isActive()) {
                     ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((double)1.0F);
                  }
               } else {
                  ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((double)8.0F);
               }
            } else {
               if (!((Keybind)((TickShift)meteordevelopment.meteorclient.systems.modules.Modules.get().get(TickShift.class)).Key.get()).isPressed() && !((TimerPlus)meteordevelopment.meteorclient.systems.modules.Modules.get().get(TimerPlus.class)).isActive()) {
                  ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((double)1.0F);
               }

               ((IVec3d)event.movement).set(event.movement.x, event.movement.y - (Double)this.fastSpeed.get(), event.movement.z);
            }
         } else if (!((Keybind)((TickShift)meteordevelopment.meteorclient.systems.modules.Modules.get().get(TickShift.class)).Key.get()).isPressed() && !((TimerPlus)meteordevelopment.meteorclient.systems.modules.Modules.get().get(TimerPlus.class)).isActive()) {
            ((Timer)meteordevelopment.meteorclient.systems.modules.Modules.get().get(Timer.class)).setOverride((double)1.0F);
         }

      }
   }

   public static enum Mode {
      FAST,
      STRICT;

      // $FF: synthetic method
      private static Mode[] $values() {
         return new Mode[]{FAST, STRICT};
      }
   }
}
