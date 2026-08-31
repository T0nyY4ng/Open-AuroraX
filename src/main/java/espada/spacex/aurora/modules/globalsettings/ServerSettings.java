package espada.spacex.aurora.modules.globalsettings;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;

public class ServerSettings extends Modules {
   private final SettingGroup sgGeneral;
   public final Setting<Boolean> cc;
   public final Setting<Boolean> oldVerCrystals;
   public final Setting<Boolean> oldVerDamage;

   public ServerSettings() {
      super(Aurora.Settings, "Server", "Global server settings for every aurora module.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.cc = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("CC Hitboxes")).description("Newly placed crystals require 1 block tall space without entity hitboxes.")).defaultValue(false)).build());
      this.oldVerCrystals = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("1.12.2 Crystals")).description("Requires 2 block tall space to place crystals.")).defaultValue(false)).build());
      this.oldVerDamage = this.sgGeneral.add(((BoolSetting.Builder)((BoolSetting.Builder)((BoolSetting.Builder)(new BoolSetting.Builder()).name("1.12.2 Damage")).description("Calculates damages in old way.")).defaultValue(false)).build());
   }
}
