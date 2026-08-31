package espada.spacex.aurora.modules.globalsettings;

import espada.spacex.aurora.Aurora;
import espada.spacex.aurora.Modules;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.utils.render.color.SettingColor;

public class ColorSetting extends Modules {
   public static ColorSetting INSTANCE = new ColorSetting();
   private final SettingGroup sgGeneral;
   public final Setting<SettingColor> Color;

   public ColorSetting() {
      super(Aurora.Settings, "ChatColor", "Set Chat Color.");
      this.sgGeneral = this.settings.getDefaultGroup();
      this.Color = this.sgGeneral.add(((meteordevelopment.meteorclient.settings.ColorSetting.Builder)((meteordevelopment.meteorclient.settings.ColorSetting.Builder)(new meteordevelopment.meteorclient.settings.ColorSetting.Builder()).name("Color")).description("COLOR")).defaultValue(new SettingColor(255, 255, 255, 0)).build());
   }
}
