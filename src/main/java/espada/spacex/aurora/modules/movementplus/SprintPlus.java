package espada.spacex.aurora.modules.movementplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.modules.playerplus.ScaffoldPlus;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.EnumSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.player.PlayerUtils;
import meteordevelopment.orbit.EventHandler;

public class SprintPlus extends Modules {
   private final SettingGroup sgGeneral;
   public final Setting<SprintMode> sprintMode;
   public final Setting<Boolean> hungerCheck;

   public SprintPlus() {
      super(Aurora.MovementPlus, "Sprint+", "Non shit sprint!");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.sprintMode = this.sgGeneral.add(((EnumSetting.Builder)((EnumSetting.Builder)((EnumSetting.Builder)(new EnumSetting.Builder()).name("Mode")).description("The method of sprinting.")).defaultValue(SprintPlus.SprintMode.Vanilla)).build());
      this.hungerCheck = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("HungerCheck")).description("Should we check if we have enough hunger to sprint")).defaultValue(true)).build());
   }

   @EventHandler(
      priority = 200
   )
   private void onTick(TickEvent.Pre event) {
      if (!ScaffoldPlus.shouldStopSprinting || !meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(ScaffoldPlus.class)) {
         if (this.mc.player != null && this.mc.world != null) {
            if ((Boolean)this.hungerCheck.get() && this.mc.player.getHungerManager().getFoodLevel() < 6) {
               this.mc.player.setSprinting(false);
               return;
            }

            switch (((SprintMode)this.sprintMode.get()).ordinal()) {
               case 0:
                  if (this.mc.options.forwardKey.isPressed()) {
                     this.mc.player.setSprinting(true);
                  }
                  break;
               case 1:
                  if (PlayerUtils.isMoving()) {
                     this.mc.player.setSprinting(true);
                  }
                  break;
               case 2:
                  this.mc.player.setSprinting(true);
            }
         }

      }
   }

   public void onDeactivate() {
      if (this.mc.player != null && this.mc.world != null) {
         this.mc.player.setSprinting(false);
      }

   }

   public static enum SprintMode {
      Vanilla,
      Omni,
      Rage;

      // $FF: synthetic method
      private static SprintMode[] $values() {
         return new SprintMode[]{Vanilla, Omni, Rage};
      }
   }
}
