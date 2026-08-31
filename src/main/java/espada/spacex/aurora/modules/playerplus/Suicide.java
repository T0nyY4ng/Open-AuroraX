package espada.spacex.aurora.modules.playerplus;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import espada.spacex.aurora.modules.combatplus.autocrystal.AutoCrystal;
import meteordevelopment.meteorclient.events.game.OpenScreenEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.gui.screen.DeathScreen;

public class Suicide extends Modules {
   private final SettingGroup sgGeneral;
   public final Setting<Boolean> disableDeath;
   public final Setting<Boolean> enableCA;

   public Suicide() {
      super(Aurora.PlayerPlus, "Suicide", "Kills yourself. Recommended.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.disableDeath = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Disable On Death")).description("Disables the module on death.")).defaultValue(true)).build());
      this.enableCA = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("Enable Auto Crystal")).description("Enables auto crystal when enabled.")).defaultValue(true)).build());
   }

   public void onActivate() {
      if ((Boolean)this.enableCA.get() && !meteordevelopment.meteorclient.systems.modules.Modules.get().isActive(AutoCrystal.class)) {
         ((AutoCrystal)meteordevelopment.meteorclient.systems.modules.Modules.get().get(AutoCrystal.class)).toggle();
      }

   }

   @EventHandler(
      priority = 6969
   )
   private void onDeath(OpenScreenEvent event) {
      if (event.screen instanceof DeathScreen && (Boolean)this.disableDeath.get()) {
         this.toggle();
         this.sendDisableMsg("died");
      }

   }
}
